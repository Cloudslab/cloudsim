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

import org.apache.commons.math3.distribution.ParetoDistribution;

/**
 * A pseudo random number generator following the
 * <a href="https://en.wikipedia.org/wiki/Pareto_distribution">Pareto</a> distribution.
 * 
 * @author Marcos Dias de Assuncao
 * @since CloudSim Toolkit 1.0
 */
public class ParetoDistr implements ContinuousDistribution {

	/** The internal Pareto pseudo random number generator. */
	private final ParetoDistribution numGen;

	/**
	 * Instantiates a new Pareto pseudo random number generator.
	 * 
	 * @param seed the seed
	 * @param shape the shape
	 * @param location the location
	 */
	public ParetoDistr(Random seed, double shape, double location) {
		this(shape, location);
		numGen.reseedRandomGenerator(seed.nextLong());
	}

	/**
	 * Instantiates a new Pareto pseudo random number generator.
	 * 
	 * @param shape the shape
	 * @param location the location
	 */
	public ParetoDistr(double shape, double location) {
		numGen = new ParetoDistribution(location, shape);
	}

	@Override
	public double sample() {
		return numGen.sample();
	}

}
