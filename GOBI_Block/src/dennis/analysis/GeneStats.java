package dennis.analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Comparator;
import java.util.TreeSet;

import dennis.similarities.GeneSimilarities;
import dennis.similarities.SimilarityObject;
import dennis.util.GenePair;
import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;

public class GeneStats {

	// gene toHighest amountMissingData toHighestAllowed

	private Species query, target;
	private GeneSimilarities simHandlerQuery, simHandlerTarget;
	private String outputDir;
	private BufferedWriter bwSequenceIdentityOptimizedSp1, bwExpressionOptimizedSp1,
			bwGreedySequenceIdentityOptimizedSp1, bwGreedyExpressionOptimizedSp1;
	private BufferedWriter bwSequenceIdentityOptimizedSp2, bwExpressionOptimizedSp2,
			bwGreedySequenceIdentityOptimizedSp2, bwGreedyExpressionOptimizedSp2;
	private TreeSet<String> unmatchedGenesSequenceIdSp1, unmatchedGenesSequenceIdSp2, unmatchedGenesGreedySequenceIdSp1,
			unmatchedGenesGreedySequenceIdSp2, unmatchedGenesExprSp1, unmatchedGenesExprSp2,
			unmatchedGenesGreedyExprSp1, unmatchedGenesGreedyExprSp2;

	public GeneStats(Species query, Species target, String outputDir) {
		this.query = query;
		this.target = target;
		simHandlerQuery = UtilityManager.getSimilarityHandler().getSimilarities(query, target);
		simHandlerTarget = UtilityManager.getSimilarityHandler().getSimilarities(target, query);
		this.outputDir = outputDir;
		File f = new File(outputDir);
		f.mkdirs();
	}

	public void addBipartiteMatching(BipartiteMatching biMatch, boolean seqId, boolean greedy) {
		for (GenePair gp : biMatch.getMatches().keySet()) {
			if (seqId) {
				if (greedy) {
					writeGeneLine(gp, simHandlerQuery, bwGreedySequenceIdentityOptimizedSp1);
					writeGeneLine(gp.switchGenes(), simHandlerTarget, bwGreedySequenceIdentityOptimizedSp2);
				} else {
					writeGeneLine(gp, simHandlerQuery, bwSequenceIdentityOptimizedSp1);
					writeGeneLine(gp.switchGenes(), simHandlerTarget, bwSequenceIdentityOptimizedSp2);
				}
			} else {
				if (greedy) {
					writeGeneLine(gp, simHandlerQuery, bwGreedyExpressionOptimizedSp1);
					writeGeneLine(gp.switchGenes(), simHandlerTarget, bwGreedyExpressionOptimizedSp2);
				} else {
					writeGeneLine(gp, simHandlerQuery, bwExpressionOptimizedSp1);
					writeGeneLine(gp.switchGenes(), simHandlerTarget, bwExpressionOptimizedSp2);
				}
			}
		}
		for (String unmatched_query : biMatch.getUnmatchedQuery()) {
			if (seqId) {
				if (greedy) {
					unmatchedGenesGreedySequenceIdSp1.add(unmatched_query);
				} else {
					unmatchedGenesSequenceIdSp1.add(unmatched_query);
				}
			} else {
				if (greedy) {
					unmatchedGenesGreedyExprSp1.add(unmatched_query);
				} else {
					unmatchedGenesExprSp1.add(unmatched_query);
				}
			}
		}
		for (String unmatched_target : biMatch.getUnmatchedTarget()) {
			if (seqId) {
				if (greedy) {
					unmatchedGenesGreedySequenceIdSp2.add(unmatched_target);
				} else {
					unmatchedGenesSequenceIdSp2.add(unmatched_target);
				}
			} else {
				if (greedy) {
					unmatchedGenesGreedyExprSp2.add(unmatched_target);
				} else {
					unmatchedGenesExprSp2.add(unmatched_target);
				}
			}
		}
	}

