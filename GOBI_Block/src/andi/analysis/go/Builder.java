package andi.analysis.go;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import andi.analysis.go.total.Total_Organism;
import andi.tree.Node_Data;
import andi.tree.Plot;
import andi.tree.Tree;
import andi.tree.Tree.Cluster_method;
import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;

public class Builder {

	public Builder() {
		UtilityManager um = new UtilityManager(
				"/home/m/maieran/git/Bioinformaniacs/GOBI_Block/bin/andi/analysis/go/config.txt", false, true, false);
		Iterator<Species> it_sp = UtilityManager.speciesIterator();
		Tree tot_org_tree;
		TreeSet<String> all_orthologues = new TreeSet<>();
		Vector<Node_Data> orgs = new Vector<>();
		while(it_sp.hasNext()) {
			Species s1 = it_sp.next();
			Iterator<Species> it_sp_2 = UtilityManager.speciesIterator();
			orgs.add(new Total_Organism(s1.getId(), s1.getName(),s1));
			TreeSet<String> genes = new TreeSet<>();
			while(it_sp_2.hasNext()) {
				Species s2 = it_sp_2.next();
				if(s1.getId()==s2.getId())
					continue;
				genes.addAll(UtilityManager.getSimilarityHandler().getAllGenesWithAnOrtholog(s1, s2));
			}
			all_orthologues.addAll(genes);
			Vector<String> orth = new Vector<>();
			((Total_Organism)orgs.get(orgs.size()-1)).set_genes(orth);
		}
		Vector<String> all = new Vector<>();
		all.addAll(all_orthologues);
		for(Node_Data o:orgs) {
			((Total_Organism)o).set_all_orthologues(all);
			for(Node_Data o2:orgs) {
				Total_Organism org1 = (Total_Organism)o;
				Total_Organism org2 = (Total_Organism)o2;
				System.out.println(org1.get_Name()+ " - "+org2.get_Name()+":"+UtilityManager.getSimilarityHandler().getAllGenesWithAnOrtholog(org1.get_Species(), org2.get_Species()).size());
			}
		
		}
		tot_org_tree = new Tree(orgs);
		System.out.println(tot_org_tree);
		try {
			Runtime.getRuntime().exec("display "+Plot.get_plot(tot_org_tree));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		for (Entry<String, LinkedList<String>> s : GOHandler.getAllMappedGOs(null, "ENSGALG00000003855").entrySet()) {
//			System.out.println(s.getKey());
//			for(String s2:s.getValue())
//				System.out.println("\t"+s2);
//		}
	}

	public static void main(String[] args) {
		Builder b = new Builder();
	}

}
