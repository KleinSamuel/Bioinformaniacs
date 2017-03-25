package kikky.heatmap;

import java.util.ArrayList;

public class Test {
	public static void main(String[] args) {
		ArrayList<Sample_Data> x = new ArrayList<Sample_Data>();
		x.add(new Small_Sample("a",0));
		x.add(new Small_Sample("b",1));
		x.add(new Small_Sample("c",2));
		ArrayList<Sample_Data> y = new ArrayList<Sample_Data>();
		y.add(new Small_Sample("a",0));
		y.add(new Small_Sample("b",0));
		y.add(new Small_Sample("c",0));
		Number[][] m = new Number[y.size()][x.size()];
		for(int i = 0; i<y.size(); i++)
			for(int j = 0; j<x.size(); j++)
				 m[i][j] = 0-j;
		HeatMap hm = new HeatMap("Test", y, x, m);
//		hm.plot();
		
//		HeatMap hm = new HeatMap("Test", y, x);
//		hm.plot();
	}
}
