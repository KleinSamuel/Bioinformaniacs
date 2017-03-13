package andi.tree;

import java.util.Collection;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;


public class Tree {
	public enum Cluster_method{WPGMA,UPGMA,NJ;};

	private TreeMap<Integer,Node> nodes;
	private Node root;
	private TreeMap<Node, Node_Data> leaves;
	private TreeSet<Node> inner;
	private HashSet<Node_Data> nds;
	private Cluster_method cm = Cluster_method.WPGMA;
	public Tree() {
		init();
		nds = new HashSet<>();
	}
	
	public Tree(Collection<Node_Data> nds) {
		init();
		nds = new HashSet<>();
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
		// TODO Auto-generated method stub
		
	}

	private void build_wpgma() {
		// TODO Auto-generated method stub
		
	}
	
}
