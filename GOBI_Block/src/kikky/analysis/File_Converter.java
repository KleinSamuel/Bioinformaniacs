package kikky.analysis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import dennis.counter.CounterUtils;
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
									data_path + "geneLengths/" + organism.getId() + ".geneLengths");
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
