package kikky.heatmap;

public interface Sample_Data extends Comparable<Sample_Data> {

	public String get_Name();

	public double get_value(Sample_Data sd);
	
	public boolean equals(Object obj);
	
	public int hashCode();
}
