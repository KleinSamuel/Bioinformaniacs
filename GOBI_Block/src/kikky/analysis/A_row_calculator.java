package kikky.analysis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import dennis.tissues.Tissue;
import dennis.utility_manager.Experiment;
import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;
import kikky.heatmap.Sample_Data;

public class A_row_calculator {
	private static long start;

	public static void main(String[] args) {
		start = System.currentTimeMillis();
		System.out.println(systemInfoString() + "Starting to generate all partners");
		ArrayList<Sample_Data> fpkm_samples = new ArrayList<>();
		new UtilityManager(null, false, false, true);
		String data_path = UtilityManager.getConfig("output_directory");
		for (Iterator<Species> it_org = UtilityManager.speciesIterator(); it_org.hasNext();) {
			Species organism = it_org.next();
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
		fpkm_samples.sort(new TissueComparator<>());
		System.out.println(systemInfoString() + "Starting to calculate values to all partners");
		Sample_Data fs_cur = fpkm_samples.get(Integer.parseInt(args[0]) - 7000);
		String temp = "";
		int index = 0;
		for (Sample_Data sd2 : fpkm_samples) {
			System.out.println(systemInfoString() + fs_cur.get_Name() + " vs " + sd2.get_Name() + "[" + index++ + "|"
					+ fpkm_samples.size() + "]");
			if (index++ > 10)
				break;
			temp += "\t" + fs_cur.get_value(sd2);
		}
		try {
			BufferedWriter bw = new BufferedWriter(
					new FileWriter("/home/a/adamowicz/GoBi/Block/results/files/" + args[0] + "FPKM.txt"));
			bw.write(temp.substring(1));
			bw.close();
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
