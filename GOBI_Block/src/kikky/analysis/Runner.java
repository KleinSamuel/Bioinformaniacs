package kikky.analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Runner {
	private static long start;

	public static void main(String[] args) throws InterruptedException {
		start = System.currentTimeMillis();
		if (args[1].equals("phaseone")) {
			try {
				String path = "/home/proj/biocluster/praktikum/genprakt-ws16/bioinformaniacs/Kikky/";
				String type = args[0];
				BufferedReader br = new BufferedReader(new FileReader(path + "plot/go_vals_all_" + type + ".txt"));
				BufferedWriter bw = new BufferedWriter(new FileWriter(path + "plot/vals_" + type + ".txt"));
				String line = br.readLine();
				int usedlines = 0;
				while ((line = br.readLine()) != null && ++usedlines < 101) {
					System.out.println(systemInfoString() + "current line: " + line);
					String[] split = line.split("\t");
					bw.write("###" + line + "###\n");
					if (type.equals("FPKM")) {
						Analysis.FPKM("phaseone", split[1]);
						Thread.sleep(10000);
						check_in_grid();
						Analysis.FPKM("phasetwo", split[1]);
						Thread.sleep(15000);
					} else if (type.equals("DEP")) {
						Analysis.DEP("phaseone", split[1]);
						Thread.sleep(10000);
						check_in_grid();
						Analysis.DEP("phasetwo", split[1]);
						Thread.sleep(15000);
					}
					write_info(bw, path + "plot/plot_vals_" + split[1] + "_" + type + ".txt");
					Process plotting;
					plotting = Runtime.getRuntime().exec("bash /home/a/adamowicz/GoBi/Block/results/Delete.sh " + path
							+ "plot/plot_vals_" + split[1] + "_" + type + ".txt");
					plotting.waitFor();
					bw.flush();
				}
				bw.close();
				br.close();
				System.out.println(systemInfoString() + "Terminated");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (args[1].equals("phasetwo")) {
			try {
				String path = "/home/proj/biocluster/praktikum/genprakt-ws16/bioinformaniacs/Kikky/";
				String type = args[0];
				BufferedReader br = new BufferedReader(new FileReader(path + "plot/vals_" + type + ".txt"));
				String line;
				while ((line = br.readLine()) != null) {
					
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void check_in_grid() throws InterruptedException, IOException {
		Process plotting;
		plotting = Runtime.getRuntime().exec("bash /home/a/adamowicz/GoBi/Block/results/checkAnalysis.sh");
		plotting.waitFor();
	}

	private static void write_info(BufferedWriter bw, String file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		while ((line = br.readLine()) != null) {
			if (line.startsWith("#Correlation")) {
				bw.write(line + "\n");
				bw.write(br.readLine() + "\n");
				bw.write(br.readLine() + "\n");
				br.readLine();
				br.readLine();
				bw.write(br.readLine() + "\n");
				bw.write(br.readLine() + "\n");
			}
		}
		br.close();
	}

	public static String systemInfoString() {
		String out = "[";
		out += (System.currentTimeMillis() - start) / 1000 + "." + (System.currentTimeMillis() - start) % 1000 + "s";
		out += "|" + ((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024d / 1024d
				/ 1024d * 1000d)) / 1000d + "GB]";
		return out;
	}
}
