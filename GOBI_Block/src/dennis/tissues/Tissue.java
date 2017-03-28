package dennis.tissues;

import java.util.LinkedList;

import dennis.utility_manager.Experiment;

public class Tissue implements Comparable<Tissue> {

	private String name;
	private int number;

	private LinkedList<Experiment> experiments;

	public Tissue(String name) {
		this.name = name;
		this.number = 0;
		this.experiments = new LinkedList<>();
	}

	public String getName() {
		return name;
	}

	/**
	 * 
	 * @return list of experiments
	 */
	public LinkedList<Experiment> getExperiments() {
		return experiments;
	}

	/**
	 * 
	 * @return number of experiments in the species for this tissue
	 */
	public int getNumber() {
		return number;
	}

	public void addExperiment(Experiment dir) {
		experiments.add(dir);
		number = experiments.size();
	}

	/**
	 * 
	 * @param name
	 * @return experiment with given name; null if name not in experiments
	 */
	public Experiment getExperiment(String name) {
		for (Experiment e : experiments) {
			if (e.getName().equals(name)) {
				return e;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public int compareTo(Tissue o) {
		return name.compareTo(o.getName());
	}

	public boolean equals(Tissue o) {
		if (o.getName().equals(this.name) && o.getNumber() == this.number)
			return true;
		return false;
	}
}
