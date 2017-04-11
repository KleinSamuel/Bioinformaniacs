package andi.tree;

import java.util.ArrayList;
import java.util.Iterator;
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
	private Tree t;
	private static boolean only_node_dist = true;

	public Node(int id, Node p, Tree t) {
		this.id = id;
		childs = new TreeMap<>();
		this.parent = p;
		this.t = t;
	}

	public Node(int id, TreeMap<Node, Double> childs, Node parent, double total_dist, Node_Data data,
			TreeMap<Node, Node_Data> leaves, Vector<Boolean> shared, Tree t) {
		this.id = id;
		this.childs = (TreeMap<Node, Double>) childs.clone();
		if (parent != null)
			this.parent = parent.clone();
		this.total_dist = total_dist;
		this.data = data;
		if (leaves != null)
			this.leaves = (TreeMap<Node, Node_Data>) leaves.clone();
		this.shared = shared;
		this.t = t;

	}

	public Node(int id, Tree t) {
		this.id = id;
		childs = new TreeMap<>();
		this.t = t;
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

	public Node clone() {
		return new Node(id, childs, parent, total_dist, data, leaves, shared, t);
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

	public void round_to(int decimals) {
		total_dist = round(decimals, total_dist);
		for (Node c : childs.keySet())
			childs.put(c, round(decimals, childs.get(c)));

	}

	public double round(int decimals, double val) {
		double mult = 1;
		for (int i = 0; i < decimals; i++)
			mult *= 10;
		int d = (int) (val * mult);
		return (double) (d / mult);
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
		// else
		// out += this.shared_info();
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

	@Override
	public String get_distance_measurement() {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<Node> get_path(Node other) {
		ArrayList<Node> path = new ArrayList<>();

		ArrayList<Node> p1 = new ArrayList<>();
		p1.add(this);
		while (!p1.get(p1.size() - 1).is_root())
			p1.add(p1.get(p1.size() - 1).get_Parent());

		ArrayList<Node> p2 = new ArrayList<>();
		p2.add(other);
		while (!p2.get(p2.size() - 1).is_root())
			p2.add(p2.get(p2.size() - 1).get_Parent());
		// System.out.println("\t\tP1:\t"+p1);
		// System.out.println("\t\tP2:\t"+p2);
		// System.out.print("\t\t");
		for (Node n : p1) {
			path.add(n);
			if (!p2.contains(n)) {
				// System.out.print(n+"-(p)->");
				continue;
			}
			// System.out.print(n+"-(tp)->");
			for (int i = p2.indexOf(n) - 1; i >= 0; i--) {
				// if(i>0)
				// System.out.print(p2.get(i)+"-(c)->");
				// else
				// System.out.println(p2.get(i));
				path.add(p2.get(i));
			}
			break;
		}
		return path;
	}

	public static void set_only_node_dist(boolean dist) {
		Node.only_node_dist = dist;
	}

	public double get_dist(Node other) {
		ArrayList<Node> path = get_path(other);
		if (Node.only_node_dist)
			return path.size() - 1;
		double dist = 0;
		for (int i = 0; i < path.size() - 1; i++)
			dist += path.get(i).get_simple_dist(path.get(i + 1));
		return dist;
	}

	public double get_simple_dist(Node other) {
		return Math.abs(other.total_dist - this.total_dist);
	}

	public Node get_first_child() {
		if (is_leaf())
			return null;
		Iterator<Node> nodes = childs.keySet().iterator();
		Node c1 = nodes.next();
		Node c2 = nodes.next();

		Node first = c1.get_id() < c2.get_id() ? c1 : c2;
		Node second = c1.get_id() > c2.get_id() ? c1 : c2;
		if (first.is_leaf() && second.is_leaf())
			return first;
		if (first.is_leaf() | second.is_leaf())
			return first.is_leaf() ? first : second;
		return first;
	}

	public Node get_second_child() {
		if (is_leaf())
			return null;
		Iterator<Node> nodes = childs.keySet().iterator();
		Node c1 = nodes.next();
		Node c2 = nodes.next();

		Node first = c1.get_id() < c2.get_id() ? c1 : c2;
		Node second = c1.get_id() > c2.get_id() ? c1 : c2;
		if (first.is_leaf() && second.is_leaf())
			return second;
		if (first.is_leaf() | second.is_leaf())
			return first.is_leaf() ? second : first;
		return second;
	}

	@Override
	public String unique_name() {
		// TODO Auto-generated method stub
		return null;
	}

}
