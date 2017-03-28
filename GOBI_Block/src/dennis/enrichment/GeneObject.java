package dennis.enrichment;

public class GeneObject {
	private String name;
	private double log2fc, raw_pval, adj_pval;

	public GeneObject(String name, double log2fc, double raw_pval, double adj_pval) {
		this.name = name;
		this.log2fc = log2fc;
		this.raw_pval = raw_pval;
		this.adj_pval = adj_pval;
	}

	public GeneObject(String ebOutputLine) {
		String[] split = ebOutputLine.split("\t");
		name = split[0];
		log2fc = Double.parseDouble(split[1]);
		if (split[2].equals("NA")) {
			raw_pval = 1d;
		} else {
			raw_pval = Double.parseDouble(split[2]);
			if (raw_pval == 0) {
				raw_pval = Double.MIN_VALUE;
			}
		}
		if (split[3].equals("NA")) {
			adj_pval = 1d;
		} else {
			adj_pval = Double.parseDouble(split[3]);
			if (adj_pval == 0) {
				adj_pval = Double.MIN_VALUE;
			}
		}
	}

	public String getName() {
		return name;
	}

	public double getLog2fc() {
		return log2fc;
	}

	public double getRaw_pval() {
		return raw_pval;
	}

	public double getAdj_pval() {
		return adj_pval;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
}