package org.cloudbus.cloudsim.distributions;

import java.util.Random;

/**
 * The Class LomaxDistribution.
 */
public class LomaxDistribution extends ParetoDistr implements ContinuousDistribution {

	/** The shift. */
	private final double shift;

	/**
	 * Instantiates a new lomax distribution.
	 *
	 * @param shape the shape
	 * @param location the location
	 * @param shift the shift
	 */
	public LomaxDistribution(double shape, double location, double shift) {
		super(shape, location);

		if(shift>location){
			throw new IllegalArgumentException("Shift must be smaller or equal than location");
		}

		this.shift=shift;
	}

	/**
	 * Instantiates a new lomax distribution.
	 *
	 * @param seed the seed
	 * @param shape the shape
	 * @param location the location
	 * @param shift the shift
	 */
	public LomaxDistribution(Random seed, double shape, double location, double shift) {
		super(seed, shape, location);

		if(shift>location){
			throw new IllegalArgumentException("Shift must be smaller or equal than location");
		}

		this.shift=shift;
	}

	/* (non-Javadoc)
	 * @see cloudsim.distributions.ParetoDistr#sample()
	 */
	@Override
	public double sample() {
		return super.sample()-shift;
	}

}
