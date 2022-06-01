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
		boolean enableOutput = true;
		boolean outputToFile = false;
		String inputFolder = LrMmt.class.getClassLoader().getResource("workload/planetlab").getPath();
		String outputFolder = "output";
		String workload = ""; // PlanetLab workload
		String vmAllocationPolicy = ""; // Local Regression (LR) VM allocation policy
		String vmSelectionPolicy = ""; // Minimum Migration Time (MMT) VM selection policy
		String parameter = ""; // the safety parameter of the LR policy

		File workloadsFolder = new File(
				"/workspace/cloudsim-latest/modules/cloudsim-examples/target/classes/workload/planetlab");
		String[] workloadArray = workloadsFolder.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});

		// Loop thorugh all workloads
		for (String workloadName : workloadArray) {
			workload = workloadName;
			System.out.println(workload);
			new PlanetLabRunner(
					enableOutput,
					outputToFile,
					inputFolder,
					outputFolder,
					workload,
					vmAllocationPolicy,
					vmSelectionPolicy,
					parameter);
		} // End for workload

	} // End main
} // End class