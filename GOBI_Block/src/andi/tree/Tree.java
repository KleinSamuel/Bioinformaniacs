package andi.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class Tree {
	public enum Cluster_method {
		WPGMA, UPGMA, NJ;
	};

	private TreeMap<Integer, Node> nodes;
	private Node root;
	private TreeMap<Node, Node_Data> leaves;
	private TreeSet<Node> inner;
	private HashSet<Node_Data> nds;
	private Cluster_method cm = Cluster_method.UPGMA;

	public Tree() {
		init();
		nds = new HashSet<>();
	}

	public Tree(Collection<Node_Data> nds) {
		init();
		this.nds = new HashSet<>();
		this.nds.addAll(nds);
		build();
	}

	private void init() {
		root = new Node(0);
		leaves = new TreeMap<>();
		inner = new TreeSet<>();
		nodes = new TreeMap<>();
		inner_node(root);
	}

	public void change_cluster_method(Cluster_method cm) {
		this.cm = cm;
	}

	private void inner_node(Node i) {
		nodes.put(i.get_id(), i);
		inner.add(i);
	}

	private void leaf_node(Node l, Node_Data nd) {
		nodes.put(l.get_id(), l);
		leaves.put(l, nd);
	}

	public void build(Collection<Node_Data> nds) {
		this.nds.addAll(nds);
		build();
	}

	public void add_leaf(Node_Data nd) {
		nds.add(nd);
		rebuild();
	}

	private void rebuild() {
		init();
		build();

	}

	public String toString() {
		return toString("root: " + root.toString(), "\n   ", root);
	}

	private String toString(String all, String div, Node parent) {
		String last = all;
		int count = 0;
		for (Node n : parent.get_children().keySet()) {
			count++;
			last += div + "|" + div + "|--" + n.toString();
			if (!n.is_leaf())
				last = toString(last, count == parent.get_children().size() ? div + "    " : div + "|   ", n);
		}
		return last;
	}

	private void build() {
		switch (cm) {
		case WPGMA:
			build_wpgma();
			break;
		case UPGMA:
			build_upgma();
			break;
		case NJ:
			build_nj();
			break;
		default:
			break;
		}
	}

	private void build_nj() {
		// TODO Auto-generated method stub

	}

	private void build_upgma() {
		ArrayList<Node> ns = new ArrayList<>();
		double[][] dists;
		if (leaves == null)
			leaves = new TreeMap<>();
		if (leaves.size() != nds.size()) {
			for (Node_Data nd : nds) {
				Node l = new Node(nodes.size());
				l.set_Data(nd);
				leaf_node(l, nd);
				ns.add(l);
			}
		} else
			ns.addAll(leaves.keySet());
		dists = new double[ns.size()][ns.size()];
		for (int x = 0; x < ns.size(); x++) {
			for (int y = x; y < ns.size(); y++) {
				double dist = 0;
				if (x != y)
					dist = Math.abs(leaves.get(ns.get(x)).compute_distance(leaves.get(ns.get(y))));
				dists[x][y] = dists[y][x] = dist;
			}
		}
		upgma(ns, dists, null);
	}

	private void upgma(ArrayList<Node> ns, double[][] old_mat, ArrayList<Node> old_data) {
		double[][] dists = new double[ns.size()][ns.size()];
		ArrayList<Node> new_nodes = new ArrayList<>();
		double min = Double.MAX_VALUE;
		Node n_min_1 = ns.get(0);
		Node n_min_2 = ns.get(1);
		new_nodes.addAll(ns);
		if (old_data == null) {
			dists = old_mat;
			for (int x = 0; x < ns.size(); x++)
				for (int y = x + 1; y < ns.size(); y++) {
					double dist = dists[y][x];
					if (dist < min) {
						min = dist;
						n_min_1 = ns.get(x);
						n_min_2 = ns.get(y);
					}
				}
		} else
			for (int x = 0; x < ns.size(); x++)
				for (int y = x; y < ns.size(); y++) {
					double dist = 0;
					if (x != y) {
						if (y != ns.size() - 1)
							dist = old_mat[old_data.indexOf(ns.get(x))][old_data.indexOf(ns.get(y))];
						else {
							int count = 0;
							for (Node c : ns.get(ns.size() - 1).get_children().keySet()) {
								double n_count = c.count_leaves();
								count += n_count;
								dist += (n_count * old_mat[old_data.indexOf(c)][old_data.indexOf(ns.get(x))]);
							}
							dist /= count;
						}
						if (dist < min) {
							min = dist;
							n_min_1 = ns.get(x);
							n_min_2 = ns.get(y);
						}
					}
					dists[y][x] = dist;
					dists[x][y] = dist;
				}
		if (ns.size() > 2) {
			Node c = new Node(nodes.size());
			double dist = min / 2;
			c.set_total_dist(dist);
			c.add_child(n_min_1, dist);
			c.add_child(n_min_2, dist);
			new_nodes.remove(new_nodes.indexOf(n_min_1));
			new_nodes.remove(new_nodes.indexOf(n_min_2));
			new_nodes.add(c);
			inner_node(c);
			upgma(new_nodes, dists, ns);
		} else {
			root.set_total_dist(dists[1][0] / 2);
			root.add_child(ns.get(0), (dists[1][0] / 2));
			root.add_child(ns.get(1), (dists[1][0] / 2));
		}
	}

	private void build_wpgma() {
		// TODO Auto-generated method stub

	}
	
	public String to_newick() {
		return to_newick(root);
	}
	
	public String to_newick(Node next) {
		String newick = "(";
		int count = 0;
		for(Node n:next.get_children().keySet()) {
			count++;
			if(count!=1)
			newick+=",";
			if(!n.is_leaf())
				newick+=to_newick(n);
			else
			newick+=n.get_Name()+":"+n.dist_to_parent();
		}
		if(next.is_root())
		newick+=")O;";
		else
			newick+=")"+next.get_Name()+":"+next.dist_to_parent();
		return newick;
	}

}