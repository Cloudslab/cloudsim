package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.container.core.Container;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.examples.network.datacenter.NetworkConstants;
import org.cloudbus.cloudsim.network.datacenter.*;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class DiamondAppMultiExtensionExample {

	/** The guestList. */
	private static List<GuestEntity> guestList;

	private static NetworkDatacenter datacenter;

	private static DatacenterBroker broker;

	private static int numberOfHosts = 4;
	private static int numberOfVms = 4;
	private static int numberOfContainers = 0;
	/**
	 * Example of diamond-shaped DAG.
	 *
	 * 	 B
	 * 	/ \
	 * A   D
	 * 	\ /
	 *   C
	 * 
	 * @param args
	 *            the args
	 */
	public static void main(String[] args) {

		Log.printLine("Starting DiamondAppExample...");

		try {
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			// Datacenters are the resource providers in CloudSim. We need at
			// list one of them to run a CloudSim simulation
			datacenter = createDatacenter("Datacenter_0");

			// Third step: Create Broker
			broker = createBroker();

			// Fifth step: Create one Cloudlet

			guestList = CreateVMs(datacenter.getId());

			AppCloudlet app = new AppCloudlet(AppCloudlet.APP_Workflow, 0, 0, broker.getId());
			createTaskList(app);

			// submit vm list to the broker

			broker.submitGuestList(guestList);
			broker.submitCloudletList(app.cList);

			// Sixth step: Starts the simulation
			CloudSim.startSimulation();

			CloudSim.stopSimulation();

			// Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();
			printCloudletList(newList);
			System.out.println("numberofcloudlet " + newList.size() + " Data transfered "
					+ NetworkGlobals.totaldatatransfer);

			Log.printLine("DiamondAppExample finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
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
		List<NetworkHost> hostList = new ArrayList<>();

		// 2. A Machine contains one or more PEs or CPUs/Cores.
		// In this example, it will have only one core.
		// List<Pe> peList = new ArrayList<Pe>();

		int mips = 1;
		int ram = 2048; // host memory (MB)
		long storage = 1000000; // host storage
		int bw = 10000;

		for (int i=0; i<numberOfHosts; i++) {
			List<Pe> peList = new ArrayList<>();
			peList.add(new Pe(0, new PeProvisionerSimple(mips)));
			hostList.add(new NetworkHost(
					i,
					new RamProvisionerSimple(ram),
					new BwProvisionerSimple(bw),
					storage,
					peList,
					new VmSchedulerTimeShared(peList)));
		}



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
					new VmAllocationPolicySimple(hostList),
					storageList,
					0);

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Create Internal Datacenter network
		CreateNetwork(datacenter);
		return datacenter;
	}

	// We strongly encourage users to develop their own broker policies, to
	// submit vms and cloudlets according
	// to the specific rules of the simulated scenario
	/**
	 * Creates the broker.
	 * 
	 * @return the datacenter broker
	 */
	private static DatacenterBroker createBroker() {
		DatacenterBroker broker = null;
		try {
			broker = new DatacenterBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	/**
	 * Prints the Cloudlet objects.
	 * 
	 * @param list
	 *            list of Cloudlets
	 * @throws IOException
	 */
	private static void printCloudletList(List<Cloudlet> list) throws IOException {
		int size = list.size();
		Cloudlet cloudlet;
		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID"
				+ indent + "Time" + indent + "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (Cloudlet value : list) {
			cloudlet = value;
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS");
				Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent
						+ cloudlet.getGuestId() + indent + indent + dft.format(cloudlet.getActualCPUTime())
						+ indent + indent + dft.format(cloudlet.getExecStartTime()) + indent + indent
						+ dft.format(cloudlet.getFinishTime()));
			}
		}

	}

	/**
	 * Creates virtual machines in a datacenter
	 * @param datacenterId The id of the datacenter where to create the VMs.
	 */
	private static ArrayList<GuestEntity> CreateVMs(int datacenterId) {
		ArrayList<GuestEntity> vmList = new ArrayList<>();

		int mips = 1;
		long size = 10000; // image size (MB)
		int ram = 512; // vm memory (MB)
		long bw = 1000;
		int pesNumber = 1;
		String vmm = "Xen";

		for (int i=0; i<numberOfVms; i++) {
			vmList.add(new Vm(
					i,
					broker.getId(),
					mips,
					pesNumber,
					ram,
					bw,
					size,
					vmm,
					new NetworkCloudletSpaceSharedScheduler()));
		}

		String containerManager = "Docker";
		for (int i=numberOfVms; i<numberOfContainers; i++) {
			vmList.add(new Container(
					i,
					broker.getId(),
					mips,
					pesNumber,
					ram,
					bw,
					size,
					containerManager,
					new NetworkCloudletSpaceSharedScheduler(), 0));
		}

		return vmList;
	}

	static private void createTaskList(AppCloudlet appCloudlet) {
		long fileSize = NetworkConstants.FILE_SIZE;
		long outputSize = NetworkConstants.OUTPUT_SIZE;
		int memory = 100;
		UtilizationModel utilizationModel = new UtilizationModelFull();

		// Define cloudlets
		NetworkCloudlet cla = new NetworkCloudlet(
				NetworkConstants.currentCloudletId,
				0,
				1,
				fileSize,
				outputSize,
				memory,
				utilizationModel,
				utilizationModel,
				utilizationModel);
		NetworkConstants.currentCloudletId++;
		cla.setUserId(broker.getId());
		cla.submittime = CloudSim.clock();
		cla.currStagenum = -1;
		cla.setGuestId(guestList.get(0).getId());
		appCloudlet.cList.add(cla);

		NetworkCloudlet clb = new NetworkCloudlet(
				NetworkConstants.currentCloudletId,
				0,
				1,
				fileSize,
				outputSize,
				memory,
				utilizationModel,
				utilizationModel,
				utilizationModel);
		NetworkConstants.currentCloudletId++;
		clb.setUserId(broker.getId());
		clb.submittime = CloudSim.clock();
		clb.currStagenum = -1;
		clb.setGuestId(guestList.get(1).getId());
		appCloudlet.cList.add(clb);

		NetworkCloudlet clc = new NetworkCloudlet(
				NetworkConstants.currentCloudletId,
				0,
				1,
				fileSize,
				outputSize,
				memory,
				utilizationModel,
				utilizationModel,
				utilizationModel);
		NetworkConstants.currentCloudletId++;
		clc.setUserId(broker.getId());
		clc.submittime = CloudSim.clock();
		clc.currStagenum = -1;
		clc.setGuestId(guestList.get(2).getId());
		appCloudlet.cList.add(clc);

		NetworkCloudlet cld = new NetworkCloudlet(
				NetworkConstants.currentCloudletId,
				0,
				1,
				fileSize,
				outputSize,
				memory,
				utilizationModel,
				utilizationModel,
				utilizationModel);
		NetworkConstants.currentCloudletId++;
		cld.setUserId(broker.getId());
		cld.currStagenum = -1;
		cld.setGuestId(guestList.get(3).getId());
		appCloudlet.cList.add(cld);


		// Configure task stages within the cloudlets
		//
		cla.stages.add(new TaskStage(TaskStage.TaskStageStatus.EXECUTION, -1, 1000, 0, memory, cla));
		cla.stages.add(new TaskStage(TaskStage.TaskStageStatus.WAIT_SEND, 1000, -1, 1, memory, clb));
		cla.stages.add(new TaskStage(TaskStage.TaskStageStatus.WAIT_SEND, 1000, -1, 2, memory, clc));

		//
		clb.stages.add(new TaskStage(TaskStage.TaskStageStatus.WAIT_RECV, -1, -1, 0, memory, cla));
		clb.stages.add(new TaskStage(TaskStage.TaskStageStatus.EXECUTION, -1, 1000, 1, memory, clb));
		clb.stages.add(new TaskStage(TaskStage.TaskStageStatus.WAIT_SEND, 1000, -1, 2, memory, cld));

		//
		clc.stages.add(new TaskStage(TaskStage.TaskStageStatus.WAIT_RECV, -1, -1, 0, memory, cla));
		clc.stages.add(new TaskStage(TaskStage.TaskStageStatus.EXECUTION, -1, 2000, 1, memory, clc));
		clc.stages.add(new TaskStage(TaskStage.TaskStageStatus.WAIT_SEND, 1000, -1, 2, memory, cld));

		//
		cld.stages.add(new TaskStage(TaskStage.TaskStageStatus.WAIT_RECV, -1, -1, 0, memory, clb));
		cld.stages.add(new TaskStage(TaskStage.TaskStageStatus.WAIT_RECV, -1, -1, 1, memory, clc));
		cld.stages.add(new TaskStage(TaskStage.TaskStageStatus.EXECUTION, -1, 1000, 2, memory, cld));
	}

	private static void CreateNetwork(NetworkDatacenter dc) {
		// Create switch
		Switch edgeSwitch = new Switch("Edge0", NetworkConstants.EdgeSwitchPort, Switch.SwitchLevel.EDGE_LEVEL,
					NetworkConstants.SwitchingDelayEdge, NetworkConstants.BandWidthEdgeHost, NetworkConstants.BandWidthEdgeAgg, dc);
			// edgeswitch[i].uplinkSwitches.add(null);
		dc.registerSwitch(edgeSwitch);
		// aggswitch[(int)
		// (i/Constants.AggSwitchPort)].downlinkSwitches.add(edgeswitch[i]);

		// Attach to hosts
		for (NetworkHost netHost : dc.<NetworkHost>getHostList()) {
			dc.attachSwitchToHost(edgeSwitch, netHost);
		}

	}
}
