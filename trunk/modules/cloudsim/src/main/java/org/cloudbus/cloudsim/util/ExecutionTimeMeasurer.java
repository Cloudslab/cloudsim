package org.cloudbus.cloudsim.util;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class ExecutionTimeMeasurer.
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
