package org.cloudbus.cloudsim.examples.power.planetlab;

import java.io.File;
import java.io.FilenameFilter;
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
		String workload; // PlanetLab workload
		String vmAllocationPolicy; // Local Regression (LR) VM allocation policy
		String vmSelectionPolicy; // Minimum Migration Time (MMT) VM selection policy
		String parameter; // the safety parameter of the LR policy

		String[] workloadArray = { "20110303", "20110306", "20110309", "20110322", "20110325", "20110403", "20110409", "20110411", "20110412", "2011020" };
		String[] vmAllocationPolicyArray = { "iqr", "lr", "lrr", "mad", "thr" };
		String[] vmSelectionPolicyArray = { "mc", "mmt", "mu", "rs" };

		// Loop thorugh all workloads
		for (String workloadName : workloadArray) {
			workload = workloadName;
			System.out.println("workload: " + workload);
			// Loop through all vmAllocationPolicies
			for (String vmAllocationPolicyName : vmAllocationPolicyArray) {
				vmAllocationPolicy = vmAllocationPolicyName;
				System.out.println("vmAllocationPolicy: " + vmAllocationPolicy);
				// Set safety parameter based on vmAllocationPolicy
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
				// Loop through all vmSelectionPolicies
				for (String vmSelectionPolicyName : vmSelectionPolicyArray) {
					vmSelectionPolicy = vmSelectionPolicyName;
					System.out.println("vmSelectionPolicy: " + vmSelectionPolicy);
					
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