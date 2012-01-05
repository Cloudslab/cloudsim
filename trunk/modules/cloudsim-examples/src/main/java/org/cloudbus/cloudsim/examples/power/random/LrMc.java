package org.cloudbus.cloudsim.examples.power.random;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyMigrationLocalRegression;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyMigrationThresholds;
import org.cloudbus.cloudsim.power.PowerVmSelectionPolicy;
import org.cloudbus.cloudsim.power.PowerVmSelectionPolicyMaximumCorrelation;
import org.cloudbus.cloudsim.power.PowerVmSelectionPolicyRandomSelection;

/**
 * A simulation of a heterogeneous non-power aware data center: all hosts consume maximum power all
 * the time.
 */
public class LrMc {

	/**
	 * Creates main() to run this example.
	 * 
	 * @param args the args
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws IOException {
		String experimentName = "lr_mc";
		String outputFolder = "output";

		Log.setDisabled(!RandomConstants.ENABLE_OUTPUT);
		Log.printLine("Starting " + experimentName);

		try {
			CloudSim.init(1, Calendar.getInstance(), false);

			DatacenterBroker broker = RandomHelper.createBroker();
			int brokerId = broker.getId();

			List<Cloudlet> cloudletList = RandomHelper.createCloudletList(brokerId, RandomConstants.NUMBER_OF_VMS);
			List<Vm> vmList = RandomHelper.createVmList(brokerId, cloudletList.size());
			List<PowerHost> hostList = RandomHelper.createHostList(RandomConstants.NUMBER_OF_HOSTS);

			PowerVmSelectionPolicy vmSelectionPolicy = new PowerVmSelectionPolicyMaximumCorrelation(
					new PowerVmSelectionPolicyRandomSelection());
			PowerDatacenter datacenter = (PowerDatacenter) RandomHelper.createDatacenter("Datacenter",
					PowerDatacenter.class, hostList,
					new PowerVmAllocationPolicyMigrationLocalRegression(hostList, vmSelectionPolicy,
							RandomConstants.SCHEDULING_INTERVAL, new PowerVmAllocationPolicyMigrationThresholds(
									hostList, vmSelectionPolicy, 0.8)), -1);

			datacenter.setDisableMigrations(false);

			broker.submitVmList(vmList);
			broker.submitCloudletList(cloudletList);

			double lastClock = CloudSim.startSimulation();

			List<Cloudlet> newList = broker.getCloudletReceivedList();
			Log.printLine("Received " + newList.size() + " cloudlets");

			CloudSim.stopSimulation();

			RandomHelper.printResults(datacenter, vmList, lastClock, experimentName, RandomConstants.OUTPUT_CSV,
					outputFolder);

		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}

		Log.printLine("Finished " + experimentName);
	}

}
