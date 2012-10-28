package org.cloudbus.cloudsim.examples.power;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerDynamicWorkload;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.HostDynamicWorkload;
import org.cloudbus.cloudsim.HostStateHistoryEntry;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.VmSchedulerTimeSharedOverSubscription;
import org.cloudbus.cloudsim.VmStateHistoryEntry;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerDatacenterBroker;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerHostUtilizationHistory;
import org.cloudbus.cloudsim.power.PowerVm;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyMigrationAbstract;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.util.MathUtil;

/**
 * The Class Helper.
 * 
 * If you are using any algorithms, policies or workload included in the power package, please cite
 * the following paper:
 * 
 * Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012
 * 
 * @author Anton Beloglazov
 */
public class Helper {

	/**
	 * Creates the vm list.
	 * 
	 * @param brokerId the broker id
	 * @param vmsNumber the vms number
	 * 
	 * @return the list< vm>
	 */
	public static List<Vm> createVmList(int brokerId, int vmsNumber) {
		List<Vm> vms = new ArrayList<Vm>();
		for (int i = 0; i < vmsNumber; i++) {
			int vmType = i / (int) Math.ceil((double) vmsNumber / Constants.VM_TYPES);
			vms.add(new PowerVm(
					i,
					brokerId,
					Constants.VM_MIPS[vmType],
					Constants.VM_PES[vmType],
					Constants.VM_RAM[vmType],
					Constants.VM_BW,
					Constants.VM_SIZE,
					1,
					"Xen",
					new CloudletSchedulerDynamicWorkload(Constants.VM_MIPS[vmType], Constants.VM_PES[vmType]),
					Constants.SCHEDULING_INTERVAL));
		}
		return vms;
	}

	/**
	 * Creates the host list.
	 * 
	 * @param hostsNumber the hosts number
	 * 
	 * @return the list< power host>
	 */
	public static List<PowerHost> createHostList(int hostsNumber) {
		List<PowerHost> hostList = new ArrayList<PowerHost>();
		for (int i = 0; i < hostsNumber; i++) {
			int hostType = i % Constants.HOST_TYPES;

			List<Pe> peList = new ArrayList<Pe>();
			for (int j = 0; j < Constants.HOST_PES[hostType]; j++) {
				peList.add(new Pe(j, new PeProvisionerSimple(Constants.HOST_MIPS[hostType])));
			}

			hostList.add(new PowerHostUtilizationHistory(
					i,
					new RamProvisionerSimple(Constants.HOST_RAM[hostType]),
					new BwProvisionerSimple(Constants.HOST_BW),
					Constants.HOST_STORAGE,
					peList,
					new VmSchedulerTimeSharedOverSubscription(peList),
					Constants.HOST_POWER[hostType]));
		}
		return hostList;
	}

