/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2010, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

/**
 * This interface must be implemented by sensors to
 * specific data center features.
 *
 * @author		Rodrigo N. Calheiros
 * @since		CloudSim Toolkit 1.0
 */
public interface Sensor<T extends Number> {

	/**
	 * Sets the data center the sensor monitors
	 * @param dataCenter data center monitored by this sensor
	 */
	void setDatacenter(FederatedDatacenter datacenter);

	/**
	 * Updates internal measurement of the monitored feature
	 * @return -1 if current measurements falls below the minumum threshold,
	 *         +1 if measurement falls above the maximum threshold,
	 *          0 if the measurement is within the specified interval
	 */
	int monitor();

	/**
	 * Sets the upper limit of the feature monitored by the sensor
	 * @param value maximum value allowed to this sensor
	 */
	void setUpperThreshold(T value);

	/**
	 * Sets the lower limit of the feature monitored by the sensor
	 * @param value minimum value allowed to this sensor
	 */
	void setLowerThreshold(T value);

}
