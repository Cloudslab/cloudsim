/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

/**
 * The UtilizationModel interface needs to be implemented in order to provide a fine-grained control
 * over resource usage by a Cloudlet.
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public interface UtilizationModel {

	/**
	 * Returns utilization in percents according to the time.
	 * 
	 * @param time the time
	 * @return utilization percentage
	 */
	double getUtilization(double time);

}