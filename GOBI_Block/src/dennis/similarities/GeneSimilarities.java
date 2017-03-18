package dennis.similarities;

import java.util.HashMap;
import java.util.Map.Entry;

import dennis.utility_manager.Species;

public class GeneSimilarities {

	private Species species1, species2;
	private String id;

	// geneIdSpecies1, geneIdSpecies2
	private HashMap<String, HashMap<String, SimilarityObject>> sims;

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

	public HashMap<String, SimilarityObject> getSimilarities(String gene) {
		return sims.get(gene);
	}

	public SimilarityObject getSimilarity(String gene1, String gene2) {
		HashMap<String, SimilarityObject> s = sims.get(gene1);
		if (s == null)
			return null;
		return s.get(gene2);
	}

	public boolean isSimilar(String gene1, String gene2) {
		return getSimilarity(gene1, gene2) != null;
	}

	public String getGeneWithHighestIdentity(String geneOfInterest) {
		String highestGene = null;
		double maxIdentityScore = Double.NEGATIVE_INFINITY;
		HashMap<String, SimilarityObject> similarities = sims.get(geneOfInterest);
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

	/*
	 * returnen Map: key = gene1, value = HashMap: key = gene2, value =
	 * SimilarityObject
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
