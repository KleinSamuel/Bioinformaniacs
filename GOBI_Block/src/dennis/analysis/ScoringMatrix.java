package dennis.analysis;

import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import dennis.util.GenePair;
import dennis.utility_manager.UtilityManager;

public class ScoringMatrix implements Comparable<ScoringMatrix> {

	private double[][] matrix;
	private int maxX, maxY;
	private double maxScore;
	private String[] geneIds1, geneIds2;
	private TreeMap<GenePair, ScoringObject> matches = null;
	private TreeSet<String> unmatched = null;

	public ScoringMatrix(String[] geneIds1, String[] geneIds2, ScoringFunction scoringFunction) {
		maxX = 0;
		maxY = 0;
		maxScore = -1;
		this.geneIds1 = geneIds1;
		this.geneIds2 = geneIds2;
		buildScoringMatrix(scoringFunction);
	}

	public void buildScoringMatrix(ScoringFunction scoring) {

		matrix = new double[geneIds1.length + 1][geneIds2.length + 1];
		for (int i = 0; i < matrix.length; i++) {
			matrix[i][0] = 0;
		}
		for (int i = 0; i < matrix[0].length; i++) {
			matrix[0][i] = 0;
		}
		for (int i = 1; i < matrix.length; i++) {
			for (int j = 1; j < matrix[i].length; j++) {
				double score = scoring.score(new GenePair(geneIds1[i - 1], geneIds2[j - 1]));
				matrix[i][j] = Math.max(Math.max(matrix[i - 1][j], matrix[i][j - 1]), matrix[i - 1][j - 1] + score);
				if (matrix[i][j] > matrix[maxX][maxY]) {
					maxX = i;
					maxY = j;
				}
			}
		}
		maxScore = matrix[maxX][maxY];
		backtrack(maxX, maxY);
	}

	public TreeMap<GenePair, ScoringObject> backtrack(int x, int y) {
		if (unmatched != null && matches != null) {
			return matches;
		}
		unmatched = new TreeSet<>();
		matches = new TreeMap<>();

		while (x > 1 && y > 1 && matrix[x][y] > 0) {

			if (matrix[x - 1][y - 1] < matrix[x][y]) {
				matches.put(new GenePair(geneIds1[x - 1], geneIds2[y - 1]),
						new ScoringObject(matrix[x][y] - matrix[x - 1][y - 1]));
				x--;
				y--;
			} else {
				if (matrix[x - 1][y] > matrix[x][y - 1]) {
					unmatched.add(geneIds1[x - 1]);
					x--;
				} else {
					unmatched.add(geneIds2[y - 1]);
					y--;
				}
			}

		}

		return matches;
	}

	public double[][] getMatrix() {
		return matrix;
	}

	public int getMaxX() {
		return maxX;
	}

	public int getMaxY() {
		return maxY;
	}

	public double getMaxScore() {
		return maxScore;
	}

	public String matrixToString() {

		StringBuilder sb = new StringBuilder();

		sb.append(UtilityManager.getSpeciesIDFromGeneID(geneIds1[0]) + " <-> "
				+ UtilityManager.getSpeciesIDFromGeneID(geneIds2[0]) + "\n" + "score: " + maxScore + "\n");

		sb.append("\t");
		for (String s : geneIds2) {
			sb.append("\t" + s);
		}
		sb.append("\n");

		for (int i = 0; i < matrix[0].length; i++) {
			sb.append("\t" + matrix[0][i]);
		}
		sb.append("\n");

		for (int i = 1; i < matrix.length; i++) {
			sb.append(geneIds1[i - 1]);
			for (int j = 0; j < matrix[i].length; j++) {
				sb.append("\t" + matrix[i][j]);
			}
			sb.append("\n");
		}

		sb.append("\n");
		for (Entry<GenePair, ScoringObject> gp : matches.entrySet()) {
			sb.append(gp.getKey() + "\t" + gp.getValue().getScore() + "\n");
		}

		sb.append("\n");

		return sb.toString();
	}

	public String[] getGeneIds1() {
		return geneIds1;
	}

	public String[] getGeneIds2() {
		return geneIds2;
	}

	public TreeMap<GenePair, ScoringObject> getMatches() {
		return matches;
	}

	public TreeSet<String> getUnmatched() {
		return unmatched;
	}

	@Override
	public int compareTo(ScoringMatrix o) {
		int comp = Double.compare(maxScore, o.getMaxScore());
		if (comp != 0)
			return comp;
		comp = maxX - o.getMaxX();
		if (comp != 0)
			return comp;
		comp = maxY - o.getMaxY();
		if (comp != 0)
			return comp;
		return compare(geneIds1, geneIds2);
	}

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

}
