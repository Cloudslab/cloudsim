package org.cloudbus.cloudsim.examples.power.planetlab;

import java.io.IOException;

import org.cloudbus.cloudsim.examples.power.RunnerAbstract;

public class Runner extends RunnerAbstract {

	public static void main(String[] args) throws IOException {
		boolean enableOutput = false;
		if (args[0].equals("1")) {
			enableOutput = true;
		}
		System.out.println("enable output: " + enableOutput);
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

		run(enableOutput, true, inputFolder, outputFolder, workload, vmAllocationPolicy, vmSelectionPolicy,
				parameter);
	}

}