	public void writeGeneLine(GenePair gp, GeneSimilarities sims, BufferedWriter bw) {

		int isHighestSeqId = isHighestSeqId(gp, sims) ? 1 : 0,
				isHighestSeqIdAllowed = isHighestSeqIdAllowed(gp, sims) ? 1 : 0;
		try {
			bw.write(gp.getKey() + "\t" + isHighestSeqId + "\t" + isHighestSeqIdAllowed + "\n");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public boolean isHighestSeqId(GenePair gp, GeneSimilarities simHandler) {
		return simHandler.getGeneWithHighestIdentity(gp.getKey()).equals(gp.getValue());
	}

	public boolean isHighestSeqIdAllowed(GenePair gp, GeneSimilarities simHandler) {
		TreeSet<SimilarityObject> sortedSims = new TreeSet<>(new Comparator<SimilarityObject>() {

			@Override
			public int compare(SimilarityObject o1, SimilarityObject o2) {
				return -Double.compare(o1.getMaximumIdentityScore(), o2.getMaximumIdentityScore());
			}

		});
		sortedSims.addAll(simHandler.getSimilarities(gp.getKey()).values());

		for (SimilarityObject so : sortedSims) {
			if (InputDataPreparator.getAllowedGenes().contains(so.getTarget_geneId())) {
				return gp.getValue().equals(so.getTarget_geneId());
			}
		}

		return false;
	}

	public Species getQuerySpecies() {
		return query;
	}

	public Species getTargetSpecies() {
		return target;
	}

	public GeneStats open() {
		try {
			init();
			String header = "gene_id\tto_highest\tto_highest_allowed\n";
			bwSequenceIdentityOptimizedSp1 = new BufferedWriter(new FileWriter(
					new File(outputDir + query.getId() + "_" + target.getId() + "_seqIdOptimized.geneStats")));
			bwSequenceIdentityOptimizedSp1.write(header);
			bwExpressionOptimizedSp1 = new BufferedWriter(new FileWriter(
					new File(outputDir + query.getId() + "_" + target.getId() + "_expressionOptimized.geneStats")));
			bwExpressionOptimizedSp1.write(header);

			bwGreedySequenceIdentityOptimizedSp1 = new BufferedWriter(new FileWriter(
					new File(outputDir + query.getId() + "_" + target.getId() + "_greedySeqIdOptimized.geneStats")));
			bwGreedySequenceIdentityOptimizedSp1.write(header);
			bwGreedyExpressionOptimizedSp1 = new BufferedWriter(new FileWriter(new File(
					outputDir + query.getId() + "_" + target.getId() + "_greedyExpressionOptimized.geneStats")));
			bwGreedyExpressionOptimizedSp1.write(header);

			bwSequenceIdentityOptimizedSp2 = new BufferedWriter(new FileWriter(
					new File(outputDir + target.getId() + "_" + query.getId() + "_seqIdOptimized.geneStats")));
			bwSequenceIdentityOptimizedSp2.write(header);
			bwExpressionOptimizedSp2 = new BufferedWriter(new FileWriter(
					new File(outputDir + target.getId() + "_" + query.getId() + "_expressionOptimized.geneStats")));
			bwExpressionOptimizedSp2.write(header);

			bwGreedySequenceIdentityOptimizedSp2 = new BufferedWriter(new FileWriter(
					new File(outputDir + target.getId() + "_" + query.getId() + "_greedySeqIdOptimized.geneStats")));
			bwGreedySequenceIdentityOptimizedSp2.write(header);
			bwGreedyExpressionOptimizedSp2 = new BufferedWriter(new FileWriter(new File(
					outputDir + target.getId() + "_" + query.getId() + "_greedyExpressionOptimized.geneStats")));
			bwGreedyExpressionOptimizedSp2.write(header);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return this;
	}

	public void init() {
		unmatchedGenesSequenceIdSp1 = new TreeSet<>();
		unmatchedGenesSequenceIdSp2 = new TreeSet<>();
		unmatchedGenesGreedySequenceIdSp1 = new TreeSet<>();
		unmatchedGenesGreedySequenceIdSp2 = new TreeSet<>();
		unmatchedGenesExprSp1 = new TreeSet<>();
		unmatchedGenesExprSp2 = new TreeSet<>();
		unmatchedGenesGreedyExprSp1 = new TreeSet<>();
		unmatchedGenesGreedyExprSp2 = new TreeSet<>();
	}

	public void close() {
		try {
			bwSequenceIdentityOptimizedSp1.close();
			bwExpressionOptimizedSp1.close();
			bwGreedySequenceIdentityOptimizedSp1.close();
			bwGreedyExpressionOptimizedSp1.close();

			bwSequenceIdentityOptimizedSp2.close();
			bwExpressionOptimizedSp2.close();
			bwGreedySequenceIdentityOptimizedSp2.close();
			bwGreedyExpressionOptimizedSp2.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
