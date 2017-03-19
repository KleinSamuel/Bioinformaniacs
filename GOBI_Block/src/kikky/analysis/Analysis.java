package kikky.analysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import dennis.tissues.Tissue;
import dennis.utility_manager.Experiment;
import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;
import kikky.heatmap.Sample_Data;

public class Analysis {
	private static long start;

	public static void main(String[] args) {
		start = System.currentTimeMillis();
		ArrayList<Sample_Data> fpkm_samples = new ArrayList<>();
		System.out.println(systemInfoString() + "Starting Utility Manager");
		new UtilityManager(null, false, false, true);
		String data_path = UtilityManager.getConfig("output_directory");
		System.out.println(systemInfoString() + "Starting to save gene count infos");
		for (Iterator<Species> it_org = UtilityManager.speciesIterator(); it_org.hasNext();) {
			Species organism = it_org.next();
			System.out.println(systemInfoString() + "Starting to save gene count for " + organism.getName());
			for (Iterator<Tissue> it_tis = UtilityManager.tissueIterator(organism); it_tis.hasNext();) {
				Tissue tissue = it_tis.next();
				for (Experiment exp : tissue.getExperiments()) {
					String map = "star";
					String path = data_path + organism.getId() + "/" + tissue.getName() + "/" + exp.getName() + "/"
							+ map + "/gene.counts";
					FPKM_Single fs = new FPKM_Single(organism, tissue.getName(), exp.getName(), path,
							data_path + "geneLengths/" + organism.getId() + ".geneLengths");
					fpkm_samples.add(fs);
				}
			}
		}
		System.out.println(systemInfoString() + "Starting to generate values for HeatMap");
		fpkm_samples.sort(new TissueComparator<>());
		Process plotting;
		try {
			System.out.println("qsub -b Y -t 7000-" + (7000+(fpkm_samples.size() - 1))
					+ " -N FPKM -P prakt_proj -l vf=8000M,h_rt=1:00:00 -o $HOME/grid -e $HOME/grid \"/home/a/adamowicz/GoBi/Block/results/callAnalysis.sh\"");
			plotting = Runtime.getRuntime().exec("qsub -b Y -t 7000-" + (7000+(fpkm_samples.size() - 1))
					+ " -N FPKM -P short_proj -l vf=8000M,h_rt=1:00:00 -o $HOME/grid -e $HOME/grid \"/home/a/adamowicz/GoBi/Block/results/callAnalysis.sh\"");
//			plotting.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(systemInfoString() + "Terminated");
	}

	public static String systemInfoString() {
		String out = "[";
		out += (System.currentTimeMillis() - start) / 1000 + "." + (System.currentTimeMillis() - start) % 1000 + "s";
		out += "|" + ((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024d / 1024d
				/ 1024d * 1000d)) / 1000d + "GB]";
		return out;
	}
}
