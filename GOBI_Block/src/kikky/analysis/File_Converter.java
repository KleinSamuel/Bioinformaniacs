package kikky.analysis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import dennis.counter.CounterUtils;
import dennis.enrichment.EBUtils;
import dennis.tissues.Tissue;
import dennis.utility_manager.Experiment;
import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;

public class File_Converter {
	private static long start;

	public static void main(String[] args) {
		start = System.currentTimeMillis();

		if (args[0].equals("FPKM")) {
			new UtilityManager("/home/a/adamowicz/git/Bioinformaniacs/GOBI_Block/ressources/config.txt", false, false,
					false);
			String data_path = UtilityManager.getConfig("output_directory");
			String data = UtilityManager.getConfig("old_output_directory");
			for (Iterator<Species> it_org = UtilityManager.speciesIterator(); it_org.hasNext();) {
				Species organism = it_org.next();
				for (Iterator<Tissue> it_tis = UtilityManager.tissueIterator(organism); it_tis.hasNext();) {
					Tissue tissue = it_tis.next();
					for (Experiment exp : tissue.getExperiments()) {
						String map = "star";
						String path = data_path + organism.getId() + "/" + tissue.getName() + "/" + exp.getName() + "/"
								+ map;
						try {
							BufferedWriter bw = new BufferedWriter(new FileWriter(path + "/fpkm.counts"));
							HashMap<String, Double> hm = Calculator.FPKM_generator(path + "/gene.counts",
									data + "geneLengths/" + organism.getId() + ".geneLengths");
							bw.write("gene_id\tfpkm_value");
							for (String key : hm.keySet())
								bw.write("\n" + key + "\t" + hm.get(key));
							bw.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		} else if (args[0].equals("DES")) {
			new UtilityManager("/home/a/adamowicz/git/Bioinformaniacs/GOBI_Block/ressources/config.txt", false, false,
					false);
			String data_path = UtilityManager.getConfig("output_directory");
			for (Iterator<Species> it_org = UtilityManager.speciesIterator(); it_org.hasNext();) {
				Species organism = it_org.next();
				ArrayList<String> raw_dir = new ArrayList<>();
				ArrayList<String> raw_files = new ArrayList<>();
				for (Iterator<Tissue> it_tis = UtilityManager.tissueIterator(organism); it_tis.hasNext();) {
					Tissue tissue = it_tis.next();
					raw_files.add(data_path + organism.getId() + "/" + tissue.getName() + "/star_tissue_average.counts");
					raw_dir.add(data_path + organism.getId() + "/" + tissue.getName() + "/");
				}
				String mix_path = data_path + organism.getId() + "/tissue_mix.count";
				CounterUtils.createAverageCountFile(raw_files, mix_path);
				for (String raw : raw_dir) {
					ArrayList<String> a1 = new ArrayList<>();
					a1.add(raw + "star_tissue_average.counts");
					ArrayList<String> a2 = new ArrayList<>();
					a2.add(mix_path);
					EBUtils.runEnrichment(a1, a2, raw, "vsTissuemix", false);
				}
			}
		}
	}

	public static String systemInfoString() {
		String out = "[";
		out += (System.currentTimeMillis() - start) / 1000 + "." + (System.currentTimeMillis() - start) % 1000 + "s";
		out += "|" + ((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024d / 1024d
				/ 1024d * 1000d)) / 1000d + "GB]";
		return out;
	}
}
