package kikky.analysis;

import java.io.File;
import java.util.Iterator;
import java.util.TreeSet;

import dennis.tissues.Tissue;
import dennis.utility_manager.Experiment;
import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;

public class Analysis {
	private static long start;

	public static void main(String[] args) {
		start = System.currentTimeMillis();
		TreeSet<FPKM_Single> fpkm_samples = new TreeSet<>();
		System.out.println(systemInfoString() + "Starting Utility Manager");
		new UtilityManager(null, false, false, true);
//		String data_path = UtilityManager.getConfig("output_directory");
//		System.out.println(systemInfoString() + "Starting to save gene count infos");
//		for (Iterator<Species> it_org = UtilityManager.speciesIterator(); it_org.hasNext();) {
//			Species organism = it_org.next();
//			for (Iterator<Tissue> it_tis = UtilityManager.tissueIterator(organism); it_tis.hasNext();) {
//				Tissue tissue = it_tis.next();
//				for (Experiment exp : tissue.getExperiments()) {
//					String map = "star";
//					String path = data_path + organism.getId() + "/" + tissue.getName() + "/" + exp.getName() + "/"
//							+ map + "/gene.counts";
//					File f = new File(path);
//					FPKM_Single fs = new FPKM_Single(organism.getId(), organism.getName(), tissue.getName(),
//							exp.getName(), path, data_path + "geneLengths/" + organism.getId() + ".geneLengths");
//					fpkm_samples.add(fs);
//					// if (!f.exists()) {
//					// System.out.println("Dennis hat verkackt");
//					// System.out.println(path);
//					// }
//				}
//			}
//		}
//		System.out.println(systemInfoString() + "Starting to generate infos");
//		for(FPKM_Single fs : fpkm_samples){
//			System.out.println(fs.get_Name());
//		}
		System.out.println(systemInfoString() + "Terminated");
	}

	public static String systemInfoString() {
		String out = "[";
		out += (System.currentTimeMillis() - start) / 1000 + "." + (System.currentTimeMillis() - start) % 1000 + "s";
		out += "|"
				+ (int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (Math.pow(1024, 2)))
				+ "MB] ";
		return out;
	}
}
