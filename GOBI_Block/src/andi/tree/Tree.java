package andi.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

import andi.analysis.go.total.Organism_Data;
import andi.analysis.go.total.Organism_Data.Distance_measurement;
import andi.analysis.go.total.Organism_Data.Gene_focus;

public class Tree /*
					 * extends AbstractTreeModel<Node> implements
					 * TreeSelectableModel
					 */ {
	public enum Cluster_method {
		WPGMA, UPGMA;
	};

	private TreeMap<Integer, Node> nodes;
	private static Node super_root = new Node(-1);
	private Node root;
	private TreeMap<Node, Node_Data> leaves;
	private TreeSet<Node> inner;
	private TreeSet<Node_Data> nds;
	private Cluster_method cm = Cluster_method.UPGMA;
	private Distance_measurement dm = Distance_measurement.Avg_seq_id_max;
	private Gene_focus gf = Gene_focus.All_genes;
	private int round_val = 2;

	public Tree(Collection<Node_Data> nds) {
		// super(super_root);
		init();
		this.nds = new TreeSet<>();
		this.nds.addAll(nds);
		build();
	}

	public Tree(Collection<Node_Data> nds, boolean build) {
		// super(super_root);
		init();
		this.nds = new TreeSet<>();
		this.nds.addAll(nds);
		if (build)
			build();
	}

	// #Boa Constructor
	public Tree(Collection<Node_Data> nds, Cluster_method cm) {
		// super(super_root);
		init();
		this.nds = new TreeSet<>();
		this.nds.addAll(nds);
		build();
	}

	public Tree() {
		// super(super_root);
		init();
	}

	public Tree add_Node_Data(Collection<Node_Data> nds) {
		this.nds = new TreeSet<>();
		this.nds.addAll(nds);
		build();
		return this;
	}

	public void construct(Distance_measurement dm, Gene_focus gf) {
		System.out.println("\tconstruct with "+dm+" and "+gf);
		for (Node_Data nd : nds) {
			if (nd instanceof Organism_Data) {
				Organism_Data org = (Organism_Data) nd;
				org.set_gene_focus(gf);
				org.set_distance_measurement(dm);
			}
		}
	}

	public void set_cluster_method(Cluster_method cm) {
		this.cm = cm;
	}

	public Tree change_cluster_method(Cluster_method cm) {
		if (this.cm == cm)
			return this;
		this.cm = cm;
		this.rebuild();
		return this;
	}

	public void set_distance_measurement(Distance_measurement dm) {
		this.dm = dm;
	}

	public void set_gene_focus(Gene_focus gf) {
		this.gf = gf;
		
	}

	public Tree change_distance_measurement(Distance_measurement dm) {
		if (this.dm == dm)
			return this;
		this.dm = dm;
		rebuild();
		return this;
	}

	public Tree change_gene_focus(Gene_focus gf) {
		if (this.gf == gf)
			return this;
		this.gf = gf;
		rebuild();
		return this;
	}

	public String get_distance_measurement() {
		return leaves.values().iterator().next().get_distance_measurement();
	}

	private void init() {
		root = new Node(0);
		super_root.reset();
		super_root.add_child(root, 0);
		leaves = new TreeMap<>();
		inner = new TreeSet<>();
		nodes = new TreeMap<>();
		// inner_node(root);
	}

	private void inner_node(Node i) {
		nodes.put(i.get_id(), i);
		inner.add(i);
		i.compute_shared();
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

	public void rebuild() {
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

	public Tree build() {
		if (this.nds.first() instanceof Organism_Data)
			construct(dm, gf);
		switch (cm) {
		case WPGMA:
			build_wpgma();
			break;
		case UPGMA:
			build_upgma();
			break;
		default:
			break;
		}
		return this;
	}

	public int get_round_val() {
		return round_val;
	}

	// private void build_nj() {
	// ArrayList<Node> ns = new ArrayList<>();
	// double[][] dists;
	// leaves = new TreeMap<>();
	// for (Node_Data nd : nds) {
	// Node l = new Node(nodes.size());
	// l.set_Data(nd);
	// leaf_node(l, nd);
	// ns.add(l);
	// }
	// dists = new double[ns.size()][ns.size()];
	// for (int x = 0; x < ns.size(); x++) {
	// for (int y = x; y < ns.size(); y++) {
	// double dist = 0;
	// if (x != y)
	// dist =
	// Math.abs(leaves.get(ns.get(x)).compute_distance(leaves.get(ns.get(y))));
	// dists[x][y] = dists[y][x] = dist;
	// }
	// }
	// nj(ns, dists);
	// }

	// private double sum_row(double[][] data, int row) {
	// double out = 0;
	// for (double d : data[row])
	// out += d;
	// return out;
	//
	// }
	//
	// private void nj(ArrayList<Node> ns, double[][] dists) {
	// double[][] new_dists = new double[ns.size()][ns.size()];
	// ArrayList<Node> new_nodes = new ArrayList<>();
	// ArrayList<Double> netto_dist = new ArrayList<>();
	// new_nodes.addAll(ns);
	// double min_val = Double.MAX_VALUE;
	// Node min_n1 = ns.get(0);
	// Node min_n2 = ns.get(1);
	// for (int row = 0; row < ns.size(); row++)
	// netto_dist.add(sum_row(dists, row) / (ns.size() - 2));
	// if (ns.size() > 2) {
	// for (int row = 1; row < ns.size(); row++)
	// for (int col = row + 1; col < ns.size(); col++) {
	// double dist = dists[row][col] - (netto_dist.get(row) +
	// netto_dist.get(col));
	// // temp_mat[row][col]=dist;
	// if (dist < min_val) {
	// min_val = dist;
	// min_n1 = ns.get(row);
	// min_n2 = ns.get(col);
	// }
	// }
	// }
	// Node c = new Node(nodes.size());
	// double dist_n1 = dists[ns.indexOf(min_n1)][ns.indexOf(min_n2)] +
	// netto_dist.get(ns.indexOf(min_n1))
	// - netto_dist.get(ns.indexOf(min_n2));
	// double dist_n2 = dists[ns.indexOf(min_n1)][ns.indexOf(min_n2)] - dist_n1;
	// c.add_child(min_n1, dist_n1);
	// c.add_child(min_n2, dist_n2);
	// if (ns.size() > 2) {
	// new_nodes.remove(ns.indexOf(min_n1));
	// new_nodes.remove(ns.indexOf(min_n2));
	// new_nodes.add(c);
	// inner_node(c);
	// for (int row = 1; row < new_nodes.size(); row++) {
	// for (int col = row + 1; col < new_nodes.size(); col++) {
	// if(row<new_nodes.size()-1)
	// new_dists[row][col] = row<new_nodes.size()-1 ?
	// dists[ns.indexOf(new_nodes.get(row))][ns.indexOf(new_nodes.get(col))]
	// :dists[ns.indexOf(new_nodes.get(row))][ns.indexOf(min_n1)]+dists[ns.indexOf(new_nodes.get(row))][ns.indexOf(min_n2)]-dists[ns.indexOf(new_nodes.get(row))][ns.indexOf(new_nodes.get(col))];
	// }
	// }
	// nj(new_nodes,new_dists);
	//
	// } else {
	// root.add_child(ns.get(0), dist_n1);
	// root.add_child(ns.get(1), dist_n2);
	// }
	//
	// }

	private void build_upgma() {
		ArrayList<Node> ns = new ArrayList<>();
		double[][] dists;
		leaves = new TreeMap<>();
		for (Node_Data nd : nds) {
			Node l = new Node(nodes.size());
			l.set_Data(nd);
			leaf_node(l, nd);
			ns.add(l);
		}
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
			inner_node(root);
		}
	}

	private void build_wpgma() {
		ArrayList<Node> ns = new ArrayList<>();
		double[][] dists;
		leaves = new TreeMap<>();
		for (Node_Data nd : nds) {
			Node l = new Node(nodes.size());
			l.set_Data(nd);
			leaf_node(l, nd);
			ns.add(l);
		}
		dists = new double[ns.size()][ns.size()];
		for (int x = 0; x < ns.size(); x++) {
			for (int y = x; y < ns.size(); y++) {
				double dist = 0;
				if (x != y)
					dist = Math.abs(leaves.get(ns.get(x)).compute_distance(leaves.get(ns.get(y))));
				dists[x][y] = dists[y][x] = dist;
			}
		}
		wpgma(ns, dists, null);
	}

	public String get_cluster_method() {
		return cm.toString();
	}

	private void wpgma(ArrayList<Node> ns, double[][] old_mat, ArrayList<Node> old_data) {
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
							for (Node c : ns.get(ns.size() - 1).get_children().keySet())
								dist += (old_mat[old_data.indexOf(c)][old_data.indexOf(ns.get(x))]);
							dist /= 2;
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
			wpgma(new_nodes, dists, ns);
		} else {
			root.set_total_dist(dists[1][0] / 2);
			root.add_child(ns.get(0), (dists[1][0] / 2));
			root.add_child(ns.get(1), (dists[1][0] / 2));
			inner_node(root);
		}
	}

	public String to_newick() {
		return to_newick(root);
	}

	public String to_newick(Node next) {
		String newick = "(";
		int count = 0;
		for (Node n : next.get_children().keySet()) {
			count++;
			if (count != 1)
				newick += ",";
			if (!n.is_leaf())
				newick += to_newick(n);
			else
				newick += n.get_Name() + ":" + n.dist_to_parent();
		}
		if (next.is_root())
			newick += ")root;";
		else
			newick += ")" + next.get_Name() + ":" + next.dist_to_parent();
		return newick;
	}

	public String to_R_newick(Node next, Node current_root, boolean node_ids) {
		String newick = "(";
		int count = 0;
		for (Node n : next.get_children().keySet()) {
			count++;
			if (count != 1)
				newick += ",";
			if (!n.is_leaf())
				newick += to_R_newick(n, current_root, node_ids);
			else
				newick += "__" + n.get_Name().replaceAll(" ", "_") + ":" + n.dist_to_parent();
		}
		if (next == current_root)
			newick += ")root:" + this.get_root_offset(current_root) + ";";
		else
			newick += node_ids ? (")" + next.get_id() + ":" + next.dist_to_parent())
					: (")" + next.get_Name().replaceAll(" ", "_") + ":" + next.dist_to_parent());
		return newick;
	}

	public Node get_root() {
		return root;
	}

	public ArrayList<Double> get_distances_rev(Node n) {
		double self = n.get_total_dist();
		TreeSet<Double> dists = n.get_distances();
		ArrayList<Double> new_dists = new ArrayList<>();
		for (double d : dists) {
			double new_d = d;
			new_d = self - new_d;
			new_dists.add(new_d + this.get_root_offset(n));
		}
		return new_dists;
	}

	public ArrayList<Double> get_distances(Node n) {
		TreeSet<Double> dists = n.get_distances();
		ArrayList<Double> new_dists = new ArrayList<>();
		for (double d : dists) {
			new_dists.add(d);
		}
		return new_dists;
	}

	public String distances_to_String(TreeSet<Double> dists) {
		String out = "";
		for (double d : dists)
			out += (d) + ",";
		return out.substring(0, out.length() - 1);
	}

	public String distances_to_String(ArrayList<Double> dists, boolean rev) {
		Collections.sort(dists);
		String out = "";
		if (rev)
			for (double d : dists) {
				String val = d + "                ";
				val = val.substring(0, round_val + 2);
				out += (val) + ",";
			}
		else
			for (int i = dists.size() - 1; i >= 0; i--)
				out += dists.get(i) + ",";
		return out.substring(0, out.length() - 1);

	}

	public void normalize_distances() {
		double total = 100;
		double max = root.get_total_dist();
		double factor = total / max;
		for (Node n : nodes.values()) {
			n.set_total_dist(n.get_total_dist() * factor);
		}
		for (Node n : nodes.values())
			for (Node c : n.get_children().keySet()) {
				n.add_child(c, n.get_total_dist());
			}
	}

	public String data_tile() {
		return leaves.values().iterator().next().data_title();
	}

	public double get_root_offset(Node current_root) {
		return current_root.get_total_dist() * 0.1;
	}

	public Node get_Node(int id) {
		return nodes.get(id);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cm == null) ? 0 : cm.hashCode());
		result = prime * result + ((leaves == null) ? 0 : leaves.hashCode());
		result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
		result = prime * result + ((root == null) ? 0 : root.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tree other = (Tree) obj;
		if (cm != other.cm)
			return false;
		if (leaves == null) {
			if (other.leaves != null)
				return false;
		} else if (!leaves.equals(other.leaves))
			return false;
		if (nodes == null) {
			if (other.nodes != null)
				return false;
		} else if (!nodes.equals(other.nodes))
			return false;
		if (root == null) {
			if (other.root != null)
				return false;
		} else if (!root.equals(other.root))
			return false;
		return true;
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
