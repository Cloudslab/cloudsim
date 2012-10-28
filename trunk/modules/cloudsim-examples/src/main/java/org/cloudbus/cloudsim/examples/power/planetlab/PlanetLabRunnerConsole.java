package org.cloudbus.cloudsim.examples.power.planetlab;

import java.io.IOException;

/**
 * This is a universal example runner that can be used for running examples from console. All the
 * parameters are specified as command line parameters.
 * 
 * If you are using any algorithms, policies or workload included in the power package please cite
 * the following paper:
 * 
 * Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012
 * 
 * @author Anton Beloglazov
 * @since Jan 5, 2012
 */
public class PlanetLabRunnerConsole {

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws IOException {
		boolean enableOutput = false;
		boolean outputToFile = true;
		if (args[0].equals("1")) {
			enableOutput = true;
		}
		String inputFolder = args[1];
		String outputFolder = args[2];
		String workload = args[3];
		String vmAllocationPolicy = args[4];
		String vmSelectionPolicy = "";
		if (args.length >= 6 && args[5] != null && !args[5].isEmpty()) {
			vmSelectionPolicy = args[5];
		}
		String parameter = "";
		if (args.length >= 7 && args[6] != null && !args[6].isEmpty()) {
			parameter = args[6];
		}

		new PlanetLabRunner(
				enableOutput,
				outputToFile,
				inputFolder,
				outputFolder,
				workload,
				vmAllocationPolicy,
				vmSelectionPolicy,
				parameter);
	}

}
