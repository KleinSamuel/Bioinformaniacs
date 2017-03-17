package andi.tree;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

public class Plot {

	private String R_path = "/home/proj/biosoft/software/R/R-3.3.0/bin/Rscript";
	private Tree t;
	private static HashMap<Boolean, HashMap<Tree, TreeMap<Node, File>>> paths = new HashMap<>();
	private String temp_dir = "";
	private static boolean node_names = false;

	public Plot(Tree t) {
		this.t = t;
	}

	public File plot(Node n, String temp_dir) throws Exception {
		this.temp_dir = temp_dir;
		return plot(n);
	}

	public static void set_node_names(boolean b) {
		node_names = b;
	}

	public File plot(Node n) throws Exception {
		File plot;
		File r_script;
		File r_newick;
		if (temp_dir.equals("")) {
			plot = File.createTempFile("R_phylo_", ".png");
			r_script = File.createTempFile("R_script_", ".R");
			r_newick = File.createTempFile("R_newick_", ".txt");
		} else {
			plot = File.createTempFile("R_phylo_", ".png", new File(temp_dir));
			r_script = File.createTempFile("R_script_", ".R", new File(temp_dir));
			r_newick = File.createTempFile("R_newick", ".txt", new File(temp_dir));
		}
		plot.deleteOnExit();
		r_script.deleteOnExit();
		r_newick.deleteOnExit();
		System.out.println(r_newick);
		BufferedWriter bw = new BufferedWriter(new FileWriter(r_newick));
		bw.write(t.to_R_newick(n, n, node_names));

		bw.close();
		bw = new BufferedWriter(new FileWriter(r_script));
		bw.write("library(ape);");
		bw.newLine();
		bw.write("png(filename=\"" + plot.getAbsolutePath() + "\", res=100,width = 800, height = 800);");
		bw.newLine();
		bw.write("tree <-read.tree(\"" + r_newick.getAbsolutePath() + "\");");
		bw.newLine();
		bw.write("par(mar=c(7.02,0.82,1.5,0.42));");
		bw.newLine();
		bw.write("plot(tree, root.edge=T, use.edge.length=T, show.node.label=F);");
		bw.newLine();
		if (node_names) {
			bw.write("nodelabels()");
			bw.newLine();
			bw.write("tiplabels()");
			bw.newLine();
		}
		bw.write("title(main=\"" + t.data_tile() + "\")");
		bw.newLine();
		bw.write("axis(1,pos=0.5,at=c(" + t.distances_to_String(t.get_distances_rev(n)) + "),labels=c("
				+ t.distances_to_String((TreeSet<Double>) t.get_distances(n).descendingSet()) + "));");
		bw.newLine();
		for (double d : remove_first_last(t.get_distances_rev(n))) {
			bw.write("abline(v=" + d + ",lty=3,lwd=0.3);");
			bw.newLine();
		}
		bw.write("mtext(\"Distance(" + t.get_cluster_method() + ")\",side=1,line=2,at="
				+ (int) ((n.get_total_dist() / 2) + t.get_root_offset(n)) + ");");
		bw.newLine();
		bw.write("dev.off();");
		bw.close();
		Process plotting = Runtime.getRuntime().exec(R_path + " " + r_script.getAbsolutePath());
		plotting.waitFor();
		return plot;
	}

	private TreeSet<Double> remove_first_last(TreeSet<Double> ts) {
		TreeSet<Double> out = new TreeSet<>();
		out.addAll(ts);
		out.remove(ts.first());
		out.remove(ts.last());
		return out;
	}

	private String double_to_space(TreeSet<Double> ts) {
		String out = "";
		for (Double d : ts)
			out += "\" \",";
		return out.substring(0, out.length() - 1);
	}

	public static File get_plot(Tree tree, Node n) {
		if (!paths.containsKey(node_names))
			paths.put(node_names, new HashMap<>());
		if (!paths.get(node_names).containsKey(tree))
			paths.get(node_names).put(tree, new TreeMap<>());
		if (!paths.get(node_names).get(tree).containsKey(n)) {
			Plot p = new Plot(tree);
			try {
				paths.get(node_names).get(tree).put(n, p.plot(n));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return paths.get(node_names).get(tree).get(n);
	}

	public static File get_plot(Tree tree, Node n, String temp_dir) {
		if (!paths.containsKey(node_names))
			paths.put(node_names, new HashMap<>());
		if (!paths.get(node_names).containsKey(tree))
			paths.get(node_names).put(tree, new TreeMap<>());
		if (!paths.get(node_names).get(tree).containsKey(n)) {
			Plot p = new Plot(tree);
			try {
				paths.get(node_names).get(tree).put(n, p.plot(n, temp_dir));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return paths.get(node_names).get(tree).get(n);
	}

	public static File get_plot(Tree tree, int id) {
		if (!paths.containsKey(node_names))
			paths.put(node_names, new HashMap<>());
		if (!paths.get(node_names).containsKey(tree))
			paths.get(node_names).put(tree, new TreeMap<>());
		if (!paths.get(node_names).get(tree).containsKey(tree.get_Node(id))) {
			Plot p = new Plot(tree);
			try {
				paths.get(node_names).get(tree).put(tree.get_Node(id), p.plot(tree.get_Node(id)));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return paths.get(node_names).get(tree).get(tree.get_Node(id));
	}

	public static File get_plot(Tree tree, int id, String temp_dir) {
		if (!paths.containsKey(node_names))
			paths.put(node_names, new HashMap<>());
		if (!paths.get(node_names).containsKey(tree))
			paths.get(node_names).put(tree, new TreeMap<>());
		if (!paths.get(node_names).get(tree).containsKey(tree.get_Node(id))) {
			Plot p = new Plot(tree);
			try {
				paths.get(node_names).get(tree).put(tree.get_Node(id), p.plot(tree.get_Node(id), temp_dir));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return paths.get(node_names).get(tree).get(tree.get_Node(id));
	}
}
