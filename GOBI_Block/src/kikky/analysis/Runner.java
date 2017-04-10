package kikky.analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
						Analysis.FPKM("phaseone", split[1], "false");
						Thread.sleep(10000);
						check_in_grid();
						Analysis.FPKM("phasetwo", split[1], "false");
						Thread.sleep(15000);
					} else if (type.equals("DEP")) {
						Analysis.DEP("phaseone", split[1], "false");
						Thread.sleep(10000);
						check_in_grid();
						Analysis.DEP("phasetwo", split[1], "false");
						Thread.sleep(15000);
					}else if (type.equals("DES")) {
						Analysis.DES("phaseone", split[1], "false");
						Thread.sleep(10000);
						check_in_grid();
						Analysis.DES("phasetwo", split[1], "false");
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
				HashMap<String, Double> vals = new HashMap<>();
				BufferedReader br = new BufferedReader(new FileReader(path + "plot/vals_" + type + ".txt"));
				String line;
				while ((line = br.readLine()) != null) {
					String header = line;
					double tissue = Double.parseDouble(br.readLine().split("\t")[1]);
					for (int i = 0; i < 4; i++)
						br.readLine();
					double spec = Double.parseDouble(br.readLine().split("\t")[1]);
					for (int i = 0; i < 4; i++)
						br.readLine();
					vals.put(header, tissue - spec);
				}
				br.close();
				Map<String, Double> sorted = sortByValue(vals);
				for (String key : sorted.keySet())
					System.out.println(key + "\t" + sorted.get(key));
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

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
}