	/**
	 * Creates the broker.
	 * 
	 * @return the datacenter broker
	 */
	public static DatacenterBroker createBroker() {
		DatacenterBroker broker = null;
		try {
			broker = new PowerDatacenterBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return broker;
	}

	/**
	 * Creates the datacenter.
	 * 
	 * @param name the name
	 * @param datacenterClass the datacenter class
	 * @param hostList the host list
	 * @param vmAllocationPolicy the vm allocation policy
	 * @param simulationLength
	 * 
	 * @return the power datacenter
	 * 
	 * @throws Exception the exception
	 */
	public static Datacenter createDatacenter(
			String name,
			Class<? extends Datacenter> datacenterClass,
			List<PowerHost> hostList,
			VmAllocationPolicy vmAllocationPolicy) throws Exception {
		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this resource
		double costPerBw = 0.0; // the cost of using bw in this resource

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch,
				os,
				vmm,
				hostList,
				time_zone,
				cost,
				costPerMem,
				costPerStorage,
				costPerBw);

		Datacenter datacenter = null;
		try {
			datacenter = datacenterClass.getConstructor(
					String.class,
					DatacenterCharacteristics.class,
					VmAllocationPolicy.class,
					List.class,
					Double.TYPE).newInstance(
					name,
					characteristics,
					vmAllocationPolicy,
					new LinkedList<Storage>(),
					Constants.SCHEDULING_INTERVAL);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		return datacenter;
	}

	/**
	 * Gets the times before host shutdown.
	 * 
	 * @param hosts the hosts
	 * @return the times before host shutdown
	 */
	public static List<Double> getTimesBeforeHostShutdown(List<Host> hosts) {
		List<Double> timeBeforeShutdown = new LinkedList<Double>();
		for (Host host : hosts) {
			boolean previousIsActive = true;
			double lastTimeSwitchedOn = 0;
			for (HostStateHistoryEntry entry : ((HostDynamicWorkload) host).getStateHistory()) {
				if (previousIsActive == true && entry.isActive() == false) {
					timeBeforeShutdown.add(entry.getTime() - lastTimeSwitchedOn);
				}
				if (previousIsActive == false && entry.isActive() == true) {
					lastTimeSwitchedOn = entry.getTime();
				}
				previousIsActive = entry.isActive();
			}
		}
		return timeBeforeShutdown;
	}

	/**
	 * Gets the times before vm migration.
	 * 
	 * @param vms the vms
	 * @return the times before vm migration
	 */
	public static List<Double> getTimesBeforeVmMigration(List<Vm> vms) {
		List<Double> timeBeforeVmMigration = new LinkedList<Double>();
		for (Vm vm : vms) {
			boolean previousIsInMigration = false;
			double lastTimeMigrationFinished = 0;
			for (VmStateHistoryEntry entry : vm.getStateHistory()) {
				if (previousIsInMigration == true && entry.isInMigration() == false) {
					timeBeforeVmMigration.add(entry.getTime() - lastTimeMigrationFinished);
				}
				if (previousIsInMigration == false && entry.isInMigration() == true) {
					lastTimeMigrationFinished = entry.getTime();
				}
				previousIsInMigration = entry.isInMigration();
			}
		}
		return timeBeforeVmMigration;
	}

	/**
	 * Prints the results.
	 * 
	 * @param datacenter the datacenter
	 * @param lastClock the last clock
	 * @param experimentName the experiment name
	 * @param outputInCsv the output in csv
	 * @param outputFolder the output folder
	 */
	public static void printResults(
			PowerDatacenter datacenter,
			List<Vm> vms,
			double lastClock,
			String experimentName,
			boolean outputInCsv,
			String outputFolder) {
		Log.enable();
		List<Host> hosts = datacenter.getHostList();

		int numberOfHosts = hosts.size();
		int numberOfVms = vms.size();

		double totalSimulationTime = lastClock;
		double energy = datacenter.getPower() / (3600 * 1000);
		int numberOfMigrations = datacenter.getMigrationCount();

		Map<String, Double> slaMetrics = getSlaMetrics(vms);

		double slaOverall = slaMetrics.get("overall");
		double slaAverage = slaMetrics.get("average");
		double slaDegradationDueToMigration = slaMetrics.get("underallocated_migration");
		// double slaTimePerVmWithMigration = slaMetrics.get("sla_time_per_vm_with_migration");
		// double slaTimePerVmWithoutMigration =
		// slaMetrics.get("sla_time_per_vm_without_migration");
		// double slaTimePerHost = getSlaTimePerHost(hosts);
		double slaTimePerActiveHost = getSlaTimePerActiveHost(hosts);

		double sla = slaTimePerActiveHost * slaDegradationDueToMigration;

		List<Double> timeBeforeHostShutdown = getTimesBeforeHostShutdown(hosts);

		int numberOfHostShutdowns = timeBeforeHostShutdown.size();

		double meanTimeBeforeHostShutdown = Double.NaN;
		double stDevTimeBeforeHostShutdown = Double.NaN;
		if (!timeBeforeHostShutdown.isEmpty()) {
			meanTimeBeforeHostShutdown = MathUtil.mean(timeBeforeHostShutdown);
			stDevTimeBeforeHostShutdown = MathUtil.stDev(timeBeforeHostShutdown);
		}

		List<Double> timeBeforeVmMigration = getTimesBeforeVmMigration(vms);
		double meanTimeBeforeVmMigration = Double.NaN;
		double stDevTimeBeforeVmMigration = Double.NaN;
		if (!timeBeforeVmMigration.isEmpty()) {
			meanTimeBeforeVmMigration = MathUtil.mean(timeBeforeVmMigration);
			stDevTimeBeforeVmMigration = MathUtil.stDev(timeBeforeVmMigration);
		}

		if (outputInCsv) {
			File folder = new File(outputFolder);
			if (!folder.exists()) {
				folder.mkdir();
			}
			File folder1 = new File(outputFolder + "/stats");
			if (!folder1.exists()) {
				folder1.mkdir();
			}
			File folder2 = new File(outputFolder + "/time_before_host_shutdown");
			if (!folder2.exists()) {
				folder2.mkdir();
			}
			File folder3 = new File(outputFolder + "/time_before_vm_migration");
			if (!folder3.exists()) {
				folder3.mkdir();
			}
			File folder4 = new File(outputFolder + "/metrics");
			if (!folder4.exists()) {
				folder4.mkdir();
			}

			StringBuilder data = new StringBuilder();
			String delimeter = ",";

			data.append(experimentName + delimeter);
			data.append(parseExperimentName(experimentName));
			data.append(String.format("%d", numberOfHosts) + delimeter);
			data.append(String.format("%d", numberOfVms) + delimeter);
			data.append(String.format("%.2f", totalSimulationTime) + delimeter);
			data.append(String.format("%.5f", energy) + delimeter);
			data.append(String.format("%d", numberOfMigrations) + delimeter);
			data.append(String.format("%.10f", sla) + delimeter);
			data.append(String.format("%.10f", slaTimePerActiveHost) + delimeter);
			data.append(String.format("%.10f", slaDegradationDueToMigration) + delimeter);
			data.append(String.format("%.10f", slaOverall) + delimeter);
			data.append(String.format("%.10f", slaAverage) + delimeter);
			// data.append(String.format("%.5f", slaTimePerVmWithMigration) + delimeter);
			// data.append(String.format("%.5f", slaTimePerVmWithoutMigration) + delimeter);
			// data.append(String.format("%.5f", slaTimePerHost) + delimeter);
			data.append(String.format("%d", numberOfHostShutdowns) + delimeter);
			data.append(String.format("%.2f", meanTimeBeforeHostShutdown) + delimeter);
			data.append(String.format("%.2f", stDevTimeBeforeHostShutdown) + delimeter);
			data.append(String.format("%.2f", meanTimeBeforeVmMigration) + delimeter);
			data.append(String.format("%.2f", stDevTimeBeforeVmMigration) + delimeter);

			if (datacenter.getVmAllocationPolicy() instanceof PowerVmAllocationPolicyMigrationAbstract) {
				PowerVmAllocationPolicyMigrationAbstract vmAllocationPolicy = (PowerVmAllocationPolicyMigrationAbstract) datacenter
						.getVmAllocationPolicy();

				double executionTimeVmSelectionMean = MathUtil.mean(vmAllocationPolicy
						.getExecutionTimeHistoryVmSelection());
				double executionTimeVmSelectionStDev = MathUtil.stDev(vmAllocationPolicy
						.getExecutionTimeHistoryVmSelection());
				double executionTimeHostSelectionMean = MathUtil.mean(vmAllocationPolicy
						.getExecutionTimeHistoryHostSelection());
				double executionTimeHostSelectionStDev = MathUtil.stDev(vmAllocationPolicy
						.getExecutionTimeHistoryHostSelection());
				double executionTimeVmReallocationMean = MathUtil.mean(vmAllocationPolicy
						.getExecutionTimeHistoryVmReallocation());
				double executionTimeVmReallocationStDev = MathUtil.stDev(vmAllocationPolicy
						.getExecutionTimeHistoryVmReallocation());
				double executionTimeTotalMean = MathUtil.mean(vmAllocationPolicy
						.getExecutionTimeHistoryTotal());
				double executionTimeTotalStDev = MathUtil.stDev(vmAllocationPolicy
						.getExecutionTimeHistoryTotal());

				data.append(String.format("%.5f", executionTimeVmSelectionMean) + delimeter);
				data.append(String.format("%.5f", executionTimeVmSelectionStDev) + delimeter);
				data.append(String.format("%.5f", executionTimeHostSelectionMean) + delimeter);
				data.append(String.format("%.5f", executionTimeHostSelectionStDev) + delimeter);
				data.append(String.format("%.5f", executionTimeVmReallocationMean) + delimeter);
				data.append(String.format("%.5f", executionTimeVmReallocationStDev) + delimeter);
				data.append(String.format("%.5f", executionTimeTotalMean) + delimeter);
				data.append(String.format("%.5f", executionTimeTotalStDev) + delimeter);

				writeMetricHistory(hosts, vmAllocationPolicy, outputFolder + "/metrics/" + experimentName
						+ "_metric");
			}

			data.append("\n");

			writeDataRow(data.toString(), outputFolder + "/stats/" + experimentName + "_stats.csv");
			writeDataColumn(timeBeforeHostShutdown, outputFolder + "/time_before_host_shutdown/"
					+ experimentName + "_time_before_host_shutdown.csv");
			writeDataColumn(timeBeforeVmMigration, outputFolder + "/time_before_vm_migration/"
					+ experimentName + "_time_before_vm_migration.csv");

		} else {
			Log.setDisabled(false);
			Log.printLine();
			Log.printLine(String.format("Experiment name: " + experimentName));
			Log.printLine(String.format("Number of hosts: " + numberOfHosts));
			Log.printLine(String.format("Number of VMs: " + numberOfVms));
			Log.printLine(String.format("Total simulation time: %.2f sec", totalSimulationTime));
			Log.printLine(String.format("Energy consumption: %.2f kWh", energy));
			Log.printLine(String.format("Number of VM migrations: %d", numberOfMigrations));
			Log.printLine(String.format("SLA: %.5f%%", sla * 100));
			Log.printLine(String.format(
					"SLA perf degradation due to migration: %.2f%%",
					slaDegradationDueToMigration * 100));
			Log.printLine(String.format("SLA time per active host: %.2f%%", slaTimePerActiveHost * 100));
			Log.printLine(String.format("Overall SLA violation: %.2f%%", slaOverall * 100));
			Log.printLine(String.format("Average SLA violation: %.2f%%", slaAverage * 100));
			// Log.printLine(String.format("SLA time per VM with migration: %.2f%%",
			// slaTimePerVmWithMigration * 100));
			// Log.printLine(String.format("SLA time per VM without migration: %.2f%%",
			// slaTimePerVmWithoutMigration * 100));
			// Log.printLine(String.format("SLA time per host: %.2f%%", slaTimePerHost * 100));
			Log.printLine(String.format("Number of host shutdowns: %d", numberOfHostShutdowns));
			Log.printLine(String.format(
					"Mean time before a host shutdown: %.2f sec",
					meanTimeBeforeHostShutdown));
			Log.printLine(String.format(
					"StDev time before a host shutdown: %.2f sec",
					stDevTimeBeforeHostShutdown));
			Log.printLine(String.format(
					"Mean time before a VM migration: %.2f sec",
					meanTimeBeforeVmMigration));
			Log.printLine(String.format(
					"StDev time before a VM migration: %.2f sec",
					stDevTimeBeforeVmMigration));

			if (datacenter.getVmAllocationPolicy() instanceof PowerVmAllocationPolicyMigrationAbstract) {
				PowerVmAllocationPolicyMigrationAbstract vmAllocationPolicy = (PowerVmAllocationPolicyMigrationAbstract) datacenter
						.getVmAllocationPolicy();

				double executionTimeVmSelectionMean = MathUtil.mean(vmAllocationPolicy
						.getExecutionTimeHistoryVmSelection());
				double executionTimeVmSelectionStDev = MathUtil.stDev(vmAllocationPolicy
						.getExecutionTimeHistoryVmSelection());
				double executionTimeHostSelectionMean = MathUtil.mean(vmAllocationPolicy
						.getExecutionTimeHistoryHostSelection());
				double executionTimeHostSelectionStDev = MathUtil.stDev(vmAllocationPolicy
						.getExecutionTimeHistoryHostSelection());
				double executionTimeVmReallocationMean = MathUtil.mean(vmAllocationPolicy
						.getExecutionTimeHistoryVmReallocation());
				double executionTimeVmReallocationStDev = MathUtil.stDev(vmAllocationPolicy
						.getExecutionTimeHistoryVmReallocation());
				double executionTimeTotalMean = MathUtil.mean(vmAllocationPolicy
						.getExecutionTimeHistoryTotal());
				double executionTimeTotalStDev = MathUtil.stDev(vmAllocationPolicy
						.getExecutionTimeHistoryTotal());

				Log.printLine(String.format(
						"Execution time - VM selection mean: %.5f sec",
						executionTimeVmSelectionMean));
				Log.printLine(String.format(
						"Execution time - VM selection stDev: %.5f sec",
						executionTimeVmSelectionStDev));
				Log.printLine(String.format(
						"Execution time - host selection mean: %.5f sec",
						executionTimeHostSelectionMean));
				Log.printLine(String.format(
						"Execution time - host selection stDev: %.5f sec",
						executionTimeHostSelectionStDev));
				Log.printLine(String.format(
						"Execution time - VM reallocation mean: %.5f sec",
						executionTimeVmReallocationMean));
				Log.printLine(String.format(
						"Execution time - VM reallocation stDev: %.5f sec",
						executionTimeVmReallocationStDev));
				Log.printLine(String.format("Execution time - total mean: %.5f sec", executionTimeTotalMean));
				Log.printLine(String
						.format("Execution time - total stDev: %.5f sec", executionTimeTotalStDev));
			}
			Log.printLine();
		}

		Log.setDisabled(true);
	}

	/**
	 * Parses the experiment name.
	 * 
	 * @param name the name
	 * @return the string
	 */
	public static String parseExperimentName(String name) {
		Scanner scanner = new Scanner(name);
		StringBuilder csvName = new StringBuilder();
		scanner.useDelimiter("_");
		for (int i = 0; i < 4; i++) {
			if (scanner.hasNext()) {
				csvName.append(scanner.next() + ",");
			} else {
				csvName.append(",");
			}
		}
		scanner.close();
		return csvName.toString();
	}

	/**
	 * Gets the sla time per active host.
	 * 
	 * @param hosts the hosts
	 * @return the sla time per active host
	 */
	protected static double getSlaTimePerActiveHost(List<Host> hosts) {
		double slaViolationTimePerHost = 0;
		double totalTime = 0;

		for (Host _host : hosts) {
			HostDynamicWorkload host = (HostDynamicWorkload) _host;
			double previousTime = -1;
			double previousAllocated = 0;
			double previousRequested = 0;
			boolean previousIsActive = true;

			for (HostStateHistoryEntry entry : host.getStateHistory()) {
				if (previousTime != -1 && previousIsActive) {
					double timeDiff = entry.getTime() - previousTime;
					totalTime += timeDiff;
					if (previousAllocated < previousRequested) {
						slaViolationTimePerHost += timeDiff;
					}
				}

				previousAllocated = entry.getAllocatedMips();
				previousRequested = entry.getRequestedMips();
				previousTime = entry.getTime();
				previousIsActive = entry.isActive();
			}
		}

		return slaViolationTimePerHost / totalTime;
	}

	/**
	 * Gets the sla time per host.
	 * 
	 * @param hosts the hosts
	 * @return the sla time per host
	 */
	protected static double getSlaTimePerHost(List<Host> hosts) {
		double slaViolationTimePerHost = 0;
		double totalTime = 0;

		for (Host _host : hosts) {
			HostDynamicWorkload host = (HostDynamicWorkload) _host;
			double previousTime = -1;
			double previousAllocated = 0;
			double previousRequested = 0;

			for (HostStateHistoryEntry entry : host.getStateHistory()) {
				if (previousTime != -1) {
					double timeDiff = entry.getTime() - previousTime;
					totalTime += timeDiff;
					if (previousAllocated < previousRequested) {
						slaViolationTimePerHost += timeDiff;
					}
				}

				previousAllocated = entry.getAllocatedMips();
				previousRequested = entry.getRequestedMips();
				previousTime = entry.getTime();
			}
		}

		return slaViolationTimePerHost / totalTime;
	}

	/**
	 * Gets the sla metrics.
	 * 
	 * @param vms the vms
	 * @return the sla metrics
	 */
	protected static Map<String, Double> getSlaMetrics(List<Vm> vms) {
		Map<String, Double> metrics = new HashMap<String, Double>();
		List<Double> slaViolation = new LinkedList<Double>();
		double totalAllocated = 0;
		double totalRequested = 0;
		double totalUnderAllocatedDueToMigration = 0;

		for (Vm vm : vms) {
			double vmTotalAllocated = 0;
			double vmTotalRequested = 0;
			double vmUnderAllocatedDueToMigration = 0;
			double previousTime = -1;
			double previousAllocated = 0;
			double previousRequested = 0;
			boolean previousIsInMigration = false;

			for (VmStateHistoryEntry entry : vm.getStateHistory()) {
				if (previousTime != -1) {
					double timeDiff = entry.getTime() - previousTime;
					vmTotalAllocated += previousAllocated * timeDiff;
					vmTotalRequested += previousRequested * timeDiff;

					if (previousAllocated < previousRequested) {
						slaViolation.add((previousRequested - previousAllocated) / previousRequested);
						if (previousIsInMigration) {
							vmUnderAllocatedDueToMigration += (previousRequested - previousAllocated)
									* timeDiff;
						}
					}
				}

				previousAllocated = entry.getAllocatedMips();
				previousRequested = entry.getRequestedMips();
				previousTime = entry.getTime();
				previousIsInMigration = entry.isInMigration();
			}

			totalAllocated += vmTotalAllocated;
			totalRequested += vmTotalRequested;
			totalUnderAllocatedDueToMigration += vmUnderAllocatedDueToMigration;
		}

		metrics.put("overall", (totalRequested - totalAllocated) / totalRequested);
		if (slaViolation.isEmpty()) {
			metrics.put("average", 0.);
		} else {
			metrics.put("average", MathUtil.mean(slaViolation));
		}
		metrics.put("underallocated_migration", totalUnderAllocatedDueToMigration / totalRequested);
		// metrics.put("sla_time_per_vm_with_migration", slaViolationTimePerVmWithMigration /
		// totalTime);
		// metrics.put("sla_time_per_vm_without_migration", slaViolationTimePerVmWithoutMigration /
		// totalTime);

		return metrics;
	}

	/**
	 * Write data column.
	 * 
	 * @param data the data
	 * @param outputPath the output path
	 */
	public static void writeDataColumn(List<? extends Number> data, String outputPath) {
		File file = new File(outputPath);
		try {
			file.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(0);
		}
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			for (Number value : data) {
				writer.write(value.toString() + "\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	/**
	 * Write data row.
	 * 
	 * @param data the data
	 * @param outputPath the output path
	 */
	public static void writeDataRow(String data, String outputPath) {
		File file = new File(outputPath);
		try {
			file.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(0);
		}
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(data);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	/**
	 * Write metric history.
	 * 
	 * @param hosts the hosts
	 * @param vmAllocationPolicy the vm allocation policy
	 * @param outputPath the output path
	 */
	public static void writeMetricHistory(
			List<? extends Host> hosts,
			PowerVmAllocationPolicyMigrationAbstract vmAllocationPolicy,
			String outputPath) {
		// for (Host host : hosts) {
		for (int j = 0; j < 10; j++) {
			Host host = hosts.get(j);

			if (!vmAllocationPolicy.getTimeHistory().containsKey(host.getId())) {
				continue;
			}
			File file = new File(outputPath + "_" + host.getId() + ".csv");
			try {
				file.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
				System.exit(0);
			}
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				List<Double> timeData = vmAllocationPolicy.getTimeHistory().get(host.getId());
				List<Double> utilizationData = vmAllocationPolicy.getUtilizationHistory().get(host.getId());
				List<Double> metricData = vmAllocationPolicy.getMetricHistory().get(host.getId());

				for (int i = 0; i < timeData.size(); i++) {
					writer.write(String.format(
							"%.2f,%.2f,%.2f\n",
							timeData.get(i),
							utilizationData.get(i),
							metricData.get(i)));
				}
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
	}

	/**
	 * Prints the Cloudlet objects.
	 * 
	 * @param list list of Cloudlets
	 */
	public static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "\t";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Resource ID" + indent + "VM ID" + indent
				+ "Time" + indent + "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId());

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				Log.printLine(indent + "SUCCESS" + indent + indent + cloudlet.getResourceId() + indent
						+ cloudlet.getVmId() + indent + dft.format(cloudlet.getActualCPUTime()) + indent
						+ dft.format(cloudlet.getExecStartTime()) + indent + indent
						+ dft.format(cloudlet.getFinishTime()));
			}
		}
	}

	/**
	 * Prints the metric history.
	 * 
	 * @param hosts the hosts
	 * @param vmAllocationPolicy the vm allocation policy
	 */
	public static void printMetricHistory(
			List<? extends Host> hosts,
			PowerVmAllocationPolicyMigrationAbstract vmAllocationPolicy) {
		for (int i = 0; i < 10; i++) {
			Host host = hosts.get(i);

			Log.printLine("Host #" + host.getId());
			Log.printLine("Time:");
			if (!vmAllocationPolicy.getTimeHistory().containsKey(host.getId())) {
				continue;
			}
			for (Double time : vmAllocationPolicy.getTimeHistory().get(host.getId())) {
				Log.format("%.2f, ", time);
			}
			Log.printLine();

			for (Double utilization : vmAllocationPolicy.getUtilizationHistory().get(host.getId())) {
				Log.format("%.2f, ", utilization);
			}
			Log.printLine();

			for (Double metric : vmAllocationPolicy.getMetricHistory().get(host.getId())) {
				Log.format("%.2f, ", metric);
			}
			Log.printLine();
		}
	}

}
