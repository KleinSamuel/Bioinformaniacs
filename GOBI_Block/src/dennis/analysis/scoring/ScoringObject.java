package dennis.analysis.scoring;

/**
 * class all other scorings should extend
 * 
 * @author Dennis
 *
 */
public class ScoringObject {

	private double score;

	public ScoringObject(double score) {
		this.score = score;
	}

	public double getScore() {
		return score;
	}

}
