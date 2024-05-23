package org.cloudbus.cloudsim.examples.network.datacenter;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.GuestEntity;
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

public class TandemAppExample {

	private static List<GuestEntity> guestList;

	private static List<NetworkHost> hostList = new ArrayList<>();

	private static List<AppCloudlet> appCloudletList;

	private static NetworkDatacenter datacenter;

	private static NetworkDatacenterBroker broker;

	private static final int numberOfHosts = 2;
	private static final int numberOfVms = 4;

	/**
	 * Example of tandem DAG: A ---> B
	 * with Datacenter configuration:        switch
	 * 								   		 /	 \
	 * 									 Host0    Host1
	 * 						   			VM0 VM2  VM1 VM3
	 *
	 * Depending on the cloudlet placement, the network may be used or not.
	 * 
	 * @param args the args
	 * @author Remo Andreoli
	 */
	public static void main(String[] args) {

		Log.printLine("Starting TandemAppExample...");

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
			NetworkDatacenterBroker.setLinkDC(datacenter);
			// broker.setLinkDC(datacenter0);
			// Fifth step: Create one Cloudlet

			guestList = CreateVMs(datacenter.getId());

			appCloudletList = CreateAppCloudlets(1);

			// submit vm list to the broker

			broker.submitGuestList(guestList);
			broker.submitCloudletList(appCloudletList);

			// Sixth step: Starts the simulation
			CloudSim.startSimulation();

			CloudSim.stopSimulation();

			// Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();
			printCloudletList(newList);
			System.out.println("numberofcloudlet " + newList.size() + " Cached "
					+ NetworkDatacenterBroker.cachedcloudlet + " Data transfered "
					+ NetworkTags.totaldatatransfer);

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
		// 2. A Machine contains one or more PEs or CPUs/Cores.
		// In this example, it will have only one core.
		// List<Pe> peList = new ArrayList<Pe>();

		int mips = 10;
		int ram = 2048; // host memory (MB)
		long storage = 1000000; // host storage
		int bw = 100000;

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

	/**
	 * Creates the broker.
	 * 
	 * @return the datacenter broker
	 */
	private static NetworkDatacenterBroker createBroker() {
		NetworkDatacenterBroker broker = null;
		try {
			broker = new NetworkDatacenterBroker("Broker", 2);
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
		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID"
				+ indent + "Time" + indent + "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (Cloudlet cloudlet : list) {
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

		return vmList;
	}

	private static ArrayList<AppCloudlet> CreateAppCloudlets(int apps) {
		ArrayList<AppCloudlet> cloudletList = new ArrayList<>();

		for(int appId = 0; appId < apps; appId++) {
			AppCloudlet app = new AppCloudlet(AppCloudlet.APP_Workflow, appId, 2000, broker.getId());
			createTaskList(app);

			cloudletList.add(app);
		}

		return cloudletList;
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
		cla.setUserId(appCloudlet.getUserId());
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
		clb.setUserId(appCloudlet.getUserId());
		clb.submittime = CloudSim.clock();
		clb.currStagenum = -1;
		clb.setGuestId(guestList.get(1).getId());
		appCloudlet.cList.add(clb);

		// Configure task stages within the cloudlets
		//
		cla.stages.add(new TaskStage(NetworkTags.EXECUTION, -1, 1000, 0, memory, cla));
		cla.stages.add(new TaskStage(NetworkTags.WAIT_SEND, 1000, -1, 1, memory, clb));

		//
		clb.stages.add(new TaskStage(NetworkTags.WAIT_RECV, -1, 0, 0, memory, cla));
		clb.stages.add(new TaskStage(NetworkTags.EXECUTION, -1, 1000, 1, memory, clb));
	}

	private static void CreateNetwork(NetworkDatacenter dc) {
		// Create ToR switch
		Switch ToRSwitch = new Switch("Edge0", NetworkConstants.EdgeSwitchPort, NetworkTags.EDGE_LEVEL,
					0, NetworkConstants.BandWidthEdgeHost, NetworkConstants.BandWidthEdgeAgg, dc);

		dc.registerSwitch(ToRSwitch);

		// Attach to hosts
		for (NetworkHost netHost : dc.<NetworkHost>getHostList()) {
			dc.attachSwitchToHost(ToRSwitch, netHost);
		}

	}
}
