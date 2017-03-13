package andi.tree;

import java.util.TreeSet;

public class Node implements Comparable {
	private int id;
	private TreeSet<Node> childs;
	private Node parent;
	private double value;

	public Node(int id, Node p) {
		this.id = id;
		childs = new TreeSet<>();
		this.parent = p;
	}
	
	public Node(int id) {
		this.id = id;
		childs = new TreeSet<>();
		value = 0;
	}

	@Override
	public int compareTo(Object o) {
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
	
	public void add_child(Node c) {
		childs.add(c);
	}
	
	public void set_value(double v) {
		this.value = v;
	}
	

}
