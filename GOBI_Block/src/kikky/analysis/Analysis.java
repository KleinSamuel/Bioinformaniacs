package kikky.analysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import dennis.tissues.Tissue;
import dennis.utility_manager.Experiment;
import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;
import kikky.heatmap.HeatMap;
import kikky.heatmap.Sample_Data;

public class Analysis {
	private static long start;

	public static void main(String[] args) {
		start = System.currentTimeMillis();
		ArrayList<Sample_Data> fpkm_samples = new ArrayList<>();
		System.out.println(systemInfoString() + "Starting Utility Manager");
		new UtilityManager("/home/a/adamowicz/git/Bioinformaniacs/GOBI_Block/ressources/config.txt", false, false,
				false);
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

		fpkm_samples.sort(new TissueComparator<>());
		if (args[0].equals("phase one")) {
			System.out.println(systemInfoString() + "Starting phase one!");
			System.out.println(systemInfoString() + "Starting to generate values for HeatMap");
			Process plotting;
			try {
				for (int i = 1; i <= 10; i++) {
					int id = 7000 + (10 * (i - 1));
					plotting = Runtime.getRuntime()
							.exec("qsub -b Y -t " + (id + 1) + "-"
									+ /* (7000 + (fpkm_samples.size() - 1)) */(id + 10)
									+ " -N FPKM -P short_proj -l vf=8000M,h_rt=1:00:00 -o $HOME/grid -e $HOME/grid \"/home/a/adamowicz/GoBi/Block/results/callAnalysis.sh\" "
									+ (7000 + i) + " " + id);
				}
				Process start_checker;
				start_checker = Runtime.getRuntime().exec("bash checkAnalysis.sh");
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println(systemInfoString() + "Terminated");
		} else if (args[0].equals("phase two")) {
			System.out.println(systemInfoString() + "Starting phase two!");
			Number[][] matrix = new Number[10][10];
			for (int i = 1; i <= matrix.length; i++) {
				for (int j = 1; j <= matrix[i - 1].length; j++) {
					try {
						BufferedReader br = new BufferedReader(new FileReader("files/" + i + "-" + j + "FPKM.txt"));
						matrix[i - 1][j - 1] = Double.parseDouble(br.readLine());
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			ArrayList<Sample_Data> al = new ArrayList<>();
			for (int i = 0; i < 10; i++)
				al.add(fpkm_samples.get(i));
			HeatMap hm = new HeatMap("FPKM", al, al, matrix);
			hm.plot();
			System.out.println(systemInfoString() + "Terminated");
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
