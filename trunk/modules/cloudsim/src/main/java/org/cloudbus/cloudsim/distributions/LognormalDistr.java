/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.distributions;

import java.util.Random;

import org.apache.commons.math3.distribution.LogNormalDistribution;

/**
 * The Class LognormalDistr.
 * 
 * @author Marcos Dias de Assuncao
 * @since CloudSim Toolkit 1.0
 */
public class LognormalDistr implements ContinuousDistribution {

	
	/** The num gen. */
	private final LogNormalDistribution numGen;


	/**
	 * Instantiates a new lognormal distr.
	 * 
	 * @param seed the seed
	 * @param mean the mean
	 * @param dev the dev
	 */
	public LognormalDistr(Random seed, double shape, double scale) {
		this(shape, scale);
		numGen.reseedRandomGenerator(seed.nextLong());
	}

	/**
	 * Instantiates a new lognormal distr.
	 * 
	 * @param mean the mean
	 * @param dev the dev
	 */
	public LognormalDistr(double shape, double scale) {
		numGen = new LogNormalDistribution(scale, shape);
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.distributions.ContinuousDistribution#sample()
	 */
	@Override
	public double sample() {
		return numGen.sample();
	}

}
