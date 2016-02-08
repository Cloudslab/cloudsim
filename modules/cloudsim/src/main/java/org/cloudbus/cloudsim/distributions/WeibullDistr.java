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

import org.apache.commons.math3.distribution.WeibullDistribution;

/**
 * A pseudo random number generator following the 
 * <a href="https://en.wikipedia.org/wiki/Weibull_distribution">Weibull distribution</a>.
 * 
 * @author Marcos Dias de Assuncao
 * @since CloudSim Toolkit 1.0
 */
public class WeibullDistr implements ContinuousDistribution {

	/** The internal Weibull pseudo random number generator. */
	private final WeibullDistribution numGen;

	/**
	 * Instantiates a new Weibull pseudo random number generator.
	 * 
	 * @param seed the seed
	 * @param alpha the alpha
	 * @param beta the beta
	 */
	public WeibullDistr(Random seed, double alpha, double beta) {
		this(alpha, beta);
		numGen.reseedRandomGenerator(seed.nextLong());
	}

	/**
	 * Instantiates a new Weibull pseudo random number generator.
	 * 
	 * @param alpha the alpha
	 * @param beta the beta
	 */
	public WeibullDistr(double alpha, double beta) {
		numGen = new WeibullDistribution(alpha, beta);
	}

	@Override
	public double sample() {
		return numGen.sample();
	}

}
