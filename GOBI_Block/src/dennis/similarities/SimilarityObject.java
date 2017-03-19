package dennis.similarities;

import java.util.HashMap;

public class SimilarityObject {

	private String query_geneId, target_geneId;
	/**
	 * prot1, prot2, identityScore
	 */
	private HashMap<String, HashMap<String, Double>> prots;
	private double maxIdentityScore;

	public SimilarityObject(double maxScore, String query, String target) {
		prots = new HashMap<>();
		this.maxIdentityScore = maxScore;
		query_geneId = query;
		target_geneId = target;
	}

	public void addProteinSimilarity(String prot1, String prot2, double simScore) {
		HashMap<String, Double> sim = prots.get(prot1);
		if (sim == null) {
			sim = new HashMap<>();
			prots.put(prot1, sim);
		}
		sim.put(prot2, simScore);
	}

	public double getMaximumIdentityScore() {
		return maxIdentityScore;
	}

	public String getQuery_geneId() {
		return query_geneId;
	}

	public String getTarget_geneId() {
		return target_geneId;
	}

	public HashMap<String, HashMap<String, Double>> getProts() {
		return prots;
	}

}
