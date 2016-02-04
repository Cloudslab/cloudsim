/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

/**
 * The UtilizationModelFull class is a simple model, according to which a Cloudlet always utilizes
 * a given allocated resource at 100%, all the time.
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class UtilizationModelFull implements UtilizationModel {

	/**
	 * Gets the utilization percentage of a given resource
         * in relation to the total capacity of that resource allocated
         * to the cloudlet.
         * @param time the time to get the resource usage, that isn't considered
         * for this UtilizationModel.
	 * @return Always return 1 (100% of utilization), independent of the time.
	 */
	@Override
	public double getUtilization(double time) {
		return 1;
	}

}
