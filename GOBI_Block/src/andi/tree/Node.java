package andi.tree;

import java.util.Collection;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

public class Node implements Node_Data {
	private int id;
	private TreeMap<Node, Double> childs;
	private Node parent;
	private double total_dist;
	private Node_Data data;
	private TreeMap<Node, Node_Data> leaves;
	private Vector<Boolean> shared;

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
		return this.id < 1;
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
		out += "(" + id + ")";
		if (this.is_leaf())
			out += " Data: " + this.data.toString();
		else
			out += this.shared_info();
		return out;
	}

	public void set_Data(Node_Data nd) {
		this.data = nd;
	}

	@Override
	public double compute_distance(Node_Data nd) {
		return 0;
	}

	public String shared_info() {
		String out = "";
		if (!this.is_root())
			out += "Dist_to_Parent: " + parent.get_children().get(this) + "; ";
		if (this.is_leaf())
			return out;
		out += "Leaves: " + this.count_leaves() + "; ";
		out += "Dist_to_Leaves: " + total_dist + "; ";
		out += "Sharing " + this.shared_type() + ": ";
		for (Object o : this.get_leaves().firstEntry().getValue().get_shared(this.get_share_vector()))
			out += o.toString() + ", ";
		return out.substring(0, out.length() - 2);
	}

	public TreeMap<Node, Double> get_children() {
		return childs;
	}

	@Override
	public String get_Name() {
		if (this.is_leaf())
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
		if (this.is_root()) {
			childs.clear();
			total_dist = 0;
			data = null;
			leaves = null;
		}
	}

	public TreeSet<Double> get_distances() {
		TreeSet<Double> dists = new TreeSet<>();
		dists.add(this.total_dist);
		for (Node c : childs.keySet())
			dists.addAll(c.get_distances());
		return dists;
	}

	@Override
	public String data_title() {
		return "";
	}

	@Override
	public Vector<?> get_shared(Vector<Boolean> shared) {
		return null;
	}

	public void compute_shared() {
		if (this.is_leaf()) {
			shared = this.data.get_share_vector();
			return;
		}
		Vector<Boolean> v1 = null;
		Vector<Boolean> v2 = null;
		for (Node n : this.childs.keySet())
			if (v1 == null)
				v1 = n.get_share_vector();
			else
				v2 = n.get_share_vector();
		shared = Node_Data.combine_shared(v1, v2);
	}

	@Override
	public Vector<Boolean> get_share_vector() {
		if (shared == null)
			compute_shared();
		return shared;
	}

	@Override
	public String shared_type() {
		return this.get_leaves().firstEntry().getValue().shared_type();
	}

	// @Override
	// public Node getChild(Node node, int index) {
	// if(index > node.get_children().size()){
	// return null;
	// }else{
	// int cnt = 0;
	// for(Node n : node.get_children().keySet()){
	// if(cnt == index){
	// return n;
	// }
	// cnt++;
	// }
	// }
	// return null;
	// }
	//
	// @Override
	// public int getChildCount(Node node) {
	// return node.get_children().size();
	// }
	//
	// @Override
	// public boolean isLeaf(Node node) {
	// return node.is_leaf();
	// }

}
