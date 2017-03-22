package sam.mapper_comparison;

public class TestRunner {

	public static void main(String[] args) {
		
		String line = "ENSGALG00000022964	2.61297977510462	4.46124867009472e-08	8.50849831278798e-07";
		
		String[] array = line.split("\t");
		
		double d = Double.parseDouble(array[3]);
		
		System.out.println(d);
		
		
	}
	
}
