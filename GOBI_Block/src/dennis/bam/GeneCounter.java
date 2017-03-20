package dennis.bam;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import dennis.genomeAnnotation.Gene;

public class GeneCounter {

	// anyPcrNonAmbigous; pcrIndex = 0NonAmbigous; anyPcrAmbigousWeighted;
	// pcrIndex=0AmbigousWeighted
	private HashMap<Gene, Double[]> counts;
	private Integer[] totalCounts;

	// rps transcript merged intron uniq multi
	private Integer[] bamFileCounts;

	public GeneCounter() {
		counts = new HashMap<>();
		totalCounts = new Integer[] { 0, 0, 0, 0 };
		bamFileCounts = new Integer[] { 0, 0, 0, 0, 0, 0 };
	}

	public void addReadObject(ReadObject rp) {
		bamFileCounts[0]++;
		LinkedList<Gene> matchedGenes = rp.getMatchedGenes();
		if (matchedGenes.isEmpty()) {
			return;
		}
		boolean ambigous = matchedGenes.size() > 1;
		for (Gene g : matchedGenes) {
			Double[] countArr = counts.get(g);
			if (countArr == null) {
				countArr = new Double[] { 0d, 0d, 0d, 0d };
				counts.put(g, countArr);
			}
			if (ambigous) {
				countArr[2] += (1d / matchedGenes.size());
				if (rp.getPcrIndex() == 0) {
					countArr[3] += (1d / matchedGenes.size());
				}
			} else {
				countArr[0]++;
				totalCounts[0]++;
				if (rp.getPcrIndex() == 0) {
					countArr[1]++;
					totalCounts[1]++;
				}
			}
		}
		if (ambigous) {
			bamFileCounts[5]++;

			totalCounts[2]++;
			if (rp.getPcrIndex() == 0) {
				totalCounts[3]++;
			}
		} else {
			bamFileCounts[4]++;
		}
		if (rp.isTranscriptomic())
			bamFileCounts[1]++;
		else if (rp.isMerged())
			bamFileCounts[2]++;
		else if (rp.isIntronic())
			bamFileCounts[3]++;
	}

	public void writeOutput(String outputDir) {
		try {
			File f = new File(outputDir);
			f.mkdirs();
			f = new File(outputDir + "/gene.counts");
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			bw.write("geneId\tnonAmbigousAnyPcr\tnonAmbigousPcr0\tambigousWeightedAnyPcr\tambigousWeightedPcr0\n");
			Gene g = null;
			for (Iterator<Gene> it = BamFileReader.ga.iterator(); it.hasNext();) {
				g = it.next();
				Double[] arr = counts.get(g);
				if (arr == null) {
					arr = new Double[] { 0d, 0d, 0d, 0d };
					bw.write(g.getId() + "\t" + arr[0].intValue() + "\t" + arr[1].intValue() + "\t" + arr[2] + "\t"
							+ arr[3] + "\n");
				} else {
					bw.write(g.getId() + "\t" + arr[0].intValue() + "\t" + arr[1].intValue() + "\t" + arr[2] + "\t"
							+ arr[3] + "\n");
				}
			}
			bw.close();
			f = new File(outputDir + "/total.counts");
			bw = new BufferedWriter(new FileWriter(f));
			bw.write("rps\ttranscript\tmerged\tintron\tuniq\tmulti\n");
			StringBuilder sb = new StringBuilder();
			for (Integer i : bamFileCounts) {
				sb.append(i + "\t");
			}
			sb.deleteCharAt(sb.length() - 1);
			bw.write(sb.toString() + "\n");
			bw.write("anyPcrNonAmbigous\tpcrIndex0NonAmbigous\tanyPcrAmbigousWeighted\tpcrIndex0AmbigousWeighted\n");
			bw.write(totalCounts[0] + "\t" + totalCounts[1] + "\t" + totalCounts[2] + "\t" + totalCounts[3] + "\n");
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
