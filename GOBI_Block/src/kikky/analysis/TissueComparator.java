package kikky.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class TissueComparator<Sample_Data> implements Comparator<Sample_Data> {
	private ArrayList<String> tissues = new ArrayList<>(
			Arrays.asList("brain", "lung", "kidney", "cerebellum", "skm", "spleen", "testis", "heart", "liver"));

	@Override
	public int compare(Sample_Data o1, Sample_Data o2) {
		if (o1.getClass() == FPKM_Single.class && o2.getClass() == FPKM_Single.class) {
			FPKM_Single fs1 = (FPKM_Single) o1;
			FPKM_Single fs2 = (FPKM_Single) o2;
			return tissues.indexOf(fs1.get_tissue().getName()) - tissues.indexOf(fs2.get_tissue().getName());
		}
		return 0;
	}

}
