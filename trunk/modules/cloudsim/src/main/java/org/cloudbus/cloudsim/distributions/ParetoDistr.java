/*
 * Title:        CloudSim Toolkit
 * Descripimport java.util.Random;
mulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.distributions;

import java.util.Random;

/**
 * The Class ParetoDistr.
 * 
 * @author Marcos Dias de Assuncao
 * @since CloudSim Toolkit 1.0
 */
public class ParetoDistr implements ContinuousDistribution {

	/** The num gen. */
	private final Random numGen;

	/** The shape. */
	private final double shape;

	/** The location. */
	private final double location;

	/**
	 * Instantiates a new pareto distr.
	 * 
	 * @param seed the seed
	 * @param shape the shape
	 * @param location the location
	 */
	public ParetoDistr(Random seed, double shape, double location) {
		if (shape <= 0.0 || location <= 0.0) {
			throw new IllegalArgumentException("Mean and deviation must be greater than 0.0");
		}

		numGen = seed;
		this.shape = shape;
		this.location = location;
	}

	/**
	 * Instantiates a new pareto distr.
	 * 
	 * @param shape the shape
	 * @param location the location
	 */
	public ParetoDistr(double shape, double location) {
		if (shape <= 0.0 || location <= 0.0) {
			throw new IllegalArgumentException("Mean and deviation must be greater than 0.0");
		}

		numGen = new Random(System.currentTimeMillis());
		this.shape = shape;
		this.location = location;
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.distributions.ContinuousDistribution#sample()
	 */
	@Override
	public double sample() {
		return location / Math.pow(numGen.nextDouble(), 1 / shape);
	}

}
