package andi.analysis.go.total;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import andi.tree.Node_Data;
import dennis.similarities.SimilarityObject;
import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;

public class Total_Organism implements Node_Data {
	private Vector<String> all_orthologues;
	private Vector<String> self_genes;
	private Vector<String> orthologue_genes;
	private String org_name;
	private int id;
	private Vector<Boolean> shared;
	private Species org;

	public Total_Organism(int id, String name, Species org) {
		this.id = id;
		this.org_name = name;
		this.org = org;
	}

	public void set_all_orthologues(Vector<String> all) {
		all_orthologues = all;
		Iterator<String> it_all = all.iterator();
		shared = new Vector<>();
		for( String g = "";it_all.hasNext();g=it_all.next())
			shared.add(self_genes.contains(g)|orthologue_genes.contains(g));
	}

	public void set_self_genes(Vector<String> genes) {
		this.self_genes = genes;
	}
	
	public void set_orthologue_genes(Vector<String> genes) {
		this.orthologue_genes = genes;
	}

	@Override
	public int compareTo(Node_Data o) {
		if (!(o instanceof Total_Organism))
			return -1;
		Total_Organism other = (Total_Organism) o;
		return this.id - other.id;
	}

	@Override
	public double compute_distance(Node_Data nd) {
		if (nd == null || !(nd instanceof Total_Organism))
			return Double.MAX_VALUE;
		Total_Organism other = (Total_Organism) nd;
		double sim_score = 0;
		double count = 0;
		for (String gene : UtilityManager.getSimilarityHandler().getAllGenesWithAnOrtholog(this.get_Species(),
				other.get_Species())) {
			HashMap<String, SimilarityObject> sims = UtilityManager.getSimilarityHandler()
					.getSimilarities(this.get_Species(), other.get_Species()).getSimilarities(gene);
			for (SimilarityObject so : sims.values()) {
				sim_score += so.getMaximumIdentityScore();
				count++;
			}
		}
		return 1 - (sim_score / count);
	}

	public Species get_Species() {
		return org;
	}

	@Override
	public String get_Name() {
		return org_name;
	}

	@Override
	public String data_title() {
		return "Average Sequence Identity of Othologues between Organisms";
	}

	@Override
	public Vector<?> get_shared(Vector<Boolean> shared) {
		Iterator<Boolean> s = shared.iterator();
		Iterator<String> all = all_orthologues.iterator();
		Vector<String> out = new Vector<>();
		while (s.hasNext())
			if (s.next())
				out.add(all.next());
			else
				all.next();
		return out;
	}

	@Override
	public String shared_type() {
		return "Othologues";
	}

	@Override
	public Vector<Boolean> get_share_vector() {
		return shared;
	}

	public String toString() {
		return this.get_Name() + "(" + this.id + ")";
	}

}
