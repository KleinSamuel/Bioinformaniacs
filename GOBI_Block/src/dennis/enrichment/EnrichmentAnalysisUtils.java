package dennis.enrichment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;

public class EnrichmentAnalysisUtils {

	public final static double default_threshold = 0.05;
	public static Comparator<GeneObject> comp;
	public static String valueOfInterest = "geneId";

	/**
	 * returns all genes under threshold; threshold can be null -> default <=
	 * 0.05 if(DEmethod == null) -> returns all gene under threshold in all
	 * DEMethods -> possible values DESeq, edgeR, limma; valueOfInterest =
	 * [fc|raw|adj|null]; default for sorting is geneId default for threshold is
	 * adj_pval <= 0.05
	 */
	public static TreeSet<GeneObject> getGenesUnderThreshold(Species s, String tissue1, String tissue2, String mapper,
			Double threshold, String DEmethod, String valueOfInterest, boolean pcrIndex0) {
		if (valueOfInterest != null) {
			EnrichmentAnalysisUtils.valueOfInterest = valueOfInterest.toLowerCase();
		}
		if (threshold == null) {
			threshold = default_threshold;
		}
		String t1 = tissue1, t2 = tissue2;
		if (tissue1.compareTo(tissue2) > 0) {
			t1 = tissue2;
			t2 = tissue1;
		}
		String fileName = t1 + "_" + t2;
		if (pcrIndex0)
			fileName += "_pcr0";
		if (DEmethod != null) {
			return getSubset(readDEfile(UtilityManager.getConfig("enrichment_output") + s.getId() + "/" + t1 + "_" + t2
					+ "/" + mapper + "/" + fileName + "." + DEmethod), threshold);
		} else {
			HashSet<GeneObject> geneNames = new HashSet<>();
			TreeSet<GeneObject> limmaGenes = getSubset(readDEfile(UtilityManager.getConfig("enrichment_output")
					+ s.getId() + "/" + t1 + "_" + t2 + "/" + mapper + "/" + fileName + ".limma"), threshold);
			geneNames.addAll(limmaGenes);
			TreeSet<GeneObject> deSeqGenes = getSubset(readDEfile(UtilityManager.getConfig("enrichment_output")
					+ s.getId() + "/" + t1 + "_" + t2 + "/" + mapper + "/" + fileName + ".DESeq"), threshold);
			HashSet<GeneObject> overlap = new HashSet<>();
			for (GeneObject go : deSeqGenes) {
				if (geneNames.contains(go))
					overlap.add(go);
			}
			geneNames = new HashSet<>(overlap);
			TreeSet<GeneObject> edgeRgenes = getSubset(readDEfile(UtilityManager.getConfig("enrichment_output")
					+ s.getId() + "/" + t1 + "_" + t2 + "/" + mapper + "/" + fileName + ".edgeR"), threshold);
			overlap = new HashSet<>();
			for (GeneObject go : edgeRgenes) {
				if (geneNames.contains(go))
					overlap.add(go);
			}
			TreeSet<GeneObject> genes = new TreeSet<>(getComparator());
			genes.addAll(overlap);
			return genes;
		}

	}

	public static TreeSet<GeneObject> readDEfile(String filePath) {
		TreeSet<GeneObject> genes = new TreeSet<>(getComparator());
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(filePath)));
			br.readLine();
			String line = null;

			while ((line = br.readLine()) != null) {
				genes.add(new GeneObject(line));
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return genes;
	}

	/**
	 * sortedSet should better be sorted ascending by valueOfInterest
	 * 
	 * @return subset of sortedSet containing all genes under the threshold
	 */
	public static TreeSet<GeneObject> getSubset(TreeSet<GeneObject> sortedSet, double threshold) {
		TreeSet<GeneObject> ret = new TreeSet<>(getComparator());
		for (Iterator<GeneObject> it = sortedSet.iterator(); it.hasNext();) {
			GeneObject go = it.next();
			if (isInThreshold(go, threshold)) {
				ret.add(go);
			} else {
				break;
			}
		}
		return ret;
	}

	public static boolean isInThreshold(GeneObject go, double threshold) {
		switch (valueOfInterest) {
		case "fc":
			return go.getLog2fc() <= threshold;
		case "adj":
			return go.getAdj_pval() <= threshold;
		case "raw":
			return go.getRaw_pval() <= threshold;
		default:
			return go.getAdj_pval() <= threshold;
		}
	}

	public static void setValueOfInterest(String valueOfInterest) {
		EnrichmentAnalysisUtils.valueOfInterest = valueOfInterest;
	}

	public static Comparator<GeneObject> getComparator() {
		if (comp == null) {
			comp = new Comparator<GeneObject>() {

				@Override
				public int compare(GeneObject o1, GeneObject o2) {
					switch (valueOfInterest) {
					case "fc":
						return Double.compare(o1.getLog2fc(), o2.getLog2fc());
					case "adj":
						return Double.compare(o1.getAdj_pval(), o2.getAdj_pval());
					case "raw":
						return Double.compare(o1.getRaw_pval(), o2.getRaw_pval());
					default:
						return o1.getName().compareTo(o2.getName());
					}
				}
			};
		}
		return comp;
	}
}
