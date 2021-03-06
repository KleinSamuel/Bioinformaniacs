package kikky.heatmap;

import kikky.analysis.Point_Info;

public interface Sample_Data extends Comparable<Sample_Data> {

	public String get_name();

	public double get_value(Sample_Data sd);
	
	public boolean equals(Object obj);
	
	public int hashCode();
}
