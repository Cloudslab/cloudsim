package org.cloudbus.cloudsim.tieredconfigurations;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DatacenterFactory {

    // High resource datacenter
    public static Datacenter createHighResourceDatacenter(String name) throws Exception {
        int mips = 2000; // High MIPS (high processing power)
        // Create Host with its id and list of PEs and add them to the list of machines
        int hostId = 0;
        int ram = 32768; // 32 GB RAM
        long storage = 2000000; // 2 TB storage
        int bw = 20000; // 20 Gbps bandwidth

        //return createDatacenter(name, mips, hostId, ram, storage, bw);
        return createDatacenter(name, 5.0);
    }

    // Medium resource datacenter
    public static Datacenter createMediumResourceDatacenter(String name) throws Exception {
        int mips = 1500; // Medium MIPS
        // Host characteristics
        int hostId = 1;
        int ram = 16384; // 16 GB RAM
        long storage = 1000000; // 1 TB storage
        int bw = 10000; // 10 Gbps bandwidth

        //return createDatacenter(name, mips, hostId, ram, storage, bw);
        return createDatacenter(name, 3.0);
    }

    // Low resource datacenter
    public static Datacenter createLowResourceDatacenter(String name) throws Exception {
        int mips = 1000; // Low MIPS
        // Host characteristics
        int hostId = 2;
        int ram = 8192; // 8 GB RAM
        long storage = 500000; // 500 GB storage
        int bw = 5000; // 5 Gbps bandwidth

        //return createDatacenter(name, mips, hostId, ram, storage, bw);
        return createDatacenter(name, 1.0);
    }

    // This was obtained from CloudSimExample1 with a few edits
    private static Datacenter createDatacenter(String name, int mips, int hostId, int ram, long storage, int bw) {

        // Here are the steps needed to create a PowerDatacenter:
        // 1. We need to create a list to store
        // our machine
        List<Host> hostList = new ArrayList<>();

        // 2. A Machine contains one or more PEs or CPUs/Cores.
        // In this example, it will have only one core.
        List<Pe> peList = new ArrayList<>();

        // int mips = 1000; obtained from builder

        // 3. Create PEs and add these into a list.
        peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating

        // 4. Create Host with its id and list of PEs and add them to the list of machines
        // int hostId = 0;
        // int ram = 2048; // host memory (MB)
        // long storage = 1000000; // host storage
        // int bw = 10000;
        // obtained from builder

        hostList.add(
                new Host(
                        hostId,
                        new RamProvisionerSimple(ram),
                        new BwProvisionerSimple(bw),
                        storage,
                        peList,
                        new VmSchedulerTimeShared(peList)
                )
        ); // This is our machine

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

    private static Datacenter createDatacenter(String name, double pcost){

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store
		//    our machine
		List<Host> hostList = new ArrayList<>();

        // Host 1
        hostList.add(createHost(0, 2, 4 * 1024, 1000));

        // Host 2
        hostList.add(createHost(1, 4, 8 * 1024, 2000));

        // Host 3
        hostList.add(createHost(2, 8, 16 * 1024, 4000));

        // Host 4
        hostList.add(createHost(3, 1, 1024, 500));

        hostList.add(createHost(4, 16, 32* 1024, 8000));

        // 5. Create a DatacenterCharacteristics object that stores the
		//    properties of a data center: architecture, OS, list of
		//    Machines, allocation policy: time- or space-shared, time zone
		//    and its price (G$/Pe time unit).
		String arch = "x86";      // system architecture
		String os = "Linux";          // operating system
		String vmm = "Xen";
		double time_zone = 10.0;         // time zone this resource located
		double cost = pcost;              // the cost of using processing in this resource
		double costPerMem = 0.05;		// the cost of using memory in this resource
		double costPerStorage = 0.001;	// the cost of using storage in this resource
		double costPerBw = 0.0;			// the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<>();	//we are not adding SAN devices by now

	    DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
	                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);


		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

    private static Host createHost(int hostId, int cpu, int memory, int mips) {
        List<Pe> peList = new ArrayList<>();
        for (int i = 0; i < cpu; i++) {
            peList.add(new Pe(i, new PeProvisionerSimple(mips)));
        }

        int ram = memory; // host memory (MB)
        long storage = 1000000; // host storage
        int bw = 10000; // host bandwidth

        return new Host(
                hostId,
                new RamProvisionerSimple(ram),
                new BwProvisionerSimple(bw),
                storage,
                peList,
                new VmSchedulerSpaceShared(peList)
        );
    }
}
