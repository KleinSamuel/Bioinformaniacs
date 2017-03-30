package dennis.analysis;

import java.util.TreeMap;

import dennis.util.GenePair;

public class BipartitMappingDifferenceObject {

	private BipartitMapping bi_query, bi_target;
	private int numberDifferentMappings, numberSameMappings;
	private double maxScoreDiff;

	public BipartitMappingDifferenceObject(BipartitMapping bi1, BipartitMapping bi2) {
		bi_query = bi1;
		bi_target = bi2;
		calcDifferentMappings();
		calcMaxScoreDifference();
	}

	public void calcDifferentMappings() {
		TreeMap<GenePair, ScoringObject> query_matches = bi_query.getScoringMatrix().last().getMatches();
		TreeMap<GenePair, ScoringObject> target_matches = bi_target.getScoringMatrix().last().getMatches();

		int same = 0, diff = query_matches.size() + target_matches.size();
		for (GenePair gp : query_matches.keySet()) {
			if (target_matches.containsKey(gp)) {
				same++;
			} else {
				diff -= 2;
			}
		}

		numberDifferentMappings = diff;
		numberSameMappings = same;
	}

	public void calcMaxScoreDifference() {
		maxScoreDiff = bi_query.getScoringMatrix().last().getMaxScore()
				- bi_target.getScoringMatrix().last().getMaxScore();
	}

	public BipartitMapping getQueryMapping() {
		return bi_query;
	}

	public BipartitMapping getTargetMapping() {
		return bi_target;
	}

}
