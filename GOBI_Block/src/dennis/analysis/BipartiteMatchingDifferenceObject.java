package dennis.analysis;

import java.util.TreeMap;

import dennis.analysis.scoring.ScoringObject;
import dennis.util.GenePair;

public class BipartiteMatchingDifferenceObject {

	private BipartiteMatching bi_query, bi_target;
	private int numberDifferentMappings, numberSameMappings;
	private double maxScoreDiff;
	public boolean equal = false;

	public BipartiteMatchingDifferenceObject(BipartiteMatching bi1, BipartiteMatching bi2) {
		bi_query = bi1;
		bi_target = bi2;
		calcDifferentMappings();
		calcMaxScoreDifference();
	}

	public void calcDifferentMappings() {
		TreeMap<GenePair, ScoringObject> query_matches = bi_query.getMatches();
		TreeMap<GenePair, ScoringObject> target_matches = bi_target.getMatches();

		int same = 0, diff;
		for (GenePair gp : query_matches.keySet()) {
			if (target_matches.containsKey(gp)) {
				same++;
			}
		}
		diff = bi_query.getMatches().size() + bi_target.getMatches().size() - same - same;

		numberDifferentMappings = diff;
		numberSameMappings = same;
		if (bi_query.getMatches().keySet().containsAll(bi_target.getMatches().keySet())) {
			if (bi_query.getUnmatched().containsAll(bi_target.getUnmatched())) {
				equal = true;
			}
		}
	}

	public void calcMaxScoreDifference() {
		maxScoreDiff = bi_query.getScore() - bi_target.getScore();
	}

	public BipartiteMatching getQueryMapping() {
		return bi_query;
	}

	public BipartiteMatching getTargetMapping() {
		return bi_target;
	}

	public int getNumberOfDifferentMappings() {
		return numberDifferentMappings;
	}

	public int getNumberOfSameMappings() {
		return numberSameMappings;
	}

	public double getMaxScoreDiff() {
		return maxScoreDiff;
	}

	public boolean equal() {
		return equal;
	}

}
