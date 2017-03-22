package dennis.similarities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;

import dennis.utility_manager.Species;

public class GeneSimilarities {

	private Species species1, species2;
	private String id;

	/**
	 * <geneIdSpecies1, <geneIdSpecies2, SimilarityObject>>
	 */
	private HashMap<String, HashMap<String, SimilarityObject>> sims;

	private HashSet<String> genesWithPartner = null;

	public GeneSimilarities(Species sp1, Species sp2) {
		sims = new HashMap<>();
		species1 = sp1;
		species2 = sp2;
		id = species1.getId() + "." + species2.getId();
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	public void addSimilarity(String gene1, String gene2, SimilarityObject sim) {
		HashMap<String, SimilarityObject> s = sims.get(gene1);
		if (s == null) {
			s = new HashMap<>();
			sims.put(gene1, s);
		}
		s.put(gene2, sim);
	}

	public HashSet<String> getGenesWithPartner() {
		if (genesWithPartner == null) {
			genesWithPartner = new HashSet<>();
			genesWithPartner.addAll(sims.keySet());
		}
		return genesWithPartner;
	}

	/**
	 * (info)use method in similarityHandler to avoid null pointers
	 * 
	 * @param query_gene
	 * @return all similar genes in species2
	 */
	public HashMap<String, SimilarityObject> getSimilarities(String gene) {
		return sims.get(gene);
	}

	/**
	 * 
	 * @param gene1
	 * @param gene2
	 * @return SimilarityObject between the two genes null if genes are not
	 *         similar
	 */
	public SimilarityObject getSimilarity(String gene1, String gene2) {
		HashMap<String, SimilarityObject> s = sims.get(gene1);
		if (s == null)
			return null;
		return s.get(gene2);
	}

	/**
	 * 
	 * @param gene1
	 * @param gene2
	 * @return boolean if genes are similar
	 */
	public boolean isSimilar(String gene1, String gene2) {
		return getSimilarity(gene1, gene2) != null;
	}

	/**
	 * 
	 * @param query_gene_id
	 * @return targte_gene_id with highest identity null if no gene is similar
	 */
	public String getGeneWithHighestIdentity(String query_gene_id) {
		String highestGene = null;
		double maxIdentityScore = Double.NEGATIVE_INFINITY;
		HashMap<String, SimilarityObject> similarities = sims.get(query_gene_id);
		if (similarities == null) {
			return null;
		}
		for (Entry<String, SimilarityObject> e : similarities.entrySet()) {
			if (e.getValue().getMaximumIdentityScore() > maxIdentityScore) {
				highestGene = e.getKey();
			}
		}
		return highestGene;
	}

	public String getId() {
		return id;
	}

	/**
	 * @return Map: key = query_gene, value = HashMap: key = target_gene, value
	 *         = SimilarityObject; one entry for every multimapped gene
	 */
	public HashMap<String, HashMap<String, SimilarityObject>> getMultimappedGenes() {
		HashMap<String, HashMap<String, SimilarityObject>> multimapped = new HashMap<>();
		for (Entry<String, HashMap<String, SimilarityObject>> e : sims.entrySet()) {
			if (e.getValue().size() > 1) {
				multimapped.put(e.getKey(), e.getValue());
			}
		}
		return multimapped;
	}

}
