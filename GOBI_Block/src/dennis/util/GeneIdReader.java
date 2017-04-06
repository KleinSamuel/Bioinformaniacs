package dennis.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.LinkedList;

import dennis.genomeAnnotation.Gene;
import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;

public class GeneIdReader {

	public static LinkedList<String> get_gene_ids_in_GTF_order(Species sp) {

		LinkedList<String> geneIds = new LinkedList<>();

		String geneIdFile = UtilityManager.getConfig("gene_id_files") + sp.getId() + ".genes";

		File geneFile = new File(geneIdFile);
		if (!geneFile.exists()) {
			return write_gene_ids_in_gtf_order(sp);
		}
		try {

			BufferedReader br = new BufferedReader(new FileReader(geneFile));
			String geneId = null;
			while ((geneId = br.readLine()) != null) {
				geneIds.add(geneId);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		return geneIds;

	}

	public static LinkedList<String> write_gene_ids_in_gtf_order(Species s) {

		String geneIdFilePath = UtilityManager.getConfig("gene_id_files") + s.getId() + ".genes";

		File geneFile = new File(geneIdFilePath);
		geneFile.mkdirs();

		LinkedList<String> genes = new LinkedList<>();

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(geneFile));
			String geneId = null;
			for (Iterator<Gene> geneIt = s.getGenomeAnnotation().iterator(); geneIt.hasNext();) {
				geneId = geneIt.next().getId();
				bw.write(geneId + "\n");
				genes.add(geneId);
			}

			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return genes;
	}

}
