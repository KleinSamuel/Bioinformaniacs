package sam.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ExternalWriter {

	private File outputFile;
	private BufferedWriter bw;
	private boolean append;
	
	public ExternalWriter(File outputFile, boolean append){
		this.outputFile = outputFile;
		openWriter();
	}
	
	public void openWriter(){
		try {
			
			bw = new BufferedWriter(new FileWriter(outputFile, append));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void closeWriter(){
		try {
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void write(String s){
		try {
			bw.write(s);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
