package dennis.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeSet;

import dennis.similarities.NxMmapping;

public class BipartitMapping {

	private ScoringFunction scoring;
	private NxMmapping input;
	private TreeSet<ScoringMatrix> bestScore;
	private ArrayList<String> geneIds1, geneIds2;

	public BipartitMapping(NxMmapping input, ScoringFunction scoring) {
		this.input = input;
		this.scoring = scoring;
		bestScore = new TreeSet<>();
		calculateBestBipartitMatching();
	}

	public void calculateBestBipartitMatching() {

		String[] genes1 = geneIds1.toArray(new String[geneIds1.size()]),
				genes2 = geneIds2.toArray(new String[geneIds2.size()]);

		String[] genesToPermutate = genes1;
		int speciesToPermutateId = input.getSpecies(true).getId();
		if (genes2.length < genes1.length) {
			genesToPermutate = genes2;
			speciesToPermutateId = input.getSpecies(false).getId();
		}
		// sorted alphabetically
		TreeSet<String[]> perms = new TreeSet<>(new Comparator<String[]>() {

			@Override
			public int compare(String[] o1, String[] o2) {
				if (o1.length == 0)
					return -1;
				if (o2.length == 0)
					return 1;
				int i = 0;
				while (i < o1.length && i < o2.length) {
					int comp = o1[i].compareTo(o2[i]);
					if (comp != 0)
						return comp;
					i++;
				}
				if (i == o1.length)
					return -1;
				return 1;
			}
		});
		BipartitMapping.getAllPermutations(genesToPermutate, genesToPermutate.length, perms);

		// store old max --> will be replaced --> reset if old was better
		ScoringMatrix scoringMatrix = null;
		for (String[] perm : perms) {
			if (speciesToPermutateId == input.getSpecies(true).getId()) {
				scoringMatrix = new ScoringMatrix(perm, genes2, scoring);
			} else {
				scoringMatrix = new ScoringMatrix(genes1, perm, scoring);
			}
			bestScore.add(scoringMatrix);
		}

	}

	public static void getAllPermutations(String[] arr, int n, TreeSet<String[]> out) {

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

	public ScoringFunction getScoring() {
		return scoring;
	}

	public NxMmapping getInput() {
		return input;
	}

	public TreeSet<ScoringMatrix> getScoringMatrix() {
		return bestScore;
	}

	public ArrayList<String> getGeneIds1() {
		return geneIds1;
	}

	public ArrayList<String> getGeneIds2() {
		return geneIds2;
	}

}
