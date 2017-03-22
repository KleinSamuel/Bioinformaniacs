package sam.mapper_comparison;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.util.Pair;

import debugStuff.DebugMessageFactory;
import dennis.enrichment.EnrichmentAnalysisUtils;
import dennis.enrichment.GeneObject;
import dennis.similarities.SimilarityHandler;
import dennis.tissues.Tissue;
import dennis.tissues.TissueHandler;
import dennis.utility_manager.Species;
import dennis.utility_manager.UtilityManager;
import sam.utils.ExternalWriter;

public class HeatmapFactory {

	private ArrayList<MapperxMethodPair> mapperDePairList;

	private final String PATH_TO_EB_FILES = "/home/proj/biocluster/praktikum/genprakt/bioinformaniacs/EB/";
	private final String PATH_TO_HEATMAP_OUTPUT = "/home/proj/biocluster/praktikum/genprakt/bioinformaniacs/sam/";
	
	private ExternalWriter extBW_heatmapsInfo;
	private ExternalWriter extBW_heatmapsContent;

	private HashMap<Species, HashMap<Species, HashSet<String>>> orthologeGenesPerSpecies;
	private ArrayList<Species> speciesList;

	public HeatmapFactory() {
		createMapperPairs();
		orthologeGenesPerSpecies = new HashMap<>();
		getSpecies();
		getAllOrthologeGenes();
		extBW_heatmapsInfo = new ExternalWriter(new File(PATH_TO_HEATMAP_OUTPUT+"heatmaps.info"));
	}

	private void getSpecies() {
		this.speciesList = new ArrayList<>();
		ArrayList<String> species_ids = new ArrayList<String>(Arrays.asList(new File(PATH_TO_EB_FILES).list()));
		for (String species_id : species_ids) {
			speciesList.add(UtilityManager.getSpecies(Integer.parseInt(species_id)));
		}
	}

	private void createMapperPairs() {
		mapperDePairList = new ArrayList<>();
		for (Mapper mapper : Mapper.values()) {
			for (DEmethods method : DEmethods.values()) {
				mapperDePairList.add(new MapperxMethodPair(mapper, method));
			}
		}
	}

	public void createHeatmapForEachMapperPair(boolean createInfoFile) {

		for(MapperxMethodPair pair : this.mapperDePairList){
		
			Pair<Vector<TissuePairCompare>, Vector<TreeSet<GeneObject>>> output = prepareHeatmapForMapperPair(pair, createInfoFile);
//			extBW_heatmapsContent = new ExternalWriter(new File(PATH_TO_HEATMAP_OUTPUT+pair.getMapper()+"_"+pair.getMethod()+"_heatmap.content"));
//			Heatmap heatmap = computeHeatmap(output, pair.getMapper().getPathName(), pair.getMethod().getPathName());
//			extBW_heatmapsContent.closeWriter();
//			storeHeatmapToDisk(PATH_TO_HEATMAP_OUTPUT+"heatmap_"+pair.getMapper().getPathName()+"_"+pair.getMethod().getPathName()+".ser", heatmap);
			
		 }
	}
	
	public void createHeatmapForMapperPair(MapperxMethodPair pair){
		Pair<Vector<TissuePairCompare>, Vector<TreeSet<GeneObject>>> output = prepareHeatmapForMapperPair(pair, false);
		
		extBW_heatmapsContent = new ExternalWriter(new File(PATH_TO_HEATMAP_OUTPUT+pair.getMapper()+"_"+pair.getMethod()+"_heatmap.content"));
		Heatmap heatmap = computeHeatmap(output, pair.getMapper().getPathName(), pair.getMethod().getPathName());
		extBW_heatmapsContent.closeWriter();
		storeHeatmapToDisk(PATH_TO_HEATMAP_OUTPUT+pair.getMapper()+"_"+pair.getMethod()+"_heatmap.ser", heatmap);
	}

