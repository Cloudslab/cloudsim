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
 * A pseudo random number generator following the
 * <a href="http://en.wikipedia.org/wiki/Zipf's_law">Zipf</a> distribution.
 * 
 * @author Marcos Dias de Assuncao
 * @since CloudSim Toolkit 1.0
 */
public class ZipfDistr implements ContinuousDistribution {

	/** The internal random number generator. */
	private final Random numGen;

	/** The shape. */
	private final double shape;

	/** The den. */
	private double den;

	/**
	 * Instantiates a new Zipf pseudo random number generator.
	 * 
	 * @param seed the seed
	 * @param shape the shape
	 * @param population the population
	 */
	public ZipfDistr(long seed, double shape, int population) {
		if (shape <= 0.0 || population < 1) {
			throw new IllegalArgumentException("Mean must be greater than 0.0 and population greater than 0");
		}
		numGen = new Random(seed);
		this.shape = shape;

		computeDen(shape, population);
	}

	/**
	 * Instantiates a new Zipf pseudo random number generator.
	 * 
	 * @param shape the shape
	 * @param population the population
	 */
	public ZipfDistr(double shape, int population) {
		if (shape <= 0.0) {
			throw new IllegalArgumentException("Mean must be greated than 0.0 and population greater than 0");
		}
		numGen = new Random(System.currentTimeMillis());
		this.shape = shape;
		computeDen(shape, population);
	}

	@Override
	public double sample() {
		double variate = numGen.nextDouble();
		double num = 1;
		double nextNum = 1 + 1 / Math.pow(2, shape);
		double j = 3;

		while (variate > nextNum / den) {
			num = nextNum;
			nextNum += 1 / Math.pow(j, shape);
			j++;
		}

		return num / den;
	}

	/**
	 * Compute the den.
	 * 
	 * @param shape the shape
	 * @param population the population
	 */
	private void computeDen(double shape, int population) {
		den = 0.0;
		for (int j = 1; j <= population; j++) {
			den += 1 / Math.pow(j, shape);
		}
	}

}
