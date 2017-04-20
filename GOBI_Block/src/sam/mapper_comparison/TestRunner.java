package sam.mapper_comparison;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import sam.utils.ExternalWriter;

public class TestRunner {
	
	public void repareLimma(File limmaContent, File limmaInfo){
		
		File newInfo = new File(limmaInfo.getAbsolutePath().replace(".info", "_new.info"));
		File newContent = new File(limmaContent.getAbsolutePath().replace(".content", "_new.content"));
		
		ExternalWriter extInfo = new ExternalWriter(newInfo, false);
		ExternalWriter extCon = new ExternalWriter(newContent, false);
		extInfo.openWriter();
		extCon.openWriter();
		
		try {
			
			BufferedReader br = new BufferedReader(new FileReader(limmaContent));
			
			String line = null;
			int counter = 0;
			
			ArrayList<Integer> toRemove = new ArrayList<>();
			
			while((line = br.readLine()) != null){
				if(line.split("\t").length < 219){
					toRemove.add(counter);
				}
				counter++;
			}
			
			BufferedReader brInfo = new BufferedReader(new FileReader(limmaInfo));
			
			line = null;
			counter = 0;
			
			while((line = brInfo.readLine()) != null){
				if(!toRemove.contains(counter)){
					extInfo.write(line+"\n");
					System.out.println("WRITE:\t"+line);
				}
				counter++;
			}
			
			br.close();
			br = new BufferedReader(new FileReader(limmaContent));
			line = null;
			counter = 0;
			
			while((line = br.readLine()) != null){
				
				String[] lineArray = line.split("\t");
				
				String newS = "";
				
				for (int i = 0; i < lineArray.length; i++) {
					
					if(lineArray.length <= 219){
						newS += lineArray[i]+"\t";
					}else if(i != toRemove.get(0)){
						newS += lineArray[i]+"\t";
					}
				}
				
				if(!toRemove.contains(counter)){
					extCon.write(newS+"\n");
				}
				counter++;
			}
			
			brInfo.close();
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		extInfo.closeWriter();
		extCon.closeWriter();
		
	}
	
	public void checkRowAndColAmount(File content, File info){
		
		try{
		
			BufferedReader br = new BufferedReader(new FileReader(content));
			
			String line = null;
			int counterRow = 0;
			
			while((line = br.readLine()) != null){
				if(line.split("\t").length < 198){
					System.out.println("FAIL at "+counterRow+"\t\tSize:\t"+line.split("\t").length);
				}
				counterRow++;
			}
			
			System.out.println("ROWS:\t"+counterRow);
			
			br.close();
		
		} catch(FileNotFoundException e){
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public static void main(String[] args) {
		
		TestRunner tr = new TestRunner();
		
		File content = new File("/home/proj/biocluster/praktikum/genprakt/bioinformaniacs/sam/output/hisat_limma_fc/HISAT_LIMMA_heatmap.content");
		File info = new File("/home/proj/biocluster/praktikum/genprakt/bioinformaniacs/sam/output/hisat_limma_fc/HISAT_LIMMA_heatmap.info");
		
//		File contentNew = new File("/home/proj/biocluster/praktikum/genprakt/bioinformaniacs/sam/output/contextmap_limma_fc/TOPHAT_LIMMA_heatmap_new.content");
		
//		tr.repareLimma(content, info);
		tr.checkRowAndColAmount(content, info);
		
	}
	
}
