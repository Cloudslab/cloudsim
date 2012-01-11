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
 * The Class LomaxDistribution.
 * 
 * @author Marcos Dias de Assuncao
 * @since CloudSim Toolkit 1.0
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

		if (shift > location) {
			throw new IllegalArgumentException("Shift must be smaller or equal than location");
		}

		this.shift = shift;
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

		if (shift > location) {
			throw new IllegalArgumentException("Shift must be smaller or equal than location");
		}

		this.shift = shift;
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.distributions.ParetoDistr#sample()
	 */
	@Override
	public double sample() {
		return super.sample() - shift;
	}

}
