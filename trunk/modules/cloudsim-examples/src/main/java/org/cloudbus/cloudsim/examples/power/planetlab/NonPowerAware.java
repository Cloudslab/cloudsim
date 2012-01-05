package org.cloudbus.cloudsim.examples.power.planetlab;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.power.Constants;
import org.cloudbus.cloudsim.examples.power.Helper;
import org.cloudbus.cloudsim.power.PowerDatacenterNonPowerAware;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicySimple;

public class NonPowerAware {

	public static void main(String[] args) throws IOException {
		String experimentName = "npa";
		String outputFolder = "output";
		String inputFolder = "input";

		Log.setDisabled(!Constants.ENABLE_OUTPUT);
		Log.printLine("Starting " + experimentName);

		try {
			CloudSim.init(1, Calendar.getInstance(), false);

			DatacenterBroker broker = Helper.createBroker();
			int brokerId = broker.getId();

			List<Cloudlet> cloudletList = PlanetLabHelper.createCloudletListPlanetLab(brokerId, inputFolder);
			List<Vm> vmList = Helper.createVmList(brokerId, cloudletList.size());
			List<PowerHost> hostList = Helper.createHostList(cloudletList.size() / 2);

			PowerDatacenterNonPowerAware datacenter = (PowerDatacenterNonPowerAware) Helper.createDatacenter(
					"Datacenter",
					PowerDatacenterNonPowerAware.class,
					hostList,
					new PowerVmAllocationPolicySimple(hostList));

			datacenter.setDisableMigrations(true);

			broker.submitVmList(vmList);
			broker.submitCloudletList(cloudletList);

			double lastClock = CloudSim.startSimulation();

			List<Cloudlet> newList = broker.getCloudletReceivedList();
			Log.printLine("Received " + newList.size() + " cloudlets");

			CloudSim.stopSimulation();

			Helper.printResults(
					datacenter,
					vmList,
					lastClock,
					experimentName,
					Constants.OUTPUT_CSV,
					outputFolder);

		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}

		Log.printLine("Finished " + experimentName);
	}

}
