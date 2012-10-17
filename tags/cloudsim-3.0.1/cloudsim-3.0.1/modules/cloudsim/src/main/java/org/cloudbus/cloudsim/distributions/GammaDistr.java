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
 * The Class GammaDistr.
 * 
 * @author Marcos Dias de Assuncao
 * @since CloudSim Toolkit 1.0
 */
public class GammaDistr implements ContinuousDistribution {

	/** The num gen. */
	private final Random numGen;

	/** The alpha. */
	private final int alpha;

	/** The beta. */
	private final double beta;

	/**
	 * Instantiates a new gamma distr.
	 * 
	 * @param seed the seed
	 * @param alpha the alpha
	 * @param beta the beta
	 */
	public GammaDistr(Random seed, int alpha, double beta) {
		if (alpha <= 0 || beta <= 0.0) {
			throw new IllegalArgumentException("Alpha and beta must be greater than 0.0");
		}

		numGen = seed;
		this.alpha = alpha;
		this.beta = beta;
	}

	/**
	 * Instantiates a new gamma distr.
	 * 
	 * @param alpha the alpha
	 * @param beta the beta
	 */
	public GammaDistr(int alpha, double beta) {
		if (alpha <= 0 || beta <= 0.0) {
			throw new IllegalArgumentException("Alpha and beta must be greater than 0.0");
		}

		numGen = new Random(System.currentTimeMillis());
		this.alpha = alpha;
		this.beta = beta;
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.distributions.ContinuousDistribution#sample()
	 */
	@Override
	public double sample() {
		double sum = 0.0;
		for (int i = 0; i < alpha; i++) {
			sum += Math.log(numGen.nextDouble());
		}

		return -beta * sum;
	}

}
