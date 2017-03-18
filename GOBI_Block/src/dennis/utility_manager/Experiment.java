package dennis.utility_manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Experiment {

	private String name;
	private int reads = -1;
	private boolean paired = false, strandSpec = false;

	public Experiment(String dir, Species sp) {
		name = dir;
		parseXml(sp);
	}

	public void parseXml(Species s) {
		try {
			BufferedReader br = new BufferedReader(
					new FileReader(new File(UtilityManager.getConfig("main_bam_directory") + "/" + s.getId() + "/"
							+ name + "/rnaseq_info.xml")));

			String line = null;

			line = br.readLine();
			String[] split = line.split("\\s+");

			String token = split[1].split("=\"")[1];
			reads = Integer.parseInt(token.substring(0, token.length() - 1));

			line = br.readLine().trim();

			split = line.split("\\s+");
			token = split[1].split("=\"")[1];
			paired = Boolean.parseBoolean(token.substring(0, token.length() - 1));

			token = split[2].split("=\"")[1];
			strandSpec = Boolean.parseBoolean(token.substring(0, token.length() - 1));

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public String getName() {
		return name;
	}

	public int getReads() {
		return reads;
	}

	public boolean isPaired() {
		return paired;
	}

	public boolean isStrandSpec() {
		return strandSpec;
	}

}
