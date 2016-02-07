/*
 * ** Network and Service Differentiation Extensions to CloudSim 3.0 **
 *
 * Gokul Poduval & Chen-Khong Tham
 * Computer Communication Networks (CCN) Lab
 * Dept of Electrical & Computer Engineering
 * National University of Singapore
 * October 2004
 *
 * Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2004, The University of Melbourne, Australia and
 * National University of Singapore
 * ParameterException.java - Thrown for illegal parameters
 *
 */

package org.cloudbus.cloudsim;

/**
 * This exception is to report bad or invalid parameters given during constructor.
 * 
 * @author Gokul Poduval
 * @author Chen-Khong Tham, National University of Singapore
 * @since CloudSim Toolkit 1.0
 * @todo It would be used the native class InvalidArgumentException instead of this new one.
 */
public class ParameterException extends Exception {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The message. */
	private final String message;

	/**
	 * Constructs a new exception with <tt>null</tt> as its detail message.
	 * 
	 * @pre $none
	 * @post $none
	 */
	public ParameterException() {
		super();
		message = null;
	}

	/**
	 * Creates a new ParameterException object.
	 * 
	 * @param message an error message
	 * @pre $none
	 * @post $none
	 */
	public ParameterException(String message) {
		super();
		this.message = message;
	}

	@Override
	public String toString() {
		return message;
	}

}
