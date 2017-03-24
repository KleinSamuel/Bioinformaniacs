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
import kikky.heatmap.HeatMap;
import kikky.heatmap.Sample_Data;

public class Analysis {
	private static long start;

	public static void main(String[] args) {
		start = System.currentTimeMillis();
		FPKM(args[0]);
	}

	private static void FPKM(String phase) {
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
							+ map + "/fpkm.counts";
					FPKM_Single fs = new FPKM_Single(organism, tissue.getName(), exp.getName(), path);
					fpkm_samples.add(fs);
				}
			}
		}

		fpkm_samples.sort(new TissueComparator<>());
		if (phase.equals("phase one")) {
			try {
				System.out.println(systemInfoString() + "Starting phase one!");
				BufferedWriter bw = new BufferedWriter(
						new FileWriter("/home/a/adamowicz/GoBi/Block/results/fpkm.info"));
				bw.write("Number\tOrganism_ID\tOrganism_name\tTissue\tExperiment");
				for (int i = 1; i <= fpkm_samples.size(); i++) {
					FPKM_Single fs = (FPKM_Single) fpkm_samples.get(i - 1);
					bw.write("\n" + i + "#\t" + fs.get_init_info());
				}
				bw.close();
				System.out.println(systemInfoString() + "Starting to generate values for HeatMap");
				Process plotting;
				for (int i = 1; i <= fpkm_samples.size(); i++) {
					int id = 7000;
					plotting = Runtime.getRuntime().exec("qsub -b Y -t " + (id + 1) + "-" + (id + fpkm_samples.size())
							+ " -N FPKM -P short_proj -l vf=8000M,h_rt=1:00:00 -o $HOME/grid -e $HOME/grid \"/home/a/adamowicz/GoBi/Block/results/callAnalysis.sh\" "
							+ (7000 + 1) + " " + id);
					System.out.println();
				}
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println(systemInfoString() + "Terminated");
		} else if (phase.equals("phase two")) {
			System.out.println(systemInfoString() + "Starting phase two!");
			int max = 5;
			Number[][] matrix = new Number[max][max];
			for (int i = 1; i <= matrix.length; i++) {
				for (int j = 1; j <= matrix[i - 1].length; j++) {
					matrix[i - 1][j - 1] = File_Preparer.read_file_fpkm("files/" + i + "-" + j + "FPKM.txt",
							fpkm_samples, ((FPKM_Single) fpkm_samples.get(i)).get_species(),
							((FPKM_Single) fpkm_samples.get(j)).get_species());
				}
			}
			ArrayList<Sample_Data> al = new ArrayList<>();
			for (int i = 1; i <= max; i++)
				al.add(fpkm_samples.get(i - 1));

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
