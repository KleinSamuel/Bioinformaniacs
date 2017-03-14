package andi.tree;

import java.util.TreeMap;

public class Node implements Node_Data {
	private int id;
	private TreeMap<Node, Double> childs;
	private Node parent;
	private double value;
	private Node_Data data;

	public Node(int id, Node p) {
		this.id = id;
		childs = new TreeMap<>();
		this.parent = p;
	}

	public Node(int id) {
		this.id = id;
		childs = new TreeMap<>();
		value = 0;
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
		childs.put(c, dist);
	}

	public void set_parent(Node p) {
		this.parent = p;
	}

	public int count_leaves() {
		int c = 0;
		if(this.is_leaf())
			return 1;
		for (Node n : childs.keySet())
				c += n.count_leaves();
		return c;
	}

	public boolean is_leaf() {
		return this.childs.isEmpty();
	}

	public void set_value(double v) {
		this.value = v;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double compute_distance(Node_Data nd) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String shared_info() {
		// TODO Auto-generated method stub
		return null;
	}

	public TreeMap<Node, Double> get_children() {
		return childs;
	}

}
