/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.distributions;

import java.util.Random;

import org.apache.commons.math3.distribution.UniformRealDistribution;

/**
 * A pseudo random number generator following the 
 * <a href="https://en.wikipedia.org/wiki/Uniform_distribution_(continuous)">
 * Uniform continuous distribution</a>.
 * 
 * @author Marcos Dias de Assuncao
 * @since CloudSim Toolkit 1.0
 */
public class UniformDistr implements ContinuousDistribution {

	/** The internal uniform pseudo random number generator. */
	private final UniformRealDistribution numGen;

	/**
	 * Creates new uniform pseudo random number generator.
	 * 
	 * @param min minimum value
	 * @param max maximum value
	 */
	public UniformDistr(double min, double max) {
		numGen = new UniformRealDistribution(min, max);
	}

	/**
	 * Creates new uniform pseudo random number generator.
	 * 
	 * @param min minimum value
	 * @param max maximum value
	 * @param seed simulation seed to be used
	 */
	public UniformDistr(double min, double max, long seed) {
		this(min, max);
		numGen.reseedRandomGenerator(seed);
	}

	@Override
	public double sample() {
		return numGen.sample();
	}

	/**
	 * Generates a new pseudo random number based on the generator and values provided as
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
	 * Sets the random number generator's seed.
	 * 
	 * @param seed the new seed for the generator
	 */
	public void setSeed(long seed) {
		numGen.reseedRandomGenerator(seed);
	}

}