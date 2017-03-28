package kikky.heatmap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

public class Barplot {
	private String R_path = "/home/proj/biosoft/software/R/R-3.3.0/bin/Rscript";

	private String title, xlab, ylab;
	private StringBuilder x = new StringBuilder(), names = new StringBuilder(), text = new StringBuilder();
	private String log = "", las = "1,", ylim = "";
	private int names_size = 0;
	private boolean genes = false;

	public Barplot(String title, String xlab, String ylab) {
		this.title = title;
		this.xlab = xlab;
		this.ylab = ylab;
	}

	public void set_boolean(boolean b) {
		genes = b;
	}

	public void set_values(Vector<Double> x) {
		double highest = 0;
		for (Double d_x : x) {
			if (highest < d_x)
				highest = d_x;
			this.x.append(",").append(d_x);
			this.text.append(",\"").append(d_x).append("\"");
		}
		this.x.deleteCharAt(0);
		this.x.insert(0, "x<-c(");
		this.x.append(");");
		if (genes) {
			set_lim(highest);
			set_text(highest);
		}
	}

	private void set_text(double highest) {
		this.text.deleteCharAt(0);
		this.text.insert(0, "mid,1000,labels=c(");
		this.text.append(")");
		this.text.insert(0, "text(");
		this.text.append(");");
	}

	private void set_lim(double highest) {
		if (highest < 15000)
			ylim = "ylim=c(0,15000),";
		else if (highest < 20000)
			ylim = "ylim=c(0,20000),";
	}

	public void set_values(String x) {
		this.x.append("x<-c(").append(x).append(");");
	}

	public void setnames(Vector<String> names) {
		for (String s_n : names)
			this.names.append(",").append(s_n);
		this.names.deleteCharAt(0);
		this.names.insert(0, "names<-c(");
		this.names.append(");");
		names_size = names.size();
	}

	public void setnames(String names, int size) {
		this.names.append("names<-c(").append(names).append(");");
		names_size = size;
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
			tmp = File.createTempFile("bar", "plot");
			tmp.deleteOnExit();
			PrintWriter bw = new PrintWriter(new FileWriter(tmp));
			bw.println(String.format("png('%s');", filename));
			bw.println(x.toString());
			bw.println(names.toString());
			bw.println(String.format("mid <- barplot(x, names.arg=names, las=" + las + log + ylim + " col = rainbow("
					+ names_size + "), xlab=\"\", ylab=\"\");"));
			bw.println(String.format("title(main='%s', xlab='%s', ylab='%s');", title, xlab, ylab));
			bw.println(text.toString());
			bw.close();
			// System.out.println(R_path + " " + tmp.getAbsolutePath());
			Process plotting = Runtime.getRuntime().exec(R_path + " " + tmp.getAbsolutePath());
			plotting.waitFor();
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tmp.getAbsolutePath();
	}
}
