package sam.mapper_comparison;

public enum Mapper {

	STAR, CONTEXTMAP, TOPHAT, HISAT;
	
	public String getPathName(){
		switch (this) {
		case STAR:
			return "star";
		case CONTEXTMAP:
			return "contextmap";
		case TOPHAT:
			return "tophat2";
		case HISAT:
			return "hisat";
		default:
			return null;
		}
	}
	
	public Mapper getMapperForString(String s){
		switch (s) {
		case "star":
			return Mapper.STAR;
		case "contextmap":
			return Mapper.CONTEXTMAP;
		case "hisat":
			return Mapper.HISAT;
		case "tophat":
			return Mapper.TOPHAT;
		default:
			return null;
		}
	}
	
}
