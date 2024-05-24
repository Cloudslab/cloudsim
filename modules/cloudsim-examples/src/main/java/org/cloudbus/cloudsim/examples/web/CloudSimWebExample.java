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
import org.cloudbus.cloudsim.EX.DatacenterBrokerEX;
import org.cloudbus.cloudsim.EX.disk.*;
import org.cloudbus.cloudsim.EX.util.CustomLog;
import org.cloudbus.cloudsim.EX.util.TextUtil;
import org.cloudbus.cloudsim.web.*;
import org.cloudbus.cloudsim.web.workload.brokers.WebBroker;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.uncommons.maths.number.ConstantGenerator;
import org.uncommons.maths.number.NumberGenerator;
import org.uncommons.maths.random.GaussianGenerator;
import org.uncommons.maths.random.MersenneTwisterRNG;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class CloudSimWebExample {

    private static DataItem data = new DataItem(5);

    /**
     * Creates main method to run this example.
     * 
     * @throws IOException
     * @throws SecurityException
     */
    public static void main(final String[] args) throws SecurityException, IOException {

        // Step 0: Set up the logger
        Properties props = new Properties();
        try (InputStream is = Files.newInputStream(Paths.get("custom_log.properties"))) {
            props.load(is);
        }
        CustomLog.configLogger(props);
        CustomLog.printLine("Example of Web session modelling.");

        try {
            // Step1: Initialize the CloudSim package. It should be called
            // before creating any entities.
            int numBrokers = 1;         // number of brokers we'll be using
            boolean trace_flag = false; // mean trace events

            // Initialize CloudSim
            CloudSim.init(numBrokers, Calendar.getInstance(), trace_flag);

            // Step 2: Create Datacenters - the resource providers in CloudSim
            Datacenter datacenter0 = createDatacenter("WebDataCenter");

            // Step 3: Create Broker
            WebBroker broker = new WebBroker("Broker", 5, 10000, datacenter0.getId());

            // Step 4: Create virtual machines
            List<Vm> vmlist = new ArrayList<>();

            // VM description
            int mips = 250;
            int ioMips = 200;
            long size = 10000;  // image size (MB)
            int ram = 512;      // vm memory (MB)
            long bw = 1000;
            int pesNumber = 1;  // number of cpus
            String vmm = "Xen"; // VMM name

            // create two VMs
            HddVm appServerVM = new HddVm("App-Srv", broker.getId(), mips, ioMips, pesNumber, ram, bw, size, vmm,
                    new HddCloudletSchedulerTimeShared(), new Integer[0]);

            HddVm dbServerVM = new HddVm("Db-Srv", broker.getId(), mips, ioMips, pesNumber, ram, bw, size, vmm,
                    new HddCloudletSchedulerTimeShared(), new Integer[0]);

            ILoadBalancer balancer = new SimpleWebLoadBalancer(1, "127.0.0.1", Arrays.asList(appServerVM),
                    new SimpleDBBalancer(dbServerVM));
            broker.addLoadBalancer(balancer);

            // add the VMs to the vmList
            vmlist.add(appServerVM);
            vmlist.add(dbServerVM);

            // submit vm list to the broker
            broker.submitGuestList(vmlist);

            List<WebSession> sessions = generateRandomSessions(broker, 100);
            
            // Submit the sessions with 1 sec delay, to wait for the VMs to boot.
            // NOTE: if you have a delay policy (see IVmBootDelayDistribution) you may
            // need to delay the submission with more.
            broker.submitSessionsAtTime(sessions, balancer.getAppId(), 1);

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
    }

    private static List<WebSession> generateRandomSessions(final DatacenterBrokerEX broker, final int sessionNum) {
        // Create Random Generators
        Random rng = new MersenneTwisterRNG();
        GaussianGenerator cpuGen = new GaussianGenerator(1000, 20, rng);
        GaussianGenerator ramGen = new GaussianGenerator(5, 1, rng);
        GaussianGenerator ioGen = new GaussianGenerator(1000, 20, rng);
        NumberGenerator<Integer> modifiesDataGen = new ConstantGenerator<>(0);

        Map<String, NumberGenerator<? extends Number>> generators = new HashMap<>();
        generators.put(StatGenerator.CLOUDLET_LENGTH, cpuGen);
        generators.put(StatGenerator.CLOUDLET_RAM, ramGen);
        generators.put(StatGenerator.CLOUDLET_IO, ioGen);
        generators.put(StatGenerator.CLOUDLET_MODIFIES_DATA, modifiesDataGen);
        IGenerator<WebCloudlet> asGenerator = new StatGenerator(generators, data);
        IGenerator<Collection<WebCloudlet>> dbGenerator = new CompositeGenerator<>(new StatGenerator(
                generators, data));

        List<WebSession> sessions = new ArrayList<>();
        for (int i = 0; i < sessionNum; i++) {
            WebSession session = new WebSession(asGenerator, dbGenerator, broker.getId(), -1, 100);
            sessions.add(session);
        }
        return sessions;
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

        // This is our machine
        hostList.add(new HddHost(new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList, hddList,
                new VmSchedulerTimeShared(peList), new VmDiskScheduler(hddList)));

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

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone,
                cost, costPerMem, costPerStorage, costPerBw);

        // 6. Finally, we need to create a PowerDatacenter object.
        Datacenter datacenter = null;
        try {
            datacenter = new HddDataCenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList,
                    0);
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

        CustomLog.printLine("\n\n========== OUTPUT ==========");
        // Printe header line
        CustomLog.printLine(TextUtil.getCaptionLine(WebCloudlet.class));

        // Print details for each cloudlet
        for (Cloudlet value : list) {
            cloudlet = value;
            CustomLog.print(TextUtil.getTxtLine(cloudlet));
        }
    }
}
