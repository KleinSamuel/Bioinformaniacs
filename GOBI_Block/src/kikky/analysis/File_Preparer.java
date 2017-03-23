package kikky.analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import kikky.heatmap.Barplot;
import kikky.heatmap.Sample_Data;
import kikky.heatmap.Scatterplot;

public class File_Preparer {
	public static double read_file_fpkm(String file, ArrayList<Sample_Data> values) {
		double value = 0;
		String[] x_genes, y_genes;
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
			br.close();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return value;
	}

	public static void create_boxplot(String sample_1, String sample_2, String used, String f_name) {
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
