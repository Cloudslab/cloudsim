package org.cloudbus.cloudsim.examples.power.planetlab;

import java.io.IOException;

import org.cloudbus.cloudsim.examples.power.RunnerAbstract;

public class IqrMc extends RunnerAbstract {

	public static void main(String[] args) throws IOException {
		boolean enableOutput = true;
		String inputFolder = "input";
		String outputFolder = "output";
		String workload = "20110303";
		String vmAllocationPolicy = "iqr";
		String vmSelectionPolicy = "mc";
		String parameter = "1.0";

		run(enableOutput, false, inputFolder, outputFolder, workload, vmAllocationPolicy, vmSelectionPolicy,
				parameter);
	}

}
