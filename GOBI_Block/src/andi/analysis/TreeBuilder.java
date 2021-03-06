package andi.analysis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import andi.analysis.Organism_Data.Distance_measurement;
import andi.analysis.Organism_Data.Gene_focus;
import andi.tree.Node;
import andi.tree.Node_Data;
import andi.tree.Plot;
import andi.tree.Tree;
import andi.tree.Tree.Cluster_method;
import dennis.tissues.Tissue;
import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;

public class TreeBuilder {
	public enum Tree_status {
		Prepared, Built, None, Init;
	};

	private TreeMap<Species, TreeSet<Tissue>> species_tissues;

	private ArrayList<String> all_orthologues;
	private TreeMap<Species, TreeSet<String>> orthologues_own;
	private TreeMap<Species, TreeSet<String>> orthologues_paired;
	private TreeMap<Species, Vector<Boolean>> shared;
	private TreeMap<Tissue, TreeSet<Node_Data>> leave_data;
	private TreeSet<Node_Data> basic_leaves;
	private ArrayList<Process> open_viewers;
	private ArrayList<Tree> go_trees;
	private ArrayList<Tree> de_trees;
	private Tree_status de_tree_status = Tree_status.None;
	private Tree_status go_tree_status = Tree_status.None;
	private TreeSet<String> t_filter;
	private TreeSet<String> s_filter;
	private Tree orthologue_tree;
	private int top_count  = 50;
	private Distance_measurement orthologue_dm = Distance_measurement.Avg_seq_id_max;
	private Distance_measurement de_dm = Distance_measurement.DE_count;
	private Distance_measurement go_dm = Distance_measurement.GO_tissue_basic;

	public TreeBuilder(ArrayList<String> species, ArrayList<String> tissues, boolean um_initialized) {
		if (!um_initialized)
			init_um();
		set_species_filter(species);
		set_tissue_filter(tissues);
		init();

	}

	private void init() {
		this.species_tissues = new TreeMap<>();
		this.open_viewers = new ArrayList<>();
		go_tree_status = Tree_status.Init;
		de_tree_status = Tree_status.Init;
		Iterator<Species> it_sp = UtilityManager.speciesIterator();
		leave_data = new TreeMap<>();
		while (it_sp.hasNext()) {
			Species s = it_sp.next();
			if (s_filter == null || s_filter.contains(s.getName())) {
				this.species_tissues.put(s, new TreeSet<>());
				Iterator<Tissue> it_ti = UtilityManager.tissueIterator(s);
				while (it_ti.hasNext()) {
					Tissue t = it_ti.next();
					if (t_filter == null || t_filter.contains(t.getName())) {
						leave_data.put(t, new TreeSet<>());
						this.species_tissues.get(s).add(t);
					}
				}
			}
		}
		compute_orthologues();
		prepare_leaves();
	}

	private void prepare_leaves() {
		basic_leaves = new TreeSet<>();
		for (Species s : species_tissues.keySet()) {
			Organism_Data od = new Organism_Data(s.getId(), s.getName(), s);
			od.set_all_orthologues(all_orthologues);
			od.set_own_genes(orthologues_own.get(s));
			od.set_orthologue_genes(orthologues_paired.get(s));
			od.set_shared_vector(shared.get(s));
			basic_leaves.add(od);
		}
		for (Species s : species_tissues.keySet())
			for (Tissue t : species_tissues.get(s)) {
				Organism_Data od = new Organism_Data(s.getId(), s.getName(), s, t.getName(),
						"/home/proj/biocluster/praktikum/genprakt/bioinformaniacs/data_update/" + s.getId() + "/"
								+ t.getName() + "/star_tissue_average.counts",
						Distance_measurement.GO_tissue_basic);
				od.set_all_orthologues(all_orthologues);
				od.set_own_genes(orthologues_own.get(s));
				od.set_orthologue_genes(orthologues_paired.get(s));
				od.set_shared_vector(shared.get(s));
				leave_data.get(t).add(od);
			}

	}

