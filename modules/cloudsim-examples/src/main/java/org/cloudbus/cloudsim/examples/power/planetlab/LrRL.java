package org.cloudbus.cloudsim.examples.power.planetlab;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.power.Algorithm;
import org.cloudbus.cloudsim.power.Environment;

/**
 * A simulation of a heterogeneous power aware data center that applies the Local Regression (LR) VM
 * allocation policy and Minimum Migration Time (MMT) VM selection policy.
 * 
 * This example uses a real PlanetLab workload: 20110303.
 * 
 * The remaining configuration parameters are in the Constants and PlanetLabConstants classes.
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
public class LrRL {

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws IOException {
		//Log.printLine("WE ARE STARTING");
		boolean enableOutput = true;
		boolean outputToFile = false;
		String inputFolder = LrMmt.class.getClassLoader().getResource("workload/planetlab").getPath();
		String outputFolder = "output";
		String workload = "20110303"; // PlanetLab workload
		String vmAllocationPolicy = "lr"; // Local Regression (LR) VM allocation policy
		//Here we add our new VM selection policy 
		String vmSelectionPolicy = "RL"; // New VM selection policy
		String parameter = "1.2"; // the safety parameter of the LR policy
		
		int loops = 30;
		Double[] alphas = {0.2, 0.4, 0.6, 0.8, 1.0};
		Double[] gammas = {0.2, 0.4, 0.6, 0.8, 1.0};
		String[] algorithms = {"Q-Learning", "SARSA"};
		String[] policies = {"egreedy", "softmax"};
		
			
		for(int a=0; a<alphas.length; a++) {
			for(int g=0; g<gammas.length; g++) {
					
				double x = 0;
				
				for(int i=0; i<loops; i++) {
					//WE COULD ESSENTIALLY CREATE 10 OF THESE BUT WE NEED TO HAVE THE SAME Q VALUES SO HOW DO WE HOLD ONTO THESE?
						
						
						if(i >= 23) {
							x = 10;
						}
						else {
							x = i*0.4;

						}
						
						Log.printLine("Run Information");
						Log.printLine(i);
						Log.printLine(alphas[a]);
						Log.printLine(gammas[g]);
						Log.printLine(algorithms[0]);
						Log.printLine(policies[0]);
						
						Algorithm.setRandomActionValue(10.0-x);
						Algorithm.setSelectedAlgorithm(algorithms[0]);
						Algorithm.setActionSelectionPolicy(policies[0]);
						Algorithm.setAlpha(alphas[a]);
						Algorithm.setGamma(gammas[g]);
						Algorithm.setMigrationCount(0);
		
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
				//SO AFTER THIS THEN WE NEED TO DO A CLEAR OF ALL THE Q VALUES 
				Environment.clearQValues();
				//System.exit(0);
				
			}
		}
		
		
		
	}

}