package andi.tree;

import java.util.Collection;

public interface Node_Data extends Comparable<Node_Data>{
	public String toString();
	
	public double compute_distance(Node_Data nd);
	
	public String shared_info(Collection<Node_Data> nds);
	
	public boolean equals(Object o);
	
	public String get_Name();

	
}
