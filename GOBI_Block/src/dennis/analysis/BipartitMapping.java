package dennis.analysis;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

import dennis.similarities.NxMmapping;
import dennis.util.GenePair;

public class BipartitMapping {

	private TreeMap<GenePair, ScoringObject> matches;
	private TreeSet<String> unmatched;
	private ScoringFunction scoring;
	private NxMmapping input;
	private double[][] scoringMatrix;
	private int posXofMaxScore = 0, posYofMaxScore = 0;
	private ArrayList<String> geneIds1, geneIds2;

	public BipartitMapping(NxMmapping input, ScoringFunction scoring) {
		this.input = input;
		this.scoring = scoring;
		geneIds1 = new ArrayList<>(input.getGenesFromSpecies(true).size());
		geneIds1.addAll(input.getGenesFromSpecies(true));
		geneIds2 = new ArrayList<>(input.getGenesFromSpecies(false).size());
		geneIds2.addAll(input.getGenesFromSpecies(false));
		buildScoringMatrix();
	}

	public double[][] buildScoringMatrix() {

		double[][] scoringMatrix = new double[geneIds1.size() + 1][geneIds2.size() + 1];
		for (int i = 0; i < scoringMatrix.length; i++) {
			scoringMatrix[i][0] = 0;
		}
		for (int i = 0; i < scoringMatrix[0].length; i++) {
			scoringMatrix[0][i] = 0;
		}
		for (int i = 1; i < scoringMatrix.length; i++) {
			for (int j = 1; j < scoringMatrix[i].length; j++) {
				double score = scoring.score(new GenePair(geneIds1.get(i - 1), geneIds2.get(j - 1)));
				scoringMatrix[i][j] = Math.max(Math.max(scoringMatrix[i - 1][j], scoringMatrix[i][j - 1]),
						scoringMatrix[i - 1][j - 1] + score);
				if (scoringMatrix[i][j] > scoringMatrix[posXofMaxScore][posYofMaxScore]) {
					posXofMaxScore = i;
					posYofMaxScore = j;
				}
			}
		}
		this.scoringMatrix = scoringMatrix;
		return scoringMatrix;
	}

	public TreeMap<GenePair, ScoringObject> backtrack() {
		unmatched = new TreeSet<>();
		matches = new TreeMap<>();

		int x = posXofMaxScore, y = posYofMaxScore;

		while (x > 1 && y > 1 && scoringMatrix[x][y] > 0) {

			if (scoringMatrix[x - 1][y - 1] < scoringMatrix[x][y]) {
				matches.put(new GenePair(geneIds1.get(x - 1), geneIds2.get(y - 1)),
						new ScoringObject(scoringMatrix[x][y] - scoringMatrix[x - 1][y - 1]));
			} else {

			}

		}

		return matches;
	}

	public TreeMap<GenePair, ScoringObject> getMatches() {
		return matches;
	}

	public ScoringFunction getScoring() {
		return scoring;
	}

	public NxMmapping getInput() {
		return input;
	}

	public double[][] getScoringMatrix() {
		return scoringMatrix;
	}

	public int getPosXofMaxScore() {
		return posXofMaxScore;
	}

	public int getPosYofMaxScore() {
		return posYofMaxScore;
	}

	public ArrayList<String> getGeneIds1() {
		return geneIds1;
	}

	public ArrayList<String> getGeneIds2() {
		return geneIds2;
	}

	public String matrixToString() {

		StringBuilder sb = new StringBuilder();

		sb.append(input.getSpecies(true) + " <-> " + input.getSpecies(false) + "\n\n");

		sb.append("\t");
		for (String s : input.getGenesFromSpecies(false)) {
			sb.append("\t" + s);
		}
		sb.append("\n");

		for (int i = 0; i < scoringMatrix[0].length; i++) {
			sb.append("\t" + scoringMatrix[0][i]);
		}
		sb.append("\n");

		for (int i = 1; i < scoringMatrix.length; i++) {
			sb.append(geneIds1.get(i - 1));
			for (int j = 0; j < scoringMatrix[i].length; j++) {
				sb.append("\t" + scoringMatrix[i][j]);
			}
			sb.append("\n");
		}

		return sb.toString();
	}

}
