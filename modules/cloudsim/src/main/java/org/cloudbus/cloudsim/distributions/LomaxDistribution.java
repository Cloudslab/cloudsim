/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.distributions;

import java.util.Random;

/**
 * A pseudo random number generator following the 
 * <a href="https://en.wikipedia.org/wiki/Lomax_distribution">
 * Lomax distribution</a>.
 * 
 * @author Marcos Dias de Assuncao
 * @since CloudSim Toolkit 1.0
 */
public class LomaxDistribution extends ParetoDistr implements ContinuousDistribution {

	/** The shift. */
	private final double shift;

	/**
	 * Instantiates a new lomax pseudo random number generator.
	 * 
	 * @param shape the shape
	 * @param location the location
	 * @param shift the shift
	 */
	public LomaxDistribution(double shape, double location, double shift) {
		super(shape, location);

		if (shift > location) {
			throw new IllegalArgumentException("Shift must be smaller or equal than location");
		}

		this.shift = shift;
	}

	/**
	 * Instantiates a new lomax pseudo random number generator.
	 * 
	 * @param seed the seed
	 * @param shape the shape
	 * @param location the location
	 * @param shift the shift
	 */
	public LomaxDistribution(Random seed, double shape, double location, double shift) {
		super(seed, shape, location);

		if (shift > location) {
			throw new IllegalArgumentException("Shift must be smaller or equal than location");
		}

		this.shift = shift;
	}

	@Override
	public double sample() {
		return super.sample() - shift;
	}

}
