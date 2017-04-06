package dennis.analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import dennis.enrichment.EnrichmentAnalysisUtils;
import dennis.enrichment.GeneObject;
import dennis.genomeAnnotation.Gene;
import dennis.tissues.TissueHandler;
import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;
import javafx.util.Pair;

public class CheckGeneOccurences {

	public CheckGeneOccurences(Species sp, String mapper, String deMethod) {
		writeNumberOfGenes(sp, mapper, deMethod);
		writeNumberOfGenesWithPartner(sp, mapper, deMethod);
	}

	public void writeNumberOfGenesWithPartner(Species sp, String mapper, String deMethod) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(
					new File(
							"/home/proj/biocluster/praktikum/genprakt/bioinformaniacs/d‰‰‰hn/gene_occurences_with_orthologue.txt"),
					true));

			TreeMap<String, TreeSet<String>> genes = readDEfiles(sp, mapper, deMethod);

			bw.write(sp.getId() + "\t" + sp.getName() + "\t");

			TreeMap<Integer, Integer> tpsPerGene = new TreeMap<>();

			int numberOfGenes = 0;
			for (Iterator<Gene> geneIt = sp.getGenomeAnnotation().iterator(); geneIt.hasNext();) {
				String gene = geneIt.next().getId();
				if (UtilityManager.getSimilarityHandler().getAllSimilarities(sp, gene).isEmpty()) {
					continue;
				}
				Integer count = 0;
				for (Entry<String, TreeSet<String>> e : genes.entrySet()) {
					if (e.getValue() != null && !e.getValue().isEmpty() && e.getValue().contains(gene)) {
						count++;
					}
				}
				Integer counts = tpsPerGene.get(count);
				if (counts == null) {
					tpsPerGene.put(count, 1);
				} else {
					tpsPerGene.put(count, counts + 1);
				}
				numberOfGenes++;
			}

			bw.write(numberOfGenes + "");
			for (Integer i = 0; i <= genes.size(); i++) {
				Integer j = tpsPerGene.get(i);
				if (j == null)
					j = 0;
				bw.write("\t" + j);
			}
			bw.write("\n");

			bw.close();
		} catch (

		Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void writeNumberOfGenes(Species sp, String mapper, String deMethod) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(
					new File("/home/proj/biocluster/praktikum/genprakt/bioinformaniacs/d‰‰‰hn/gene_occurences.txt"),
					true));

			TreeMap<String, TreeSet<String>> genes = readDEfiles(sp, mapper, deMethod);

			bw.write(sp.getId() + "\t" + sp.getName() + "\t");

			TreeMap<Integer, Integer> tpsPerGene = new TreeMap<>();

			int numberOfGenes = 0;
			for (Iterator<Gene> geneIt = sp.getGenomeAnnotation().iterator(); geneIt.hasNext();) {
				String gene = geneIt.next().getId();
				Integer count = 0;
				for (Entry<String, TreeSet<String>> e : genes.entrySet()) {
					if (e.getValue() != null && !e.getValue().isEmpty() && e.getValue().contains(gene)) {
						count++;
					}
				}
				Integer counts = tpsPerGene.get(count);
				if (counts == null) {
					tpsPerGene.put(count, 1);
				} else {
					tpsPerGene.put(count, counts + 1);
				}
				numberOfGenes++;
			}

			bw.write(numberOfGenes + "");
			for (Integer i = 0; i <= genes.size(); i++) {
				Integer j = tpsPerGene.get(i);
				if (j == null)
					j = 0;
				bw.write("\t" + j);
			}
			bw.write("\n");

			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public TreeMap<String, TreeSet<String>> readDEfiles(Species sp, String mapper, String deMethod) {
		TreeMap<String, TreeSet<String>> genes = new TreeMap<>();
		for (Pair<String, String> tp : TissueHandler.getAllPossibleTissuePairs()) {
			TreeSet<String> in = new TreeSet<>();
			File f = new File(UtilityManager.getConfig("enrichment_output") + sp.getId() + "/" + tp.getKey() + "_"
					+ tp.getValue() + "/" + mapper + "/" + tp.getKey() + "_" + tp.getValue() + "." + deMethod);
			if (f.exists()) {
				for (GeneObject go : EnrichmentAnalysisUtils.readDEfile(UtilityManager.getConfig("enrichment_output")
						+ sp.getId() + "/" + tp.getKey() + "_" + tp.getValue() + "/" + mapper + "/" + tp.getKey() + "_"
						+ tp.getValue() + "." + deMethod)) {
					in.add(go.getName());
				}
			}
			genes.put(tp.getKey() + "_" + tp.getValue(), in);
		}
		return genes;
	}

}
