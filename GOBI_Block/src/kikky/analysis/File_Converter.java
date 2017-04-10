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
				HashMap<String, ArrayList<String>> exps = new HashMap<>();
				for (Iterator<Tissue> it_tis = UtilityManager.tissueIterator(organism); it_tis.hasNext();) {
					Tissue tissue = it_tis.next();
					exps.put(tissue.getName(), new ArrayList<>());
					for (Experiment exp : tissue.getExperiments()) {
						exps.get(tissue.getName()).add(data_path + organism.getId() + "/" + tissue + "/" + exp.getName()
								+ "/star/gene.counts");
					}
				}
				ArrayList<String> tms = new ArrayList<>();
				for (int i = 1; i <= 3; i++) {
					ArrayList<String> files = new ArrayList<>();
					for (String tissue : exps.keySet()) {
						files.add(exps.get(tissue).get(i % exps.get(tissue).size()));
					}
					CounterUtils.createAverageCountFile(files, data_path + organism.getId() + "/tm" + i + ".count");
					tms.add(data_path + organism.getId() + "/tm" + i + ".count");
				}
				for (String tissue : exps.keySet()) {
					EBUtils.runEnrichment(tms, exps.get(tissue), data_path + organism.getId() + "/" + tissue,
							"vsTissuemix", false);
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
