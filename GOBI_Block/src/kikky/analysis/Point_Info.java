package kikky.analysis;

import java.util.HashMap;

public class Point_Info {
	private HashMap<String, String> mates;
	private double[] x;
	private double[] y;
	private String point_info_text = "";

	public Point_Info(HashMap<String, String> mates, double[] x, double[] y) {
		this.mates = mates;
		this.x = x;
		this.y = y;
	}

	public String get_point_info_text() {
		return point_info_text;
	}

	public double[] get_x() {
		return x;
	}

	public double[] get_y() {
		return y;
	}

	public void scatter_plot(String x, String y, String query_genes, String target_genes) {
		point_info_text += "\n#Scatterplot";
		point_info_text += "\n#x c(" + x + ")";
		point_info_text += "\n#y c(" + y + ")";
		point_info_text += "\n#x_genes " + query_genes;
		point_info_text += "\n#y_genes " + target_genes;
	}

	public void percentage_mates_to_all(int size_query, int size_target) {
		point_info_text += "\n#Percentage_mate_all";
		point_info_text += "\nquery=" + mates.size() + "|" + size_query + " target=" + mates.size() + "|" + size_target;
	}

	public void go_mapping() {
		// TODO
	}

}
