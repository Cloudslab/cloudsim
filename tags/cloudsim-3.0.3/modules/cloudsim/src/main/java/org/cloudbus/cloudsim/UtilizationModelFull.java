/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

/**
 * The UtilizationModelFull class is a simple model, according to which a Cloudlet always utilize
 * all the available CPU capacity.
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class UtilizationModelFull implements UtilizationModel {

	/*
	 * (non-Javadoc)
	 * @see cloudsim.power.UtilizationModel#getUtilization(double)
	 */
	@Override
	public double getUtilization(double time) {
		return 1;
	}

}
