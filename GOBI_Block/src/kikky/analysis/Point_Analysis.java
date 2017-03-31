package kikky.analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import dennis.tissues.Tissue;
import dennis.tissues.TissuePair;
import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;

public class Point_Analysis {
	private long start;
	private static final String path = "/home/proj/biocluster/praktikum/genprakt-ws16/bioinformaniacs/Kikky/";
	private static BufferedWriter bw;

	public static void main(String[] args) throws NumberFormatException, IOException {
		long real_start = System.currentTimeMillis();
		bw = new BufferedWriter(new FileWriter(path + "files/" + args[0] + "-" + args[4] + args[3] + ".txt"));
		bw.write("### used go: " + args[4] + " ###");
		for (int i = Integer.parseInt(args[1]); i <= Integer.parseInt(args[2]); i++) {
			bw.write("\n#Pair " + args[0] + "-" + i);
			new Point_Analysis(args[0], i + "", args[3], args[4], false);
		}
		bw.close();
		String out = "[";
		out += (System.currentTimeMillis() - real_start) / 1000 + "." + (System.currentTimeMillis() - real_start) % 1000
				+ "s";
		out += "|" + ((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024d / 1024d
				/ 1024d * 1000d)) / 1000d + "GB]";
		System.out.println(out + "Full Terminated");
	}

	public Point_Analysis(String first, String secound, String type, String filter, boolean clicked) {
		if (clicked) {
			try {
				bw = new BufferedWriter(
						new FileWriter(path + "files/" + first + "-" + secound + "-" + filter + type + ".txt"));
				bw.write("### used go: " + filter + " ###");
				bw.write("\n#Pair " + first + "-" + secound);
				analyse(first, secound, filter, clicked, type);
				bw.close();
			} catch (NumberFormatException | IOException e) {
				e.printStackTrace();
			}
		} else {
			analyse(first, secound, filter, clicked, type);
		}
	}

	public void analyse(String first, String secound, String filter, boolean clicked, String type) {
		try {
			start = System.currentTimeMillis();
			System.out.println(systemInfoString() + "Starting to generate partners");
			new UtilityManager("/home/a/adamowicz/git/Bioinformaniacs/GOBI_Block/ressources/config.txt", false, false,
					false);
			String data_path = UtilityManager.getConfig("output_directory");
			BufferedReader br = new BufferedReader(
					new FileReader("/home/a/adamowicz/GoBi/Block/results/" + type + ".info"));
			String line;
			Sample sd_query = null;
			Sample sd_target = null;
			while ((line = br.readLine()) != null) {
				if (line.startsWith(first+ "#")) {
					if (type.equals("FPKM"))
						sd_query = generate_FPKM_Sample(line, data_path, filter);
					if (type.equals("DEP"))
						sd_query = generate_DEP_Sample(line, filter);
				}
				if (line.startsWith(secound + "#")) {
					if (type.equals("FPKM"))
						sd_target = generate_FPKM_Sample(line, data_path, filter);
					if (type.equals("DEP"))
						sd_target = generate_DEP_Sample(line, filter);
				}
				if (sd_query != null && sd_target != null)
					break;
			}
			br.close();
			if (clicked) {
				sd_query.set_info(true);
				sd_target.set_info(true);
			}
			if (type.equals("FPKM")) {
				System.out.println(systemInfoString() + "Starting to calculate values to partner");
				System.out.println(systemInfoString() + ((FPKM_Single) sd_query).get_name() + " vs "
						+ ((FPKM_Single) sd_target).get_name());
				String temp = ((FPKM_Single) sd_query).get_value(((FPKM_Single) sd_target)) + "";
				System.out.println(temp);

				bw.write("\n#Heatmap_value\n" + temp);
				if (((FPKM_Single) sd_query).get_tissue().getName()
						.equals(((FPKM_Single) sd_target).get_tissue().getName()))
					bw.write("\n#tt");
				else
					bw.write("\n#tat");
			} else if (type.equals("DEP")) {
				System.out.println(systemInfoString() + "Starting to calculate values to partner");
				System.out.println(systemInfoString() + ((DE_Pairs) sd_query).get_name() + " vs "
						+ ((DE_Pairs) sd_target).get_name());
				String temp = ((DE_Pairs) sd_query).get_value(((DE_Pairs) sd_target)) + "";
				System.out.println(temp);

				bw.write("\n#Heatmap_value\n" + temp);
				if (((DE_Pairs) sd_query).get_tissuepair().equals(((DE_Pairs) sd_target).get_tissuepair()))
					bw.write("\n#tt");
				else
					bw.write("\n#tat");
			}
			if (sd_query.get_species_ID() == sd_target.get_species_ID())
				bw.write("\n#oo");
			else
				bw.write("\n#oao");
			if (filter.equals("all")) {
				bw.write("\n#GOs");
				bw.write("\n" + sd_query.gos_asString());
			}
			Point_Info pInfo = sd_query.get_point_info();
			bw.write(pInfo.get_point_info_text());
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(systemInfoString() + "Terminated");
	}

	public FPKM_Single generate_FPKM_Sample(String line, String data_path, String filter) {
		String[] split = line.split("\t");
		int organism_id = Integer.parseInt(split[1]);
		Species s = new Species(organism_id, split[2], split[3], split[4], null);
		String tissue = split[5];
		String exp = split[6];
		String map = "star";
		String path = data_path + organism_id + "/" + tissue + "/" + exp + "/" + map + "/fpkm.counts";
		return new FPKM_Single(s, new Tissue(tissue), exp, path, filter);
	}

	public DE_Pairs generate_DEP_Sample(String line, String filter) {
		String[] split = line.split("\t");
		int organism_id = Integer.parseInt(split[1]);
		Species s = new Species(organism_id, split[2], split[3], split[4], null);
		TissuePair tissues = new TissuePair(new Tissue(split[5]), new Tissue(split[6]));
		String path = split[7];
		return new DE_Pairs(s, tissues, path, filter);
	}

	public String systemInfoString() {
		String out = "[";
		out += (System.currentTimeMillis() - start) / 1000 + "." + (System.currentTimeMillis() - start) % 1000 + "s";
		out += "|" + ((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024d / 1024d
				/ 1024d * 1000d)) / 1000d + "GB]";
		return out;
	}
}
