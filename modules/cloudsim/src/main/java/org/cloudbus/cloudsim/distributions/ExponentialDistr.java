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
 * An exponential number generator.
 * 
 * @author Marcos Dias de Assuncao
 * @since CloudSim Toolkit 1.0
 */
public class ExponentialDistr implements ContinuousDistribution {

	/** The num gen. */
	private final Random numGen;

	/** The mean. */
	private final double mean;

	/**
	 * Creates a new exponential number generator.
	 * 
	 * @param seed the seed to be used.
	 * @param mean the mean for the distribution.
	 */
	public ExponentialDistr(long seed, double mean) {
		if (mean <= 0.0) {
			throw new IllegalArgumentException("Mean must be greater than 0.0");
		}
		numGen = new Random(seed);
		this.mean = mean;
	}

	/**
	 * Creates a new exponential number generator.
	 * 
	 * @param mean the mean for the distribution.
	 */
	public ExponentialDistr(double mean) {
		if (mean <= 0.0) {
			throw new IllegalArgumentException("Mean must be greated than 0.0");
		}
		numGen = new Random(System.currentTimeMillis());
		this.mean = mean;
	}

	/**
	 * Generate a new random number.
	 * 
	 * @return the next random number in the sequence
	 */
	@Override
	public double sample() {
		return -mean * Math.log(numGen.nextDouble());
	}

}
