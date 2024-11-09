package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.EX.DatacenterBrokerEX;
import org.cloudbus.cloudsim.container.utils.CustomCSVWriter;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.core.HostEntity;
import org.cloudbus.cloudsim.core.VirtualEntity;
import org.cloudbus.cloudsim.distributions.ExponentialDistr;
import org.cloudbus.cloudsim.examples.network.datacenter.NetworkConstants;
import org.cloudbus.cloudsim.network.datacenter.*;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.selectionPolicies.SelectionPolicyLeastFull;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

public class CloudSimMultiExtensionPaperExample {

	private static List<VirtualEntity> vmList;
	private static List<GuestEntity> containerList;

	private static List<HostEntity> hostList;

    private static List<AppCloudlet> appCloudletList;

	private static DatacenterBrokerEX broker;

	// For reproducibility purposes (for the CloudSim 7G paper):
	// Please don't touch these 3 variables, or you may cause some out-of-bound exceptions
	// when configuring the various cloudlet placement and virtualization configurations.
	public static final int numberOfHosts = 4;
	public static int numberOfVms = 4;
	public static int numberOfContainers = 4;

	public static int vmVirtOverhead = 5;
	public static int contVirtOverhead = 3;

	public static int numberOfPeriodicActivations = 1;
	public static int firstTaskExecLength = 10000;
	public static int secondTaskExecLength = 10000;
	public static long payloadSize = 1000000000; // 1 Gigabyte

	private static ExponentialDistr distr;

	public static int deadline = 2000;

	public static long vmMips = 7800;
	public static long vmBw = 1000000000; // 1 Gbps

	public static long contMips = vmMips;
	public static long contBw = vmBw;

	public static long hostToSwitchBw = 1000000000; // 1 Gbps
	public static long switchToSwitchBw = 1000000000;

	public static Map<String, Integer> cloudletToGuest;
	public static boolean nestedContainers = false;

	public static String fileInfo = "";

	/**
	 *
	 * A customised version of TandemAppExample5 for the CloudSim7G lab presentation and Wiley SPE paper.
	 *
	 * Example of periodic tandem DAG: A ---> B
	 * with Datacenter configuration:
*                                                Agg. Switch
* 										         /        \
* 										ToR switch        ToR switch
* 								   		 /	 \  		   /	 \
* 									 Host0    Host1     Host2    Host3
	 * 						   		  VM4      VM5       VM6      VM7
	 *                               [C8]     [C9]      [C10]     [C11]  optional
	 *
	 * Depending on the cloudlet placement, the network may be used or not.
	 * 
	 * @param args the args
	 * @author Remo Andreoli
	 */
	public static void main(String[] args) {
		NetworkConstants.currentCloudletId = 0;
		distr = new ExponentialDistr(5, (double) firstTaskExecLength/vmMips + (double) secondTaskExecLength/vmMips);

		Log.println("Starting TandemAppExample5...");

		try {
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			broker = new DatacenterBrokerEX("Broker", 1000000);

			hostList = CreateHosts();

			vmList = CreateVms();
			containerList = CreateContainers();

			appCloudletList = new ArrayList<>();

			for (int i = 0; i < numberOfPeriodicActivations; i++) {
				AppCloudlet app = new AppCloudlet(AppCloudlet.APP_Workflow, i, deadline, broker.getId());
				createTaskList(app);
				appCloudletList.add(app);

				broker.submitCloudletList(app.cList, distr.sample());
			}

			// Datacenters are the resource providers in CloudSim. We need at
			// list one of them to run a CloudSim simulation
			NetworkDatacenter datacenter = createDatacenter("Datacenter_0");

			// submit vm list to the broker
			broker.submitGuestList(vmList);
			broker.submitGuestList(containerList);

			// Sixth step: Starts the simulation
			CloudSim.startSimulation();

			CloudSim.stopSimulation();

			// Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();
			printCloudletList(newList);
			System.out.println("numberofcloudlet " + newList.size() + " Data transfered "
					+ datacenter.totalDataTransfer);

			Log.println("TandemAppExample5 finished!");

			/*for (AppCloudlet app : appCloudletList) {
				for (NetworkCloudlet ncl : app.cList) {
					ncl.stats();
				}
			}*/

			String paddedD = String.format("%04d", deadline);
			String paddedP = String.format("%04d", deadline);
			CustomCSVWriter writer = new CustomCSVWriter("/tmp/D"+paddedD+"-P"+paddedP+fileInfo+".csv");

			String[] header = {"appId", "startTime", "endTime", "makespan","E2E", "period", "lateness", "noise"};
			writer.writeTofile(header, false);

			DecimalFormat df = new DecimalFormat("#.#####");
			df.setRoundingMode(RoundingMode.CEILING);

			for (AppCloudlet app : appCloudletList) {
				String[] data = new String[header.length];

				data[0] = String.valueOf(app.appID);
				data[1] = df.format(app.cList.get(0).getExecStartTime());
				data[2] = df.format(app.cList.get(1).getExecFinishTime());
				data[3] = df.format(app.cList.get(1).getExecFinishTime() - app.cList.get(0).getExecStartTime());
				data[4] = String.valueOf(deadline);
				data[5] = String.valueOf(deadline);
				data[6] = df.format(app.getLateness());
				data[7] = "False";
				writer.writeTofile(data, true);
			}

			for (AppCloudlet app : appCloudletList) {
				Log.println("App #"+ app.appID + " " +(app.cList.get(1).getExecFinishTime() - app.cList.get(0).getExecStartTime()));
			}

			System.out.println("Result in "+writer.getFileAddress());
		} catch (Exception e) {
			e.printStackTrace();
			Log.println("Unwanted errors happen");
		}
	}

