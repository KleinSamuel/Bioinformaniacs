package kikky.heatmap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.TreeMap;

public class SpecialLineplot {
	private String R_path = "/home/proj/biosoft/software/R/R-3.3.0/bin/Rscript";

	private String title, xlab, ylab;
	private String line1 = "", line2 = "", legend = "";
	private String log = "", las = "1,";
	private double max = 0;

	public SpecialLineplot(String title, String xlab, String ylab) {
		this.title = title;
		this.xlab = xlab;
		this.ylab = ylab;
	}

	public void set_values(TreeMap<Double, Double> tt, TreeMap<Double, Double> tat) {
		StringBuilder x = new StringBuilder(), y = new StringBuilder();
		for (double key : tt.keySet()) {
			x.append(",").append(key);
			y.append(",").append(tt.get(key));
			if (tt.get(key) > max)
				max = tt.get(key);
		}
		x.deleteCharAt(0);
		y.deleteCharAt(0);
		line1 = "x1<-c(" + x.toString() + "); y1<-c(" + y.toString() + ");";
		x = new StringBuilder();
		y = new StringBuilder();
		for (double key : tat.keySet()) {
			x.append(",").append(key);
			y.append(",").append(tat.get(key));
			if (tat.get(key) > max)
				max = tat.get(key);
		}
		x.deleteCharAt(0);
		y.deleteCharAt(0);
		line2 = "x2<-c(" + x.toString() + "); y2<-c(" + y.toString() + ");";

	}

	public void setLegend(String a, String b) {
		legend = "\"" + a + "\",\"" + b + "\"";
	}

	public void rotate_bar_name(int i) {
		if (i >= 0 && i < 4)
			las = i + ",";
	}

	public void set_log(boolean x, boolean y) {
		if (x && y)
			log += "log = \"xy\",";
		else if (x)
			log += "log = \"x\",";
		else if (y)
			log += "log = \"y\",";
	}

	public String plot(String filename) {
		File tmp = null;
		try {
			tmp = File.createTempFile("line", "plot");
			tmp.deleteOnExit();
			PrintWriter bw = new PrintWriter(new FileWriter(tmp));
			bw.println(String.format("png('%s');", filename));
			bw.println(line1);
			bw.println(line2);
			bw.println("plot(x1, y1, las=" + las + log
					+ " xlab=\"\", ylab=\"\",col=\"red\", xlim=range(0,1), ylim=range(0," + ((int) max)
					+ "), type=\"l\");");
			bw.println("par(new=TRUE);plot(x2, y2, col=\"blue\", axes=FALSE,xlab=\"\", ylab=\"\", type=\"l\");");
			bw.println("legend(0," + ((int) max) + ",c(" + legend + "),col=c(\"red\",\"blue\"),lty=c(1,1))");
			bw.println(String.format("title(main='%s', xlab='%s', ylab='%s');", title, xlab, ylab));
			bw.close();
			System.out.println(R_path + " " + tmp.getAbsolutePath());
			Process plotting = Runtime.getRuntime().exec(R_path + " " + tmp.getAbsolutePath());
			plotting.waitFor();
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tmp.getAbsolutePath();
	}
}
