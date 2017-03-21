package dennis.GO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.TreeSet;

public class GOmapping {

	/**
	 * GeneId, GOIds
	 */
	private HashMap<String, TreeSet<String>> GOperGene;

	/**
	 * GOId, GeneIds
	 */
	private HashMap<String, TreeSet<String>> genesPerGO;

	public GOmapping(String mappingFile) {
		GOperGene = new HashMap<>();
		genesPerGO = new HashMap<>();
		readGOmapping(mappingFile);
	}

	/**
	 * 
	 * @param geneId
	 * @return all directly mapped GOs(directly = most specific)
	 */
	public TreeSet<String> getGOsMappedToGene(String geneId) {
		return GOperGene.get(geneId);
	}

	/**
	 * 
	 * @param goId
	 * @return all genes directly mapped to GO
	 */
	public TreeSet<String> getGenesMappedToGoTerm(String goId) {
		return genesPerGO.get(goId);
	}

	public void readGOmapping(String mappingFile) {
		try {
			System.out.println("Reading GOmappingFile: " + mappingFile);
			BufferedReader br = new BufferedReader(new FileReader(new File(mappingFile)));
			String line = null;
			br.readLine();
			while ((line = br.readLine()) != null) {
				String[] split = line.split("\t");
				String[] goArray = split[2].split("[|]");
				TreeSet<String> gos = GOperGene.get(split[0]);
				if (gos == null) {
					gos = new TreeSet<>();
					GOperGene.put(split[0], gos);
				}
				for (String s : goArray) {
					gos.add(s);
					TreeSet<String> genes = genesPerGO.get(s);
					if (genes == null) {
						genes = new TreeSet<>();
						genesPerGO.put(s, genes);
					}
					genes.add(s);
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
