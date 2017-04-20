package sam.mapper_comparison;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
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
import sam.utils.HeatmapFromFileReader;

public class HeatmapFactory {

	private ArrayList<MapperxMethodPair> mapperDePairList;

	private final String PATH_TO_EB_FILES = "/home/proj/biocluster/praktikum/genprakt/bioinformaniacs/EB/";
	public static String PATH_TO_HEATMAP_OUTPUT = "/home/proj/biocluster/praktikum/genprakt/bioinformaniacs/sam/";
	public static String PATH_TO_FINAL_HEATMAPS = "/home/proj/biocluster/praktikum/genprakt/bioinformaniacs/sam/finalHeatmaps/";
	public static final Double THRESHOLD_PVAL = 0.05;
	public static final int VECTOR_SIZE = 100;
	
	private ExternalWriter extBW_heatmapsInfo;
	private ExternalWriter extBW_heatmapsContent;
	private ExternalWriter extBW_size;

	private HashMap<Species, HashMap<Species, HashSet<String>>> orthologeGenesPerSpecies;
	private ArrayList<Species> speciesList;
	
	File cm_deseq_content = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"output/contextmap_deseq_fc/CONTEXTMAP_DESEQ_heatmap.content");
	File cm_deseq_info = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"output/contextmap_deseq_fc/CONTEXTMAP_DESEQ_heatmap.info");
	File cm_edger_content = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"output/contextmap_edger_fc/CONTEXTMAP_EDGER_heatmap.content");
	File cm_edger_info = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"output/contextmap_edger_fc/CONTEXTMAP_EDGER_heatmap.info");
	File cm_limma_content = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"output/contextmap_limma_fc/CONTEXTMAP_LIMMA_heatmap.content");
	File cm_limma_info = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"output/contextmap_limma_fc/CONTEXTMAP_LIMMA_heatmap.info");
	
	File th_deseq_content = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"output/tophat_deseq_fc/TOPHAT_DESEQ_heatmap.content");
	File th_deseq_info = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"output/tophat_deseq_fc/TOPHAT_DESEQ_heatmap.info");
	File th_edger_content = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"output/tophat_edger_fc/TOPHAT_EDGER_heatmap.content");
	File th_edger_info = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"output/tophat_edger_fc/TOPHAT_EDGER_heatmap.info");
	File th_limma_content = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"output/tophat_limma_fc/TOPHAT_LIMMA_heatmap.content");
	File th_limma_info = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"output/tophat_limma_fc/TOPHAT_LIMMA_heatmap.info");
	
	File hs_deseq_content = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"output/hisat_deseq_fc/HISAT_DESEQ_heatmap.content");
	File hs_deseq_info = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"output/hisat_deseq_fc/HISAT_DESEQ_heatmap.info");
	File hs_edger_content = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"output/hisat_edger_fc/HISAT_EDGER_heatmap.content");
	File hs_edger_info = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"output/hisat_edger_fc/HISAT_EDGER_heatmap.info");
	File hs_limma_content = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"output/hisat_limma_fc/HISAT_LIMMA_heatmap.content");
	File hs_limma_info = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"output/hisat_limma_fc/HISAT_LIMMA_heatmap.info");
	
	File st_deseq_content = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"output/star_deseq_fc/STAR_DESEQ_heatmap.content");
	File st_deseq_info = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"output/star_deseq_fc/STAR_DESEQ_heatmap.info");
	File st_edger_content = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"output/star_edger_fc/STAR_EDGER_heatmap.content");
	File st_edger_info = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"output/star_edger_fc/STAR_EDGER_heatmap.info");
	File st_limma_content = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"output/star_limma_fc/STAR_LIMMA_heatmap.content");
	File st_limma_info = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"output/star_limma_fc/STAR_LIMMA_heatmap.info");

	public HeatmapFactory(boolean mastermap){
		
	}
	
	public HeatmapFactory() {
		createMapperPairs();
		orthologeGenesPerSpecies = new HashMap<>();
		getSpecies();
		getAllOrthologeGenes();
	}

	private void getSpecies() {
		this.speciesList = new ArrayList<>();
		ArrayList<String> species_ids = new ArrayList<String>(Arrays.asList(new File(PATH_TO_EB_FILES).list()));
		for (String species_id : species_ids) {
			System.out.println(species_id);
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

	public void createHeatmapForEachMapperPair() {
		
		extBW_size = new ExternalWriter(new File(PATH_TO_HEATMAP_OUTPUT+"size.info"), false);

		for(MapperxMethodPair pair : this.mapperDePairList){
		
			extBW_heatmapsInfo = new ExternalWriter(new File(PATH_TO_HEATMAP_OUTPUT+pair.getMapper()+"_"+pair.getMethod()+"_heatmap.info"), false);
			Pair<Vector<TissuePairCompare>, Vector<TreeSet<GeneObject>>> output = prepareHeatmapForMapperPair(pair);
			extBW_size.write(pair.getMapper()+"_"+pair.getMethod()+":\t"+output.getValue().size()+"\n");
			extBW_heatmapsInfo.closeWriter();
			
//			extBW_heatmapsContent = new ExternalWriter(new File(PATH_TO_HEATMAP_OUTPUT+pair.getMapper()+"_"+pair.getMethod()+"_heatmap.content"));
//			Heatmap heatmap = computeHeatmap(output, pair.getMapper().getPathName(), pair.getMethod().getPathName());
//			extBW_heatmapsContent.closeWriter();
//			storeHeatmapToDisk(PATH_TO_HEATMAP_OUTPUT+"heatmap_"+pair.getMapper().getPathName()+"_"+pair.getMethod().getPathName()+".ser", heatmap);
			
		 }
		
		extBW_size.closeWriter();
	}
	
	public void createHeatmapForMapperPair(MapperxMethodPair pair, int todo_line, boolean pval){
		
		extBW_heatmapsInfo = new ExternalWriter(new File(PATH_TO_HEATMAP_OUTPUT+pair.getMapper()+"_"+pair.getMethod()+"_heatmap_tmp.info"), false);
		Pair<Vector<TissuePairCompare>, Vector<TreeSet<GeneObject>>> output = prepareHeatmapForMapperPair(pair);
		extBW_heatmapsInfo.closeWriter();
		
		extBW_heatmapsContent = new ExternalWriter(new File(PATH_TO_HEATMAP_OUTPUT+pair.getMapper()+"_"+pair.getMethod()+"_heatmap_tmp_"+todo_line+".content"), false);
		Heatmap heatmap = computeHeatmap(output, pair.getMapper().getPathName(), pair.getMethod().getPathName(), todo_line, pval);
		extBW_heatmapsContent.closeWriter();
//		storeHeatmapToDisk(PATH_TO_HEATMAP_OUTPUT+pair.getMapper()+"_"+pair.getMethod()+"_heatmap.ser", heatmap);
	}

	public Pair<Vector<TissuePairCompare>, Vector<TreeSet<GeneObject>>> prepareHeatmapForMapperPair(MapperxMethodPair pair) {

		DebugMessageFactory.printInfoDebugMessage(true, "Create Heatmap for: " + pair.getMapper() + " - " + pair.getMethod());

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

					extBW_heatmapsInfo.write(tissue_1_string+","+tissue_2_string+","+species.getName()+","+species.getId()+"\n");

					tissuePairs.add(tissuePair);
					geneObjects.add(set);

				}
				tissueCounter++;
			}
			speciesCounter++;
		}
		
		return new Pair<>(tissuePairs, geneObjects);
	}

	public Heatmap computeHeatmap(Pair<Vector<TissuePairCompare>, Vector<TreeSet<GeneObject>>> pair, String mapper, String method, int todo_line, boolean pval) {

		DebugMessageFactory.printInfoDebugMessage(true, "Compute HeatMap [TissuePairAmount: " + pair.getKey().size() + " | GeneAmount: " + pair.getValue().size() + "]");

		ArrayList<String> tissuePairs = new ArrayList<>();
		Double[][] scores = new Double[pair.getFirst().size()][pair.getFirst().size()];

		for (TissuePairCompare tp : pair.getFirst()) {
			tissuePairs.add(tp.getKey() + "_" + tp.getValue() + "_" + tp.getSpecies().getName());
		}

		Vector<TreeSet<GeneObject>> treeSets = pair.getValue();

		PearsonsCorrelation pc = new PearsonsCorrelation();
		
		int counter = 1;
		long all_time = 0;
		long start;
		long end;

		int i = todo_line;
		
		extBW_heatmapsContent.write(i+"\t");

		Species a = pair.getFirst().get(i).getSpecies();

		for (int j = 0; j < treeSets.size(); j++) {
			
			start = System.currentTimeMillis();
			DebugMessageFactory.printInfoDebugMessage(true, "Progress:\t"+counter+"/"+treeSets.size());

			TreeSet<GeneObject> geneSet_a = (TreeSet)treeSets.get(i).clone();

			Species b = pair.getFirst().get(j).getSpecies();
			TreeSet<GeneObject> geneSet_b = (TreeSet)treeSets.get(j).clone();

			List<Double> valueList_tissue_a = new LinkedList<>();
			List<Double> valueList_tissue_b = new LinkedList<>();
			
			ArrayList<Double> valueMap_tissue_a = new ArrayList<>();
			ArrayList<Double> valueMap_tissue_b = new ArrayList<>();

			/* get orthologe genes for tissue pair a */
			for (GeneObject go : geneSet_a) {

				String orthologeGeneID = getOrthologeGene(a, b, go.getName());
				
				if(orthologeGeneID != null){
					
					GeneObject tmp = getGeneObjectFromList(orthologeGeneID, geneSet_b);
					
					if(pval){
						if(tmp != null && go.getAdj_pval() <= THRESHOLD_PVAL && tmp.getAdj_pval() <= THRESHOLD_PVAL){
							valueList_tissue_a.add(go.getAdj_pval());
							valueList_tissue_b.add(tmp.getAdj_pval());
							geneSet_b.remove(tmp);
						}
					}else{
						if(tmp != null && go.getAdj_pval() <= THRESHOLD_PVAL && tmp.getAdj_pval() <= THRESHOLD_PVAL){
//							valueList_tissue_a.add(go.getLog2fc());
//							valueList_tissue_b.add(tmp.getLog2fc());
							
							valueMap_tissue_a.add(go.getLog2fc());
							valueMap_tissue_b.add(tmp.getLog2fc());
							
							geneSet_b.remove(tmp);
						}
					}
				}
			}
			
			Pair<LinkedList<Double>, LinkedList<Double>> tmp = getBestValues(valueMap_tissue_a, valueMap_tissue_b);
			
			valueList_tissue_a = tmp.getFirst();
			valueList_tissue_b = tmp.getSecond();
			
			double[] tmp_a = new double[valueList_tissue_a.size()];
			double[] tmp_b = new double[valueList_tissue_b.size()];

			for (int k = 0; k < valueList_tissue_a.size(); k++) {
				tmp_a[k] = valueList_tissue_a.get(k);
				tmp_b[k] = valueList_tissue_b.get(k);
			}
			
			Double tmp_double;

			if (tmp_a.length < 2) {
				tmp_double = 0d;
			}else{
				tmp_double = pc.correlation(tmp_a, tmp_b);
			}

			scores[i][j] = tmp_double;
			
			extBW_heatmapsContent.write(tmp_double+"\t");
			
			counter++;
			
			end = System.currentTimeMillis();
			
			all_time += end-start;
			
			DebugMessageFactory.printInfoDebugMessage(true, "Mean time needed for 1 tissue-pair:\t"+(all_time/counter)+" ms");

		}
		
		extBW_heatmapsContent.write("\n");

		if(counter != treeSets.size()){
			System.err.println("STOPPED AT "+counter);
		}

		Heatmap out = new Heatmap(mapper, method, tissuePairs, scores);
		return out;
	}
	
	public Pair<LinkedList<Double>,LinkedList<Double>> getBestValues(ArrayList<Double> list_a, ArrayList<Double> list_b){
		
		System.out.println("SIZE INPUT:\t"+list_a.size());
		
		ComparablePairTMP[] array_a = new ComparablePairTMP[list_a.size()];
		ComparablePairTMP[] array_b = new ComparablePairTMP[list_b.size()];
		
		for (int i = 0; i < list_a.size(); i++) {
			array_a[i] = new ComparablePairTMP(i, list_a.get(i));
			array_b[i] = new ComparablePairTMP(i, list_b.get(i));
		}
		
		Arrays.sort(array_a);
		Arrays.sort(array_b);
		
		LinkedList<Double> out_a = new LinkedList<>();
		LinkedList<Double> out_b = new LinkedList<>();
		
		int index_a = 0;
		int index_b = 0;
		
		// TODO possible out of bounds exception catch with check if list a smaller than VECTOR_SIZE
		while(out_a.size() < VECTOR_SIZE){
			
			if(array_a[index_a].value == array_b[index_b].value){
				out_a.add(list_a.get(array_a[index_a].index));
				out_b.add(list_b.get(array_a[index_a].index));
				
				index_a++;
				index_b++;
			}else if(array_a[index_a].value > array_b[index_b].value){
				out_a.add(list_a.get(array_a[index_a].index));
				out_b.add(list_b.get(array_a[index_a].index));
				
				index_a++;
			}else{
				out_a.add(list_a.get(array_b[index_b].index));
				out_b.add(list_b.get(array_b[index_b].index));
				
				index_b++;
			}
			
		}
		
		System.out.println("SIZE OUPUT:\t"+out_a.size());
		
		return new Pair<>(out_a, out_b);
	}
	
	public class ComparablePairTMP implements Comparable<ComparablePairTMP> {

		public final int index;
		public final double value;
		
		public ComparablePairTMP(int index, double value) {
			this.index = index;
			this.value = value;
		}
		
		@Override
		public int compareTo(ComparablePairTMP other) {
			return -1 * Double.valueOf(this.value).compareTo(other.value);
		}
		
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
	
	public ArrayList<String> getAllTissuePairs(){
		ArrayList<String> tissues = new ArrayList<>();
		for (Iterator<String> iterator = TissueHandler.tissueNameIterator(); iterator.hasNext();){
			tissues.add(iterator.next());
		}
		
		ArrayList<String> tissuePairs = new ArrayList<>();
		
		for (int i = 0; i < tissues.size(); i++) {
			for (int j = i+1; j < tissues.size(); j++) {
				tissuePairs.add(tissues.get(i)+"_"+tissues.get(j));
			}
		}
		
		return tissuePairs;
	}
	
	public ArrayList<String> getAllTissuePairsWithSpecies(){
		ArrayList<String> tissuePairs = getAllTissuePairs();
		ArrayList<String> out = new ArrayList<>();
		
		for (Iterator<Species> iterator = UtilityManager.speciesIterator(); iterator.hasNext();){
			Species spec = iterator.next();
			for(String s : tissuePairs){
				out.add(s.split("_")[0]+","+s.split("_")[1]+","+spec.getName()+","+spec.getId());
			}
		}
		return out;
	}
	
	/**
	 * Generate a heatmap from heatmaps showing the difference in each cell
	 * 
	 * @param heatmaps a collection of heatmaps
	 * @return master heatmap showing the difference of each cell
	 */
	public MasterHeatmap generateMasterHeatmap(ArrayList<Heatmap> heatmaps){
		
		/* contains all tissue-pairs for every species like this: tissue1_tissue2_species name */
		ArrayList<String> tissuePairsWithSpecies = getAllTissuePairsWithSpecies();
		
		MasterHeatmap masterHM = new MasterHeatmap(tissuePairsWithSpecies.size());
		
		ExternalWriter extInfo = new ExternalWriter(new File(PATH_TO_HEATMAP_OUTPUT+"masterHeatmap.info"), false);
		extInfo.openWriter();
		
		int row = 0;
		for(String tissuePair_row : tissuePairsWithSpecies){
			
			extInfo.write(tissuePair_row+"\n");
			
			int col = 0;
			for(String tissuePair_col : tissuePairsWithSpecies){
				
				TreeMap<String, Double> currentCellInfo = new TreeMap<>();
				
				for(Heatmap heatmap : heatmaps){
					
					Double tmp_score = 0d;
					String mapper = heatmap.getMapper();
					String method = heatmap.getMethod();
					int tmp_row = -1;
					int tmp_col = -1;
					
					/* find current tissue pair for current species */
					int tmp_counter = 0;
					boolean found_row = false;
					boolean found_col = false;
					for(String s : heatmap.getTissuePairs()){
						
						if(!found_row && s.equals(tissuePair_row)){
							tmp_row = tmp_counter;
							found_row = true;
						}
						if(!found_col && s.equals(tissuePair_col)){
							tmp_col = tmp_counter;
							found_col = true;
						}
						if(found_col && found_row){
							break;
						}
						tmp_counter++;
					}
					
					/* tissue pair was not found in current heatmap */
					if(tmp_col >= 0 && tmp_row >= 0){
						tmp_score = heatmap.getScores()[tmp_row][tmp_col];
					}
					currentCellInfo.put(mapper+"_"+method, tmp_score);
				}
				
				masterHM.addEntryToMatrix(row, col, currentCellInfo);
				
				col++;
			}
			row++;
		}
		
		extInfo.closeWriter();
			  
		return masterHM;
	}
	
	public void createHeatmapPlot(Mapper mapper, DEmethods method, String path){
		HeatmapFromFileReader hr = new HeatmapFromFileReader();
		
		MapperxMethodPair pair = new MapperxMethodPair(mapper, method);
		
		File heatmap = new File(path+pair.getMapper()+"_"+pair.getMethod()+"_heatmap.content");
		File heatmapInfo = new File(path+pair.getMapper()+"_"+pair.getMethod()+"_heatmap.info");
		File csv = new File(path+pair.getMapper()+"_"+pair.getMethod()+"_heatmap.csv");
		
		hr.readHeatmapIntoCSV(heatmap, heatmapInfo, csv);
		
		HeatmapVisualizer hv = new HeatmapVisualizer();
		File script = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"heatmap_example.R");
		String outputDir = path+pair.getMapper()+"_"+pair.getMethod()+".png";
		hv.createHeatmapWithR(script, csv, outputDir);
	}
	
	public void readAllHeatmapFiles(){
		
		HeatmapFromFileReader hr = new HeatmapFromFileReader();
		HeatmapFactory hmf = new HeatmapFactory(true);
		
		Heatmap cm_deseq = hr.readHeatmapIntoObject(cm_deseq_content, cm_deseq_info);
		Heatmap cm_edger = hr.readHeatmapIntoObject(cm_edger_content, cm_edger_info);
//		Heatmap cm_limma = hr.readHeatmapIntoObject(cm_limma_content, cm_limma_info);
		
		Heatmap th_deseq = hr.readHeatmapIntoObject(th_deseq_content, th_deseq_info);
		Heatmap th_edger = hr.readHeatmapIntoObject(th_edger_content, th_edger_info);
		Heatmap th_limma = hr.readHeatmapIntoObject(th_deseq_content, th_deseq_info);
		
		Heatmap hs_deseq = hr.readHeatmapIntoObject(hs_deseq_content, hs_deseq_info);
		Heatmap hs_edger = hr.readHeatmapIntoObject(hs_edger_content, hs_edger_info);
		Heatmap hs_limma = hr.readHeatmapIntoObject(hs_deseq_content, hs_deseq_info);
		
		Heatmap st_deseq = hr.readHeatmapIntoObject(st_deseq_content, st_deseq_info);
		Heatmap st_edger = hr.readHeatmapIntoObject(st_edger_content, st_edger_info);
		Heatmap st_limma = hr.readHeatmapIntoObject(st_deseq_content, st_deseq_info);
		
		ArrayList<Heatmap> heatmaps = new ArrayList<>();
		heatmaps.add(cm_deseq);
		heatmaps.add(cm_edger);
//		heatmaps.add(cm_limma);
		
		heatmaps.add(th_deseq);
		heatmaps.add(th_edger);
		heatmaps.add(th_limma);
		
		heatmaps.add(hs_deseq);
		heatmaps.add(hs_edger);
		heatmaps.add(hs_limma);
		
		heatmaps.add(st_deseq);
		heatmaps.add(st_edger);
		heatmaps.add(st_limma);
		
		MasterHeatmap masterHM = hmf.generateMasterHeatmap(heatmaps);
		masterHM.writeToFile(new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"masterHeatmap.content"));
		
		hmf.createMasterHeatmapPlot(hmf.PATH_TO_HEATMAP_OUTPUT);
	}
	
	public void compareEachMapper(){
		
		HeatmapFromFileReader hr = new HeatmapFromFileReader();
		
		PearsonsCorrelation pc = new PearsonsCorrelation();
		
		Double[][] finalContent = new Double[12][12];
		ArrayList<String> finalInfo = new ArrayList<>();
		
		int row = 0;
		int col = 0;
		
		for(Mapper mapper1 : Mapper.values()){
			for(DEmethods method1 : DEmethods.values()){
			
				String h1_string = mapper1.name()+"_"+method1.name()+"_heatmap";
				File heatmap1_content = new File(PATH_TO_FINAL_HEATMAPS+h1_string+".content");
				File heatmap1_info = new File(PATH_TO_FINAL_HEATMAPS+h1_string+".info");
				Heatmap heatmap1 = hr.readHeatmapIntoObject(heatmap1_content, heatmap1_info);
				ArrayList<String> tissuepairs1 = heatmap1.getTissuePairs();
				finalInfo.add(mapper1.name()+"_"+method1.name());
				
				for(Mapper mapper2 : Mapper.values()){
					for(DEmethods method2 : DEmethods.values()){
						
						String h2_string = mapper2.name()+"_"+method2.name()+"_heatmap";
						File heatmap2_content = new File(PATH_TO_FINAL_HEATMAPS+h2_string+".content");
						File heatmap2_info = new File(PATH_TO_FINAL_HEATMAPS+h2_string+".info");
						Heatmap heatmap2 = hr.readHeatmapIntoObject(heatmap2_content, heatmap2_info);
						ArrayList<String> tissuepairs2 = heatmap2.getTissuePairs();
						
						Vector<Double> scores1 = new Vector<>();
						Vector<Double> scores2 = new Vector<>();
						
						for (int tp_1_1 = 0; tp_1_1 < tissuepairs1.size(); tp_1_1++) {
							for (int tp_1_2 = 0; tp_1_2 < tissuepairs1.size(); tp_1_2++) {
								
								String tissuepair1 = tissuepairs1.get(tp_1_1)+"-"+tissuepairs1.get(tp_1_2);
								
								for (int tp_2_1 = 0; tp_2_1 < tissuepairs2.size(); tp_2_1++) {
									for (int tp_2_2 = 0; tp_2_2 < tissuepairs2.size(); tp_2_2++) {
										
										String tissuepair2 = tissuepairs2.get(tp_2_1)+"-"+tissuepairs2.get(tp_2_2);
										
										if(tissuepair1.equals(tissuepair2)){
											scores1.add(heatmap1.getScores()[tp_1_1][tp_1_2]);
											scores2.add(heatmap2.getScores()[tp_2_1][tp_2_2]);
										}
										
									}
								}
								
							}
						}
						
						double correlation = pc.correlation(createArrayFromVector(scores1), createArrayFromVector(scores2));
						
						finalContent[row][col] = correlation;
						
						col++;
					}
				}
				
				row++;
				col=0;
			}
		}
		
	}
	
	public void compareMMPair(Mapper mapper1, DEmethods method1, Mapper mapper2, DEmethods method2){
		
		HeatmapFromFileReader hr = new HeatmapFromFileReader();
		
		PearsonsCorrelation pc = new PearsonsCorrelation();
		
		String h1_string = mapper1.name()+"_"+method1.name()+"_heatmap";
		File heatmap1_content = new File(PATH_TO_FINAL_HEATMAPS+h1_string+".content");
		File heatmap1_info = new File(PATH_TO_FINAL_HEATMAPS+h1_string+".info");
		Heatmap heatmap1 = hr.readHeatmapIntoObject(heatmap1_content, heatmap1_info);
		ArrayList<String> tissuepairs1 = heatmap1.getTissuePairs();
				
		String h2_string = mapper2.name()+"_"+method2.name()+"_heatmap";
		File heatmap2_content = new File(PATH_TO_FINAL_HEATMAPS+h2_string+".content");
		File heatmap2_info = new File(PATH_TO_FINAL_HEATMAPS+h2_string+".info");
		Heatmap heatmap2 = hr.readHeatmapIntoObject(heatmap2_content, heatmap2_info);
		ArrayList<String> tissuepairs2 = heatmap2.getTissuePairs();
		
		Vector<Double> scores1 = new Vector<>();
		Vector<Double> scores2 = new Vector<>();
		
		String s1 = "heart,skm,macaca mulatta,9544";
		String s2 = "cerebellum,testis,mus musculus,10090";
		String s3 = "skm,testis,mus musculus,10090";
		
		for (int tp_1_1 = 0; tp_1_1 < tissuepairs1.size(); tp_1_1++) {
			for (int tp_1_2 = 0; tp_1_2 < tissuepairs1.size(); tp_1_2++) {
				
				String tissuepair1 = tissuepairs1.get(tp_1_1)+"-"+tissuepairs1.get(tp_1_2);
				
				for (int tp_2_1 = 0; tp_2_1 < tissuepairs2.size(); tp_2_1++) {
					for (int tp_2_2 = 0; tp_2_2 < tissuepairs2.size(); tp_2_2++) {
						
						String tissuepair2 = tissuepairs2.get(tp_2_1)+"-"+tissuepairs2.get(tp_2_2);
						
						if(tissuepair1.equals(s1) || tissuepair1.equals(s2) || tissuepair1.equals(s3)){
							continue;
						}
						
						if(tissuepair2.equals(s1) || tissuepair2.equals(s2) || tissuepair2.equals(s3)){
							continue;
						}
						
						if(tissuepair1.equals(tissuepair2)){
							scores1.add(heatmap1.getScores()[tp_1_1][tp_1_2]);
							scores2.add(heatmap2.getScores()[tp_2_1][tp_2_2]);
						}
						
					}
				}
				
			}
		}
		
		double correlation = pc.correlation(createArrayFromVector(scores1), createArrayFromVector(scores2));
		
		ExternalWriter ext = new ExternalWriter(new File(PATH_TO_HEATMAP_OUTPUT+mapper1+"_"+method1+"-"+mapper2+"_"+method2+".cor"), false);
		ext.openWriter();
		ext.write(correlation+"\n");
		ext.closeWriter();
		
	}
	
	public double[] createArrayFromVector(Vector<Double> list){
		
		double[] out = new double[list.size()];
		
		for (int i = 0; i < list.size(); i++) {
			if(list.get(i) == null){
				out[i] = 0d;
			}else{
				out[i] = list.get(i);
			}
		}
		return out;
	}
	
	public void createMasterHeatmapPlot(String path){
		
		HeatmapFromFileReader hr = new HeatmapFromFileReader();
		
		File heatmap = new File(path+"masterHeatmap.content");
		File heatmapInfo = new File(path+"masterHeatmap.info");
		File csv = new File(path+"masterHeatmap.csv");
		
		hr.readHeatmapIntoCSV(heatmap, heatmapInfo, csv);
		
		HeatmapVisualizer hv = new HeatmapVisualizer();
		File script = new File(HeatmapFactory.PATH_TO_HEATMAP_OUTPUT+"heatmap_example.R");
		String output = path+"masterHeatmap.png";
		hv.createHeatmapWithR(script, csv, output);
		
	}

	public static void main(String[] args) {
		
		Mapper mapper = Mapper.STAR.getMapperForString(args[0]);
		DEmethods method = DEmethods.DESEQ.getMethodForString(args[1]);
		int todo_line = Integer.parseInt(args[2])-1;
		boolean pval = Boolean.parseBoolean(args[3]);
		
		if(args.length >= 5){
			String outputPath = args[4];
			HeatmapFactory.PATH_TO_HEATMAP_OUTPUT = outputPath;
		}
		if(mapper == null || method == null){
			System.err.println("WRONG ARGUMENTS! (mapper method)");
			System.exit(1);
		}
		
		UtilityManager um = new UtilityManager("/home/proj/biocluster/praktikum/genprakt/bioinformaniacs/config.txt", false, false, false);
		
		MapperxMethodPair pair = new MapperxMethodPair(mapper, method);
		DebugMessageFactory.printInfoDebugMessage(true, "Creating heatmap for: "+mapper.toString()+"-"+method.toString());
//
		HeatmapFactory hmf = new HeatmapFactory();
//		
		hmf.createHeatmapForMapperPair(pair, todo_line, pval);
		
//		hmf.readAllHeatmapFiles();
		
//		hmf.compareEachMapper();
		
//		Mapper mapper2 = Mapper.STAR.getMapperForString(args[2]);
//		DEmethods method2 = DEmethods.DESEQ.getMethodForString(args[3]);
//		
//		hmf.compareMMPair(mapper, method, mapper2, method2);
		
	}

}
