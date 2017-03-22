package andi.tree;

import java.util.Iterator;
import java.util.Vector;

public interface Node_Data extends Comparable<Node_Data>{
	
	
	public String toString();
	
	public double compute_distance(Node_Data nd);
	
	
	public boolean equals(Object o);
	
	public String get_Name();
	
	public String data_title();
	
	public Vector<?> get_shared(Vector<Boolean> shared);
	
	
	public String shared_type();
	
	public Vector<Boolean> get_share_vector();
	
	public static Vector<Boolean> combine_shared(Vector<Boolean> v1, Vector<Boolean> v2){
		Vector<Boolean> out = new Vector<>();
		if(v1==null||v2==null||v1.size()!=v2.size())
			return out;
		Iterator<Boolean> i1=v1.iterator();
		Iterator<Boolean> i2=v2.iterator();
		while(i1.hasNext())
			out.add((Boolean)(((boolean)i1.next())&((boolean)i2.next())));
		return out;
	}

	
}
