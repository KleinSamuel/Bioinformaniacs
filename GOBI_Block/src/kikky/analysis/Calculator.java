package kikky.analysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;

import dennis.counter.CounterUtils;

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
		HashMap<String, Double> gene_rawcount = CounterUtils.readCountFile(count_file, false, false, true, false);
		return FPKM_generator(gene_rawcount, gene_file);
	}

	public static HashMap<String, Double> FPKM_generator(HashMap<String, Double> gene_rawcount, String gene_file) {
		HashMap<String, Double> gene_data = new HashMap<>();
		HashMap<String, Double> fpkm_data = new HashMap<>();
		try {
			double allcounts = 0;
			// Reading the gene_id to length file, for further calculations
			BufferedReader br1 = new BufferedReader(new FileReader(gene_file));
			String line = br1.readLine();
			while ((line = br1.readLine()) != null) {
				String[] split = line.split("\t");
				gene_data.put(split[0], Double.parseDouble(split[1]));
			}
			br1.close();
			// Calculating all values
			for (double val : gene_rawcount.values())
				allcounts += val;
			// Turning count values to fpkm values
			allcounts /= 1000000;
			for (String gene_id : gene_rawcount.keySet()) {
				if (gene_data.get(gene_id) != null && gene_rawcount.get(gene_id) != 0.0) {
					double fpkm_value = gene_rawcount.get(gene_id)
							/ (allcounts * (((double) gene_data.get(gene_id)) / 1000));
					fpkm_data.put(gene_id, fpkm_value);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fpkm_data;
	}

}
