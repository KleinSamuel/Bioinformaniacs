package dennis.similarities;

import java.util.HashMap;

public class SimilarityObject {

	// prot1, prot2, identityScore
	private HashMap<String, HashMap<String, Double>> prots;
	private double maxIdentityScore;

	public SimilarityObject(double maxScore) {
		prots = new HashMap<>();
		this.maxIdentityScore = maxScore;
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

}
