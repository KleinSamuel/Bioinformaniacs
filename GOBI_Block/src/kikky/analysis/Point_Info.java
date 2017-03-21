package kikky.analysis;

import java.util.HashMap;

public class Point_Info {
	private HashMap<String, String> mates;
	private double[] x;
	private double[] y;
	private StringBuilder point_info_text = new StringBuilder();

	public Point_Info(HashMap<String, String> mates, double[] x, double[] y) {
		this.mates = mates;
		this.x = x;
		this.y = y;
	}

	public String get_point_info_text() {
		return point_info_text.toString();
	}

	public double[] get_x() {
		return x;
	}

	public double[] get_y() {
		return y;
	}

	public void scatter_plot(String x, String y, String query_genes, String target_genes) {
		point_info_text.append("\n#Scatterplot");
		point_info_text.append("\n#x c(").append(x).append(")");
		point_info_text.append("\n#y c(").append(y).append(")");
		point_info_text.append("\n#x_genes ").append(query_genes);
		point_info_text.append("\n#y_genes ").append(target_genes);
	}

	public void percentage_mates_to_all(int size_query, int size_target) {
		point_info_text.append("\n#Percentage_mate_all");
		point_info_text.append("\nquery=" + mates.size()).append("|").append(size_query).append(" target=")
				.append(mates.size()).append("|").append(size_target);
	}

	public void go_mapping() {
		// TODO
	}

}
