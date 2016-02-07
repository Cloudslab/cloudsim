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
 * Measurement of execution times of CloudSim's methods.
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 3.0
 */
public class ExecutionTimeMeasurer {

	/** A map of execution times where each key
         * represents the name of the method/process being its
         * execution time computed and each key is the
         * time the method/process started (in milliseconds). 
         * Usually, this name is the method/process name, making
         * easy to identify the execution times into the map.
         * 
         * @todo The name of the attribute doesn't match with what it stores.
         * It in fact stores the method/process start time,
         * no the time it spent executing.
         */
	private final static Map<String, Long> executionTimes = new HashMap<String, Long>();

	/**
	 * Start measuring the execution time of a method/process.
         * Usually this method has to be called at the first line of the method
         * that has to be its execution time measured.
	 * 
	 * @param name the name of the method/process being measured.
         * @see #executionTimes
	 */
	public static void start(String name) {
		getExecutionTimes().put(name, System.currentTimeMillis());
	}

	/**
	 * Finalizes measuring the execution time of a method/process.
	 * 
	 * @param name the name of the method/process being measured.
	 * @return the time the method/process spent in execution (in seconds)
         * @see #executionTimes
	 */
	public static double end(String name) {
		double time = (System.currentTimeMillis() - getExecutionTimes().get(name)) / 1000.0;
		getExecutionTimes().remove(name);
		return time;
	}

	/**
	 * Gets map the execution times.
	 * 
	 * @return the execution times map
         * @see #executionTimes
	 */
	public static Map<String, Long> getExecutionTimes() {
		return executionTimes;
	}

}