	/**
	 * Creates the datacenter.
	 * 
	 * @param name
	 *            the name
	 * 
	 * @return the datacenter
	 */
	private static NetworkDatacenter createDatacenter(String name) {
		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this resource
		double costPerBw = 0.0; // the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<>(); // no SAN devices as of now

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

		NetworkDatacenter datacenter = null;
		try {
			datacenter = new NetworkDatacenter(
					name,
					characteristics,
					new VmAllocationWithSelectionPolicy(hostList, new SelectionPolicyLeastFull()),
					storageList,
					0);

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Create Internal Datacenter network
		CreateNetwork(datacenter);
		return datacenter;
	}

	/**
	 * Prints the Cloudlet objects.
	 * 
	 * @param list
	 *            list of Cloudlets
	 * @throws IOException
	 */
	private static void printCloudletList(List<Cloudlet> list) throws IOException {
		String indent = "    ";
		Log.println();
		Log.println("========== OUTPUT ==========");
		Log.println("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID"
				+ indent + "Time" + indent + "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (Cloudlet cloudlet : list) {
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getStatus() == Cloudlet.CloudletStatus.SUCCESS) {
				Log.print("SUCCESS");
				Log.println(indent + indent + cloudlet.getResourceId() + indent + indent + indent
						+ cloudlet.getGuestId() + indent + indent + dft.format(cloudlet.getActualCPUTime())
						+ indent + indent + dft.format(cloudlet.getExecStartTime()) + indent + indent
						+ dft.format(cloudlet.getExecFinishTime()));
			}
		}

	}

	private static List<HostEntity> CreateHosts() {
		int mips = 100000;
		int ram = 100000; // host memory (MB)
		long storage = 1000000; // host storage
		long bw = 10L * 1024 * 1024 * 1024; // 10 Gbps

		List<HostEntity> hostList = new ArrayList<>();
		for (int i=0; i<numberOfHosts; i++) {
			List<Pe> peList = new ArrayList<>();
			peList.add(new Pe(0, new PeProvisionerSimple(mips)));
			peList.add(new Pe(1, new PeProvisionerSimple(mips)));
			hostList.add(new NetworkHost(
					i,
					new RamProvisionerSimple(ram),
					new BwProvisionerSimple(bw),
					storage,
					peList,
					new VmSchedulerTimeShared(peList)));
		}

		return hostList;
	}


	private static List<VirtualEntity> CreateVms() {
		ArrayList<VirtualEntity> gList = new ArrayList<>();

		long size = 2048; // image size (MB)
		int ram = 4096; // vm memory (MB)
		int pesNumber = 1;
		String vmm = "Xen";

		List<Pe> peList = new ArrayList<>();
		peList.add(new Pe(0, new PeProvisionerSimple(vmMips)));

		for (int i=0; i<numberOfVms; i++) {
			gList.add(new NetworkVm(
					4 + i,
					broker.getId(),
					vmMips,
					pesNumber,
					ram,
					vmBw,
					size,
					vmm,
					new CloudletSchedulerTimeShared(),
					new VmSchedulerTimeShared(peList),
					new RamProvisionerSimple(ram),
					new BwProvisionerSimple(vmBw),
					peList));
			gList.getLast().setVirtualizationOverhead(vmVirtOverhead);

			// For nested virtualization
			if (nestedContainers)
				hostList.add(gList.getLast());
		}

		return gList;
	}

