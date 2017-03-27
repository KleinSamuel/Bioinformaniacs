package dennis.GO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;

public class OBOreader {

	// idea: read everything between two [Term] tags -> parse it

	public static Graph readOBOFile(String oboFile) {
		Graph oboGraph = new Graph();
		try {

			BufferedReader br = new BufferedReader(new FileReader(new File(oboFile)));

			String line = null;
			boolean term = false;
			LinkedList<String> bufferedTerm = new LinkedList<>();
			// skip till first term
			while ((line = br.readLine()) != null) {
				if (line.equals("[Term]")) {
					term = true;
					if (!bufferedTerm.isEmpty()) {
						parseTerm(bufferedTerm, oboGraph);
						bufferedTerm = new LinkedList<>();
					}
				} else {
					if (term && !line.isEmpty()) {
						bufferedTerm.add(line);
					}
				}
			}
			if (!bufferedTerm.isEmpty()) {
				parseTerm(bufferedTerm, oboGraph);
			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return oboGraph;
	}

	public static void parseTerm(LinkedList<String> term, Graph oboGraph) {
		String[] split = null;
		TermNode t = null;
		boolean newNode = false;
		LinkedList<String> altIds = new LinkedList<>();
		for (String s : term) {
			split = s.split(": ", 2);
			switch (split[0]) {
			case "id":
				t = oboGraph.getNode(split[1]);
				if (t == null) {
					t = new TermNode(split[1], null, null);
					newNode = true;
				}
				break;
			case "name":
				t.setName(split[1]);
				break;
			case "namespace":
				t.setNamespace(split[1]);
				break;
			case "def":
				t.setDefinition(split[1].substring(1, split[1].indexOf("\"", 1)));
				break;
			case "is_obsolete":
				if (split[1].equals("true")) {
					t.setObsolete();
				}
				break;
			case "alt_id":
				altIds.add(split[1]);
				break;
			case "is_a":
				String id = split[1].split(" ! ")[0];
				TermNode n = oboGraph.getNode(id);
				if (n == null) {
					n = new TermNode(id, null, null);
					oboGraph.addNode(n);
				}
				t.addEdge(n, Relation.IS_A);
				break;
			}
		}
		if (t != null && newNode)
			oboGraph.addNode(t);
		if (t != null) {
			for (String alt_id : altIds) {
				t.addAltId(alt_id);
				oboGraph.addAltNode(alt_id, t);
			}
		}
	}

}
