package dennis.GO;

import java.util.HashMap;

public class Graph {

	private HashMap<String, TermNode> nodes;

	public Graph() {
		nodes = new HashMap<>();
	}

	public TermNode getNode(String id) {
		return nodes.get(id);
	}

	public HashMap<String, TermNode> getTermNodes() {
		return nodes;
	}

	public void addNode(TermNode n) {
		nodes.put(n.getId(), n);
	}

	public void addAltNode(String alt_id, TermNode n) {
		nodes.put(alt_id, n);
	}

}
