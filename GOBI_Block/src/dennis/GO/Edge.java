package dennis.GO;

public class Edge {

	private TermNode source, target;
	private Relation relation;

	public Edge(TermNode source, TermNode target, Relation rel) {
		this.source = source;
		this.target = target;
		relation = Relation.IS_A;
	}

	public TermNode getSource() {
		return source;
	}

	public TermNode getTarget() {
		return target;
	}

	public Relation getRelation() {
		return relation;
	}

}
