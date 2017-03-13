package andi.tree;

public interface Node_Data extends Comparable<Node_Data>{
	public String to_String();
	
	public double compute_distance(Node_Data nd);
	
	public String shared_info();
	
	public boolean equals(Object o);

	
}
