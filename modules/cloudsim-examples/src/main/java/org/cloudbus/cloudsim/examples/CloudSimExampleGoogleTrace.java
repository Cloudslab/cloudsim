/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.examples;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.googletrace.GoogleCloudletState;
import org.cloudbus.cloudsim.googletrace.GoogleDatacenter;
import org.cloudbus.cloudsim.googletrace.GoogleTask;
import org.cloudbus.cloudsim.googletrace.GoogleTraceDatacenterBroker;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 * An example showing how to pause and resume the simulation, and create
 * simulation entities (a DatacenterBroker in this example) dynamically.
 */
public class CloudSimExampleGoogleTrace {

	private static List<GoogleTask> googleTasks;

	// //////////////////////// STATIC METHODS ///////////////////////

	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) {
		Log.printLine("Starting CloudSimExample Google Trace ...");
		long now = System.currentTimeMillis();

		try {
			// First step: Initialize the CloudSim package. It should be called
			// before creating any entities.
			int num_user = 1; // number of grid users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			// Datacenters are the resource providers in CloudSim. We need at
			// list one of them to run a CloudSim simulation
			@SuppressWarnings("unused")
			GoogleDatacenter datacenter0 = createDatacenter("cloud-0");

			String traceDatabaseURL = "jdbc:sqlite:/home/giovanni/doutorado/cloud-simulator/cloudrm-sim/data/gtrace_data.sqlite3";
			// Third step: Create Broker
			GoogleTraceDatacenterBroker broker = createBroker(
					"Google_Broker_0", traceDatabaseURL);
			int brokerId = broker.getId();

//			readDB(traceDatabaseURL);
//			createGoogleTasks(traceDatabaseURL, brokerId);
//
//			broker.submitTasks(googleTasks);

			// Fifth step: Starts the simulation
			CloudSim.startSimulation();

			// Final step: Print results when simulation is over
			// List<Cloudlet> newList = broker.getCloudletReceivedList();
			List<GoogleCloudletState> newList = broker.getReceivedCloudlets();

			CloudSim.stopSimulation();

			printCloudletStates(newList);

			Log.printLine("Execution Time "
					+ (((System.currentTimeMillis() - now) / 1000) / 60)
					+ " minutes");
			Log.printLine("CloudSimExample finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
		}
	}

	private static void createGoogleTasks(String databaseURL, int userId)
			throws SQLException {
		// reading traceFile
		Log.printLine("Reading trace from URL ...");
		Connection conn = DriverManager.getConnection(databaseURL);

		googleTasks = new ArrayList<GoogleTask>();

		if (conn != null) {
			Log.printLine("Connected to the database");
			Statement statement = conn.createStatement();
			// ResultSet results = statement
			// .executeQuery("SELECT * FROM tasks WHERE submitTime > '0' LIMIT 100"
			// );

			// ResultSet results = statement
			// .executeQuery("SELECT submitTime, runtime, cpuReq, memReq FROM tasks LIMIT 100000"
			// );

			ResultSet results = statement
					.executeQuery("SELECT * FROM tasks WHERE cpuReq > '0' AND memReq > '0' LIMIT 100000");
			// COUNT: 25834519
			// COUNT: 24776760

			int count = 0;
			while (results.next()) {
				count++;
				// System.out.println(results.getInt("COUNT(*)"));
				// System.out.println(results.getDouble("submitTime") <
				// fiveMinutes);
				System.out.println(results.getDouble("submitTime")
						+ ", "
						// + results.getDouble("jid") + ", "
						// + results.getInt("tid") + ", "
						// + results.getString("user") + ", "
						// + results.getInt("schedulingClass") + ", "
						// + results.getInt("priority") + ", "
						+ (results.getDouble("runtime")) + ","
						// + results.getDouble("endTime") + ", "
						+ results.getDouble("cpuReq") + ", "
						+ results.getDouble("memReq"));// + ", "
				// + results.getString("userClass"));

				GoogleTask task = new GoogleTask(count,
						results.getDouble("submitTime"),
						results.getDouble("runtime"),
						results.getDouble("cpuReq"),
						results.getDouble("memReq"));
				googleTasks.add(task);
			}
		}
	}

	private static void readDB(String databaseURL) throws SQLException {
		long fiveMinutes = 5 * 60 * 1000000;

		Connection conn = DriverManager.getConnection(databaseURL);

		if (conn != null) {
			Log.printLine("Connected to the database");
			Statement statement = conn.createStatement();

			int count = 0;
			long sum = 0;
			// while (sum < 20000000) {
			// ResultSet results = statement
			// .executeQuery("SELECT COUNT(*) FROM tasks WHERE cpuReq > '0' AND memReq > '0' AND submitTime >= '"
			// + (count * fiveMinutes)
			// + "' AND submitTime < '"
			// + ((count + 1) * fiveMinutes) + "'");

			ResultSet results = statement
					.executeQuery("SELECT MAX(submitTime) FROM tasks WHERE cpuReq > '0' AND memReq > '0'");

			while (results.next()) {
				long requests = results.getLong("MAX(submitTime)");
				System.out.println(count + " - Number of request: " + requests);
				sum += requests;
			}
			count++;
			// }
		}
	}

	private static GoogleDatacenter createDatacenter(String name) {

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store one or more
		// Machines
		List<Host> hostList = new ArrayList<Host>();

		// 2. A Machine contains one or more PEs or CPUs/Cores. Therefore,
		// should
		// create a list to store these PEs before creating
		// a Machine.
		List<Pe> peList1 = new ArrayList<Pe>();

		// cpu capacity is 6603.25
		// mem capacity is 5862.75133
		double mips = 6603.25;

		// double mips = 1;

		// 3. Create PEs and add these into the list.
		peList1.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store
																// Pe id and
																// MIPS Rating

		// 4. Create Host with its id and list of PEs and add them to the list
		// of machines
		int hostId = 0;
		int ram = 5862; // capacity x 100000
		long storage = Long.MAX_VALUE; // host storage is not a problem
		int bw = Integer.MAX_VALUE; // bw is not a problem

		Host host = new Host(hostId, new RamProvisionerSimple(ram),
				new BwProvisionerSimple(bw), storage, peList1,
				new VmSchedulerTimeShared(peList1));

		hostList.add(host); // This is our unique machine

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
		double costPerStorage = 0.1; // the cost of using storage in this
										// resource
		double costPerBw = 0.1; // the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are
																		// not
																		// adding
																		// SAN
																		// devices
																		// by
																		// now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch, os, vmm, hostList, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		GoogleDatacenter datacenter = null;
		try {
			datacenter = new GoogleDatacenter(name, characteristics,
					new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	// We strongly encourage users to develop their own broker policies, to
	// submit vms and cloudlets according
	// to the specific rules of the simulated scenario
	private static GoogleTraceDatacenterBroker createBroker(String name,
			String traceDabaseURL) {

		GoogleTraceDatacenterBroker broker = null;
		try {
			broker = new GoogleTraceDatacenterBroker(name, traceDabaseURL,
					"jdbc:sqlite:/tmp/googletasks.sqlite3", 5);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	/**
	 * Prints the Cloudlet objects
	 * 
	 * @param newList
	 *            list of Cloudlets
	 */
	private static void printCloudletStates(List<GoogleCloudletState> newList) {
		int size = newList.size();
		GoogleCloudletState cloudlet;

		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
				+ "Data center ID" + indent + "VM ID" + indent + indent
				+ "Time" + indent + "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = newList.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS");

				Log.printLine(indent + indent + cloudlet.getResourceId()
						+ indent + indent + indent + cloudlet.getCloudletId()
						+ indent + indent + indent + cloudlet.getRuntime()
						+ indent + indent + cloudlet.getStartTime() + indent
						+ indent + indent + cloudlet.getFinishTime());
			}
		}

	}
}
