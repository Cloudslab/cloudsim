package org.cloudbus.cloudsim;

/**
 * The Class UtilizationModelFull.
 */
public class UtilizationModelFull implements UtilizationModel {
	
	/* (non-Javadoc)
	 * @see cloudsim.power.UtilizationModel#getUtilization(double)
	 */
	public double getUtilization(double time) {
		return 1;
	}

}
