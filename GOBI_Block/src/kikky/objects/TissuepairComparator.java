package kikky.objects;

import java.util.Comparator;

public class TissuepairComparator<Sample_Data> implements Comparator<Sample_Data> {

	@Override
	public int compare(Sample_Data o1, Sample_Data o2) {
		if (o1.getClass() == DE_Pairs.class && o2.getClass() == DE_Pairs.class) {
			DE_Pairs fs1 = (DE_Pairs) o1;
			DE_Pairs fs2 = (DE_Pairs) o2;
			return fs1.get_tissuepair().compareTo(fs2.get_tissuepair());
		}
		return 0;
	}

}