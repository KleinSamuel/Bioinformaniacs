package kikky.analysis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

import dennis.similarities.SimilarityObject;
import dennis.tissues.Tissue;
import dennis.utility_manager.Experiment;
import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;
import kikky.heatmap.HeatMap;
import kikky.heatmap.Sample_Data;
import kikky.heatmap.SpecialLineplot;

public class Analysis {
	private static long start;
	private final static String path = "/home/proj/biocluster/praktikum/genprakt-ws16/bioinformaniacs/Kikky/";

	public static void main(String[] args) throws IOException {
		start = System.currentTimeMillis();
		if (args[1].equals("FPKM"))
			FPKM(args[0], args[2]);
		else if (args[1].equals("DEP")) {
		}
	}

	private static void FPKM(String phase, String filter) throws IOException {
		ArrayList<Sample_Data> fpkm_samples = new ArrayList<>();
		System.out.println(systemInfoString() + "Starting Utility Manager");
		new UtilityManager("/home/a/adamowicz/git/Bioinformaniacs/GOBI_Block/ressources/config.txt", false, false,
				false);
		String data_path = UtilityManager.getConfig("output_directory");
		System.out.println(systemInfoString() + "Starting to save gene count infos");
		for (Iterator<Species> it_org = UtilityManager.speciesIterator(); it_org.hasNext();) {
			Species organism = it_org.next();
			for (Iterator<Tissue> it_tis = UtilityManager.tissueIterator(organism); it_tis.hasNext();) {
				Tissue tissue = it_tis.next();
				for (Experiment exp : tissue.getExperiments()) {
					String map = "star";
					String path = data_path + organism.getId() + "/" + tissue.getName() + "/" + exp.getName() + "/"
							+ map + "/fpkm.counts";
					FPKM_Single fs = new FPKM_Single(organism, tissue.getName(), exp.getName(), path, filter);
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
				int id = 7000;
				plotting = Runtime.getRuntime()
						.exec("qsub -b Y -t " + (id + 1) + "-" + (id + fpkm_samples.size())
								+ " -N FPKM -P short_proj -l vf=8000M,h_rt=1:00:00 -o $HOME/grid -e $HOME/grid \"/home/a/adamowicz/GoBi/Block/results/callAnalysis.sh\" "
								+ (id + 1) + " " + (id + fpkm_samples.size()) + " FPKM " + filter);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println(systemInfoString() + "Terminated");
		} else if (phase.equals("phasetwo")) {
			System.out.println(systemInfoString() + "Starting phase two!");
			int max = fpkm_samples.size();
			BufferedWriter bw = new BufferedWriter(new FileWriter(path + "plot/plot_vals_" + filter + "_fpkm.txt"));
			Number[][] matrix = new Number[max][max];
			HashMap<String, TreeMap<Double, Double>> comp = new HashMap<>();
			comp.put("#tt", new TreeMap<Double, Double>());
			comp.put("#tat", new TreeMap<Double, Double>());
			HashMap<String, TreeMap<Double, Double>> comp_spe = new HashMap<>();
			comp_spe.put("#oo", new TreeMap<Double, Double>());
			comp_spe.put("#oao", new TreeMap<Double, Double>());
			StringBuilder sb = new StringBuilder("");
			bw.write("#Heatmap values\n");
			SimilarityObject lowest = new SimilarityObject(Double.MAX_VALUE, "", ""),
					highest = new SimilarityObject(Double.MIN_VALUE, "", "");
			for (int i = 1; i <= matrix.length; i++) {
				matrix[i - 1] = File_Preparer.read_file_fpkm("files/" + i + "-" + filter + "FPKM.txt", fpkm_samples,
						comp, comp_spe);
				for (int j = 0; j < matrix[i - 1].length; j++) {
					sb.append(matrix[i - 1][j]).append(",");
					double val = (double) matrix[i - 1][j];
					if (val > highest.getMaximumIdentityScore())
						highest = new SimilarityObject(val, i + "", j + "");
					if (val > 0 && val < lowest.getMaximumIdentityScore())
						lowest = new SimilarityObject(val, i + "", j + "");
				}
				sb.deleteCharAt(sb.length() - 1);
				sb.append("\n");
			}
			bw.write(sb.toString());
			write_correlation(comp.get("#tt"), comp.get("#tat"), bw);
			bw.close();
			HeatMap hm = new HeatMap("FPKM", fpkm_samples, fpkm_samples, matrix);
			hm.plot(path + "plot/json_" + filter + "_fpkm.txt");

			ArrayList<Sample_Data> spe = new ArrayList<>();
			spe.addAll(fpkm_samples);
			spe.sort(new SpeciesComparator<>());
			Number[][] matrix_spe = new Number[max][max];
			for (int i = 0; i < matrix.length; i++)
				for (int j = 0; j < matrix[i].length; j++)
					matrix_spe[i][j] = matrix[fpkm_samples.indexOf(spe.get(i))][fpkm_samples.indexOf(spe.get(j))];
			HeatMap hm_spe = new HeatMap("FPKM", spe, spe, matrix_spe);
			hm_spe.plot(path + "plot/json_" + filter + "_spe_fpkm.txt");

			SpecialLineplot sl = new SpecialLineplot("Correlation curve for same and different tissue pairs",
					"Correlation Number", "Number of spots");
			sl.set_values(comp.get("#tt"), comp.get("#tat"));
			sl.setLegend("same tissue", "diff tissue");
			sl.plot(path + "plot/tt_vs_tat_" + filter + "_fpkm.png");

			SpecialLineplot sl1 = new SpecialLineplot("Correlation curve for same and different species pairs",
					"Correlation Number", "Number of spots");
			sl1.set_values(comp_spe.get("#oo"), comp_spe.get("#oao"));
			sl1.setLegend("same species", "diff species");
			sl1.plot(path + "plot/tt_vs_tat_" + filter + "_spe_fpkm.png");

			new Point_Analysis((Integer.parseInt(lowest.getQuery_geneId()) + 7000) + "",
					(Integer.parseInt(lowest.getTarget_geneId()) + 7000) + "", "FPKM", filter, true);
			File_Preparer.read_file_fpkm("files/" + lowest.getQuery_geneId() + "-" + filter + "FPKM.txt", fpkm_samples,
					comp, comp_spe);
			System.out.println(lowest.getQuery_geneId()+" "+lowest.getTarget_geneId());
			new Point_Analysis((Integer.parseInt(highest.getQuery_geneId()) + 7000) + "",
					(Integer.parseInt(highest.getTarget_geneId()) + 7000) + "", "FPKM", filter, true);
			File_Preparer.read_file_fpkm("files/" + highest.getQuery_geneId() + "-" + filter + "FPKM.txt", fpkm_samples,
					comp, comp_spe);
			System.out.println(highest.getQuery_geneId()+" "+highest.getTarget_geneId());
		}
	}

	private static void write_correlation(TreeMap<Double, Double> tt, TreeMap<Double, Double> tat, BufferedWriter bw)
			throws IOException {
		bw.write("#Correlation line plot\t");
		StringBuilder sb_x = new StringBuilder(), sb_y = new StringBuilder();
		String text = "";
		double mean1 = 0.0, mean2 = 0.0;
		for (double key : tt.keySet()) {
			sb_x.append(",").append(key);
			sb_y.append(",").append(tt.get(key));
			mean1 += key * tt.get(key);
		}
		sb_x.deleteCharAt(0);
		sb_y.deleteCharAt(0);
		mean1 /= tt.size();
		text += "#mean of tt\t" + mean1 + "\n#x " + sb_x.toString() + "\n#y " + sb_y.toString() + "\n";
		sb_x = new StringBuilder();
		sb_y = new StringBuilder();
		for (double key : tat.keySet()) {
			sb_x.append(",").append(key);
			sb_y.append(",").append(tat.get(key));
			mean2 += key * tat.get(key);
		}
		sb_x.deleteCharAt(0);
		sb_y.deleteCharAt(0);
		mean2 /= tat.size();
		text += "#mean of tat\t" + mean2 + "\n#x " + sb_x.toString() + "\n#y " + sb_y.toString() + "\n";
		bw.write(Math.abs(mean1 - mean2) + "\n" + text);
	}

	public static String systemInfoString() {
		String out = "[";
		out += (System.currentTimeMillis() - start) / 1000 + "." + (System.currentTimeMillis() - start) % 1000 + "s";
		out += "|" + ((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024d / 1024d
				/ 1024d * 1000d)) / 1000d + "GB]";
		return out;
	}
}
