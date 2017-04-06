package dennis.analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;

import dennis.similarities.NxMmapping;

public class DifferenceStats {

	// gr>seq gr=seq gr<seq gr>de gr=de gr<de seq>de seq=de seq<de
	private int[] scoreComparisons;
	private BufferedWriter bw, bwGreedySeqId, bwGreedyFuzzy, bwSeqIdFuzzy;
	private String outputDir;
	private int clusterCounter;

	public DifferenceStats(String outputDir) {
		scoreComparisons = new int[9];
		clusterCounter = 0;
		this.outputDir = outputDir;
	}

	public void addMatchingDifferences(NxMmapping map, HashMap<String, BipartiteMatching> bipartiteMatchings) {
		BipartiteMatchingDifferenceObject greedyVsMaxSeqId = InputDataPreparator
				.getDifferenceBetweenBipartits(bipartiteMatchings.get("greedy"), bipartiteMatchings.get("seqId")),
				greedyVsFuzzy = InputDataPreparator.getDifferenceBetweenBipartits(bipartiteMatchings.get("greedy"),
						bipartiteMatchings.get("fuzzy")),
				maxSeqIdVsFuzzy = InputDataPreparator.getDifferenceBetweenBipartits(bipartiteMatchings.get("seqId"),
						bipartiteMatchings.get("fuzzy"));
		BipartiteMatching greed = bipartiteMatchings.get("greedy"), seqId = bipartiteMatchings.get("seqId"),
				fuzzy = bipartiteMatchings.get("fuzzy");

		System.out.println("calculated differences");

		try {
			bw.write(clusterCounter + "\t" + map.getGenesFromSpecies(true).size() + "\t"
					+ map.getGenesFromSpecies(false).size() + "\t" + map.getOverallSims() + "\t");
			bw.write(greed.toString() + "\t");
			bw.write(seqId.toString() + "\t");
			bw.write(fuzzy.toString() + "\n");

			bwGreedySeqId.write(clusterCounter + "\t" + greed.getScore() + "\t" + seqId.getScore() + "\t"
					+ greedyVsMaxSeqId.getMaxScoreDiff() + "\t" + greed.getMatches().size() + "\t"
					+ seqId.getMatches().size() + "\t" + greed.matchesToString() + "\t" + seqId.matchesToString() + "\t"
					+ greedyVsMaxSeqId.getNumberOfSameMappings() + "\t"
					+ greedyVsMaxSeqId.getNumberOfDifferentMappings() + "\t" + greedyVsMaxSeqId.equal() + "\n");

			bwGreedyFuzzy.write(clusterCounter + "\t" + greed.getScore() + "\t" + fuzzy.getScore() + "\t"
					+ greedyVsFuzzy.getMaxScoreDiff() + "\t" + greed.getMatches().size() + "\t"
					+ fuzzy.getMatches().size() + "\t" + greed.matchesToString() + "\t" + fuzzy.matchesToString() + "\t"
					+ greedyVsFuzzy.getNumberOfSameMappings() + "\t" + greedyVsFuzzy.getNumberOfDifferentMappings()
					+ "\t" + greedyVsFuzzy.equal() + "\n");

			bwSeqIdFuzzy.write(clusterCounter + "\t" + seqId.getScore() + "\t" + fuzzy.getScore() + "\t"
					+ maxSeqIdVsFuzzy.getMaxScoreDiff() + "\t" + seqId.getMatches().size() + "\t"
					+ fuzzy.getMatches().size() + "\t" + seqId.matchesToString() + "\t" + fuzzy.matchesToString() + "\t"
					+ maxSeqIdVsFuzzy.getNumberOfSameMappings() + "\t" + maxSeqIdVsFuzzy.getNumberOfDifferentMappings()
					+ "\t" + maxSeqIdVsFuzzy.equal() + "\n");

			clusterCounter++;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public DifferenceStats open() {
		try {
			File clusteringStats = new File(outputDir);
			clusteringStats.mkdirs();
			clusteringStats = new File(outputDir + "clustering.stats");
			bw = new BufferedWriter(new FileWriter(clusteringStats));
			bw.write("cluster\tN\tM\tallowed_mapping_objects\tgenes_query_species\tgenes_target_species\t");
			bw.write(
					"greedy_score\tnumber_greedy_matches\tgreedy_matches\tnumber_greedy_unmatched\tgreedy_unmatched\t");
			bw.write("seqId_score\tnumber_seqId_matches\tseqId_matches\tnumber_seqId_unmatched\tseqId_unmatched\t");
			bw.write("fuzzy_score\tnumber_fuzzy_matches\tfuzzy_matches\tnumber_fuzzy_unmatched\tfuzzy_unmatched\n");

			String header = "cluster\tscore1\tscore2\tscoringDiff\tnumber_matches1\tnumber_matches2\tsame\tdiff\tequal\n";

			bwGreedySeqId = new BufferedWriter(new FileWriter(new File(outputDir + "greedy_seqId.stats")));
			bwGreedySeqId.write(header);

			bwGreedyFuzzy = new BufferedWriter(new FileWriter(new File(outputDir + "greedy_fuzzy.stats")));
			bwGreedyFuzzy.write(header);

			bwSeqIdFuzzy = new BufferedWriter(new FileWriter(new File(outputDir + "seqId_fuzzy.stats")));
			bwSeqIdFuzzy.write(header);

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return this;
	}

	public void close() {
		try {
			bw.close();
			bwGreedySeqId.close();
			bwGreedyFuzzy.close();
			bwSeqIdFuzzy.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
