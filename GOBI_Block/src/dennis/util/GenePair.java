package dennis.util;

public class GenePair implements Comparable<GenePair> {

	private String geneKey, geneValue;

	public GenePair(String geneKey, String geneVal) {
		this.geneKey = geneKey;
		this.geneValue = geneVal;
	}

	public String getKey() {
		return geneKey;
	}

	public String getValue() {
		return geneValue;
	}

	@Override
	public String toString() {
		return "gene_pair: " + geneKey + "->" + geneValue;
	}

	@Override
	public int compareTo(GenePair o) {
		int comp = geneKey.compareTo(o.getKey());
		if (comp == 0)
			return geneValue.compareTo(o.getValue());
		return comp;
	}

	@Override
	public int hashCode() {
		return (this.geneKey + "_" + this.geneValue).hashCode();
	}

}
