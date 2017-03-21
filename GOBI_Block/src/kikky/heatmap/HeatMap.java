package kikky.heatmap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class HeatMap {
	private String R_path = "/home/proj/biosoft/software/R/R-3.3.0/bin/Rscript";
	private ArrayList<String> labels = new ArrayList<>();
	private String file_path;
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
	public HeatMap(String title, Collection<Sample_Data> col, Collection<Sample_Data> row) {
		matrix = "m <- matrix(c(";
		String temp = "";
		for (Sample_Data sd1 : row) {
			for (Sample_Data sd2 : col) {
				temp += "," + sd2.get_value(sd1);
			}
		}
		matrix = matrix + temp.substring(1) + "), nrow=" + row.size() + ", ncol=" + col.size() + ")";
		String label = "";
		for (Sample_Data sd2 : row) {
			label += ",\"" + sd2.get_name() + "\"";
		}
		labels.add("c(" + label.substring(1) + ")");
		label = "";
		for (Sample_Data sd2 : col) {
			label += ",\"" + sd2.get_name() + "\"";
		}
		labels.add("c(" + label.substring(1) + ")");
	}

	public HeatMap(String title, Collection<Sample_Data> row, Collection<Sample_Data> col, Number[][] m) {
		matrix = "m <- matrix(c(";
		String temp = "";
		for (int x = 0; x < row.size(); x++) {
			for (int y = 0; y < col.size(); y++) {
				temp += "," + m[y][x];
			}
		}
		matrix = matrix + temp.substring(1) + "), nrow=" + row.size() + ", ncol=" + col.size() + ")";
		String label = "";
		for (Sample_Data sd2 : row) {
			label += ",\"" + sd2.get_name() + "\"";
		}
		labels.add("c(" + label.substring(1) + ")");
		label = "";
		for (Sample_Data sd2 : col) {
			label += ",\"" + sd2.get_name() + "\"";
		}
		labels.add("c(" + label.substring(1) + ")");
	}

	public void plot() {
		try {
			File r_script = File.createTempFile("R_script_", ".R");
			BufferedWriter bw = new BufferedWriter(new FileWriter(r_script));
			bw.write("library(plotly);\n");
			bw.write(matrix + ";\n");
			bw.write("p <- plot_ly(x = " + labels.get(0) + ", y = " + labels.get(1) + ",z = m, type = \"heatmap\");\n");
			bw.write("json <- plotly_json(p, FALSE);");
			bw.write("write(json, \"/home/a/adamowicz/GoBi/json.txt\");");
			bw.close();
			System.out.println(R_path + " " + r_script.getAbsolutePath());
			Process plotting = Runtime.getRuntime().exec(R_path + " " + r_script.getAbsolutePath());
			plotting.waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

}