	private void set_go_tree_use_all_go_terms(boolean b) {
		if (go_tree_status == Tree_status.None)
			init();
		if (go_tree_status == Tree_status.Init)
			prepare_go_trees();
		for (Tree t : get_go_trees())
			if (go_tree_status == Tree_status.Built)
				t.change_go_to_root(b);
			else if (go_tree_status == Tree_status.Prepared) {
				System.out.println("\tset_go_to root " + b);
				t.set_go_to_root(b);
			}
	}

	private void compute_orthologues() {
		all_orthologues = new ArrayList<>();
		orthologues_own = new TreeMap<>();
		orthologues_paired = new TreeMap<>();
		shared = new TreeMap<>();

		TreeSet<String> all_orth_set = new TreeSet<>();

		for (Species s1 : species_tissues.keySet()) {
			shared.put(s1, new Vector<>());
			orthologues_own.put(s1, new TreeSet<>());
			orthologues_paired.put(s1, new TreeSet<>());
			for (Species s2 : species_tissues.keySet()) {
				if (s1 == s2)
					continue;
				HashSet<String> orths = UtilityManager.getSimilarityHandler().getAllGenesWithAnOrtholog(s1, s2);
				orthologues_own.get(s1).addAll(orths);
				all_orth_set.addAll(orths);
				orthologues_paired.get(s1)
						.addAll(UtilityManager.getSimilarityHandler().getAllGenesWithAnOrtholog(s2, s1));
			}
		}
		all_orthologues.addAll(all_orth_set);
		for (String gene : all_orthologues)
			for (Species s : species_tissues.keySet())
				shared.get(s)
						.add(orthologues_own.get(s).contains(gene) | orthologues_paired.get(s).contains(gene) ? true
								: false);

	}

	public void init_um() {
		UtilityManager um = new UtilityManager(
				"/home/m/maieran/git/Bioinformaniacs/GOBI_Block/bin/andi/analysis/config.txt", false, true, false);
	}

