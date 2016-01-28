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
 * @todo It has to be seen if the utilization models are only for cloudlets. If yes,
 * the name of the interface and implementing classes would include the word "Cloudlet"
 * to make clear their for what kind of entity they are related.
 */
public interface UtilizationModel {

	/**
	 * Gets the utilization percentage of a given resource.
	 * 
	 * @param time the time to get the resource usage.
	 * @return utilization percentage, from [0 to 1]
	 */
	double getUtilization(double time);

}