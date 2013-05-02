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
 * The Class LognormalDistr.
 * 
 * @author Marcos Dias de Assuncao
 * @since CloudSim Toolkit 1.0
 */
public class LognormalDistr implements ContinuousDistribution {

	/** The num gen. */
	private final Random numGen;

	/** The mean. */
	private final double mean;

	/** The dev. */
	private final double dev;

	/**
	 * Instantiates a new lognormal distr.
	 * 
	 * @param seed the seed
	 * @param mean the mean
	 * @param dev the dev
	 */
	public LognormalDistr(Random seed, double mean, double dev) {
		if (mean <= 0.0 || dev <= 0.0) {
			throw new IllegalArgumentException("Mean and deviation must be greater than 0.0");
		}

		numGen = seed;
		this.mean = mean;
		this.dev = dev;
	}

	/**
	 * Instantiates a new lognormal distr.
	 * 
	 * @param mean the mean
	 * @param dev the dev
	 */
	public LognormalDistr(double mean, double dev) {
		if (mean <= 0.0 || dev <= 0.0) {
			throw new IllegalArgumentException("Mean and deviation must be greater than 0.0");
		}

		numGen = new Random(System.currentTimeMillis());
		this.mean = mean;
		this.dev = dev;
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.distributions.ContinuousDistribution#sample()
	 */
	@Override
	public double sample() {
		// generate a normal variate from a uniform variate
		double n = Math.sqrt(-2 * Math.log(numGen.nextDouble()))
				* Math.sin(2 * Math.PI * numGen.nextDouble());

		// use it to generate the lognormal variate
		return Math.pow(Math.E, mean + dev * n);
	}

}
