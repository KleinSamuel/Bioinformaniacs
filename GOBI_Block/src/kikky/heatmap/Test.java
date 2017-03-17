package kikky.heatmap;

import java.util.ArrayList;

public class Test {
	public static void main(String[] args) {
		ArrayList<Sample_Data> x = new ArrayList<Sample_Data>();
		x.add(new Small_Sample("a",2));
		x.add(new Small_Sample("b",4));
		x.add(new Small_Sample("c",6));
		ArrayList<Sample_Data> y = new ArrayList<Sample_Data>();
		y.add(new Small_Sample("a",3));
		y.add(new Small_Sample("b",5));
		y.add(new Small_Sample("c",7));
		HeatMap hm = new HeatMap("Test", x, y);
		hm.plot();
	}
}
