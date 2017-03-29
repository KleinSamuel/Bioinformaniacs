package andi.analysis.go;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import andi.analysis.go.total.Organism_Data;
import andi.analysis.go.total.Organism_Data.Distance_measurement;
import andi.analysis.go.total.Organism_Data.Gene_focus;
import andi.tree.Node_Data;
import andi.tree.Plot;
import andi.tree.Tree;
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
				"/home/m/maieran/git/Bioinformaniacs/GOBI_Block/bin/andi/analysis/go/config.txt", false, true, false);
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
		System.out.println("\tprepare go trees");
		go_trees = new ArrayList<>();
		for (Tissue t : leave_data.keySet()) {
			go_trees.add(new Tree(leave_data.get(t), false));
			go_trees.get(go_trees.size() - 1).set_distance_measurement(Distance_measurement.GO_tissue_basic);
		}
		go_tree_status = Tree_status.Prepared;
	}

	public void prepare_de_trees() {
		System.out.println("\tprepare de trees");
		de_trees = new ArrayList<>();
		for (Tissue t : leave_data.keySet()) {
			de_trees.add(new Tree(leave_data.get(t), false));
			de_trees.get(de_trees.size() - 1).set_distance_measurement(Distance_measurement.DE_count);
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
			System.out.println(de_tree_status);
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
			for (Tree t : pairs.get(tissue)) {
				try {
					Process e = Runtime.getRuntime().exec("display " + Plot.get_plot(t));
					open_viewers.add(e);
				} catch (IOException e) {
					e.printStackTrace();
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

	public Tree build_avg_sequence_id_of_orthologues_tree() {

		return new Tree(basic_leaves);
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

		}
		else{
			b = new TreeBuilder(null, null, false);
			b.go_pair_view();
			b.go_pair_view(Gene_focus.de_only, Gene_focus.nonde_only);
			b.go_pair_view(Gene_focus.orthologues_only, Gene_focus.nonorthologues_only);
			b.set_go_tree_use_all_go_terms(true);
			b.go_pair_view(Gene_focus.de_only, Gene_focus.nonde_only);
			b.go_pair_view(Gene_focus.orthologues_only, Gene_focus.nonorthologues_only);
			b.de_pair_view(Gene_focus.orthologues_only, Gene_focus.nonorthologues_only);
			b.de_pair_view(Gene_focus.de_only, Gene_focus.nonde_only);
		}
	}

}
