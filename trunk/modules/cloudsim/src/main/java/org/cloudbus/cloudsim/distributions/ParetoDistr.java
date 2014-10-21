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
 * The Class ParetoDistr.
 * 
 * @author Marcos Dias de Assuncao
 * @since CloudSim Toolkit 1.0
 */
public class ParetoDistr implements ContinuousDistribution {

	/** The num gen. */
	private final ParetoDistribution numGen;

	/**
	 * Instantiates a new pareto distr.
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
	 * Instantiates a new pareto distr.
	 * 
	 * @param shape the shape
	 * @param location the location
	 */
	public ParetoDistr(double shape, double location) {
		numGen = new ParetoDistribution(location, shape);
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
