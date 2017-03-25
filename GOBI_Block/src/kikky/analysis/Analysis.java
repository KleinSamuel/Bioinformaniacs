package kikky.analysis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import dennis.tissues.Tissue;
import dennis.tissues.TissuePair;
import dennis.utility_manager.Experiment;
import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;
import kikky.heatmap.HeatMap;
import kikky.heatmap.Sample_Data;

public class Analysis {
	private static long start;
	private final String path = "/home/proj/biocluster/praktikum/genprakt-ws16/bioinformaniacs/Kikky/";

	public static void main(String[] args) throws IOException {
		start = System.currentTimeMillis();
		// FPKM(args[0]);
		DE_Pairs(args[0]);
	}

	private static void DE_Pairs(String phase) {
		String data_path = "/home/proj/biocluster/praktikum/genprakt-ws16/bioinformaniacs/EB/";
		ArrayList<Sample_Data> dep_samples = new ArrayList<>();
		System.out.println(systemInfoString() + "Starting Utility Manager");
		new UtilityManager("/home/a/adamowicz/git/Bioinformaniacs/GOBI_Block/ressources/config.txt", false, false,
				false);
		System.out.println(systemInfoString() + "Starting to save gene count infos");
		for (Iterator<Species> it_org = UtilityManager.speciesIterator(); it_org.hasNext();) {
			Species organism = it_org.next();
			System.out.println(systemInfoString() + "Starting to save gene count for " + organism.getName());
			for (Iterator<TissuePair> it_tis = UtilityManager.tissuePairIterator(organism); it_tis.hasNext();) {
				TissuePair tissues = it_tis.next();
				String map = "star";
				String path = data_path + organism.getId() + "/" + tissues.getKey() + "_" + tissues.getValue() + "/"
						+ map + "/" + tissues.getKey() + "_" + tissues.getValue() + ".DESeq";
				DE_Pairs fs = new DE_Pairs(organism, tissues, path);
				dep_samples.add(fs);
			}
		}
		dep_samples.sort(new TissueComparator<>());
		if (phase.equals("phaseone")) {
			try {
				System.out.println(systemInfoString() + "Starting phase one!");
				BufferedWriter bw = new BufferedWriter(new FileWriter("/home/a/adamowicz/GoBi/Block/results/dep.info"));
				bw.write("Number\tOrganism_ID\tOrganism_name\tTissue_key\tTissue_val\tfile_path");
				for (int i = 1; i <= dep_samples.size(); i++) {
					DE_Pairs fs = (DE_Pairs) dep_samples.get(i - 1);
					bw.write("\n" + i + "#\t" + fs.get_init_info());
				}
				bw.close();
				System.out.println(systemInfoString() + "Starting to generate values for HeatMap");
				Process plotting;
				int id = 7000;
				plotting = Runtime.getRuntime().exec("qsub -b Y -t " + (id + 1) /* + "-" + (id + dep_samples.size())*/
						+ " -N DEP -P prakt_proj -l vf=8000M,h_rt=1:00:00 -o $HOME/grid -e $HOME/grid \"/home/a/adamowicz/GoBi/Block/results/callAnalysis.sh\" 7001 " + id + " " + (id + dep_samples.size()));
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println(systemInfoString() + "Terminated");
		} else if (phase.equals("phasetwo")) {
			System.out.println(systemInfoString() + "Starting phase two!");
			int max = dep_samples.size();
			BufferedWriter bw;
			try {
				bw = new BufferedWriter(new FileWriter("/home/a/adamowicz/GoBi/Block/results/heatmaps_vals_dep.txt"));
				Number[][] matrix = new Number[max][max];
				StringBuilder sb = new StringBuilder("");
				for (int i = 1; i <= matrix.length; i++) {
					for (int j = 1; j <= matrix[i - 1].length; j++) {
						matrix[i - 1][j - 1] = File_Preparer.read_file_dep("files/" + i + "-" + j + "DEP.txt",
								dep_samples, ((DE_Pairs) dep_samples.get(i - 1)).get_species(),
								((DE_Pairs) dep_samples.get(j - 1)).get_species());
						sb.append(matrix[i - 1][j - 1]).append(",");
					}
					System.out.println(systemInfoString() + i + " rows done!");
					sb.deleteCharAt(sb.length() - 1);
					sb.append("\n");
				}
				bw.write(sb.toString());
				bw.close();
				ArrayList<Sample_Data> al = new ArrayList<>();
				for (int i = 1; i <= max; i++)
					al.add(dep_samples.get(i - 1));

				HeatMap hm = new HeatMap("FPKM", al, al, matrix);
				hm.plot("/home/a/adamowicz/GoBi/json_all_dep.txt");
				System.out.println(systemInfoString() + "Terminated");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void FPKM(String phase) throws IOException {
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
		if (phase.equals("phaseone")) {
			try {
				System.out.println(systemInfoString() + "Starting phase one!");
				BufferedWriter bw = new BufferedWriter(
						new FileWriter("/home/a/adamowicz/GoBi/Block/results/fpkm.info"));
				bw.write("Number\tOrganism_ID\tOrganism_name\tOrganism_gtf\tOrganism_chr\tTissue\tExperiment");
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
		} else if (phase.equals("phasetwo")) {
			System.out.println(systemInfoString() + "Starting phase two!");
			int max = fpkm_samples.size();
			BufferedWriter bw = new BufferedWriter(
					new FileWriter("/home/a/adamowicz/GoBi/Block/results/heatmaps_vals.txt"));
			Number[][] matrix = new Number[max][max];
			StringBuilder sb = new StringBuilder("");
			for (int i = 1; i <= matrix.length; i++) {
				for (int j = 1; j <= matrix[i - 1].length; j++) {
					matrix[i - 1][j - 1] = File_Preparer.read_file_fpkm("files/" + i + "-" + j + "FPKM.txt",
							fpkm_samples, ((FPKM_Single) fpkm_samples.get(i - 1)).get_species(),
							((FPKM_Single) fpkm_samples.get(j - 1)).get_species());
					sb.append(matrix[i - 1][j - 1]).append(",");
				}
				System.out.println(systemInfoString() + i + " rows done!");
				sb.deleteCharAt(sb.length() - 1);
				sb.append("\n");
			}
			bw.write(sb.toString());
			bw.close();
			ArrayList<Sample_Data> al = new ArrayList<>();
			for (int i = 1; i <= max; i++)
				al.add(fpkm_samples.get(i - 1));

			HeatMap hm = new HeatMap("FPKM", al, al, matrix);
			hm.plot("/home/a/adamowicz/GoBi/json_all_fpkm.txt");
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
