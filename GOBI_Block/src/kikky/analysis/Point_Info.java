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

	public void scatter_plot() {
		point_info_text += "\n#Scatterplot";
		String vector = "";
		for (double x_val : x)
			vector += "," + x_val;
		point_info_text += "\n#x c(" + vector.substring(1) + ")";
		vector = "";
		for (double y_val : y)
			vector += "," + y_val;
		point_info_text += "\n#y c(" + vector.substring(1) + ")";
		vector = "";
		String vector2 = "";
		for (String key : mates.keySet()) {
			vector += "," + key;
			vector2 += "," + mates.get(key);
		}
		point_info_text += "\n#x_genes " + vector.substring(1);
		point_info_text += "\n#y_genes " + vector2.substring(1);
	}

	public void percentage_mates_to_all(int size_query, int size_target) {
		point_info_text += "\n#Percentage_mate_all";
		point_info_text += "\nquery=" + mates.size() + "|" + size_query + " target=" + mates.size() + "|" + size_target;
	}
	
	public void go_mapping(){
		//TODO
	}

}