	private static List<GuestEntity> CreateContainers() {
		ArrayList<GuestEntity> gList = new ArrayList<>();

		long size = 2048; // image size (MB)
		int ram = 4096; // vm memory (MB)
		int pesNumber = 1;
		String vmm = "Xen";

		for (int i=0; i<numberOfContainers; i++) {
			gList.add(new NetworkContainer(
					8 + i,
					broker.getId(),
					contMips,
					pesNumber,
					ram,
					contBw,
					size,
					vmm,
					new CloudletSchedulerTimeShared()));
			gList.getLast().setVirtualizationOverhead(contVirtOverhead);

			if (nestedContainers) {
				gList.getLast().setHost(hostList.get(numberOfHosts + i));
			}
		}

		return gList;
	}

	static private void createTaskList(AppCloudlet appCloudlet) {
		long fileSize = NetworkConstants.FILE_SIZE;
		long outputSize = NetworkConstants.OUTPUT_SIZE;
		UtilizationModel utilizationModel = new UtilizationModelFull();

		// Define cloudlets
		NetworkCloudlet cla = new NetworkCloudlet(
				NetworkConstants.currentCloudletId,
				0,
				1,
				fileSize,
				outputSize,
				utilizationModel,
				utilizationModel,
				utilizationModel);
		NetworkConstants.currentCloudletId++;
		cla.setUserId(broker.getId());

		if (cloudletToGuest != null && cloudletToGuest.containsKey("cla")) {
			cla.setGuestId(cloudletToGuest.get("cla"));
		} else {
			cla.setGuestId(vmList.getFirst().getId());
		}
		appCloudlet.cList.add(cla);

		NetworkCloudlet clb = new NetworkCloudlet(
				NetworkConstants.currentCloudletId,
				0,
				1,
				fileSize,
				outputSize,
				utilizationModel,
				utilizationModel,
				utilizationModel);
		NetworkConstants.currentCloudletId++;
		clb.setUserId(broker.getId());
		if (cloudletToGuest != null && cloudletToGuest.containsKey("clb")) {
			clb.setGuestId(cloudletToGuest.get("clb"));
		} else {
			clb.setGuestId(vmList.get(1).getId());
		}
		appCloudlet.cList.add(clb);

		// Configure task stages within the cloudlets
		//
		cla.addExecutionStage(firstTaskExecLength);
		cla.addSendStage(payloadSize, clb);

		//
		clb.addRecvStage(cla);
		clb.addExecutionStage(secondTaskExecLength);
	}

	private static void CreateNetwork(NetworkDatacenter dc) {
		Switch ToRSwitch1 = new Switch("Edge0", NetworkConstants.EdgeSwitchPort, Switch.SwitchLevel.EDGE_LEVEL,
					0, hostToSwitchBw, switchToSwitchBw, dc);
		Switch ToRSwitch2 = new Switch("Edge1", NetworkConstants.EdgeSwitchPort, Switch.SwitchLevel.EDGE_LEVEL,
				0, hostToSwitchBw, switchToSwitchBw, dc);

		Switch AggrSwitch = new Switch("Aggr0", NetworkConstants.AggSwitchPort, Switch.SwitchLevel.AGGR_LEVEL,
				0, switchToSwitchBw, switchToSwitchBw, dc);

		dc.registerSwitch(ToRSwitch1);
		dc.registerSwitch(ToRSwitch2);
		dc.registerSwitch(AggrSwitch);

		dc.attachSwitchToSwitch(ToRSwitch1, AggrSwitch);
		dc.attachSwitchToSwitch(ToRSwitch2, AggrSwitch);

		// Attach to hosts
		List<NetworkHost> netHosts = dc.getHostList();
		dc.attachSwitchToHost(ToRSwitch1, netHosts.get(0));
		dc.attachSwitchToHost(ToRSwitch1, netHosts.get(1));

		dc.attachSwitchToHost(ToRSwitch2, netHosts.get(2));
		dc.attachSwitchToHost(ToRSwitch2, netHosts.get(3));
	}
}