	public Pair<Vector<TissuePairCompare>, Vector<TreeSet<GeneObject>>> prepareHeatmapForMapperPair(MapperxMethodPair pair, boolean createInfoFile) {

		DebugMessageFactory.printInfoDebugMessage(true, "Create Heatmap for: " + pair.getMapper() + " - " + pair.getMethod());
		
		if(createInfoFile){
			extBW_heatmapsInfo.write("# contains axis labels for each mapper-method-pair-heatmap (amount: 12)\n");
			extBW_heatmapsInfo.write("# tissue_1,tissue_2,organism_name,organism_id\n");
			extBW_heatmapsInfo.write("MAPPER-PAIR: "+pair.getMapper()+","+pair.getMethod()+"\n");
		}

		Vector<TissuePairCompare> tissuePairs = new Vector<>();
		Vector<TreeSet<GeneObject>> geneObjects = new Vector<>();

		String mapperName = pair.getMapper().getPathName();
		String methodName = pair.getMethod().getPathName();

		int speciesCounter = 1;
		
		for (Species species : this.speciesList) {
			
			DebugMessageFactory.printInfoDebugMessage(true, "Read files for species: "+species.getName()+" ["+speciesCounter+"/"+speciesList.size()+"]");

			String species_id = "" + species.getId();

			ArrayList<String> tissue_pairs = new ArrayList<String>(Arrays.asList(new File(PATH_TO_EB_FILES + species_id + "/").list()));

			int tissueCounter = 1;
			
			for (String tissue_pair_string : tissue_pairs) {
				
				DebugMessageFactory.printInfoDebugMessage(true, "Read files for tissue-pair: "+tissue_pair_string+" ["+tissueCounter+"/"+tissue_pairs.size()+"]");
				
				String[] tissueStringArray = tissue_pair_string.split("_");
				String tissue_1_string = tissueStringArray[0];
				String tissue_2_string = tissueStringArray[1];
				Tissue tissue_1 = TissueHandler.getTissue(species, tissue_1_string);
				Tissue tissue_2 = TissueHandler.getTissue(species, tissue_2_string);

				TissuePairCompare tissuePair = new TissuePairCompare(tissue_1, tissue_2, species);

				ArrayList<String> mapper_list = new ArrayList<String>(Arrays.asList(new File(PATH_TO_EB_FILES + species_id + "/" + tissue_pair_string + "/").list()));

				/* check if mapper exists */
				if (mapper_list.contains(mapperName)) {

					/* check if DEmethod exists */
					ArrayList<String> file_list = new ArrayList<String>(Arrays.asList(new File(PATH_TO_EB_FILES + species_id + "/" + tissue_pair_string + "/" + mapperName + "/").list()));

					boolean methodExists = false;
					for (String s : file_list) {
						if (s.contains(methodName)) {
							methodExists = true;
							break;
						}
					}
					if (!methodExists) {
						break;
					}

					String fileEnding = pair.getMethod().getPathName();
					String pathToDEFile = PATH_TO_EB_FILES + species_id + "/" + tissue_pair_string + "/" + pair.getMapper().getPathName() + "/" + tissue_pair_string + "." + fileEnding;

					TreeSet<GeneObject> set = EnrichmentAnalysisUtils.readDEfile(pathToDEFile);

					if(createInfoFile){
						extBW_heatmapsInfo.write(tissue_1_string+","+tissue_2_string+","+species.getName()+","+species.getId()+"\n");
					}
					
					tissuePairs.add(tissuePair);
					geneObjects.add(set);

				}
				tissueCounter++;
			}
			speciesCounter++;
		}
		
		if(createInfoFile){
			extBW_heatmapsInfo.write("\n");
		}
		
		return new Pair<>(tissuePairs, geneObjects);
	}

