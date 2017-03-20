package dennis.enrichment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import dennis.tissues.TissuePair;
import dennis.utility_manager.Experiment;
import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;

public class EBUtils {

	public static void runEBForAllTissuePairsAndMappers(Species s) {
		for (Iterator<String> mapper = UtilityManager.mapperIterator(); mapper.hasNext();) {
			runEBForAllTissuePairs(s, mapper.next());
		}
	}

	public static void runEBForAllTissuePairs(Species s, String mapper) {
		for (Iterator<TissuePair> tissuePairIterator = UtilityManager.tissuePairIterator(s); tissuePairIterator
				.hasNext();) {
			TissuePair tp = tissuePairIterator.next();
			LinkedList<String> filesT1 = new LinkedList<>(), filesT2 = new LinkedList<>();
			for (Experiment e : tp.getKey().getExperiments()) {
				filesT1.add(UtilityManager.getConfig("output_directory") + s.getId() + "/" + tp.getKey().getName() + "/"
						+ e.getName() + "/" + mapper + "/gene.counts");
			}
			for (Experiment e : tp.getValue().getExperiments()) {
				filesT2.add(UtilityManager.getConfig("output_directory") + s.getId() + "/" + tp.getValue().getName()
						+ "/" + e.getName() + "/" + mapper + "/gene.counts");
			}
			runEnrichment(filesT1, filesT2,
					UtilityManager.getConfig("enrichment_output") + s.getId() + "/" + tp.getKey().getName() + "_"
							+ tp.getValue().getName() + "/" + mapper + "/",
					tp.getKey().getName() + "_" + tp.getValue().getName(), false);
			runEnrichment(filesT1, filesT2,
					UtilityManager.getConfig("enrichment_output") + s.getId() + "/" + tp.getKey().getName() + "_"
							+ tp.getValue().getName() + "/" + mapper + "/",
					tp.getKey().getName() + "_" + tp.getValue().getName(), true);
		}
	}

	/**
	 * better call runEnrichment!
	 * 
	 * @return map of the paths zu den outputFiles: expr -> expressionFile; feat
	 *         -> featureFile; pheno -> phenotype info
	 * 
	 * @param fileName
	 *            the name of the file without endings... file endings will be
	 *            added by the programm
	 */
	public static HashMap<String, String> createInputDataForEBAndRunEB(Collection<String> countFilesCond1,
			Collection<String> countFilesCond2, String outputDir, String fileName, boolean pcrIndexZero) {

		HashMap<String, String> ret = new HashMap<>();

		if (pcrIndexZero) {
			fileName += "_pcr0";
		}

		new File(outputDir).mkdirs();

		HashMap<String, Integer[]> counts = new HashMap<>();
		LinkedList<String> featureList = new LinkedList<>();
		Collection<String> countFiles = new LinkedList<>();
		countFiles.addAll(countFilesCond1);
		countFiles.addAll(countFilesCond2);
		int counter = 0;
		for (String s : countFiles) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(new File(s)));
				String line = null;
				String[] split = null;
				br.readLine(); // skip header

				while ((line = br.readLine()) != null) {
					split = line.split("\t");
					featureList.add(split[0]);
					Integer[] countArr = counts.get(split[0]);
					if (countArr == null) {
						countArr = new Integer[countFiles.size()];
						counts.put(split[0], countArr);
					}
					if (pcrIndexZero) {
						countArr[counter] = Integer.parseInt(split[2]);
					} else {
						countArr[counter] = Integer.parseInt(split[1]);
					}
				}
				br.close();
				counter++;
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}

		writePhenotype(countFilesCond1, countFilesCond2, outputDir, fileName);
		ret.put("pheno", outputDir + "/" + fileName + ".pdat");

		writeFeatures(featureList, outputDir, fileName);
		ret.put("feat", outputDir + "/" + fileName + ".fdat");

		writeExpression(featureList, counts, outputDir, fileName);
		ret.put("expr", outputDir + "/" + fileName + ".expr");

		return ret;

	}

	/**
	 * 
	 * bitte nur files eines organismus... sonst crashed der mist und sinn
	 * machts auch keinen
	 * 
	 * @param countFilesCond1:
	 *            count files von condition 1
	 * @param countFilesCond2:
	 *            count files von condition 2
	 * @param outputDir:
	 *            UtilityManager.getConfig("output_directory") + ...
	 * @param fileName:
	 *            fileName ohne Endung... Name wird für feature, expression und
	 *            phenotype verwendet... endungen werden angehängt
	 * @param pcrIndexZero
	 */
	public static void runEnrichment(Collection<String> countFilesCond1, Collection<String> countFilesCond2,
			String outputDir, String fileName, boolean pcrIndexZero) {
		runEB(createInputDataForEBAndRunEB(countFilesCond1, countFilesCond2, outputDir, fileName, pcrIndexZero),
				outputDir, fileName);
	}

	public static void writeFeatures(LinkedList<String> featureList, String outputDir, String fileName) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputDir + "/" + fileName + ".fdat")));

			for (String s : featureList) {
				bw.write(s + "\t" + s + "\n");
			}

			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void writeExpression(Collection<String> features, HashMap<String, Integer[]> counts, String outputDir,
			String fileName) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputDir + "/" + fileName + ".expr")));

			for (String s : features) {
				StringBuilder sb = new StringBuilder();
				sb.append(s + "\t");
				for (int i : counts.get(s)) {
					sb.append(i + "\t");
				}
				sb.deleteCharAt(sb.length() - 1);
				sb.append("\n");
				bw.write(sb.toString());
			}

			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void writePhenotype(Collection<String> countFilesCond1, Collection<String> countFilesCond2,
			String outputDir, String fileName) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputDir + "/" + fileName + ".pdat")));
			for (int i = 1; i <= countFilesCond1.size(); i++) {
				bw.write("cond1.rep" + i + "\t0\n");
			}
			for (int j = 1; j <= countFilesCond2.size(); j++) {
				bw.write("cond2.rep" + j + "\t1\n");
			}
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * call: Rscript de_rseq.R
	 * <exprs.file> <pdat.file> <fdat.file> <de.method> <out.file> better call
	 * runEnrichment
	 */
	// ret hashmap limma -> outputfile
	public static void runEB(HashMap<String, String> inputFilePaths, String outputDir, String fileName) {
		try {
			for (Iterator<String> method = UtilityManager.DEmethodIterator(); method.hasNext();) {
				String m = method.next();
				Process p = Runtime.getRuntime()
						.exec(UtilityManager.getConfig("R_path") + " " + UtilityManager.getConfig("EB_script") + " "
								+ inputFilePaths.get("expr") + " " + inputFilePaths.get("pheno") + " "
								+ inputFilePaths.get("feat") + " " + m + " " + outputDir + "/" + fileName + "." + m);
				BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line = null;
				while ((line = in.readLine()) != null) {
					System.out.println(line);
				}
				in.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
