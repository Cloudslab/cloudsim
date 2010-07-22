package org.cloudbus.cloudsim.power.models;

/**
 * The Interface PowerModel.
 */
public interface PowerModel {
	
	/**
	 * Get power consumption by the utilization percentage
	 * according to the power model.
	 * 
	 * @param utilization the utilization
	 * 
	 * @return power consumption
	 * 
	 * @throws Exception the exception
	 */
	double getPower(double utilization) throws Exception;

}
