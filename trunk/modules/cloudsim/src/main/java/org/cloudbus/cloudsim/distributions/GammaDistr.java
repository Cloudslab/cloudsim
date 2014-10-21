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

import org.apache.commons.math3.distribution.GammaDistribution;

/**
 * The Class GammaDistr.
 * 
 * @author Marcos Dias de Assuncao
 * @since CloudSim Toolkit 1.0
 */
public class GammaDistr implements ContinuousDistribution {

	/** The num gen. */
	private final GammaDistribution numGen;

	/**
	 * Instantiates a new gamma distr.
	 * 
	 * @param seed the seed
	 * @param alpha the alpha
	 * @param beta the beta
	 */
	public GammaDistr(Random seed, int shape, double scale) {
		this(shape, scale);
		numGen.reseedRandomGenerator(seed.nextLong());
	}

	/**
	 * Instantiates a new gamma distr.
	 * 
	 * @param alpha the alpha
	 * @param beta the beta
	 */
	public GammaDistr(int shape, double scale) {
		numGen = new GammaDistribution(shape, scale);
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
