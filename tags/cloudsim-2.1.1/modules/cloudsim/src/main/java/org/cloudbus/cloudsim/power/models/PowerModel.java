/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2010, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power.models;

/**
 * The PowerModel interface needs to be implemented in order to
 * provide a model of power consumption depending on utilization
 * for system components.
 *
 * @author		Anton Beloglazov
 * @since		CloudSim Toolkit 2.0
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
