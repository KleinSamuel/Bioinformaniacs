package sam.mapper_comparison;

public enum DEmethods {

	DESEQ, LIMMA, EDGER;
	
	public String getPathName(){
		switch (this) {
		case DESEQ:
			return "DESeq";
		case LIMMA:
			return "limma";
		case EDGER:
			return "edgeR";
		default:
			return null;
		}
	}
	
	public DEmethods getMethodForString(String s){
		switch (s) {
		case "deseq":
			return DEmethods.DESEQ;
		case "edger":
			return DEmethods.EDGER;
		case "limma":
			return DEmethods.LIMMA;
		default:
			return null;
		}
	}
	
}
