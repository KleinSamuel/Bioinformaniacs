package andi.analysis.go;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import andi.analysis.go.total.Total_Organism;
import andi.analysis.go.total.Total_Organism.Distance_measurement;
import andi.tree.Node_Data;
import andi.tree.Plot;
import andi.tree.Tree;
import dennis.tissues.Tissue;
import dennis.utility_manager.Experiment;
import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;

public class TreeBuilder {

	public TreeBuilder() {
		UtilityManager um = new UtilityManager(
				"/home/m/maieran/git/Bioinformaniacs/GOBI_Block/bin/andi/analysis/go/config.txt", false, true, false);
		Tree avg_seq_id = build_avg_sequence_id_of_orthologues_tree();
		try {
			Runtime.getRuntime().exec("display " + Plot.get_plot(avg_seq_id));
			avg_seq_id.change_distance_measurement(Distance_measurement.Avg_seq_id_all);
			Runtime.getRuntime().exec("display " + Plot.get_plot(avg_seq_id));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// }
	}

	public Vector<Tree> build_tissue_trees() {
		Vector<Tree> trees = new Vector<>();
		String count_path = "/home/proj/biocluster/praktikum/genprakt-ws16/bioinformaniacs/data_update/";

		Iterator<Species> it_s = UtilityManager.speciesIterator();

		HashMap<Tissue, HashMap<Species, String>> count_paths = new HashMap<>();

		for (Species s = it_s.next(); it_s.hasNext(); s = it_s.next()) {
			Iterator<Tissue> it_t = UtilityManager.tissueIterator(s);
			for (Tissue t = it_t.next(); it_t.hasNext(); t = it_t.next()) {
				// for(Experiment e:t.getExperiments()) {
				if (!count_paths.containsKey(t))
					count_paths.put(t, new HashMap<>());
				count_paths.get(t).put(s, count_path + s.getId() + "/" + t.getName() + "/star_tissue_average.counts");
				// }
			}
		}

		for (Tissue t : count_paths.keySet()) {
			Vector<Node_Data> orgs = new Vector<>();
			for (Species s : count_paths.get(t).keySet()) {
				orgs.add(new Total_Organism(s.getId(), s.getName(), s, t.getName(), count_paths.get(t).get(s),Distance_measurement.GO_tissue_basic));
			}
			trees.add(new Tree(orgs));
		}

		return trees;

	}

	public Tree build_avg_sequence_id_of_orthologues_tree() {
		Iterator<Species> it_sp = UtilityManager.speciesIterator();
		TreeSet<String> all_orthologues = new TreeSet<>();
		Vector<Node_Data> orgs = new Vector<>();
		while (it_sp.hasNext()) {
			Species s1 = it_sp.next();
			Iterator<Species> it_sp_2 = UtilityManager.speciesIterator();
			orgs.add(new Total_Organism(s1.getId(), s1.getName(), s1));
			TreeSet<String> genes = new TreeSet<>();
			TreeSet<String> orthologues = new TreeSet<>();
			while (it_sp_2.hasNext()) {
				Species s2 = it_sp_2.next();
				if (s1.getId() == s2.getId())
					continue;
				genes.addAll(UtilityManager.getSimilarityHandler().getAllGenesWithAnOrtholog(s1, s2));
				orthologues.addAll(UtilityManager.getSimilarityHandler().getAllGenesWithAnOrtholog(s2, s1));
			}
			all_orthologues.addAll(genes);
			Vector<String> orth = new Vector<>();
			orth.addAll(genes);
			((Total_Organism) orgs.get(orgs.size() - 1)).set_self_genes(orth);
			((Total_Organism) orgs.get(orgs.size() - 1)).set_orthologue_genes(orth);
		}
		Vector<String> all = new Vector<>();
		all.addAll(all_orthologues);
		return new Tree(orgs);
	}

	public static void main(String[] args) {
		TreeBuilder b = new TreeBuilder();
	}

}
