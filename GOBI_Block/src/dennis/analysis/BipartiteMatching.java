package dennis.analysis;

import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import dennis.analysis.scoring.ScoringObject;
import dennis.similarities.NxMmapping;
import dennis.util.GenePair;

public class BipartiteMatching {

	private NxMmapping input;
	private double score;
	private TreeMap<GenePair, ScoringObject> matches;
	private TreeSet<String> unmatched;
	private String[] geneIds1, geneIds2;
	private double[][] costMatrix;

	public BipartiteMatching(NxMmapping in, int[] hungarianOut, double[][] costMatrix) {
		input = in;
		score = 0;
		matches = new TreeMap<>();
		unmatched = new TreeSet<>();
		this.geneIds1 = in.getGenesFromSpecies(true).toArray(new String[in.getGenesFromSpecies(true).size()]);
		this.geneIds2 = in.getGenesFromSpecies(false).toArray(new String[in.getGenesFromSpecies(false).size()]);
		this.costMatrix = costMatrix;
		parseHungarian(hungarianOut, costMatrix);
	}

	public BipartiteMatching(NxMmapping inputCluster, double score, TreeMap<GenePair, ScoringObject> matches,
			TreeSet<String> unmatched, TreeSet<String> matchedGeneIds1, TreeSet<String> matchedGeneIds2) {
		input = inputCluster;
		this.score = score;
		this.matches = matches;
		this.unmatched = unmatched;
		this.geneIds1 = matchedGeneIds1.toArray(new String[matchedGeneIds1.size()]);
		this.geneIds2 = matchedGeneIds2.toArray(new String[matchedGeneIds2.size()]);
	}

	private void parseHungarian(int[] hungarianOut, double[][] costMatrix) {

		System.out.println("hungarian: ");
		for (int i : hungarianOut)
			System.out.print(i + "\t");
		System.out.println();

		if (hungarianOut.length != geneIds1.length) {
			System.out.println("unequal length of hungarian output");
			System.exit(1);
		}

		TreeSet<String> matchedGeneIds1 = new TreeSet<>(), matchedGeneIds2 = new TreeSet<>();
		for (int i = 0; i < hungarianOut.length; i++) {
			if (hungarianOut[i] == -1) {
				unmatched.add(geneIds1[i]);
			} else {
				double genePairScore = costMatrix[i][hungarianOut[i]] * -1;
				if (!InputDataPreparator.IGNORE_NEGATIVE_SCORES || genePairScore > 0d) {
					matches.put(new GenePair(geneIds1[i], geneIds2[hungarianOut[i]]), new ScoringObject(genePairScore));

					score += genePairScore;

					matchedGeneIds1.add(geneIds1[i]);
					matchedGeneIds2.add(geneIds2[hungarianOut[i]]);
				} else {
					unmatched.add(geneIds1[i]);
					unmatched.add(geneIds2[hungarianOut[i]]);
				}
			}
		}
		for (String s : geneIds2) {
			if (!matchedGeneIds2.contains(s)) {
				unmatched.add(s);
			}
		}
		geneIds1 = matchedGeneIds1.toArray(new String[matchedGeneIds1.size()]);
		geneIds2 = matchedGeneIds2.toArray(new String[matchedGeneIds2.size()]);
	}

	public NxMmapping getInput() {
		return input;
	}

	public double getScore() {
		return score;
	}

	public TreeMap<GenePair, ScoringObject> getMatches() {
		return matches;
	}

	public TreeSet<String> getUnmatched() {
		return unmatched;
	}

	public String[] getGeneIds1() {
		return geneIds1;
	}

	public String[] getGeneIds2() {
		return geneIds2;
	}

	public double[][] getCostMatrix() {
		return costMatrix;
	}

	public String matchesToString() {
		StringBuilder sb = new StringBuilder();

		for (Entry<GenePair, ScoringObject> match : matches.entrySet()) {
			sb.append(match.getKey().getKey() + "-" + match.getKey().getValue() + ":" + match.getValue().getScore()
					+ ",");
		}

		sb.deleteCharAt(sb.length() - 1);

		return sb.toString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(score + "\t" + matches.size() + "\t");

		for (Entry<GenePair, ScoringObject> match : matches.entrySet()) {
			sb.append(match.getKey().getKey() + "-" + match.getKey().getValue() + ":" + match.getValue().getScore()
					+ ",");
		}

		sb.deleteCharAt(sb.length() - 1);

		sb.append("\t" + unmatched.size() + "\t");
		for (String s : unmatched) {
			sb.append(s + ",");
		}

		sb.deleteCharAt(sb.length() - 1);

		return sb.toString();
	}

}
