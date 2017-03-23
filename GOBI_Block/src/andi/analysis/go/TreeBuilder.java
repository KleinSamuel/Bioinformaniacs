package andi.analysis.go;

import java.io.IOException;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import andi.analysis.go.total.Total_Organism;
import andi.tree.Node_Data;
import andi.tree.Plot;
import andi.tree.Tree;
import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;

public class TreeBuilder {

	public TreeBuilder() {
		UtilityManager um = new UtilityManager(
				"/home/m/maieran/git/Bioinformaniacs/GOBI_Block/bin/andi/analysis/go/config.txt", false, true, false);
		Tree avg_seq_id = build_avg_sequence_id_of_orthologues_tree();
		try {
			Runtime.getRuntime().exec("display " + Plot.get_plot(avg_seq_id));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// }
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
			while (it_sp_2.hasNext()) {
				Species s2 = it_sp_2.next();
				if (s1.getId() == s2.getId())
					continue;
				genes.addAll(UtilityManager.getSimilarityHandler().getAllGenesWithAnOrtholog(s1, s2));
			}
			all_orthologues.addAll(genes);
			Vector<String> orth = new Vector<>();
			((Total_Organism) orgs.get(orgs.size() - 1)).set_genes(orth);
		}
		Vector<String> all = new Vector<>();
		all.addAll(all_orthologues);
		return new Tree(orgs);

	}

	public static void main(String[] args) {
		TreeBuilder b = new TreeBuilder();
	}

}
