/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.distributions;

import org.apache.commons.math3.distribution.ExponentialDistribution;

/**
 * An exponential number generator.
 * 
 * @author Marcos Dias de Assuncao
 * @since CloudSim Toolkit 1.0
 */
public class ExponentialDistr implements ContinuousDistribution {

	/** The num gen. */
	private final ExponentialDistribution numGen;


	/**
	 * Creates a new exponential number generator.
	 * 
	 * @param seed the seed to be used.
	 * @param mean the mean for the distribution.
	 */
	public ExponentialDistr(long seed, double mean) {
		this(mean);
		numGen.reseedRandomGenerator(seed);
	}

	/**
	 * Creates a new exponential number generator.
	 * 
	 * @param mean the mean for the distribution.
	 */
	public ExponentialDistr(double mean) {
		numGen = new ExponentialDistribution(mean);
	}

	/**
	 * Generate a new random number.
	 * 
	 * @return the next random number in the sequence
	 */
	@Override
	public double sample() {
		return numGen.sample();
	}

}