	public void view(Tree t) {
		try {
			Process p = Runtime.getRuntime().exec("display " + Plot.get_plot(t));
			open_viewers.add(p);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void view(Collection<Tree> ts) {
		try {
			for (Tree t : ts) {
				Process p = Runtime.getRuntime().exec("display " + Plot.get_plot(t));
				open_viewers.add(p);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void wait_for_close() {
		for (Process p : open_viewers)
			try {
				p.waitFor();
			} catch (InterruptedException e) {
				System.out.println(p.toString() + " threw an exception while wating for closing");
				e.printStackTrace();
			}
	}

	public void prepare_go_trees() {
		go_trees = new ArrayList<>();
		for (Tissue t : leave_data.keySet()) {
			go_trees.add(new Tree(leave_data.get(t), false));
			go_trees.get(go_trees.size() - 1).set_distance_measurement(go_dm);
		}
		go_tree_status = Tree_status.Prepared;
	}

	public void prepare_de_trees() {
		de_trees = new ArrayList<>();
		for (Tissue t : leave_data.keySet()) {
			de_trees.add(new Tree(leave_data.get(t), false));
			de_trees.get(de_trees.size() - 1).set_distance_measurement(de_dm);
		}
		de_tree_status = Tree_status.Prepared;
	}

	public ArrayList<Tree> get_go_trees() {
		if (go_tree_status == Tree_status.None)
			init();
		if (go_tree_status == Tree_status.Init)
			prepare_go_trees();
		return go_trees;
	}

	public ArrayList<Tree> get_de_trees() {
		if (de_tree_status == Tree_status.None)
			init();
		if (de_tree_status == Tree_status.Init)
			prepare_de_trees();
		return de_trees;
	}

	public ArrayList<Tree> build_go_trees() {
		if (go_tree_status == Tree_status.Prepared) {
			for (Tree t : get_go_trees())
				t.build();
			go_tree_status = Tree_status.Built;
		}
		return go_trees;
	}

	public ArrayList<Tree> build_de_trees() {
		if (de_tree_status == Tree_status.Prepared) {
			for (Tree t : get_de_trees())
				t.build();
			de_tree_status = Tree_status.Built;
		}
		return de_trees;
	}

	public void view_go_trees() {
		try {
			if (go_tree_status == Tree_status.Init)
				prepare_go_trees();
			if (go_tree_status == Tree_status.Prepared) {
				build_go_trees();
			}
			if (go_tree_status == Tree_status.Built)
				for (Tree t : get_go_trees()) {
					Process p = Runtime.getRuntime().exec("display " + Plot.get_plot(t));
					open_viewers.add(p);
				}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void view_de_trees() {
		try {
			if (de_tree_status == Tree_status.Init)
				prepare_de_trees();
			if (de_tree_status == Tree_status.Prepared) {
				build_de_trees();
			}
			if (de_tree_status == Tree_status.Built)
				for (Tree t : get_de_trees()) {
					Process p = Runtime.getRuntime().exec("display " + Plot.get_plot(t));
					open_viewers.add(p);
				}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void set_go_tree_gene_focus(Gene_focus gf) {
		if (go_tree_status == Tree_status.None)
			prepare_go_trees();
		for (Tree t : get_go_trees())
			if (go_tree_status == Tree_status.Built)
				t.change_gene_focus(gf);
			else if (go_tree_status == Tree_status.Prepared) {
				t.set_gene_focus(gf);
			}
	}

	public void set_de_tree_gene_focus(Gene_focus gf) {
		if (de_tree_status == Tree_status.None)
			prepare_de_trees();
		for (Tree t : get_de_trees())
			if (de_tree_status == Tree_status.Built)
				t.change_gene_focus(gf);
			else if (de_tree_status == Tree_status.Prepared) {
				t.set_gene_focus(gf);
			}
	}

	public void de_pair_view(Gene_focus gf1, Gene_focus gf2) {
		TreeMap<String, ArrayList<Tree>> pairs = new TreeMap<>();
		set_de_tree_gene_focus(gf1);
		if (de_tree_status == Tree_status.Init)
			prepare_de_trees();
		if (de_tree_status == Tree_status.Prepared) {
			build_de_trees();
		}
		if (de_tree_status == Tree_status.Built)
			for (Tree t : get_de_trees()) {
				String tissue = ((Organism_Data) t.get_node_data()).get_tissue();
				if (!pairs.containsKey(tissue))
					pairs.put(tissue, new ArrayList<>());
				pairs.get(tissue).add(t.clone());
			}
		set_de_tree_gene_focus(gf2);
		if (de_tree_status == Tree_status.Init)
			prepare_de_trees();
		if (de_tree_status == Tree_status.Prepared) {
			build_de_trees();
		}
		if (de_tree_status == Tree_status.Built)
			for (Tree t : get_de_trees()) {
				String tissue = ((Organism_Data) t.get_node_data()).get_tissue();
				if (!pairs.containsKey(tissue))
					pairs.put(tissue, new ArrayList<>());
				pairs.get(tissue).add(t.clone());
			}
		for (String tissue : pairs.keySet()) {
			for (Tree t : pairs.get(tissue)) {
				try {
					Process e = Runtime.getRuntime().exec("display " + Plot.get_plot(t));
					open_viewers.add(e);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			wait_for_close();
		}
	}

	public void go_pair_view(Gene_focus gf1, Gene_focus gf2) {
		TreeMap<String, ArrayList<Tree>> pairs = new TreeMap<>();
		set_go_tree_gene_focus(gf1);
		if (go_tree_status == Tree_status.Init)
			prepare_go_trees();
		if (go_tree_status == Tree_status.Prepared) {
			build_go_trees();
		}
		if (go_tree_status == Tree_status.Built)
			for (Tree t : get_go_trees()) {
				String tissue = ((Organism_Data) t.get_node_data()).get_tissue();
				if (!pairs.containsKey(tissue))
					pairs.put(tissue, new ArrayList<>());
				pairs.get(tissue).add(t.clone());
			}
		set_go_tree_gene_focus(gf2);
		if (go_tree_status == Tree_status.Init)
			prepare_go_trees();
		if (go_tree_status == Tree_status.Prepared) {
			build_go_trees();
		}
		if (go_tree_status == Tree_status.Built)
			for (Tree t : get_go_trees()) {
				String tissue = ((Organism_Data) t.get_node_data()).get_tissue();
				if (!pairs.containsKey(tissue))
					pairs.put(tissue, new ArrayList<>());
				pairs.get(tissue).add(t.clone());
			}
		for (String tissue : pairs.keySet()) {
			Tree t1 = null;
			Tree t2 = null;
			double dist = 0;
			for (Tree t : pairs.get(tissue)) {
				t.reconstruct();
				if (t1 == null) {
					System.out.println("Tree distance to orthologue:");
					t1 = t;
				} else
					t2 = t;
				dist = build_avg_sequence_id_of_orthologues_tree().compare_to(t);
				try {
					Process e = Runtime.getRuntime().exec("display " + Plot.get_plot(t));
					open_viewers.add(e);
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.out.println(
						t.get_node_data().data_title() + " - " + t.get_distance_measurement_String() + ":\t" + dist);
				if (t1 != null && t2 != null)
					System.out.println("Tree distance: " + t1.compare_to(t2));
			}
			wait_for_close();
		}
	}

	public void go_pair_view() {
		TreeMap<String, ArrayList<Tree>> pairs = new TreeMap<>();
		set_go_tree_use_all_go_terms(false);
		if (go_tree_status == Tree_status.Init)
			prepare_go_trees();
		if (go_tree_status == Tree_status.Prepared) {
			build_go_trees();
		}
		if (go_tree_status == Tree_status.Built)
			for (Tree t : get_go_trees()) {
				String tissue = ((Organism_Data) t.get_node_data()).get_tissue();
				if (!pairs.containsKey(tissue))
					pairs.put(tissue, new ArrayList<>());
				pairs.get(tissue).add(t.clone());
			}
		set_go_tree_use_all_go_terms(true);
		if (go_tree_status == Tree_status.Init)
			prepare_go_trees();
		if (go_tree_status == Tree_status.Prepared) {
			build_go_trees();
		}
		if (go_tree_status == Tree_status.Built)
			for (Tree t : get_go_trees()) {
				String tissue = ((Organism_Data) t.get_node_data()).get_tissue();
				if (!pairs.containsKey(tissue))
					pairs.put(tissue, new ArrayList<>());
				pairs.get(tissue).add(t.clone());
			}
		for (String tissue : pairs.keySet()) {
			Tree t1 = null;
			Tree t2 = null;
			double dist = 0;
			for (Tree t : pairs.get(tissue)) {
				t.reconstruct();
				if (t1 == null) {
					System.out.println("Tree distance to orthologue:");
					t1 = t;
				} else
					t2 = t;
				dist = build_avg_sequence_id_of_orthologues_tree().compare_to(t);

				System.out.println(
						t.get_node_data().data_title() + " - " + t.get_distance_measurement_String() + ":\t" + dist);
				try {
					Process e = Runtime.getRuntime().exec("display " + Plot.get_plot(t));
					open_viewers.add(e);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (t1 != null && t2 != null) {
					System.out.println("Tree distance: " + t1.compare_to(t2));
				}
			}
			wait_for_close();
		}
	}

	public void set_tissue_filter(Collection<String> tissues) {
		if (tissues == null) {
			t_filter = null;
			return;
		}
		t_filter = new TreeSet<>();
		t_filter.addAll(tissues);
		go_tree_status = Tree_status.None;
		de_tree_status = Tree_status.None;
	}

	public void set_species_filter(Collection<String> species) {
		if (species == null) {
			s_filter = null;
			return;
		}
		s_filter = new TreeSet<>();
		s_filter.addAll(species);
		go_tree_status = Tree_status.None;
		de_tree_status = Tree_status.None;
	}

	public void reset_species_filter() {
		set_species_filter(null);
	}

	public void reset_tissue_filter() {
		set_tissue_filter(null);
	}

	public void reset_filters() {
		reset_species_filter();
		reset_tissue_filter();
	}

	public void change_basic_leaves(Distance_measurement dm) {
		if (((Organism_Data) basic_leaves.first()).get_dm() == dm)
			return;

	}

	public Tree build_avg_sequence_id_of_orthologues_tree() {
		if (orthologue_tree == null)
			orthologue_tree = new Tree(basic_leaves);
		return orthologue_tree;
	}

	public void set_gene_focus(Gene_focus gf) {
		set_de_tree_gene_focus(gf);
		set_go_tree_gene_focus(gf);
	}

	public ArrayList<Tree> get_trees(ArrayList<Gene_focus> gfs, ArrayList<Cluster_method> cms,
			ArrayList<Distance_measurement> dms, ArrayList<String> tissue_filter, ArrayList<String> organism_filter) {
		ArrayList<Tree> trees = new ArrayList<>();
		set_species_filter(organism_filter);
		set_tissue_filter(tissue_filter);
		if (gfs == null) {
			gfs = new ArrayList<>();
			gfs.add(Gene_focus.All_genes);
			gfs.add(Gene_focus.de_only);
			gfs.add(Gene_focus.nonde_only);
			gfs.add(Gene_focus.nonorthologues_only);
			gfs.add(Gene_focus.orthologues_only);
		}
		// if (cms == null) {
		// cms = new ArrayList<>();
		// cms.add(Cluster_method.UPGMA);
		// cms.add(Cluster_method.WPGMA);
		// }
		if (dms == null) {
			dms = new ArrayList<>();
//			dms.add(Distance_measurement.Avg_seq_id_all);
//			dms.add(Distance_measurement.Avg_seq_id_max);
			dms.add(Distance_measurement.DE_count);
			dms.add(Distance_measurement.GO_tissue_basic);
		}
		if (dms.contains(Distance_measurement.Avg_seq_id_all))
			trees.add(build_avg_sequence_id_of_orthologues_tree()
					.change_distance_measurement(Distance_measurement.Avg_seq_id_all).clone());
		if (dms.contains(Distance_measurement.Avg_seq_id_max))
			trees.add(build_avg_sequence_id_of_orthologues_tree()
					.change_distance_measurement(Distance_measurement.Avg_seq_id_max).clone());
		for (Distance_measurement dm : dms) {
			change_distance_measurement(dm);
			for (Gene_focus gf : gfs) {
				set_gene_focus(gf);
				trees.addAll(get_trees(gf, dm));
			}
		}
		return trees;
	}

	private ArrayList<Tree> get_trees(Gene_focus gf, Distance_measurement dm) {
		ArrayList<Tree> trees = new ArrayList<>();
		if (dm == Distance_measurement.DE_count && gf != Gene_focus.nonde_only)
			for (Tree t : build_de_trees())
				trees.add(t.clone());
		if (dm == Distance_measurement.GO_tissue_basic || dm == Distance_measurement.GO_tissue_xgsa)
			for (Tree t : build_go_trees())
				trees.add(t.clone());
		return trees;
	}

	public void change_distance_measurement(Distance_measurement dm) {
		if (dm == Distance_measurement.Avg_seq_id_all || dm == Distance_measurement.Avg_seq_id_max)
			change_basic_dm(dm);
		if (dm == Distance_measurement.DE_count)
			change_de_dm(dm);
		if (dm == Distance_measurement.GO_tissue_basic || dm == Distance_measurement.GO_tissue_xgsa)
			change_go_dm(dm);
	}

	private void change_basic_dm(Distance_measurement dm) {
		if (basic_leaves != null) {
			if (orthologue_dm == dm)
				return;
			orthologue_dm = dm;
			for (Node_Data nd : basic_leaves)
				((Organism_Data) nd).set_distance_measurement(dm);
			orthologue_tree = null;
		}
	}

	private void change_de_dm(Distance_measurement dm) {
		if (de_tree_status == Tree_status.None)
			prepare_de_trees();
		for (Tree t : get_de_trees())
			if (de_tree_status == Tree_status.Built)
				t.change_distance_measurement(dm);
			else if (de_tree_status == Tree_status.Prepared) {
				t.set_distance_measurement(dm);
			}
	}

	private void change_go_dm(Distance_measurement dm) {
		if (go_tree_status == Tree_status.None)
			prepare_go_trees();
		for (Tree t : get_go_trees())
			if (go_tree_status == Tree_status.Built)
				t.change_distance_measurement(dm);
			else if (go_tree_status == Tree_status.Prepared) {
				t.set_distance_measurement(dm);
			}
	}
	
	private void change_go_top_count(int top_x) {
		if(top_x==top_count)
			return;
		top_count=top_x;
		if (go_tree_status == Tree_status.None)
			prepare_go_trees();
		for (Tree t : get_go_trees())
			if (go_tree_status == Tree_status.Built)
				t.change_top_go_term_count(top_x);
			else if (go_tree_status == Tree_status.Prepared) {
				t.set_top_go_term_count(top_x);
			}
	}

	public String get_heatmap_name(Cluster_method cm, ArrayList<Gene_focus> gf_filter,
			ArrayList<Distance_measurement> dm_filter, boolean avg, boolean dist, int top_count) {
		String out = cm.name() + "_";
		if (dm_filter.contains(Distance_measurement.Avg_seq_id_all))
			dm_filter.remove(Distance_measurement.Avg_seq_id_all);
		if (dm_filter.contains(Distance_measurement.Avg_seq_id_max))
			dm_filter.remove(Distance_measurement.Avg_seq_id_max);
		if (gf_filter.size() > 1) {
			out += "compGF(";
			for (int i = 0; i < 2; i++) {
				switch (gf_filter.get(i)) {
				case All_genes:
					out += "all";
					break;
				case de_only:
					out += "DE";
					break;
				case nonde_only:
					out += "nonDE";
					break;
				case orthologues_only:
					out += "Orth";
					break;
				case nonorthologues_only:
					out += "nonOrth";
					break;
				}
				out += i == 0 ? "-" : ")_";
			}
			out += "DM(";
			switch (dm_filter.get(0)) {
			case DE_count:
				out += "DE";
				break;
			case GO_tissue_basic:
				out += "GO("+top_count+")";
				break;
			case GO_tissue_xgsa:
				out += "XGSA";
				break;
			}
			out += ")_";

		} else {
			out += "GF(";

			switch (gf_filter.get(0)) {
			case All_genes:
				out += "all";
				break;
			case de_only:
				out += "DE";
				break;
			case nonde_only:
				out += "nonDE";
				break;
			case orthologues_only:
				out += "Orth";
				break;
			case nonorthologues_only:
				out += "nonOrth";
				break;
			}
			out += ")_compDM(";
			for (int i = 0; i < 2; i++) {
				switch (dm_filter.get(i)) {
				case DE_count:
					out += "DE";
					break;
				case GO_tissue_basic:
					out += "GO";
					break;
				case GO_tissue_xgsa:
					out += "XGSA";
					break;
				}
				out += i == 0 ? "-" : ")_";
			}
		}
		out += (avg ? "avg" : "tot") + "_" + (dist ? "dist" : "count");

		return out;
	}

	public void compute_all_interesting_heatmaps() {
		compute_all_interesting_heatmaps(false);
	}
	
	public int get_top_count() {
		return top_count;
	}

	public void compute_all_interesting_heatmaps(boolean show_when_plotted) {
		ArrayList<Gene_focus> gf_filter = new ArrayList<>(
				Arrays.asList(new Gene_focus[] { Gene_focus.All_genes, Gene_focus.de_only, Gene_focus.nonde_only,
						Gene_focus.nonorthologues_only, Gene_focus.orthologues_only }));
		ArrayList<Cluster_method> cm_filter = new ArrayList<>(
				Arrays.asList(new Cluster_method[] { Cluster_method.UPGMA }));
		ArrayList<Distance_measurement> dm_filter = new ArrayList<>(Arrays.asList(
				new Distance_measurement[] { Distance_measurement.GO_tissue_basic, Distance_measurement.DE_count }));
		System.out.println("Computing Gene-Focus differences:");

		System.out.println("Computing Distance-Measurement differences:");
		ArrayList<Tree> trees;
		for (Cluster_method cm : cm_filter) {
			for (Boolean avg : new ArrayList<Boolean>(Arrays.asList(new Boolean[] { true, false }))) {
				Tree.set_dist_avg(avg);
				for (Boolean dist : new ArrayList<Boolean>(Arrays.asList(new Boolean[] { true, false }))) {
					Node.set_node_dist(dist);
					for (Distance_measurement dm : dm_filter) {
						for (Gene_focus gf : gf_filter) {
							for (Distance_measurement dm2 : dm_filter) {
								if (dm_filter.indexOf(dm) >= dm_filter.indexOf(dm2))
									continue;
								ArrayList<Gene_focus> gfs = new ArrayList<Gene_focus>(
										Arrays.asList(new Gene_focus[] { gf }));
								ArrayList<Distance_measurement> dms = new ArrayList<Distance_measurement>(Arrays.asList(
										new Distance_measurement[] { Distance_measurement.Avg_seq_id_max, dm, dm2 }));
								trees = this.get_trees(gfs,
										new ArrayList<Cluster_method>(Arrays.asList(new Cluster_method[] { cm })), dms);
								System.out.println(
										"Done with computation of " + this.get_heatmap_name(cm, gfs, dms, avg, dist,20));
								System.out.print("Plotting");
								File map = Plot.get_heatmap(trees, this.get_heatmap_name(cm, gfs, dms, avg, dist,20));
								System.out.println(" finshed:\n" + map.getAbsolutePath());
								if (show_when_plotted)
									try {
										Runtime.getRuntime().exec("chromium " + map.getAbsolutePath());
									} catch (IOException e) {
										e.printStackTrace();
									}
							}
							for (Gene_focus gf2 : gf_filter) {
								if (gf_filter.indexOf(gf) >= gf_filter.indexOf(gf2))
									continue;
								ArrayList<Gene_focus> gfs = new ArrayList<Gene_focus>(
										Arrays.asList(new Gene_focus[] { gf, gf2 }));
								ArrayList<Distance_measurement> dms = new ArrayList<Distance_measurement>(Arrays.asList(
										new Distance_measurement[] { Distance_measurement.Avg_seq_id_max, dm }));
								trees = this.get_trees(gfs,
										new ArrayList<Cluster_method>(Arrays.asList(new Cluster_method[] { cm })), dms);
								System.out.println("Done with with computation of "
										+ this.get_heatmap_name(cm, gfs, dms, avg, dist,20));
								System.out.print("Plotting");
								File map = Plot.get_heatmap(trees, this.get_heatmap_name(cm, gfs, dms, avg, dist,20));
								System.out.println(" finshed:\n" + map.getAbsolutePath());
								if (show_when_plotted)
									try {
										Runtime.getRuntime().exec("chromium " + map.getAbsolutePath());
									} catch (IOException e) {
										e.printStackTrace();
									}
							}

						}
					}
				}
			}

		}

	}

	private ArrayList<Tree> get_trees(ArrayList<Gene_focus> gf_filter, ArrayList<Cluster_method> cm_filter,
			ArrayList<Distance_measurement> dm_filter) {
		return get_trees(gf_filter, cm_filter, dm_filter, null, null);
	}

	public static void main(String[] args) {

		boolean demo = false;
		TreeBuilder b;
		if (demo) {
			ArrayList<String> tissues = new ArrayList<>(Arrays.asList(new String[] { "brain", "testis" }));
			b = new TreeBuilder(null, tissues, false);
			b.view(b.build_avg_sequence_id_of_orthologues_tree());
			b.wait_for_close();
			b.go_pair_view();
			b.go_pair_view(Gene_focus.de_only, Gene_focus.nonde_only);
			b.go_pair_view(Gene_focus.orthologues_only, Gene_focus.nonorthologues_only);
			b.set_go_tree_use_all_go_terms(true);
			b.go_pair_view(Gene_focus.de_only, Gene_focus.nonde_only);
			b.go_pair_view(Gene_focus.orthologues_only, Gene_focus.nonorthologues_only);
			b.de_pair_view(Gene_focus.orthologues_only, Gene_focus.nonorthologues_only);
			b.de_pair_view(Gene_focus.de_only, Gene_focus.nonde_only);
		} else {
			b = new TreeBuilder(null, null, false);
//			b.compute_all_interesting_heatmaps(false);
			ArrayList<Gene_focus> gf_filter = new ArrayList<>(
					Arrays.asList(new Gene_focus[] {Gene_focus.All_genes, Gene_focus.de_only, Gene_focus.nonde_only,
							Gene_focus.nonorthologues_only, Gene_focus.orthologues_only }));
			ArrayList<Cluster_method> cm_filter = new ArrayList<>(
					Arrays.asList(new Cluster_method[] { Cluster_method.UPGMA }));
			ArrayList<Distance_measurement> dm_filter = new ArrayList<>(Arrays.asList(
					new Distance_measurement[] { Distance_measurement.GO_tissue_basic, Distance_measurement.DE_count }));
			System.out.println("Generating Trees");
			ArrayList<Tree> trees = new ArrayList<>();
			b.set_go_tree_use_all_go_terms(true);
			b.change_go_top_count(100);
			trees.addAll(b.get_trees(gf_filter,cm_filter, dm_filter));
			trees.add(b.build_avg_sequence_id_of_orthologues_tree());
//			trees.addAll(b.get_trees(null,null,null));
			try {
				Runtime.getRuntime().exec("display "+Plot.get_plot(b.build_avg_sequence_id_of_orthologues_tree()));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
//			for(Tree t:b.get_trees(gf_filter, cm_filter, dm_filter)) {
//				try {
//					Process p1 = Runtime.getRuntime().exec("display "+Plot.get_plot(t));
//					Process p2 = Runtime.getRuntime().exec("display "+Plot.get_plot(t.change_go_to_root(true).change_top_go_term_count(100)));
//					p1.waitFor();
//					p2.waitFor();
//				} catch (IOException e) {
//					e.printStackTrace();
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
			Tree.set_dist_avg(false);
			Node.set_node_dist(false);
			System.out.println("Plotting UPGMA_go_all_100_tot_count:");
			System.out.println(Plot.get_heatmap(trees, "UPGMA_go_all_100_tot_count"));
//			System.out.println("Orthologues tree:\n" + Plot.get_plot(b.build_avg_sequence_id_of_orthologues_tree()));
			int count = 0;
			for (Tree other : trees) {
				if (other.hashCode()!=b.build_avg_sequence_id_of_orthologues_tree().hashCode()&&other.compare_to(b.build_avg_sequence_id_of_orthologues_tree()) < 7)
//					System.out.println(other.data_tile() + ":\n" + Plot.get_plot(other));
					count++;
			}
			System.out.println("count = "+count);
			count=0;
			
			Node.set_node_dist(true);
			System.out.println("Plotting UPGMA_go_all_100_tot_dist:");
			System.out.println(Plot.get_heatmap(trees, "UPGMA_go_all_100_tot_dist"));
			
			for (Tree other : trees) {
				if (other.hashCode()!=b.build_avg_sequence_id_of_orthologues_tree().hashCode()&&other.compare_to(b.build_avg_sequence_id_of_orthologues_tree()) < 0.75)
//					System.out.println(other.data_tile() + ":\n" + Plot.get_plot(other));
					count++;
			}
			System.out.println("count = "+count);
			count=0;
			
			
			
			
			
			Tree.set_dist_avg(true);
			System.out.println("Plotting UPGMA_go_all_100_avg_dist:");
			System.out.println(Plot.get_heatmap(trees, "UPGMA_go_all_100_avg_dist"));
			
			
			for (Tree other : trees) {
				if (other.hashCode()!=b.build_avg_sequence_id_of_orthologues_tree().hashCode()&&other.compare_to(b.build_avg_sequence_id_of_orthologues_tree()) < 0.045)
//					System.out.println(other.data_tile() + ":\n" + Plot.get_plot(other));
					count++;
			}
			System.out.println("count = "+count);
			count=0;
			
			
			
			Node.set_node_dist(false);
			System.out.println("Plotting UPGMA_go_all_100_avg_count:");
			System.out.println(Plot.get_heatmap(trees, "UPGMA_go_all_100_avg_count"));
			
			for (Tree other : trees) {
				if (other.hashCode()!=b.build_avg_sequence_id_of_orthologues_tree().hashCode()&&other.compare_to(b.build_avg_sequence_id_of_orthologues_tree()) < 0.9)
//					System.out.println(other.data_tile() + ":\n" + Plot.get_plot(other));
					count++;
			}
			System.out.println("count = "+count);
			
//			Tree.set_dist_avg(true);
//			Node.set_node_dist(false);
		}
	}

}
