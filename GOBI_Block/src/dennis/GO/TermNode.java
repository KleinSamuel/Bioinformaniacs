package dennis.GO;

import java.util.HashMap;
import java.util.TreeSet;

public class TermNode {

	private String id, name, namespace, definition;
	private HashMap<TermNode, Edge> edges;
	private boolean isObsolete = false;
	private TreeSet<String> altIds = null;

	public TermNode(String id, String name, String namespace) {
		this.id = id;
		this.name = name;
		this.namespace = namespace;
		edges = new HashMap<>();
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getDefinition() {
		return definition;
	}

	public void setDefinition(String definition) {
		this.definition = definition;
	}

	public HashMap<TermNode, Edge> getEdges() {
		return edges;
	}

	public void addAltId(String alt_id) {
		if (altIds == null) {
			altIds = new TreeSet<>();
		}
		altIds.add(alt_id);
	}

	public TreeSet<String> altIds() {
		return altIds;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	public void addEdge(Edge e) {
		edges.put(e.getTarget(), e);
	}

	public void addEdge(TermNode target, Relation rel) {
		edges.put(target, new Edge(this, target, rel));
	}

	public void setObsolete() {
		isObsolete = true;
	}

	public boolean isObsolete() {
		return this.isObsolete;
	}

}
