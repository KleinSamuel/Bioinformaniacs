package kikky.heatmap;

public class Small_Sample implements Sample_Data {

	private String name;
	int val;

	public Small_Sample(String name, int val) {
		this.name = name;
		this.val = val;
	}

	@Override
	public int compareTo(Sample_Data o) {
		Small_Sample ss = (Small_Sample) o;
		return val - ss.val;
	}

	@Override
	public String get_Name() {
		return name;
	}

	@Override
	public double get_value(Sample_Data sd) {
		Small_Sample ss = (Small_Sample) sd;
		return val - ss.val;
	}

}
