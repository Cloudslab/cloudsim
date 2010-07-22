package org.cloudbus.cloudsim.distributions;

/**
 * Interface to be implemented by a random number generator.
 * 
 * @author Marcos Dias de Assuncao
 */
public interface ContinuousDistribution {
	
	/**
	 * Sample the random number generator.
	 * @return The sample
	 */
	double sample();
	
}
