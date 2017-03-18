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
		TermNode next = nodes.get(n.getId());
		if (next != null) {
			// dummy existiert bereits
			next.setName(n.getName());
			next.setNamespace(n.getNamespace());
			next.setDefinition(n.getDefinition());
		} else {
			nodes.put(n.getId(), n);
		}
	}

}
