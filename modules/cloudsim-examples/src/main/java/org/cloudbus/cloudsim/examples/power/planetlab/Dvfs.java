package org.cloudbus.cloudsim.examples.power.planetlab;

import java.io.IOException;

import org.cloudbus.cloudsim.examples.power.RunnerAbstract;

public class Dvfs extends RunnerAbstract {

	public static void main(String[] args) throws IOException {
		boolean enableOutput = true;
		String inputFolder = "workload/planetlab";
		String outputFolder = "output";
		String workload = "20110303";
		String vmAllocationPolicy = "dvfs";
		String vmSelectionPolicy = "";
		String parameter = "";

		run(
				enableOutput,
				false,
				inputFolder,
				outputFolder,
				workload,
				vmAllocationPolicy,
				vmSelectionPolicy,
				parameter);
	}

}
