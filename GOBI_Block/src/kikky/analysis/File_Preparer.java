package kikky.analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Vector;

import kikky.heatmap.Barplot;
import kikky.heatmap.Sample_Data;
import kikky.heatmap.Scatterplot;
 
public class File_Preparer {
	private final static String path = "/home/proj/biocluster/praktikum/genprakt-ws16/bioinformaniacs/Kikky/";

	public static Number[] read_file(String file, ArrayList<Sample_Data> values,
			HashMap<String, TreeMap<Double, Double>> comp, HashMap<String, TreeMap<Double, Double>> comp_spe,
			String type, HashMap<String, Integer> gos) {
		double value = 0;
		String[] x_genes = null, y_genes = null;
		Number[] matrix_row = new Number[values.size()];
		try {
			File f = new File(path + file);
			BufferedReader br = new BufferedReader(new FileReader(f));
			BufferedWriter bw = new BufferedWriter(new FileWriter(path + "info_files/" + type + "/" + f.getName()));
			bw.write("#Point_info\n");
			String sample_1 = "";
			String sample_2 = "";
			String line;
			int j = 0;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("#Pair")) {
					line = line.substring(6);
					String[] samples = line.split("-");
					sample_1 = values.get(Integer.parseInt(samples[0]) - 1).get_name();
					sample_2 = values.get(Integer.parseInt(samples[1]) - 1).get_name();
					bw.write(samples[0] + " " + sample_1 + "\n");
					bw.write(samples[1] + " " + sample_2 + "\n");
				}
				if (line.startsWith("#Heatmap_value")) {
					value = Double.parseDouble(br.readLine());
					String tissue_pair = br.readLine();
					String species_pair = br.readLine();
					double use_for_cor = round(value, 2);
					if (!comp.get(tissue_pair).containsKey(use_for_cor))
						comp.get(tissue_pair).put(use_for_cor, 0.0);
					comp.get(tissue_pair).put(use_for_cor, comp.get(tissue_pair).get(use_for_cor) + 1);
					if (!comp_spe.get(species_pair).containsKey(use_for_cor))
						comp_spe.get(species_pair).put(use_for_cor, 0.0);
					comp_spe.get(species_pair).put(use_for_cor, comp_spe.get(species_pair).get(use_for_cor) + 1);
					matrix_row[j++] = value;
					bw.write(value + "\n");
				}
				if (line.startsWith("#Scatterplot")) {
					Scatterplot sp = new Scatterplot(type + " distribution", sample_1, sample_2);
					sp.set_values(br.readLine().substring(3), br.readLine().substring(3));
					sp.set_log(true, true);
					sp.plot(path + "plot/" + type + "/" + f.getName().replace(type + ".txt", "Dist" + type + ".png"));
					x_genes = br.readLine().substring(9).split(",");
					y_genes = br.readLine().substring(9).split(",");
				}
				if (line.startsWith("#Percentage_mate_all")) {
					bw.write("#Percentage_mate_all\n");
					String used = br.readLine();
					bw.write(used);
					create_boxplot(sample_1, sample_2, used, f.getName(), "FPKM");
				}
				if (line.startsWith("#GOs")) {
					line = br.readLine();
					if (line != null && line.length() > 0)
						for (String s : line.split(",")) {
							if (!gos.containsKey(s))
								gos.put(s, 0);
							gos.put(s, gos.get(s) + 1);
						}
				}
			}

			br.close();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return matrix_row;
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
		bp.plot(path + "plot/" + type + "/" + f_name.replace(type + ".txt", "DistGenes.png"));
	}

	public static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}

}
