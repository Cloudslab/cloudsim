package org.cloudbus.cloudsim.examples.power.planetlab;

import java.io.IOException;

/**
 * A simulation of a heterogeneous power aware data center that applies the
 * Local Regression (LR) VM
 * allocation policy and Minimum Migration Time (MMT) VM selection policy.
 * 
 * This example uses a real PlanetLab workload: 20110303.
 * 
 * The remaining configuration parameters are in the Constants and
 * PlanetLabConstants classes.
 * 
 * If you are using any algorithms, policies or workload included in the power
 * package please cite
 * the following paper:
 * 
 * Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic
 * Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of
 * Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience
 * (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012
 * 
 * @author Anton Beloglazov
 * @since Jan 5, 2012
 */
public class RunAll {

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws IOException {
		boolean enableOutput = false;
		boolean outputToFile = false;
		String inputFolder = LrMmt.class.getClassLoader().getResource("workload/planetlab").getPath();
		String outputFolder = "output";
		// Create 3 arrays to hold experiment spcific data
		String[] workloadArray = { "20110303", "20110306", "20110309", "20110322", "20110325", "20110403", "20110409", "20110411", "20110412", "20110420" }; // PlanetLab workloads
		String[] vmAllocationPolicyArray = { "iqr", "lr", "lrr", "mad", "thr" }; // VM allocation policies
		String[] vmSelectionPolicyArray = { "mc", "mmt", "mu", "rs" }; // VM selection policies
		String parameter = ""; // Safety parameter

		// Loop thorugh all PlanetLab workloads
		for (String workload : workloadArray) {
			// Loop through all VM allocation policies
			for (String vmAllocationPolicy : vmAllocationPolicyArray) {
				// Set safety parameter based on VM allocation policy
				if (vmAllocationPolicy.equals("iqr")) {
					parameter = "1.5";
				} else if (vmAllocationPolicy.equals("lr") || vmAllocationPolicy.equals("lrr")) {
					parameter = "1.2";
				} else if (vmAllocationPolicy.equals("mad")) {
					parameter = "2.5";
				} else if (vmAllocationPolicy.equals("thr")) {
					parameter = "0.8";
				} else {
					parameter = "";
					System.out.println("Parameter not set");
					System.exit(0);
				}
				// Loop through all VM Selection Policies
				for (String vmSelectionPolicy : vmSelectionPolicyArray) {
					// Run the simulation
					new PlanetLabRunner(
							enableOutput,
							outputToFile,
							inputFolder,
							outputFolder,
							workload,
							vmAllocationPolicy,
							vmSelectionPolicy,
							parameter);
				} // End for vmSelectionPolicy
			} // End for vmAllocationPolicy
		} // End for workload
	
	} // End main
} // End class