package kikky.objects;

import java.util.Comparator;

public class SpeciesComparator<Sample_Data> implements Comparator<Sample_Data> {

	@Override
	public int compare(Sample_Data o1, Sample_Data o2) {
		if (o1.getClass() == FPKM_Single.class && o2.getClass() == FPKM_Single.class) {
			FPKM_Single fs1 = (FPKM_Single) o1;
			FPKM_Single fs2 = (FPKM_Single) o2;
			return fs1.get_species_ID() - fs2.get_species_ID();
		}
		if (o1.getClass() == DE_Pairs.class && o2.getClass() == DE_Pairs.class) {
			DE_Pairs fs1 = (DE_Pairs) o1;
			DE_Pairs fs2 = (DE_Pairs) o2;
			return fs1.get_species_ID() - fs2.get_species_ID();
		}
		return 0;
	}
}
