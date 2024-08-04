package org.cloudbus.cloudsim.examples.network.datacenter;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.distributions.UniformDistr;
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

public class BagOfTaskAppExample {

	/** The vmList. */
	private static List<Vm> vmList;

	private static List<AppCloudlet> appCloudletList;

	private static NetworkDatacenter datacenter;

	private static DatacenterBroker broker;

	private static int numberOfHosts = (int) (NetworkConstants.EdgeSwitchPort * NetworkConstants.AggSwitchPort * NetworkConstants.RootSwitchPort);

	private static int numberOfVms = numberOfHosts * NetworkConstants.maxhostVM;;

	private static int numberOfCloudlets = 4;

	/**
	 * 	 Example of bags of tasks:
	 * 	 									       Worker1
	 * 	 									      /
	 * 	 							Gatherer0 <--- Worker2
	 * 	 										  \
	 * 	 										   Worker3
	 *
	 *
	 * 	 with Datacenter configuration:           _______switch______
	 * 	  								   		 /	  /      |       \
	 * 	  									 Host0    Host1  Host2    Host3.
	 * 
	 * @param args
	 *            the args
	 */
	public static void main(String[] args) {

		Log.println("Starting CloudSimExample1...");

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
			numberOfVms = datacenter.getHostList().size() * NetworkConstants.maxhostVM;

			// Third step: Create Broker
			broker = new DatacenterBroker("Broker");
			// Fifth step: Create one Cloudlet

			vmList = CreateVMs(datacenter.getId());

			appCloudletList = CreateAppCloudlets(1);

			// submit vm list to the broker

			broker.submitGuestList(vmList);
			broker.submitCloudletList(appCloudletList.get(0).cList);

			// Sixth step: Starts the simulation
			CloudSim.startSimulation();

			CloudSim.stopSimulation();

			// Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();
			printCloudletList(newList);
			System.out.println("numberofcloudlet " + newList.size() + " Data transfered "
					+ datacenter.totalDataTransfer);

			Log.println("CloudSimExample1 finished!");
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

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store
		// our machine

		List<NetworkHost> hostList = new ArrayList<>();

		// 2. A Machine contains one or more PEs or CPUs/Cores.
		// In this example, it will have only one core.
		// List<Pe> peList = new ArrayList<Pe>();

		int mips = 1;

		// 3. Create PEs and add these into a list.
		// peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to
		// store Pe id and MIPS Rating

		// 4. Create Host with its id and list of PEs and add them to the list
		// of machines
		int ram = 2048; // host memory (MB)
		long storage = 1000000; // host storage
		int bw = 10000;
		for (int i = 0; i < numberOfHosts; i++) {
			// 2. A Machine contains one or more PEs or CPUs/Cores.
			// In this example, it will have only one core.
			// 3. Create PEs and add these into an object of PowerPeList.
			List<Pe> peList = new ArrayList<>();
			peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to
			// store
			// PowerPe
			// id and
			// MIPS
			// Rating
			peList.add(new Pe(1, new PeProvisionerSimple(mips))); // need to
			// store
			// PowerPe
			// id and
			// MIPS
			// Rating
			peList.add(new Pe(2, new PeProvisionerSimple(mips))); // need to
			// store
			// PowerPe
			// id and
			// MIPS
			// Rating
			peList.add(new Pe(3, new PeProvisionerSimple(mips))); // need to
			// store
			// PowerPe
			// id and
			// MIPS
			// Rating
			peList.add(new Pe(4, new PeProvisionerSimple(mips))); // need to
			// store
			// PowerPe
			// id and
			// MIPS
			// Rating
			peList.add(new Pe(5, new PeProvisionerSimple(mips))); // need to
			// store
			// PowerPe
			// id and
			// MIPS
			// Rating
			peList.add(new Pe(6, new PeProvisionerSimple(mips))); // need to
			// store
			// PowerPe
			// id and
			// MIPS
			// Rating
			peList.add(new Pe(7, new PeProvisionerSimple(mips))); // need to
			// store
			// PowerPe
			// id and
			// MIPS
			// Rating

			// 4. Create PowerHost with its id and list of PEs and add them to
			// the list of machines
			hostList.add(new NetworkHost(
					i,
					new RamProvisionerSimple(ram),
					new BwProvisionerSimple(bw),
					storage,
					peList,
					new VmSchedulerTimeShared(peList))); // This is our machine
		}

		// 5. Create a DatacenterCharacteristics object that stores the
		// properties of a data center: architecture, OS, list of
		// Machines, allocation policy: time- or space-shared, time zone
		// and its price (G$/Pe time unit).
		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this
		// resource
		double costPerBw = 0.0; // the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<>(); // we are
		// not
		// adding
		// SAN
		// devices by now

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

		// 6. Finally, we need to create a NetworkDatacenter object.
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
		Log.println();
		Log.println("========== OUTPUT ==========");
		Log.println("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID"
				+ indent + "Time" + indent + "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (Cloudlet value : list) {
			cloudlet = value;
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

	/**
	 * Creates virtual machines in a datacenter
	 * @param datacenterId The id of the datacenter where to create the VMs.
	 */
	private static ArrayList<Vm> CreateVMs(int datacenterId) {
		ArrayList<Vm> vmList = new ArrayList<>();

		for (int i = 0; i < numberOfVms; i++) {
			int vmid = i;
			int mips = 1;
			long size = 10000; // image size (MB)
			int ram = 512; // vm memory (MB)
			long bw = 1000;
			int pesNumber = NetworkConstants.HOST_PEs / NetworkConstants.maxhostVM;
			String vmm = "Xen"; // VMM name

			// create VM
			Vm vm = new Vm(
					vmid,
					broker.getId(),
					mips,
					pesNumber,
					ram,
					bw,
					size,
					vmm,
					new CloudletSchedulerTimeShared());

			vmList.add(vm);
		}

		return vmList;
	}

	private static ArrayList<AppCloudlet> CreateAppCloudlets(int apps) {
		ArrayList<AppCloudlet> cloudletList = new ArrayList<>();
		UniformDistr ufrnd = new UniformDistr(0, vmList.size(), 5);

		for(int appId = 0; appId < apps; appId++) {
			AppCloudlet app = new AppCloudlet(AppCloudlet.APP_Workflow, appId, 0, broker.getId());
			cloudletList.add(app);

			// Randomly select vm ids for the cloudlets within the app
			List<Integer> vmIds = new ArrayList<>();
			for (int i = 0; i < numberOfCloudlets; i++) {
				vmIds.add((int) ufrnd.sample());
			}

			if (!vmIds.isEmpty()) {
				createTaskList(app, vmIds);
				for (int i = 0; i < numberOfCloudlets; i++) {
					app.cList.get(i).setUserId(broker.getId());
				}
			}

		}

		return cloudletList;
	}

	static private void createTaskList(AppCloudlet appCloudlet, List<Integer> vmIdList) {
		//basically, each task runs the simulation and then data is consolidated in one task
		long executionTime = (long) 100 / numberOfVms;
		long fileSize = NetworkConstants.FILE_SIZE;
		long outputSize = NetworkConstants.OUTPUT_SIZE;
		int pesNumber = NetworkConstants.PES_NUMBER;
		int stgId=0;

		// First cloudlet (i=0) is the gatherer
		for(int i = 0; i< numberOfCloudlets; i++){
			UtilizationModel utilizationModel = new UtilizationModelFull();
			NetworkCloudlet cl = new NetworkCloudlet(NetworkConstants.currentCloudletId, executionTime, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			NetworkConstants.currentCloudletId++;
			cl.setUserId(broker.getId());
			cl.setGuestId(vmIdList.get(i));
			appCloudlet.cList.add(cl);
		}

		//Configure stages
		NetworkCloudlet gatherer = appCloudlet.cList.get(0);
		gatherer.addExecutionStage(NetworkConstants.COMMUNICATION_LENGTH);
		for(int i = 1; i< numberOfCloudlets; i++) {
			NetworkCloudlet worker = appCloudlet.cList.get(i);
			worker.addExecutionStage(NetworkConstants.COMMUNICATION_LENGTH);
			worker.addSendStage(NetworkConstants.COMMUNICATION_LENGTH, gatherer);

			gatherer.addRecvStage(worker);
		}
	}

	private static void CreateNetwork(NetworkDatacenter dc) {

		// Edge Switch
		Switch[] edgeswitch = new Switch[1];

		for (int i = 0; i < 1; i++) {
			edgeswitch[i] = new Switch("Edge" + i, NetworkConstants.EdgeSwitchPort, Switch.SwitchLevel.EDGE_LEVEL,
					NetworkConstants.SwitchingDelayEdge, NetworkConstants.BandWidthEdgeHost, NetworkConstants.BandWidthEdgeAgg, dc);
			// edgeswitch[i].uplinkSwitches.add(null);
			dc.registerSwitch(edgeswitch[i]);

			// aggswitch[(int)
			// (i/Constants.AggSwitchPort)].downlinkSwitches.add(edgeswitch[i]);
		}

		for (NetworkHost hs : dc.<NetworkHost>getHostList()) {
			int switchnum = (int) (hs.getId() / NetworkConstants.EdgeSwitchPort);

			dc.attachSwitchToHost(edgeswitch[switchnum], hs);
		}
	}
}
