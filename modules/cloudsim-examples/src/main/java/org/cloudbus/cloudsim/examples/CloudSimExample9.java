package org.cloudbus.cloudsim.examples;

/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.text.DecimalFormat;
import java.util.*;

/**
 * A simple example showing the 2 cloudlet scheduling models: time-shared and space-shared.
 *
 * @author Remo Andreoli
 */
public class CloudSimExample9 {
	public static DatacenterBroker broker;

	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList;
	/** The vmlist. */
	private static List<Vm> vmlist;

	/**
	 * Creates main() to run this example.
	 *
	 * @param args the args
	 */
	public static void main(String[] args) {
		Log.println("Starting CloudSimExample9...");

		try {
			// First step: Initialize the CloudSim package. It should be called before creating any entities.
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance(); // Calendar whose fields have been initialized with the current date and time.
 			boolean trace_flag = false; // trace events

			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			// Datacenters are the resource providers in CloudSim. We need at
			// list one of them to run a CloudSim simulation
			createDatacenter("Datacenter_0");

			// Third step: Create Broker
			broker = new DatacenterBroker("Broker");
			int brokerId = broker.getId();

			// Fourth step: Create one virtual machine
			vmlist = new ArrayList<>();

			// VM description
			int mips = 1000;
			long size = 10000; // image size (MB)
			int ram = 512; // tVm memory (MB)
			long bw = 1000;
			int pesNumber = 1; // number of cpus
			String vmm = "Xen"; // VMM name

			// create time-sharing VM
			vmlist.add(new Vm(0, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared()));

			// create space-sharing VM
			vmlist.add(new Vm(1, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared()));

			// submit vm list to the broker
			broker.submitGuestList(vmlist);


			cloudletList = new ArrayList<>();

			// Cloudlet properties
			int id = 0;
			long fileSize = 300;
			long outputSize = 300;
			UtilizationModel utilizationModel = new UtilizationModelFull();

			Cloudlet cloudlet1 = new Cloudlet(id, 10000, pesNumber, fileSize,
                                        outputSize, utilizationModel, utilizationModel, 
                                        utilizationModel);
			cloudlet1.setUserId(brokerId);
			cloudlet1.setGuestId(0);
			cloudletList.add(cloudlet1);
			id++;

			Cloudlet cloudlet2 = new Cloudlet(id, 100000, pesNumber, fileSize,
					outputSize, utilizationModel, utilizationModel,
					utilizationModel);
			cloudlet2.setUserId(brokerId);
			cloudlet2.setGuestId(0);
			cloudletList.add(cloudlet2);
			id++;

			Cloudlet cloudlet3 = new Cloudlet(id, 1000000, pesNumber, fileSize,
					outputSize, utilizationModel, utilizationModel,
					utilizationModel);
			cloudlet3.setUserId(brokerId);
			cloudlet3.setGuestId(0);
			cloudletList.add(cloudlet3);
			id++;

			Cloudlet cloudlet4 = new Cloudlet(id, 10000, pesNumber, fileSize,
					outputSize, utilizationModel, utilizationModel,
					utilizationModel);
			cloudlet4.setUserId(brokerId);
			cloudlet4.setGuestId(1);
			cloudletList.add(cloudlet4);
			id++;

			Cloudlet cloudlet5 = new Cloudlet(id, 100000, pesNumber, fileSize,
					outputSize, utilizationModel, utilizationModel,
					utilizationModel);
			cloudlet5.setUserId(brokerId);
			cloudlet5.setGuestId(1);
			cloudletList.add(cloudlet5);
			id++;

			Cloudlet cloudlet6 = new Cloudlet(id, 1000000, pesNumber, fileSize,
					outputSize, utilizationModel, utilizationModel,
					utilizationModel);
			cloudlet6.setUserId(brokerId);
			cloudlet6.setGuestId(1);
			cloudletList.add(cloudlet6);
			// submit cloudlet list to the broker
			broker.submitCloudletList(cloudletList);

			// Sixth step: Starts the simulation
			CloudSim.startSimulation();

			CloudSim.stopSimulation();

			//Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();
			newList.sort(Comparator.comparing(Cloudlet::getCloudletId));

			printCloudletList(newList);

			Log.println("CloudSimExample9 finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.println("Unwanted errors happen");
		}
	}

	/**
	 * Creates the datacenter.
	 *
	 * @param name the name
	 *
	 * @return the datacenter
	 */
	private static Datacenter createDatacenter(String name) {

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store
		// our machine
		List<Host> hostList = new ArrayList<>();

		// 2. A Machine contains one or more PEs or CPUs/Cores.
		// In this example, it will have only one core.
		List<Pe> peList = new ArrayList<>();

		int mips = 1000;

		// 3. Create PEs and add these into a list.
		peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating

		// 4. Create Host with its id and list of PEs and add them to the list
		// of machines
		int ram = 2048; // host memory (MB)
		long storage = 1000000; // host storage
		int bw = 10000;

		hostList.add(
			new Host(
				0,
				new RamProvisionerSimple(ram),
				new BwProvisionerSimple(bw),
				storage,
				peList,
				new VmSchedulerTimeShared(peList)
			)
		);

		hostList.add(
				new Host(
						1,
						new RamProvisionerSimple(ram),
						new BwProvisionerSimple(bw),
						storage,
						peList,
						new VmSchedulerTimeShared(peList)
				)
		);

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
		LinkedList<Storage> storageList = new LinkedList<>(); // we are not adding SAN
													// devices by now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch, os, vmm, hostList, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	/**
	 * Prints the Cloudlet objects.
	 *
	 * @param list list of Cloudlets
	 */
	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "    ";
		Log.println();
		Log.println("========== OUTPUT ==========");
		Log.println("Cloudlet ID" + indent + "STATUS" + indent
				+ "Data center ID" + indent + "VM ID" + indent + "Time" + indent
				+ "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (Cloudlet value : list) {
			cloudlet = value;
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getStatus() == Cloudlet.CloudletStatus.SUCCESS) {
				Log.print("SUCCESS");

				Log.println(indent + indent + cloudlet.getResourceId()
						+ indent + indent + indent + cloudlet.getGuestId()
						+ indent + indent
						+ dft.format(cloudlet.getActualCPUTime()) + indent
						+ indent + dft.format(cloudlet.getExecStartTime())
						+ indent + indent
						+ dft.format(cloudlet.getExecFinishTime()));
			}
		}
	}
}