/*
 *
 */
package org.cloudbus.cloudsim;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The Class Log.
 */
public class Log {

	/** The output. */
	private static OutputStream output;

	/** The disable output flag. */
	private static boolean disabled;

	/**
	 * Prints the message.
	 *
	 * @param message the message
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
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
	 * Prints the message.
	 *
	 * @param message the message
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void print(long message) {
		print(String.valueOf(message));
	}

	/**
	 * Prints the message.
	 *
	 * @param message the message
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void print(double message) {
		print(String.valueOf(message));
	}

	/**
	 * Prints the line.
	 *
	 * @param message the message
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void printLine(String message) {
		if (!isDisabled()) {
			print(message + System.getProperty("line.separator"));
		}
	}

	/**
	 * Prints the line.
	 *
	 * @param message the message
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void printLine(long message) {
		printLine(String.valueOf(message));
	}

	/**
	 * Prints the line.
	 *
	 * @param message the message
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void printLine(double message) {
		printLine(String.valueOf(message));
	}

	/**
	 * Prints the empty line.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void printLine() {
		if (!isDisabled()) {
			printLine("");
		}
	}

	/**
	 * Sets the output.
	 *
	 * @param _output the new output
	 */
	public static void setOutput(OutputStream _output) {
		output = _output;
	}

	/**
	 * Gets the output.
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
	 * @param disabled the new disabled
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

}
