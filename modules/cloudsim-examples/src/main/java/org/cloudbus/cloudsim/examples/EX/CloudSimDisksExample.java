package org.cloudbus.cloudsim.examples.EX;


import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.EX.disk.*;
import org.cloudbus.cloudsim.EX.util.CustomLog;
import org.cloudbus.cloudsim.EX.util.Id;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;

/**
 * Demonstrates how to model disk I/O operations in CloudSimEx. This example
 * uses a single host machine with two disks. Each disk has 2 data items.
 * 
 * @author nikolay.grozev
 * 
 */
public class CloudSimDisksExample {

    // CPU capacity of each processor
    private static final int HOST_MIPS = 1000;
    // I/O capacity of each disk
    private static final int HOST_MIOPS = 100;
    // RAM capacity of each host
    private static final int HOST_RAM = 2048;
    // NOTE! this is actually not used by CloudSimEx
    private static final long HOST_STORAGE = 1000000;
    // Bandwidth of each host
    private static final int HOST_BW = 10000;

    // CPU capacity of each VM
    private static final int VM_MIPS = 250;
    // Size of each VM
    private static final long VM_SIZE = 10000;
    // RAM of each VM
    private static final int VM_RAM = 512;
    // Bandwidth of each VM
    private static final long VM_BW = 1000;

    // Size of each data item
    private static final int DATA_ITEM_SIZE = 5;

    private static final String ARCH = "x86";
    private static final String OS = "Linux";
    private static final String VMM = "Xen";
    private static final double TIME_ZONE = 10.0;
    private static final double COST = 3.0;
    private static final double COST_PER_MEM = 0.05;
    private static final double COST_PER_STORAGE = 0.001;
    private static final double COST_PER_BW = 0.0;

