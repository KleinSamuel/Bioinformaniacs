package kikky.analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Calculator {

	/**
	 * Calculating fpkm values from rawcount file
	 * 
	 * @param count_file
	 *            file with the raw counts
	 * @param gene_file
	 *            file with gene_id and merged_tr_length (genlength without
	 *            introns)
	 */
	public static HashMap<String, Double> FPKM_generator(String count_file, String gene_file) {
		HashMap<String, Double> gene_rawcount = new HashMap<>();
		try {
			// Reading the count file + adding counts together
			BufferedReader br1 = new BufferedReader(new FileReader(count_file));
			String line;
			while ((line = br1.readLine()) != null) {
				String[] split = line.split("\t");
				gene_rawcount.put(split[0], Double.parseDouble(split[1]));
			}
			br1.close();
			// Turning count values to fpkm values
		} catch (IOException e) {
			e.printStackTrace();
		}
		return FPKM_generator(gene_rawcount, gene_file);
	}

	public static HashMap<String, Double> FPKM_generator(HashMap<String, Double> fpkm_data, String gene_file) {
		HashMap<String, Double> gene_data = new HashMap<>();
		try {
			double allcounts = 0;
			// Reading the gene_id to length file, for further calculations
			BufferedReader br1 = new BufferedReader(new FileReader(gene_file));
			String line;
			while ((line = br1.readLine()) != null) {
				String[] split = line.split("\t");
				gene_data.put(split[0], Double.parseDouble(split[1]));
			}
			br1.close();
			// Calculating all values
			for (double val : fpkm_data.values())
				allcounts += val;
			// Turning count values to fpkm values
			allcounts /= 1000000;
			for (String gene_id : fpkm_data.keySet()) {
				double fpkm_value = fpkm_data.get(gene_id) / (allcounts * (((double) gene_data.get(gene_id)) / 1000));
				fpkm_data.put(gene_id, fpkm_value);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fpkm_data;
	}

	public static double pearson_correlation(HashMap<String, Double> x_data, HashMap<String, Double> y_data,
			HashMap<String, String> mates) {
		double x = 0, y = 0, xy = 0, x2 = 0, y2 = 0;
		for (String gene_x_id : mates.keySet()) {
			double cur_x = x_data.get(gene_x_id);
			double cur_y = y_data.get(mates.get(gene_x_id));
			x += cur_x;
			y += cur_y;
			xy += (cur_x * cur_y);
			x2 += (cur_x * cur_x);
			y2 += (cur_y * cur_y);
		}
		double upper = (mates.size() * xy) - (x * y);
		double lower = Math.sqrt(((mates.size() * x2) - (x * x)) * ((mates.size() * y2) - (y * y)));
		return upper / lower;
	}
}
