package andi.analysis.go;

import java.util.LinkedList;
import java.util.Map.Entry;

import dennis.GO.GOHandler;
import dennis.utility_manager.UtilityManager;

public class Builder {

	public Builder() {
		UtilityManager um = new UtilityManager(
				"/home/m/maieran/git/Bioinformaniacs/GOBI_Block/bin/andi/analysis/go/config.txt", false, true, false);
		
		
		for (Entry<String, LinkedList<String>> s : GOHandler.getAllMappedGOs(null, "ENSGALG00000003855").entrySet()) {
			System.out.println(s.getKey());
			for(String s2:s.getValue())
				System.out.println("\t"+s2);
		}
	}

	public static void main(String[] args) {
		Builder b = new Builder();
	}

}
