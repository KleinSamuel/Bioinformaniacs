package andi.analysis.go;

import java.io.IOException;
import java.util.ArrayList;
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
		Prepared, Built, None;
	};

	public TreeMap<Species, TreeSet<Tissue>> species_tissues;

	public ArrayList<String> all_orthologues;
	public TreeMap<Species, TreeSet<String>> orthologues_own;
	public TreeMap<Species, TreeSet<String>> orthologues_paired;
	public TreeMap<Species, Vector<Boolean>> shared;
	public TreeMap<Tissue, TreeSet<Node_Data>> leave_data;
	public TreeSet<Node_Data> basic_leaves;
	public ArrayList<Process> open_viewers;
	public ArrayList<Tree> go_trees;
	public Tree_status go_tree_status = Tree_status.None;

	public TreeBuilder(ArrayList<Species> species, ArrayList<Tissue> tissues, boolean um_initialized) {
		if (!um_initialized)
			init_um();
		init(species, tissues);
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

	private void init(ArrayList<Species> species, ArrayList<Tissue> tissues) {
		this.species_tissues = new TreeMap<>();
		this.open_viewers = new ArrayList<>();
		Iterator<Species> it_sp = UtilityManager.speciesIterator();
		leave_data = new TreeMap<>();
		while (it_sp.hasNext()) {
			Species s = it_sp.next();
			if (species == null || species.contains(s)) {
				this.species_tissues.put(s, new TreeSet<>());
				Iterator<Tissue> it_ti = UtilityManager.tissueIterator(s);
				while (it_ti.hasNext()) {
					Tissue t = it_ti.next();
					leave_data.put(t, new TreeSet<>());
					if (tissues == null || tissues.contains(t))
						this.species_tissues.get(s).add(t);
				}
			}
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
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void prepare_go_trees() {
		System.out.println("\tprepare go tree");
		go_trees = new ArrayList<>();
		for (Tissue t : leave_data.keySet()) {
			go_trees.add(new Tree(leave_data.get(t), false));
			go_trees.get(go_trees.size()-1).set_distance_measurement(Distance_measurement.GO_tissue_basic);
		}
		go_tree_status = Tree_status.Prepared;
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

	public ArrayList<Tree> get_go_trees() {
		if (go_tree_status == Tree_status.None)
			prepare_go_trees();
		return go_trees;
	}

	public ArrayList<Tree> build_go_trees() {
		if (go_tree_status == Tree_status.Prepared) {
			for (Tree t : go_trees)
				t.build();
			go_tree_status = Tree_status.Built;
		}
		return go_trees;
	}

	public void view_go_trees() {
		try {
			if (go_tree_status == Tree_status.Prepared) {
				build_go_trees();
			}
			if (go_tree_status == Tree_status.Built)
				for (Tree t : go_trees) {
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
				System.out.println("\tset_focus " + gf);
				t.set_gene_focus(gf);
			}
	}

	public Tree build_avg_sequence_id_of_orthologues_tree() {

		return new Tree(basic_leaves);
	}

	public static void main(String[] args) {
		TreeBuilder b = new TreeBuilder(null, null, false);
		System.out.println("view avg");
		b.view(b.build_avg_sequence_id_of_orthologues_tree());
		System.out.println("set_gf_o_only");
		b.set_go_tree_gene_focus(Gene_focus.orthologues_only);
		System.out.println("view go_trees");
		b.view_go_trees();
		System.out.println("set_gf_all_g");
		b.set_go_tree_gene_focus(Gene_focus.All_genes);
		System.out.println("view go_trees");
		b.view_go_trees();
		System.out.println("wait for close");
		b.wait_for_close();

	}

}
