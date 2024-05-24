/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2019, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.examples.web;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.EX.disk.*;
import org.cloudbus.cloudsim.EX.util.CustomLog;
import org.cloudbus.cloudsim.EX.util.TextUtil;
import org.cloudbus.cloudsim.web.ILoadBalancer;
import org.cloudbus.cloudsim.web.SimpleDBBalancer;
import org.cloudbus.cloudsim.web.SimpleWebLoadBalancer;
import org.cloudbus.cloudsim.web.WebCloudlet;
import org.cloudbus.cloudsim.web.workload.StatWorkloadGenerator;
import org.cloudbus.cloudsim.web.workload.brokers.WebBroker;
import org.cloudbus.cloudsim.web.workload.freq.CompositeValuedSet;
import org.cloudbus.cloudsim.web.workload.freq.FrequencyFunction;
import org.cloudbus.cloudsim.web.workload.freq.PeriodicStochasticFrequencyFunction;
import org.cloudbus.cloudsim.web.workload.sessions.ConstSessionGenerator;
import org.cloudbus.cloudsim.web.workload.sessions.ISessionGenerator;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class CloudSimWorkloadWebExample {

    private static DataItem data = new DataItem(5);

    /**
     * Creates main() to run this example
     * 
     * @throws IOException
     * @throws SecurityException
     */
    public static void main(final String[] args) throws SecurityException, IOException {
	long start = System.currentTimeMillis();

	// Step 0: Set up the logger
	Properties props = new Properties();
	try (InputStream is = Files.newInputStream(Paths.get("custom_log.properties"))) {
	    props.load(is);
	}
	CustomLog.configLogger(props);

	try {
	    // Step1: Initialize the CloudSim package. It should be called
	    // before creating any entities.
	    int numBrokers = 1; // number of brokers we'll be using
	    boolean trace_flag = false; // mean trace events

	    // Initialize CloudSim
	    CloudSim.init(numBrokers, Calendar.getInstance(), trace_flag);

	    // Step 2: Create Datacenters - the resource providers in CloudSim
	    Datacenter datacenter0 = createDatacenter("WebDataCenter");

	    // Step 3: Create Broker
	    int refreshTime = 5;
	    WebBroker broker = new WebBroker("Broker", refreshTime, 24 * 3600, datacenter0.getId());

	    // Step 4: Create virtual machines
	    List<Vm> vmlist = new ArrayList<>();

	    // VM description
	    int mips = 250;
	    int ioMips = 200;
	    long size = 10000; // image size (MB)
	    int ram = 512; // vm memory (MB)
	    long bw = 1000;
	    int pesNumber = 1; // number of cpus
	    String vmm = "Xen"; // VMM name

	    // create two VMs
	    HddVm appServerVM = new HddVm("App-Srv", broker.getId(), mips, ioMips, pesNumber,
		    ram, bw, size, vmm, new HddCloudletSchedulerTimeShared(), new Integer[0]);
	    HddVm appServerVM2 = new HddVm("App-Srv", broker.getId(), mips, ioMips, pesNumber,
		    ram, bw, size, vmm, new HddCloudletSchedulerTimeShared(), new Integer[0]);

	    HddVm dbServerVM = new HddVm("Db-Srv", broker.getId(), mips, ioMips, pesNumber,
		    ram, bw, size, vmm, new HddCloudletSchedulerTimeShared(), new Integer[0]);

	    ILoadBalancer balancer = new SimpleWebLoadBalancer(
		    1, "127.0.0.1", Arrays.asList(appServerVM, appServerVM2), new SimpleDBBalancer(dbServerVM));
	    broker.addLoadBalancer(balancer);

	    // add the VMs to the vmList
	    vmlist.addAll(balancer.getAppServers());
	    vmlist.addAll(balancer.getDbBalancer().getVMs());

	    // submit vm list to the broker
	    broker.submitGuestList(vmlist);

	    List<StatWorkloadGenerator> workloads = generateWorkloads();
	    broker.addWorkloadGenerators(workloads, balancer.getAppId());

	    // Sixth step: Starts the simulation
	    CloudSim.startSimulation();

	    // Final step: Print results when simulation is over
	    List<Cloudlet> newList = broker.getCloudletReceivedList();

	    CloudSim.stopSimulation();

	    printCloudletList(newList);

	    // Print the debt of each user to each datacenter
	    // datacenter0.printDebts();

	    System.err.println("\nSimulation is finished!");
	} catch (Exception e) {
	    e.printStackTrace();
	    System.err.println("The simulation has been terminated due to an unexpected error");
	}

	long end = System.currentTimeMillis();
	System.out.println("Finished in " + (end - start) / 1000 + "seconds");
    }

    private static List<StatWorkloadGenerator> generateWorkloads() {
	List<StatWorkloadGenerator> workloads = new ArrayList<>();

	int asCloudletLength = 200;
	int asRam = 1;
	int dbCloudletLength = 10;
	int dbRam = 1;
	int dbCloudletIOLength = 5;
	int duration = 200;
	int numberOfCloudlets = duration / 5;

	ISessionGenerator sessGen = new ConstSessionGenerator(asCloudletLength, asRam, dbCloudletLength, dbRam,
		dbCloudletIOLength, duration, numberOfCloudlets, false, data);

	double unit = 3600;
	double periodLength = 3600 * 24;
	double nullPoint = 0;

	double h0 = 0;
	double h10 = 10 * 3600;
	double h13 = 13 * 3600;
	double h14 = 14 * 3600;
	double h17 = 17 * 3600;
	double h21 = 21 * 3600;
	double h24 = 24 * 3600;

	String[] periods = new String[] { String.format("[%f,%f] m=10 std=1", h0, h10),
		String.format("(%f,%f] m=100 std=5", h10, h13),
		String.format("(%f,%f] m=50 std=1", h13, h14),
		String.format("(%f,%f] m=200 std=5", h14, h17),
		String.format("(%f,%f] m=100 std=2", h17, h21),
		String.format("(%f,%f] m=10 std=1", h21, h24) };

	FrequencyFunction freqFun = new PeriodicStochasticFrequencyFunction(unit, periodLength, nullPoint,
		CompositeValuedSet.createCompositeValuedSet(periods));
	workloads.add(new StatWorkloadGenerator(freqFun, sessGen));
	return workloads;
    }

    private static Datacenter createDatacenter(final String name) {

	// Here are the steps needed to create a PowerDatacenter:
	// 1. We need to create a list to store
	// our machine
	List<Host> hostList = new ArrayList<>();

	// 2. A Machine contains one or more PEs or CPUs/Cores.
	// In this example, it will have only one plus.
	List<Pe> peList = new ArrayList<>();
	List<HddPe> hddList = new ArrayList<>();

	int mips = 1000;
	int iops = 1000;

	// 3. Create PEs and add these into a list.
	peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store
	// Pe id and
	// MIPS Rating

	hddList.add(new HddPe(new PeProvisionerSimple(iops), data));

	// 4. Create Host with its id and list of PEs and add them to the list
	// of machines
	int ram = 2048; // host memory (MB)
	long storage = 1000000; // host storage
	int bw = 10000;

	// This is ourmachine
	hostList.add(new HddHost(new RamProvisionerSimple(ram),
		new BwProvisionerSimple(bw), storage, peList, hddList,
		new VmSchedulerTimeShared(peList),
		new VmDiskScheduler(hddList)));

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
	LinkedList<Storage> storageList = new LinkedList<>();

	DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
		arch, os, vmm, hostList, time_zone, cost, costPerMem,
		costPerStorage, costPerBw);

	// 6. Finally, we need to create a PowerDatacenter object.
	Datacenter datacenter = null;
	try {
	    datacenter = new HddDataCenter(name, characteristics,
		    new VmAllocationPolicySimple(hostList), storageList, 0);
	} catch (Exception e) {
	    e.printStackTrace();
	}

	return datacenter;
    }

    /**
     * Prints the Cloudlet objects
     * 
     * @param list
     *            list of Cloudlets
     */
    private static void printCloudletList(final List<Cloudlet> list) {
	int size = list.size();
	Cloudlet cloudlet;

	// Print header line
	CustomLog.printLine(TextUtil.getCaptionLine(WebCloudlet.class));

	// Print details for each cloudlet
        for (Cloudlet value : list) {
            cloudlet = value;
            CustomLog.print(TextUtil.getTxtLine(cloudlet));
        }
    }
}
