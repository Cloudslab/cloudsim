package org.cloudbus.cloudsim;

/**
 * The Interface UtilizationModel.
 */
public interface UtilizationModel {
	
	/**
	 * Returns utilization in percents according to the time.
	 * 
	 * @param time the time
	 * 
	 * @return utilization percentage
	 */
	double getUtilization(double time);

}