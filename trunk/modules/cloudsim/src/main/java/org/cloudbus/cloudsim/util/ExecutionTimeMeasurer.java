/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.util;

import java.util.HashMap;
import java.util.Map;

/**
 * The class for measuring the execution time.
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 3.0
 */
public class ExecutionTimeMeasurer {

	/** The execution times. */
	private final static Map<String, Long> executionTimes = new HashMap<String, Long>();

	/**
	 * Start.
	 * 
	 * @param name the name
	 */
	public static void start(String name) {
		getExecutionTimes().put(name, System.currentTimeMillis());
	}

	/**
	 * End.
	 * 
	 * @param name the name
	 * @return the double
	 */
	public static double end(String name) {
		double time = (System.currentTimeMillis() - getExecutionTimes().get(name)) / 1000.0;
		getExecutionTimes().remove(name);
		return time;
	}

	/**
	 * Gets the execution times.
	 * 
	 * @return the execution times
	 */
	public static Map<String, Long> getExecutionTimes() {
		return executionTimes;
	}

}
