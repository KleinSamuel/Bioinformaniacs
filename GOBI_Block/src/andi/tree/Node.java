package andi.tree;

import java.util.Collection;
import java.util.TreeMap;

public class Node implements Node_Data {
	private int id;
	private TreeMap<Node, Double> childs;
	private Node parent;
	private double total_dist;
	private Node_Data data;
	private TreeMap<Node, Node_Data> leaves;

	public Node(int id, Node p) {
		this.id = id;
		childs = new TreeMap<>();
		this.parent = p;
	}

	public Node(int id) {
		this.id = id;
		childs = new TreeMap<>();
	}

	public int compareTo(Node_Data o) {
		if (o instanceof Node)
			return this.get_id() - ((Node) o).get_id();
		return -1;
	}

	public int get_id() {
		return this.id;
	}

	public boolean is_root() {
		return parent == null;
	}

	public void add_child(Node c, double dist) {
		c.set_parent(this);
		if (c.is_leaf())
			childs.put(c, dist);
		else
			childs.put(c, dist - c.get_total_dist());
	}

	public void set_parent(Node p) {
		this.parent = p;
	}

	public double get_total_dist() {
		return this.total_dist;
	}

	public int count_leaves() {
		int c = 0;
		if (this.is_leaf())
			return 1;
		for (Node n : childs.keySet())
			c += n.count_leaves();
		return c;
	}

	public boolean is_leaf() {
		return this.childs.isEmpty();
	}

	public void set_total_dist(double v) {
		this.total_dist = v;
	}

	public TreeMap<Node, Node_Data> get_leaves() {
		if (leaves == null) {
			leaves = new TreeMap<>();
			for (Node c : childs.keySet())
				if (c.is_leaf())
					leaves.put(c, c.get_data());
				else
					for (Node cc : c.get_leaves().keySet())
						leaves.put(cc, cc.get_data());
		}
		return leaves;
	}

	public Node_Data get_data() {
		return data;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Node)
			if (o != null)
				if (((Node) o).get_id() == this.get_id())
					return true;
		return false;
	}

	@Override
	public String toString() {
		String out = "";
		if(!this.is_root())
		out += "(" + id + ")";
		if (this.is_leaf()) 
			out += " Data: " + this.data.toString();
		else 
			out+=this.shared_info(null);
		return out;
	}

	public void set_Data(Node_Data nd) {
		this.data = nd;
	}

	@Override
	public double compute_distance(Node_Data nd) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	

	@Override
	public String shared_info(Collection<Node_Data> nds) {
		String out = "";
		if (!this.is_root())
			out += "Dist_to_Parent: " + parent.get_children().get(this)+"; ";
		if (this.is_leaf())
			return out;
		out += "Leaves: " + this.count_leaves()+"; ";
		out += "Dist_to_Leaves: " + total_dist+"; ";
		out += "Shared info: " + this.get_leaves().firstEntry().getValue().shared_info(this.get_leaves().values());
		return out;
	}

	public TreeMap<Node, Double> get_children() {
		return childs;
	}

	@Override
	public String get_Name() {
		if(this.is_leaf())
			return data.get_Name();
		return "";
	}
	
	public Node get_Parent() {
		return parent;
	}
	
	public double dist_to_parent() {
		return this.get_Parent().get_children().get(this);
	}
	public void reset() {
		if(this.is_root()) {
			childs.clear();
			total_dist=0;
			data=null;
			leaves=null;
		}
	}
	

}
