package kikky.heatmap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

public class RHeatMap {
	private String R_path = "/home/proj/biosoft/software/R/R-3.3.0/bin/Rscript";
	private String matrix;

	/**
	 * Konstruktor für die Heatmap. Brechnet die Werte durch
	 * getValue(Sample_Data)
	 * 
	 * @param title
	 *            titel über der heatmap
	 * @param col
	 *            labels unten (x)
	 * @param row
	 *            labels links (y)
	 */
	public RHeatMap(String title, Collection<Sample_Data> col, Collection<Sample_Data> row) {
		matrix = "m <- matrix(c(";
		String temp = "";
		for (Sample_Data sd1 : row) {
			for (Sample_Data sd2 : col) {
				temp += "," + sd2.get_value(sd1);
			}
		}
		matrix = matrix + temp.substring(1) + "), nrow=" + row.size() + ", ncol=" + col.size() + ", dimnames=(";
		String label = "";
		for (Sample_Data sd2 : row) {
			label += ",\"" + sd2.get_name() + "\"";
		}
		matrix += ("c(" + label.substring(1) + "),");
		label = "";
		for (Sample_Data sd2 : col) {
			label += ",\"" + sd2.get_name() + "\"";
		}
		matrix += ("c(" + label.substring(1) + ")))");
	}

	public RHeatMap(String title, Collection<Sample_Data> row, Collection<Sample_Data> col, Number[][] m) {
		matrix = "m <- matrix(c(";
		String temp = "";
		for (int x = 0; x < row.size(); x++) {
			for (int y = 0; y < col.size(); y++) {
				temp += "," + m[y][x];
			}
		}
		matrix = matrix + temp.substring(1) + "), nrow=" + row.size() + ", ncol=" + col.size() + ", dimnames=(";
		String label = "";
		for (Sample_Data sd2 : row) {
			label += ",\"" + sd2.get_name() + "\"";
		}
		matrix += ("c(" + label.substring(1) + "),");
		label = "";
		for (Sample_Data sd2 : col) {
			label += ",\"" + sd2.get_name() + "\"";
		}
		matrix += ("c(" + label.substring(1) + ")))");
	}

	public void plot(String file) {
		try {
			File r_script = File.createTempFile("rheatmap", ".R");
			//r_script.deleteOnExit();
			BufferedWriter bw = new BufferedWriter(new FileWriter(r_script));
			bw.write("library(ggplot2);\n");
			bw.write("library(reshape2);\n");
			bw.write("png(file=\"" + file + "\");");
			bw.write(matrix + ";\n");
			bw.write("m.melted<-melt(m)");
			bw.write("ggplot(m.melted, aes(x = Var1, y = Var2, fill = value)) + geom_tile();\n");
			bw.close();
			System.out.println(R_path + " " + r_script.getAbsolutePath());
			Process plotting = Runtime.getRuntime().exec(R_path + " " + r_script.getAbsolutePath());
			plotting.waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
