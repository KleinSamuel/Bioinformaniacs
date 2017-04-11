package andi.tree;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import andi.analysis.Organism_Data;
import andi.analysis.Organism_Data.Distance_measurement;
import andi.analysis.Organism_Data.Gene_focus;

public class Plot {

	private static String R_path = "/home/proj/biosoft/software/R/R-3.3.0/bin/Rscript";
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
		if (t.get_data_class() == Organism_Data.class && t.get_distance_measurement() == Distance_measurement.DE_count
				&& t.get_gene_focus() == Gene_focus.orthologues_only)
			t.set_round_val(3);
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
		BufferedWriter bw = new BufferedWriter(new FileWriter(r_newick));
		bw.write(t.to_R_newick(n, n, node_names));

		bw.close();
		bw = new BufferedWriter(new FileWriter(r_script));
		bw.write("library(ape);");
		bw.newLine();
		bw.write("png(filename=\"" + plot.getAbsolutePath() + "\", res=80,width = 800, height = 800);");
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
		ArrayList<Double> dists_rev = t.get_distances_rev(n);
		bw.write("axis(1,pos=0.7,at=c(" + t.distances_to_String(dists_rev, false) + "),labels=c("
				+ t.distances_to_String(t.get_distances(n), true) + "));");
		bw.newLine();
		for (int i = 1; i < dists_rev.size() - 1; i++) {
			bw.write("abline(v=" + dists_rev.get(i) + ",lty=3,lwd=0.3);");
			bw.newLine();
		}
		double leaves = n.count_leaves();
		bw.write("mtext(\"" + t.get_distance_measurement_String() + "(" + t.get_cluster_method() + ")\",side=1,line="
				+ (leaves < 5 ? 5.5 * (leaves / (0.001 + leaves)) : 0.5 * (5.0 + (leaves) / (leaves))) + ",at="
				+ ((n.get_total_dist() / 2.0) + t.get_root_offset(n)) + ");");
		bw.newLine();
		bw.write("dev.off();");
		bw.close();
		Process plotting = Runtime.getRuntime().exec(R_path + " " + r_script.getAbsolutePath());
		plotting.waitFor();
		return plot;
	}

	public static File get_plot(Tree tree, Node n) {
		return get_plot_clone(tree.clone(), n.clone());
	}

	private static File get_plot_clone(Tree tree, Node n) {
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
		return get_plot_clone(tree.clone(), n.clone(), temp_dir);
	}

	private static File get_plot_clone(Tree tree, Node n, String temp_dir) {
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

	public static File get_plot(Tree tree) {
		return get_plot(tree, tree.get_root());
	}

	public static File get_plot(Tree tree, int id) {
		return get_plot(tree, tree.get_Node(id));
	}

	public static File get_plot(Tree tree, int id, String temp_dir) {
		return get_plot(tree, tree.get_Node(id), temp_dir);
	}

	public static File get_heatmap(ArrayList<Tree> trees) {
		File heatmap_data = new File("/home/proj/biocluster/praktikum/genprakt/bioinformaniacs/Andi/heatmap.txt");
		File heatmap = new File("/home/proj/biocluster/praktikum/genprakt/bioinformaniacs/Andi/heatmap.pdf");
		try {
			double[][] heatmap_dists = new double[trees.size()][trees.size()];
			for (int x = 0; x < trees.size(); x++)
				for (int y = x; y < trees.size(); y++)
						heatmap_dists[x][y] = heatmap_dists[y][x] = trees.get(x).compare_to(trees.get(y));

			BufferedWriter bw_data = new BufferedWriter(new FileWriter(heatmap_data));
			String header = "";
			for (Tree t : trees)
				header += ((Organism_Data) t.get_node_data())
						.get_description(t.get_distance_measurement() , t.get_gene_focus()) + "\t";
			header = header.substring(0, header.length() - 1);
			bw_data.write(header);
			bw_data.newLine();
			for (double[] l : heatmap_dists) {
				String line = "";
				for (double d : l)
					line += d + "\t";
				bw_data.write(line.substring(0, line.length() - 1));
				bw_data.newLine();
			}
			bw_data.close();
			double marg = 25.0/*+((double)trees.size()/90)*/;
			double size = trees.size()<10 ? 7.0 : 27.0-(200.0/(double)trees.size());
			File heatmap_R = File.createTempFile("heatmap", ".R",
					new File("/home/proj/biocluster/praktikum/genprakt/bioinformaniacs/Andi"));
			heatmap_R.deleteOnExit();
			BufferedWriter bw_R = new BufferedWriter(new FileWriter(heatmap_R));
			bw_R.write("library(gplots)");
			bw_R.newLine();
			bw_R.write("pdf(file=\"" + heatmap.getAbsolutePath() + "\",width="+size+",height="+size+")");
			bw_R.newLine();
			bw_R.write("mycolors <- colorRampPalette(c(\"black\",\"blue\",\"white\"))(n=150)");
			bw_R.newLine();
			bw_R.write("tab <- read.table(file=\"" + heatmap_data.getAbsolutePath() + "\",header=T, sep=\"\\t\", check.names=F)");
			bw_R.newLine();
			bw_R.write("rownames(tab) <- colnames(tab)");
			bw_R.newLine();
			bw_R.write("heatmap.2(data.matrix(tab),dendrogram=\"none\",trace=\"none\",col=mycolors,margins=c("+marg+","+marg+"))");
			bw_R.newLine();
			bw_R.write("dev.off()");
			bw_R.close();
			Process plotting = Runtime.getRuntime().exec(R_path + " " + heatmap_R.getAbsolutePath());
			plotting.waitFor();

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}

		return heatmap;
	}
}
