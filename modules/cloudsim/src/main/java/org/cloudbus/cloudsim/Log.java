/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Logger used for performing logging of the simulation process. It provides the ability to
 * substitute the output stream by any OutputStream subclass.
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 * //TODO To add a method to print formatted text, such as the
 * {@link String#format(java.lang.String, java.lang.Object...)} method.
 */
public class Log {

	/** The Constant LINE_SEPARATOR. */
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	/** The stream where the log will the outputted. */
	private static OutputStream output;

	/** Indicates if the logger is disabled or not. If set to true,
         the call for any print method has no effect. */
	private static boolean disabled;
	
	/** Buffer to avoid creating new string builder upon every print. */
	private static final StringBuilder buffer = new StringBuilder();

	/**
	 * Prints a message.
	 * 
	 * @param message the message
	 */
	public static void print(String message) {
		if (!isDisabled()) {
			try {
				getOutput().write(message.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Prints the message passed as a non-String object.
	 * 
	 * @param message the message
	 */
	public static void print(Object message) {
		if (!isDisabled()) {
			print(String.valueOf(message));
		}
	}
	public static void log(String message) {
        logger.log(logLevel, message);
    }

    /**
     * Logs CPU usage information.
     *
     * @param cpuUsage The CPU usage to be logged.
     */
    public static void logCpuUsage(double cpuUsage) {
        log("CPU Usage: " + cpuUsage);
    }

    /**
     * Logs memory usage information.
     *
     * @param memoryUsage The memory usage to be logged.
     */
    public static void logMemoryUsage(double memoryUsage) {
        log("Memory Usage: " + memoryUsage);
    }

    /**
     * Logs electricity usage information.
     *
     * @param electricityUsage The electricity usage to be logged.
     */
    public static void logElectricityUsage(double electricityUsage) {
        log("Electricity Usage: " + electricityUsage);
    }
	
	/**
	 * Prints a message and a new line.
	 * 
	 * @param message the message
	 */
	public static void printLine(String message) {
		if (!isDisabled()) {
			print(message + LINE_SEPARATOR);
		}
	}

	/**
	 * Prints an empty line.
	 */
	public static void printLine() {
		if (!isDisabled()) {
			print(LINE_SEPARATOR);
		}
	}


	/**
	 * Prints the concatenated text representation of the arguments.
	 * 
	 * @param messages the messages to print
	 */
	public static void printConcat(Object... messages) {
		if (!isDisabled()) {
			buffer.setLength(0); // Clear the buffer		    
            for (Object message : messages) {
                buffer.append(message);
            }
			print(buffer);
		}
	}
	
	/**
	 * Prints the concatenated text representation of the arguments and a new line.
	 * 
	 * @param messages the messages to print
	 */
	public static void printConcatLine(Object... messages) {
		if (!isDisabled()) {
			buffer.setLength(0); // Clear the buffer		    
            for (Object message : messages) {
                buffer.append(message);
            }
			printLine(buffer);
		}
	}

	
	
	/**
	 * Prints the message passed as a non-String object and a new line.
	 * 
	 * @param message the message
	 */
	public static void printLine(Object message) {
	    if (!isDisabled()) {
		printLine(String.valueOf(message));
	    }
	}

	
	
	/**
	 * Prints a string formated as in String.format().
	 * 
	 * @param format the format
	 * @param args the args
	 */
	public static void format(String format, Object... args) {
		if (!isDisabled()) {
			print(String.format(format, args));
		}
	}

	/**
	 * Prints a string formated as in String.format(), followed by a new line.
	 * 
	 * @param format the format
	 * @param args the args
	 */
	public static void formatLine(String format, Object... args) {
		if (!isDisabled()) {
			printLine(String.format(format, args));
		}
	}

	/**
	 * Sets the output stream.
	 * 
	 * @param _output the new output
	 */
	public static void setOutput(OutputStream _output) {
		output = _output;
	}

	/**
	 * Gets the output stream.
	 * 
	 * @return the output
	 */
	public static OutputStream getOutput() {
		if (output == null) {
			setOutput(System.out);
		}
		return output;
	}

	/**
	 * Sets the disable output flag.
	 * 
	 * @param _disabled the new disabled
	 */
	public static void setDisabled(boolean _disabled) {
		disabled = _disabled;
	}

	/**
	 * Checks if the output is disabled.
	 * 
	 * @return true, if is disable
	 */
	public static boolean isDisabled() {
		return disabled;
	}

	/**
	 * Disables the output.
	 */
	public static void disable() {
		setDisabled(true);
	}

	/**
	 * Enables the output.
	 */
	public static void enable() {
		setDisabled(false);
	}

}
