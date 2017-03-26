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
	private long start;
	private static final String path = "/home/proj/biocluster/praktikum/genprakt-ws16/bioinformaniacs/Kikky/";
	private static BufferedWriter bw;

	public static void main(String[] args) throws NumberFormatException, IOException {
		long real_start = System.currentTimeMillis();

		bw = new BufferedWriter(
				new FileWriter(path + "files/" + (Integer.parseInt(args[0]) - 7000) + "-" + args[4] + "FPKM.txt"));
		bw.write("### used go: " + args[4] + " ###");
		for (int i = Integer.parseInt(args[1]); i <= Integer.parseInt(args[2]); i++) {
			bw.write("\n#Pair " + (Integer.parseInt(args[0]) - 7000) + "-" + (i - 7000));
			new Point_Analysis(args[0], i + "", args[3], args[4]);
		}
		bw.close();
		String out = "[";
		out += (System.currentTimeMillis() - real_start) / 1000 + "." + (System.currentTimeMillis() - real_start) % 1000
				+ "s";
		out += "|" + ((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024d / 1024d
				/ 1024d * 1000d)) / 1000d + "GB]";
		System.out.println(out + "Full Terminated");
	}

	public Point_Analysis(String first, String secound, String type, String filter) {
		if (type.equals("FPKM"))
			FPKM(first, secound, filter);
		else if (type.equals("DEP")) {
		}

	}

	public void FPKM(String first, String secound, String filter) {
		try {
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
				if (line.startsWith((Integer.parseInt(first) - 7000) + "#")) {
					sd_query = generate_Sample(line, data_path, filter);
				}
				if (line.startsWith((Integer.parseInt(secound) - 7000) + "#")) {
					sd_target = generate_Sample(line, data_path, filter);
				}
				if (sd_query != null && sd_target != null)
					break;
			}
			br.close();
			System.out.println(systemInfoString() + "Starting to calculate values to partner");
			System.out.println(systemInfoString() + sd_query.get_name() + " vs " + sd_target.get_name());
			String temp = sd_query.get_value(sd_target) + "";
			System.out.println(temp);

			bw.write("\n#Heatmap_value\n" + temp);
			if (((FPKM_Single) sd_query).get_tissue().equals(((FPKM_Single) sd_target).get_tissue()))
				bw.write("\n#tt");
			else
				bw.write("\n#tat");
			// Point_Info pInfo = sd_query.get_point_info();
			// bw.write(pInfo.get_point_info_text());
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(systemInfoString() + "Terminated");
	}

	public FPKM_Single generate_Sample(String line, String data_path, String filter) {
		String[] split = line.split("\t");
		int organism_id = Integer.parseInt(split[1]);
		Species s = new Species(organism_id, split[2], split[3], split[4], null);
		String tissue = split[5];
		String exp = split[6];
		String map = "star";
		String path = data_path + organism_id + "/" + tissue + "/" + exp + "/" + map + "/fpkm.counts";
		return new FPKM_Single(s, tissue, exp, path, filter);
	}

	public String systemInfoString() {
		String out = "[";
		out += (System.currentTimeMillis() - start) / 1000 + "." + (System.currentTimeMillis() - start) % 1000 + "s";
		out += "|" + ((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024d / 1024d
				/ 1024d * 1000d)) / 1000d + "GB]";
		return out;
	}
}
