package kikky.heatmap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

public class Scatterplot {
	private String R_path = "/home/proj/biosoft/software/R/R-3.3.0/bin/Rscript";

	private String title, xlab, ylab;
	private StringBuilder x = new StringBuilder(), y = new StringBuilder();
	private Vector<String> names;
	private String log = "", las = "1,";

	public Scatterplot(String title, String xlab, String ylab) {
		this.title = title;
		this.xlab = xlab;
		this.ylab = ylab;
	}

	public void set_values(Vector<Double> x, Vector<Double> y) {
		for (Double d_x : x)
			this.x.append(",").append(d_x);
		this.x.deleteCharAt(0);
		this.x.insert(0, "x<-c(");
		this.x.append(");");
		for (Double d_y : y)
			this.y.append(",").append(d_y);
		this.y.deleteCharAt(0);
		this.y.insert(0, "y<-c(");
		this.y.append(");");
	}

	public void set_values(String x, String y) {
		this.x.append("x<-").append(x);
		this.y.append("y<-").append(y);
	}

	public void setnames(Vector<String> names) {
		this.names = names;
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
			tmp = File.createTempFile("scatter", "plot");
			tmp.deleteOnExit();
			PrintWriter bw = new PrintWriter(new FileWriter(tmp));
			bw.println(String.format("png('%s');", filename));
			bw.println(x.toString());
			bw.println(y.toString());
			bw.println(String.format("plot(x, y, las=" + las + log + " xlab=\"\", ylab=\"\");"));
			bw.println(String.format("title(main='%s', xlab='%s', ylab='%s');", title, xlab, ylab));
			bw.close();
			Process plotting = Runtime.getRuntime().exec(R_path + " " + tmp.getAbsolutePath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tmp.getAbsolutePath();
	}
}
