package kikky.analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Vector;

import dennis.GO.GOHandler;
import dennis.utility_manager.Species;
import kikky.heatmap.Barplot;
import kikky.heatmap.Sample_Data;
import kikky.heatmap.Scatterplot;

public class File_Preparer {
	public static double read_file_fpkm(String file, ArrayList<Sample_Data> values, Species x_s, Species y_s) {
		double value = 0;
		String[] x_genes = null, y_genes = null;
		try {
			File f = new File("/home/a/adamowicz/GoBi/Block/results/" + file);
			BufferedReader br = new BufferedReader(new FileReader(f));
			BufferedWriter bw = new BufferedWriter(
					new FileWriter("/home/a/adamowicz/GoBi/Block/results/info_files/FPKM/" + f.getName()));
			bw.write("#Point_info\n");
			String[] samples = (f.getName().substring(0, f.getName().length() - 8)).split("-");
			String sample_1 = values.get(Integer.parseInt(samples[0])).get_name();
			String sample_2 = values.get(Integer.parseInt(samples[1])).get_name();
			bw.write(samples[0] + " " + sample_1 + "\n");
			bw.write(samples[1] + " " + sample_2 + "\n");
			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("#Heatmap_value")) {
					value = Double.parseDouble(br.readLine());
				}
				if (line.startsWith("#Scatterplot")) {
					Scatterplot sp = new Scatterplot("FPKM distribution", sample_1, sample_2);
					sp.set_values(br.readLine().substring(3), br.readLine().substring(3));
					sp.set_log(true, true);
					sp.plot("/home/a/adamowicz/GoBi/Block/results/info_files/FPKM/"
							+ f.getName().replace("FPKM.txt", "DistFPKM.png"));
					x_genes = br.readLine().substring(9).split(",");
					y_genes = br.readLine().substring(9).split(",");
				}
				if (line.startsWith("#Percentage_mate_all")) {
					bw.write("#Percentage_mate_all\n");
					String used = br.readLine();
					bw.write(used);
					create_boxplot(sample_1, sample_2, used, f.getName());
				}
			}
			HashMap<String, LinkedList<String>> x_go = create_gomapping(x_genes, x_s);
			HashMap<String, LinkedList<String>> y_go = create_gomapping(y_genes, y_s);
			HashSet<String> all_gos = new HashSet<String>();
			all_gos.addAll(x_go.keySet());
			all_gos.addAll(y_go.keySet());
			bw.write("#GO Mapping");
			StringBuilder sb = new StringBuilder();
			for (String goterm : all_gos) {
				bw.write("\n#" + goterm);
				if (x_go.containsKey(goterm)) {
					for (String gene_id : x_go.get(goterm)) {
						sb.append(",").append(gene_id);
					}
					bw.write("\n#x " + sb.substring(1).toString());
					sb.setLength(0);
				}
				if (y_go.containsKey(goterm)) {
					for (String gene_id : y_go.get(goterm)) {
						sb.append(",").append(gene_id);
					}
					bw.write("\n#y " + sb.substring(1).toString());
					sb.setLength(0);
				}
			}
			br.close();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return value;
	}

	private static HashMap<String, LinkedList<String>> create_gomapping(String[] genes, Species s) {
		HashMap<String, LinkedList<String>> gos = new HashMap<>();
		for (String x_gene : genes) {
			for (String go : GOHandler.getMappedGOterms(s, x_gene)) {
				if (!gos.containsKey(go))
					gos.put(go, new LinkedList<String>());
				gos.get(go).add(x_gene);
			}
		}
		return gos;
	}

	private static void create_boxplot(String sample_1, String sample_2, String used, String f_name) {
		used = used.replace("query=", "");
		used = used.replace("target=", "");
		String[] split = used.split(" ");
		Barplot bp = new Barplot("Used gene_ids", "", "Number of genes");
		Vector<Double> vals = new Vector<>();
		vals.add(Double.parseDouble(split[0].split("[|]")[1]));
		vals.add(Double.parseDouble(split[0].split("[|]")[0]));
		vals.add(Double.parseDouble(split[1].split("[|]")[1]));
		bp.set_values(vals);
		bp.setnames("\"" + sample_1 + "\",\"both\",\"" + sample_2 + "\"", 3);
		bp.plot("/home/a/adamowicz/GoBi/Block/results/info_files/FPKM/" + f_name.replace("FPKM.txt", "DistGenes.png"));
	}

}
