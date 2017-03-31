package kikky.analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Runner {
	public static void main(String[] args) throws InterruptedException {
		try {
			String path = "/home/proj/biocluster/praktikum/genprakt-ws16/bioinformaniacs/Kikky/";
			String type = args[0];
			BufferedReader br = new BufferedReader(new FileReader(path + "plots/go_vals_all_" + type + ".txt"));
			BufferedWriter bw = new BufferedWriter(new FileWriter(path + "plots/vals_" + type + ".txt"));
			String line = br.readLine();
			int usedlines = 0;
			while ((line = br.readLine()) != null && ++usedlines < 21) {
				String[] split = line.split("\t");
				bw.write("###" + line + "###");
				if (type.equals("FPKM")) {
					Analysis.FPKM("phaseone", split[1]);
					Analysis.FPKM("phasetwo", split[1]);
				}
				write_info(bw, path + "plot/plot_vals_" + split[1] + "_" + type + ".txt\n");
				Process plotting;
				plotting = Runtime.getRuntime().exec("bash /home/a/adamowicz/Gobi/Block/results/Delete.sh " + path
						+ "plot/plot_vals_" + split[1] + "_" + type + ".txt\n");
				plotting.waitFor();
			}
			bw.close();
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void write_info(BufferedWriter bw, String file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		while ((line = br.readLine()) != null) {
			if (line.startsWith("#Correlation line plot")) {
				bw.write(line + "\n");
				for (int i = 1; i <= 6; i++)
					bw.write(br.readLine() + "\n");
			}
		}
		br.close();
	}
}
