package kikky.analysis;

import java.util.Comparator;

public class SpeciesComparator<Sample_Data> implements Comparator<Sample_Data> {

	@Override
	public int compare(Sample_Data o1, Sample_Data o2) {
		if (o1.getClass() == FPKM_Single.class && o2.getClass() == FPKM_Single.class) {
			FPKM_Single fs1 = (FPKM_Single) o1;
			FPKM_Single fs2 = (FPKM_Single) o2;
			return fs1.get_organism_ID() - fs2.get_organism_ID();
		}
		return 0;
	}
}
