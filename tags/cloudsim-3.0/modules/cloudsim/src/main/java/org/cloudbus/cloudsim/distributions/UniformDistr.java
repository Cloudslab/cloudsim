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
 * A random number generator based on the Uniform distribution.
 * 
 * @author Marcos Dias de Assuncao
 * @since CloudSim Toolkit 1.0
 */
public class UniformDistr implements ContinuousDistribution {

	/** The num gen. */
	private final Random numGen;

	/** The min. */
	private final double mag, min;

	/**
	 * Creates new uniform distribution.
	 * 
	 * @param min minimum value
	 * @param max maximum value
	 */
	public UniformDistr(double min, double max) {
		if (min >= max) {
			throw new IllegalArgumentException("Maximum must be greater than the minimum.");
		}
		numGen = new Random();
		mag = max - min;
		this.min = min;
	}

	/**
	 * Creates new uniform distribution.
	 * 
	 * @param min minimum value
	 * @param max maximum value
	 * @param seed simulation seed to be used
	 */
	public UniformDistr(double min, double max, long seed) {
		if (min >= max) {
			throw new IllegalArgumentException("Maximum must be greater than the minimum.");
		}

		numGen = new Random(seed);
		mag = max - min;
		this.min = min;
	}

	/**
	 * Generate a new random number.
	 * 
	 * @return the next random number in the sequence
	 */
	@Override
	public double sample() {
		return (numGen.nextDouble() * (mag)) + min;
	}

	/**
	 * Generates a new random number based on the number generator and values provided as
	 * parameters.
	 * 
	 * @param rd the random number generator
	 * @param min the minimum value
	 * @param max the maximum value
	 * @return the next random number in the sequence
	 */
	public static double sample(Random rd, double min, double max) {
		if (min >= max) {
			throw new IllegalArgumentException("Maximum must be greater than the minimum.");
		}

		return (rd.nextDouble() * (max - min)) + min;
	}

	/**
	 * Set the random number generator's seed.
	 * 
	 * @param seed the new seed for the generator
	 */
	public void setSeed(long seed) {
		numGen.setSeed(seed);
	}

}