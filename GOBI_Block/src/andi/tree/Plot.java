package andi.tree;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

public class Plot {

	private String R_path = "/home/proj/biosoft/software/R/R-3.2.2/bin/Rscript";
	private Tree t;
	private static HashMap<Tree,TreeMap<Node,File>> paths = new HashMap<>();

	public Plot(Tree t) {
		this.t = t;
	}

	public File plot(Node n) throws Exception {
		File plot = File.createTempFile("R_phylo_", ".png");
//		plot.deleteOnExit();
		File r_script = File.createTempFile("R_script_", ".R");
		r_script.deleteOnExit();
		File r_newick=File.createTempFile("R_newick", ".txt");
		r_newick.deleteOnExit();
		BufferedWriter bw = new BufferedWriter(new FileWriter(r_newick));
		bw.write(t.to_R_newick(n,n));
		
		bw.close();
		bw = new BufferedWriter(new FileWriter(r_script));
		bw.write("library(ape);");
		bw.newLine();
		bw.write("png(filename=\""+plot.getAbsolutePath()+"\", res=100,width = 800, height = 800);");
		bw.newLine();
		bw.write("tree <-read.tree(\""+r_newick.getAbsolutePath()+"\");");
		bw.newLine();
		bw.write("par(mar=c(7.02,0.82,1.5,0.42));");
		bw.newLine();
		bw.write("plot(tree, root.edge=T, use.edge.length=T, show.node.label=F);");
		bw.newLine();
		bw.write("title(main=\""+t.data_tile()+"\")");
		bw.newLine();
		bw.write("axis(1,pos=0.8,at=c("+t.distances_to_String(t.get_distances_rev(n))+"),labels=c("+t.distances_to_String((TreeSet<Double>)t.get_distances(n).descendingSet())+"));");
		bw.newLine();
		for(double d:remove_first_last(t.get_distances_rev(n))) {
		bw.write("abline(v="+d+",lty=3,lwd=0.3);");
		bw.newLine();
		}
		bw.write("mtext(\"Distance\",side=1,line=5,at="+(int)((n.get_total_dist()/2)+t.get_root_offset(n))+");");
		bw.newLine();
		bw.write("dev.off();");
		bw.close();
		Process plotting = Runtime.getRuntime().exec(R_path+" "+r_script.getAbsolutePath());
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
		if(!paths.containsKey(tree))
			paths.put(tree, new TreeMap<>());
		if(!paths.get(tree).containsKey(n)) {
			Plot p=new Plot(tree);
			try {
				paths.get(tree).put(n, p.plot(n));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
			return paths.get(tree).get(n);
	}
	public static File get_plot(Tree tree, int id) {
		if(!paths.containsKey(tree))
			paths.put(tree, new TreeMap<>());
		if(!paths.get(tree).containsKey(tree.get_Node(id))) {
			Plot p=new Plot(tree);
			try {
				paths.get(tree).put(tree.get_Node(id), p.plot(tree.get_Node(id)));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
			return paths.get(tree).get(tree.get_Node(id));
	}
}
