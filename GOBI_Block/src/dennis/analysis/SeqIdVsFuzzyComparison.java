package dennis.analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import dennis.util.GenePair;

public class SeqIdVsFuzzyComparison {

	private BufferedWriter bwSequenceIdentityOptimized, bwExpressionOptimized;
	private String outputDir;

	public SeqIdVsFuzzyComparison(String outputDir) {
		this.outputDir = outputDir;
		File f = new File(outputDir);
		f.mkdirs();
	}

	public void writeSequenceIdentityOptimizedGenePair(int cluster, GenePair gp, double seqIdScore, double deScore) {
		writeGenePair(cluster, gp, seqIdScore, deScore, bwSequenceIdentityOptimized);
	}

	public void writeExpressionOptimizedGenePair(int cluster, GenePair gp, double seqIdScore, double deScore) {
		writeGenePair(cluster, gp, seqIdScore, deScore, bwExpressionOptimized);
	}

	public void writeGenePair(int cluster, GenePair gp, double seqIdScore, double deScore, BufferedWriter bw) {
		try {
			bw.write(cluster + "\t" + gp.getKey() + "\t" + gp.getValue() + "\t" + seqIdScore + "\t" + deScore + "\n");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public SeqIdVsFuzzyComparison open() {
		try {
			String header = "cluster\tgene_id_species_1\tgene_id_species_2\tsequence_identity_score\tfuzzy_DE_score\n";
			bwSequenceIdentityOptimized = new BufferedWriter(
					new FileWriter(new File(outputDir + "seqIdOptimized.mapping")));
			bwSequenceIdentityOptimized.write(header);
			bwExpressionOptimized = new BufferedWriter(
					new FileWriter(new File(outputDir + "expressionOptimized.mapping")));
			bwExpressionOptimized.write(header);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return this;
	}

	public void close() {
		try {
			bwSequenceIdentityOptimized.close();
			bwExpressionOptimized.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
