package dennis.counter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

public class CounterUtils {

	// doubles werden abgerundet
	public static void createAverageCountFile(Collection<String> countFiles, String outputFile) {
		HashMap<String, Integer[]> counts = new HashMap<>();
		LinkedList<String> inputReihenfolge = new LinkedList<>();
		for (String s : countFiles) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(new File(s)));
				String line = null;
				String[] split = null;
				br.readLine(); // skip header

				while ((line = br.readLine()) != null) {
					split = line.split("\t");
					inputReihenfolge.add(split[0]);
					Integer[] countArr = counts.get(split[0]);
					if (countArr == null) {
						countArr = parseCounts(split);
						counts.put(split[0], countArr);
					} else {
						sumIntArray(countArr, parseCounts(split));
					}
				}
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		for (Integer[] countArr : counts.values()) {
			divideArr(countArr, countFiles.size());
		}
		writeOutput(outputFile, counts, inputReihenfolge);
	}

	public static void writeOutput(String outputFile, HashMap<String, Integer[]> counts,
			LinkedList<String> inputReihenfolge) {
		try {
			File f = new File(outputFile);
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			bw.write("geneId\tnonAmbigousAnyPcr\tnonAmbigousPcr0\tambigousWeightedAnyPcr\tambigousWeightedPcr0\n");
			for (String s : inputReihenfolge) {
				Integer[] countArr = counts.get(s);
				bw.write(s + "\t" + countArr[0] + "\t" + countArr[1] + "\t" + countArr[2] + "\t" + countArr[3] + "\n");
			}
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Integer[] parseCounts(String[] line) {
		Integer[] ret = new Integer[4];
		for (int i = 0; i < 4; i++) {
			ret[i] = Integer.parseInt(line[i + 1]);
		}
		return ret;
	}

	public static void divideArr(Integer[] arr, int divider) {
		for (int i = 0; i < arr.length; i++) {
			arr[i] /= divider;
		}
	}

	public static void sumIntArray(Integer[] a, Integer[] b) {
		for (int i = 0; i < a.length; i++) {
			a[i] = a[i] + b[i];
		}
	}

}
