package kikky.analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import kikky.heatmap.Sample_Data;

public class File_Preparer {

	public static double read_file_fpkm(String file, ArrayList<Sample_Data> values) {
		double value = 0;
		try {
			File f = new File("/home/a/adamowicz/GoBi/Block/results/"+file);
			BufferedReader br = new BufferedReader(new FileReader(f));
			BufferedWriter bw = new BufferedWriter(
					new FileWriter("/home/a/adamowicz/GoBi/Block/results/info_files/FPKM/" + f.getName()));
			bw.write("#Point_info\n");
			String[] samples = (f.getName().substring(0, f.getName().length() - 8)).split("-");
			bw.write(samples[0] + " " + values.get(Integer.parseInt(samples[0])).get_name() + "\n");
			bw.write(samples[1] + " " + values.get(Integer.parseInt(samples[1])).get_name() + "\n");
			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("#Heatmap_value")) {
					value = Double.parseDouble(br.readLine());
				}
				if (line.startsWith("#Scatterplot")) {
					
				}
			}
			br.close();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return value;
	}

}