    public static void main(String[] args) throws Exception {
        // Step 1: Configure CustomLog
        Properties propers = new Properties();
        try (InputStream is = Files.newInputStream(Paths.get("custom_log.properties"))) {
            propers.load(is);
        }
        CustomLog.configLogger(propers);
        CustomLog.redirectToConsole();

        // Step 2: Initialise CloudSim. We'll use only 1 broker in this example
        CloudSim.init(1, Calendar.getInstance(), false);

        // Step 3: Prepare data items, which will be stored on the disk
        // These two items reside on the first disk
        DataItem dataItem_1_1 = new DataItem(DATA_ITEM_SIZE);
        DataItem dataItem_1_2 = new DataItem(DATA_ITEM_SIZE);

        // These two items reside on the second disk
        DataItem dataItem_2_1 = new DataItem(DATA_ITEM_SIZE);
        DataItem dataItem_2_2 = new DataItem(DATA_ITEM_SIZE);

        // Step 4: Prepare the lists of CPUs and disks of the host
        List<Pe> peList = Arrays.asList(new Pe(Id.pollId(Pe.class), new PeProvisionerSimple(HOST_MIPS)));

        HddPe disk1 = new HddPe(new PeProvisionerSimple(HOST_MIOPS), dataItem_1_1, dataItem_1_2);
        HddPe disk2 = new HddPe(new PeProvisionerSimple(HOST_MIOPS), dataItem_2_1, dataItem_2_2);
        List<HddPe> hddList = Arrays.asList(disk1, disk2);

        // Step 5: Build a list of hosts in the data centre
        List<? extends Host> hostList = Arrays.asList(new HddHost(new RamProvisionerSimple(HOST_RAM),
                new BwProvisionerSimple(HOST_BW), HOST_STORAGE, peList, hddList, new VmSchedulerTimeShared(peList),
                new VmDiskScheduler(hddList)));

        // Step 6: Build the data centre and a broker
        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(ARCH, OS, VMM, hostList, TIME_ZONE,
                COST, COST_PER_MEM, COST_PER_STORAGE, COST_PER_BW);

        List<Storage> storageList = new ArrayList<>();
        HddDataCenter datacenter = new HddDataCenter("DC", characteristics, new VmAllocationPolicySimple(hostList),
                storageList, 0);

        DatacenterBroker broker = new DatacenterBroker("Broker");

        // Step 7: Create 3 VMs and submit them to the broker. Vm1 has access to
        // both disks. Vm2 and Vm3 access only disk 1 and 2 respectively
        Vm vm1_1 = new HddVm("Test", broker.getId(), VM_MIPS, HOST_MIOPS, 1, VM_RAM, VM_BW, VM_SIZE, VMM,
                new HddCloudletSchedulerTimeShared(), new Integer[] { disk1.getId() });
        Vm vm1_2 = new HddVm("Test", broker.getId(), VM_MIPS, HOST_MIOPS, 1, VM_RAM, VM_BW, VM_SIZE, VMM,
                new HddCloudletSchedulerTimeShared(), new Integer[] { disk1.getId() });
        Vm vm2 = new HddVm("Test", broker.getId(), VM_MIPS, HOST_MIOPS, 1, VM_RAM, VM_BW, VM_SIZE, VMM,
                new HddCloudletSchedulerTimeShared(), new Integer[] { disk2.getId() });

        broker.submitGuestList(Arrays.asList(vm1_1, vm1_2, vm2));

        // Step 8: Create 4 jobs, associate them to VMs and send them to the
        // broker. Each job uses one of the 4 data items.

        HddCloudlet cloudlet1_1 = new HddCloudlet(VM_MIPS, HOST_MIOPS * 2, 5, broker.getId(), false, dataItem_1_1);
        HddCloudlet cloudlet1_2 = new HddCloudlet(VM_MIPS * 2, HOST_MIOPS, 5, broker.getId(), false, dataItem_1_2);
        HddCloudlet cloudlet_XX = new HddCloudlet(VM_MIPS * 2, HOST_MIOPS, 5, broker.getId(), false, dataItem_2_2);
        HddCloudlet cloudlet2_1 = new HddCloudlet(VM_MIPS, HOST_MIOPS * 2, 5, broker.getId(), false, dataItem_2_2);

        cloudlet1_1.setGuestId(vm1_1.getId());
        cloudlet1_2.setGuestId(vm1_2.getId());
        cloudlet_XX.setGuestId(vm1_2.getId()); // Should fail - data is not there
        cloudlet2_1.setGuestId(vm2.getId());

        broker.submitCloudletList(Arrays.asList(cloudlet1_1, cloudlet1_2, cloudlet_XX, cloudlet2_1));

        CustomLog.printf(Level.FINEST, "\nCloudletID=%d currentTime=%f, remainLen=%d, remainIOLen=%d)",
                cloudlet1_1.getCloudletId(), CloudSim.clock(), cloudlet1_1.getCloudletLength(),
                cloudlet1_1.getCloudletIOLength());
        CustomLog.printf(Level.FINEST, "\nCloudletID=%d currentTime=%f, remainLen=%d, remainIOLen=%d)",
                cloudlet1_2.getCloudletId(), CloudSim.clock(), cloudlet1_2.getCloudletLength(),
                cloudlet1_2.getCloudletIOLength());

        // Step 9: Run the simulation
        CloudSim.startSimulation();
        // ...
        CloudSim.stopSimulation();

        double cloudletExecTime1 = cloudlet1_1.getExecFinishTime() - cloudlet1_1.getExecStartTime();
        double cloudletExecTime2 = cloudlet1_2.getExecFinishTime() - cloudlet1_2.getExecStartTime();
        double cloudletExecTime3 = cloudlet_XX.getExecFinishTime() - cloudlet_XX.getExecStartTime();
        double cloudletExecTime4 = cloudlet2_1.getExecFinishTime() - cloudlet2_1.getExecStartTime();
        CustomLog.printf("ID=%d, start=%f, end=%f, duration=%f\n", cloudlet1_1.getCloudletId(),
                cloudlet1_1.getExecStartTime(), cloudlet1_1.getExecFinishTime(), cloudletExecTime1);
        CustomLog.printf("ID=%d, start=%f, end=%f, duration=%f\n", cloudlet1_2.getCloudletId(),
                cloudlet1_2.getExecStartTime(), cloudlet1_2.getExecFinishTime(), cloudletExecTime2);
        CustomLog.printf("ID=%d, start=%f, end=%f, duration=%f\n", cloudlet_XX.getCloudletId(),
                cloudlet_XX.getExecStartTime(), cloudlet_XX.getExecFinishTime(), cloudletExecTime3);
        CustomLog.printf("ID=%d, start=%f, end=%f, duration=%f\n", cloudlet2_1.getCloudletId(),
                cloudlet2_1.getExecStartTime(), cloudlet2_1.getExecFinishTime(), cloudletExecTime4);

    }
}
