package kikky.analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;
import kikky.heatmap.Sample_Data;

public class Point_Analysis {
	private static long start;

	public static void main(String[] args) throws NumberFormatException, IOException {
		start = System.currentTimeMillis();
		System.out.println(systemInfoString() + "Starting to generate partners");
		new UtilityManager("/home/a/adamowicz/git/Bioinformaniacs/GOBI_Block/ressources/config.txt", false, false,
				false);
		String data_path = UtilityManager.getConfig("output_directory");
		BufferedReader br = new BufferedReader(new FileReader("/home/a/adamowicz/GoBi/Block/results/fpkm.info"));
		String line;
		Sample_Data sd_query = null;
		Sample_Data sd_target = null;
		while ((line = br.readLine()) != null) {
			if (line.startsWith((Integer.parseInt(args[0]) - Integer.parseInt(args[2])) + "#")) {
				String[] split = line.split("\t");
				int organism_id = Integer.parseInt(split[1]);
				Species s = new Species(organism_id, split[2], split[3], split[4], null);
				String tissue = split[5];
				String exp = split[6];
				String map = "star";
				String path = data_path + organism_id + "/" + tissue + "/" + exp + "/" + map + "/fpkm.counts";
				sd_query = new FPKM_Single(s, tissue, exp, path);
			}
			if (line.startsWith((Integer.parseInt(args[1]) - 7000) + "#")) {
				String[] split = line.split("\t");
				int organism_id = Integer.parseInt(split[1]);
				Species s = new Species(organism_id, split[2], split[3], split[4], null);
				String tissue = split[5];
				String exp = split[6];
				String map = "star";
				String path = data_path + organism_id + "/" + tissue + "/" + exp + "/" + map + "/fpkm.counts";
				sd_target = new FPKM_Single(s, tissue, exp, path);
			}
			if (sd_query != null && sd_target != null)
				break;
		}
		br.close();
		System.out.println(systemInfoString() + "Starting to calculate values to partner");
		System.out.println(systemInfoString() + sd_query.get_name() + " vs " + sd_target.get_name());
		String temp = sd_query.get_value(sd_target) + "";
		System.out.println(temp);
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("/home/a/adamowicz/GoBi/Block/results/files/"
					+ (Integer.parseInt(args[0]) - Integer.parseInt(args[2])) + "-" + (Integer.parseInt(args[1]) - 7000)
					+ "FPKM.txt"));
			bw.write(temp + "\n");
			Point_Info pInfo = sd_query.get_point_info();
			bw.write(pInfo.get_point_info_text());
			bw.close();
			System.out.println((Integer.parseInt(args[0]) - Integer.parseInt(args[2])) + "-"
					+ (Integer.parseInt(args[1]) - 7000) + "FPKM.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(systemInfoString() + "Terminated");
	}

	public static String systemInfoString() {
		String out = "[";
		out += (System.currentTimeMillis() - start) / 1000 + "." + (System.currentTimeMillis() - start) % 1000 + "s";
		out += "|" + ((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024d / 1024d
				/ 1024d * 1000d)) / 1000d + "GB]";
		return out;
	}
}
