package dennis.analysis.scoring;

import java.util.Arrays;
import java.util.TreeSet;

import dennis.analysis.BipartiteMatching;
import dennis.analysis.InputDataPreparator;
import dennis.similarities.NxMmapping;
import dennis.utility_manager.Species;

public class Scoring {

	private Species querySpecies, targetSpecies;
	private SequenceIdentityScoring seqIdScoring;
	private FuzzyCorrelation fuzzyScoring;
	private GreedyScoring greedy;

	public Scoring(InputDataPreparator dataPrep) {
		querySpecies = dataPrep.getQuerySpecies();
		targetSpecies = dataPrep.getTargetSpecies();
		seqIdScoring = new SequenceIdentityScoring(querySpecies, targetSpecies);
		fuzzyScoring = new FuzzyCorrelation(querySpecies, targetSpecies, dataPrep.getDEfilesSpecies1(),
				dataPrep.getDEfilesSpecies2(), dataPrep.getTissuePairs());
		greedy = new GreedyScoring(querySpecies, targetSpecies);
	}

	public BipartiteMatching getBestBipartiteMatching(String scoringFunctionName, NxMmapping cluster, boolean greedy) {
		switch (scoringFunctionName) {

		case "seqId":
			if (greedy) {
				return this.greedy.getGreedyMapping(cluster, seqIdScoring);
			}
			return seqIdScoring.calculateBestBipartiteMatching(cluster);
		case "fuzzy":
			if (greedy) {
				return this.greedy.getGreedyMapping(cluster, fuzzyScoring);
			}
			return fuzzyScoring.calculateBestBipartiteMatching(cluster);
		}
		return null;
	}

	public Species getQuerySpecies() {
		return querySpecies;
	}

	public Species getTargetSpecies() {
		return targetSpecies;
	}

	static int steps = 1;

	public static void getAllPermutations(String[] arr, int n, TreeSet<String[]> out) {
		System.out.println(arr.length + " " + n + " " + out.size() + " " + steps++);
		for (int i = 0; i < n; i++) {
			swap(arr, i, n - 1);
			getAllPermutations(arr, n - 1, out);
			swap(arr, i, n - 1);
		}
		if (n == 1) {
			out.add(Arrays.copyOf(arr, arr.length));
			return;
		}

	}

	public static void swap(String[] arr, int a, int b) {
		String tmp = arr[a];
		arr[a] = arr[b];
		arr[b] = tmp;
	}

	public FuzzyCorrelation getFuzzyScoring() {
		return fuzzyScoring;
	}

	public SequenceIdentityScoring getSeqIdScoring() {
		return seqIdScoring;
	}

}
