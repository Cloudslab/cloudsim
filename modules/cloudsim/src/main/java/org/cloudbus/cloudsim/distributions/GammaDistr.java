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
 * A pseudo random number generator following the
 * <a href="https://en.wikipedia.org/wiki/Gamma_distribution">Gamma</a> distribution.
 * 
 * @author Marcos Dias de Assuncao
 * @since CloudSim Toolkit 1.0
 */
public class GammaDistr implements ContinuousDistribution {

	/** The internal Gamma pseudo random number generator. */
	private final GammaDistribution numGen;

	/**
	 * Instantiates a new Gamma pseudo random number generator.
	 * 
	 * @param seed the seed
	 * @param shape the shape
	 * @param scale the scale
	 */
	public GammaDistr(Random seed, int shape, double scale) {
		this(shape, scale);
		numGen.reseedRandomGenerator(seed.nextLong());
	}

	/**
	 * Instantiates a new Gamma pseudo random number generator.
	 * 
	 * @param shape the shape
	 * @param scale the scale
	 */
	public GammaDistr(int shape, double scale) {
		numGen = new GammaDistribution(shape, scale);
	}

	@Override
	public double sample() {
		return numGen.sample();
	}

}