	public Heatmap computeHeatmap(Pair<Vector<TissuePairCompare>, Vector<TreeSet<GeneObject>>> pair, String mapper, String method) {

		DebugMessageFactory.printInfoDebugMessage(true, "Compute HeatMap [TissuePairAmount: " + pair.getFirst().size() + " | GeneAmount: " + pair.getValue().size() + "]");

		ArrayList<String> tissuePairs = new ArrayList<>();
		Double[][] scores = new Double[pair.getFirst().size()][pair.getFirst().size()];

		for (TissuePairCompare tp : pair.getFirst()) {
			tissuePairs.add(tp.getKey() + "_" + tp.getValue() + "_" + tp.getSpecies().getName());
		}

		Vector<TreeSet<GeneObject>> treeSets = pair.getValue();

		PearsonsCorrelation pc = new PearsonsCorrelation();

		for (int i = 0; i < treeSets.size(); i++) {

			Species a = pair.getFirst().get(i).getSpecies();

			for (int j = 0; j < treeSets.size(); j++) {
				
				DebugMessageFactory.printInfoDebugMessage(true, "Progress:\t"+(i+1)*(j+1)+"/"+treeSets.size()*treeSets.size());

				TreeSet<GeneObject> geneSet_a = (TreeSet)treeSets.get(i).clone();

				Species b = pair.getFirst().get(j).getSpecies();
				TreeSet<GeneObject> geneSet_b = (TreeSet)treeSets.get(j).clone();

				ArrayList<Double> valueList_tissue_a = new ArrayList<>();
				ArrayList<Double> valueList_tissue_b = new ArrayList<>();

				ArrayList<GeneObject> toRemove_a = new ArrayList<>();

				/* get orthologe genes for tissue pair a */
				for (GeneObject go : geneSet_a) {

					String orthologeGeneID = getOrthologeGene(a, b, go.getName());

					valueList_tissue_a.add(go.getAdj_pval());
					toRemove_a.add(go);

					if (orthologeGeneID == null) {
						valueList_tissue_b.add(1.0);
					} else {
						GeneObject tmp = getGeneObjectFromList(orthologeGeneID, geneSet_b);
						if(tmp == null){
							valueList_tissue_b.add(1.0);
						}else{
							valueList_tissue_b.add(tmp.getAdj_pval());
							geneSet_b.remove(tmp);
						}
					}
				}

				/* remove visited genes from tissue pair a */
				for (GeneObject go : toRemove_a) {
					geneSet_a.remove(go);
				}

				/* get orthologe genes for tissue pair b */
				for (GeneObject go : geneSet_b) {
					valueList_tissue_a.add(1.0);
					valueList_tissue_b.add(go.getAdj_pval());
				}

				double[] tmp_a = new double[valueList_tissue_a.size()];
				double[] tmp_b = new double[valueList_tissue_b.size()];

				for (int k = 0; k < valueList_tissue_a.size(); k++) {
					tmp_a[k] = valueList_tissue_a.get(k);
					tmp_b[k] = valueList_tissue_b.get(k);
				}

				if (tmp_a.length < 2) {
					continue;
				}

				Double tmp_double = pc.correlation(tmp_a, tmp_b);

				scores[i][j] = tmp_double;
				
				extBW_heatmapsContent.write(tmp_double+"\t");

			}
			
			extBW_heatmapsContent.write("\n");

		}

		Heatmap out = new Heatmap(mapper, method, tissuePairs, scores);
		out.printHeatmap();
		return out;
	}

	public GeneObject getGeneObjectFromList(String id, TreeSet<GeneObject> set) {
		for (GeneObject go : set) {
			if (go.getName().equals(id)) {
				return go;
			}
		}
		return null;
	}

	public void getAllOrthologeGenes() {

		SimilarityHandler sh = UtilityManager.getSimilarityHandler();

		for (int i = 0; i < speciesList.size(); i++) {

			Species a = speciesList.get(i);
			HashMap<Species, HashSet<String>> genesPerSpecies = new HashMap<>();

			for (int j = i+1; j < speciesList.size(); j++) {

				Species b = speciesList.get(j);

				HashSet<String> genesWithOrthologes = sh.getAllGenesWithAnOrtholog(a, b);
				genesPerSpecies.put(b, genesWithOrthologes);
			}

			orthologeGenesPerSpecies.put(a, genesPerSpecies);
		}
	}

	public String getOrthologeGene(Species query_species, Species target_species, String geneID) {

		SimilarityHandler sh = UtilityManager.getSimilarityHandler();
		return sh.getSimilarities(query_species, target_species).getGeneWithHighestIdentity(geneID);

	}

	public void storeHeatmapToDisk(String path, Object hm) {
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
			oos.writeObject(hm);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public Heatmap readHeatmapFromDisk(String path) {
		Heatmap output = null;

		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
			output = (Heatmap) ois.readObject();
			ois.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return output;
	}
	
	/**
	 * Generate a heatmap from heatmaps showing the difference in each cell
	 * 
	 * @param heatmaps a collection of heatmaps
	 * @return master heatmap showing the difference of each cell
	 */
	public Heatmap generateMasterHeatmap(Collection<Heatmap> heatmaps){
		
		
		
		return null;
	}

	public static void main(String[] args) {
		
//		Mapper mapper = Mapper.STAR.getMapperForString(args[0]);
//		DEmethods method = DEmethods.DESEQ.getMethodForString(args[1]);
//		
//		if(mapper == null || method == null){
//			System.err.println("WRONG ARGUMENTS! (mapper method)");
//			System.exit(1);
//		}
//		
//		DebugMessageFactory.printInfoDebugMessage(true, "Creating heatmap for: "+mapper.toString()+"-"+method.toString());

		UtilityManager um = new UtilityManager("/home/proj/biocluster/praktikum/genprakt/bioinformaniacs/config.txt", false, false, false);

		HeatmapFactory hmf = new HeatmapFactory();
//		MapperxMethodPair pair = new MapperxMethodPair(mapper, method);
//		hmf.createHeatmapForMapperPair(pair);
		
		hmf.createHeatmapForEachMapperPair(true);
		
		hmf.extBW_heatmapsInfo.closeWriter();

	}

}
