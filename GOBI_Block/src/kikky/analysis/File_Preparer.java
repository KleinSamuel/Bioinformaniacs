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
import java.util.TreeSet;
import java.util.Vector;

import dennis.GO.GOHandler;
import dennis.utility_manager.Species;
import kikky.heatmap.Barplot;
import kikky.heatmap.Sample_Data;
import kikky.heatmap.Scatterplot;

public class File_Preparer {
	private final static String path = "/home/proj/biocluster/praktikum/genprakt-ws16/bioinformaniacs/Kikky/";

	public static Number[] read_file_fpkm(String file, ArrayList<Sample_Data> values,
			HashMap<String, ArrayList<Double>> comp) {
		double value = 0;
		String[] x_genes = null, y_genes = null;
		Number[] matrix_row = new Number[values.size()];
		try {
			File f = new File(path + file);
			BufferedReader br = new BufferedReader(new FileReader(f));
			BufferedWriter bw = new BufferedWriter(new FileWriter(path + "info_files/FPKM/" + f.getName()));
			bw.write("#Point_info\n");
			String sample_1 = "";
			String sample_2 = "";
			String line;
			int j = 0;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("#Pair")) {
					line = line.substring(5);
					String[] samples = line.split("-");
					bw.write(samples[0] + " " + sample_1 + "\n");
					bw.write(samples[1] + " " + sample_2 + "\n");
					sample_1 = values.get(Integer.parseInt(samples[0]) - 1).get_name();
					sample_2 = values.get(Integer.parseInt(samples[1]) - 1).get_name();
				}
				if (line.startsWith("#Heatmap_value")) {
					value = Double.parseDouble(br.readLine());
					comp.get(br.readLine()).add(value);
					matrix_row[j++] = value;
					bw.write(value + "\n");
				}
				if (line.startsWith("+Scatterplot")) {
					Scatterplot sp = new Scatterplot("FPKM distribution", sample_1, sample_2);
					sp.set_values(br.readLine().substring(3), br.readLine().substring(3));
					sp.set_log(true, true);
					sp.plot(path + "info_files/FPKM/" + f.getName().replace("FPKM.txt", "DistFPKM.png"));
					x_genes = br.readLine().substring(9).split(",");
					y_genes = br.readLine().substring(9).split(",");
				}
				if (line.startsWith("#Percentage_mate_all")) {
					bw.write("#Percentage_mate_all\n");
					String used = br.readLine();
					bw.write(used);
					create_boxplot(sample_1, sample_2, used, f.getName(), "FPKM");
				}
			}
			// HashSet<String> all_gos = new HashSet<String>();
			// HashMap<String, LinkedList<String>> x_go = new HashMap<>();
			// HashMap<String, LinkedList<String>> y_go = new HashMap<>();
			// if (x_genes != null)
			// x_go = create_gomapping(x_genes, x_s);
			// if (y_genes != null)
			// y_go = create_gomapping(y_genes, y_s);
			// all_gos.addAll(x_go.keySet());
			// all_gos.addAll(y_go.keySet());
			// bw.write("\n#GO Mapping");
			// StringBuilder sb = new StringBuilder();
			// for (String goterm : all_gos) {
			// bw.write("\n#" + goterm);
			// if (x_go.containsKey(goterm)) {
			// for (String gene_id : x_go.get(goterm)) {
			// sb.append(",").append(gene_id);
			// }
			// bw.write("\n#x " + sb.substring(1).toString());
			// sb.setLength(0);
			// }
			// if (y_go.containsKey(goterm)) {
			// for (String gene_id : y_go.get(goterm)) {
			// sb.append(",").append(gene_id);
			// }
			// bw.write("\n#y " + sb.substring(1).toString());
			// sb.setLength(0);
			// }
			// }
			br.close();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return matrix_row;
	}

	private static HashMap<String, LinkedList<String>> create_gomapping(String[] genes, Species s) {
		HashMap<String, LinkedList<String>> gos = new HashMap<>();
		for (String x_gene : genes) {
			TreeSet<String> mapped_gos = GOHandler.getMappedGOterms(s, x_gene);
			if (mapped_gos != null)
				for (String go : mapped_gos) {
					if (!gos.containsKey(go))
						gos.put(go, new LinkedList<String>());
					gos.get(go).add(x_gene);
				}
		}
		return gos;
	}

	private static void create_boxplot(String sample_1, String sample_2, String used, String f_name, String type) {
		used = used.replace("query=", "");
		used = used.replace("target=", "");
		String[] split = used.split(" ");
		Barplot bp = new Barplot("Used gene_ids", "", "Number of genes");
		bp.set_boolean(true);
		Vector<Double> vals = new Vector<>();
		vals.add(Double.parseDouble(split[0].split("[|]")[1]));
		double in_both = Double.parseDouble(split[0].split("[|]")[0]);
		vals.add(in_both);
		vals.add(Double.parseDouble(split[1].split("[|]")[1]));
		bp.set_values(vals);
		bp.setnames("\"" + sample_1 + "\",\"both\",\"" + sample_2 + "\"", 3);
		bp.plot(path + "info_files/" + type + "/" + f_name.replace(type + ".txt", "DistGenes.png"));
	}

}
