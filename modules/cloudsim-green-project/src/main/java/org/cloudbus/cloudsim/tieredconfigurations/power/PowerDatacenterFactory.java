package org.cloudbus.cloudsim.tieredconfigurations.power;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.power.*;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerHpProLiantMl110G4Xeon3040; 
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PowerDatacenterFactory {

    // High resource power datacenter
    public static PowerDatacenter createHighResourcePowerDatacenter(String name) throws Exception {
        int mips = 2000; // High MIPS (high processing power)
        int hostId = 0;
        int ram = 32768; // 32 GB RAM
        long storage = 2000000; // 2 TB storage
        int bw = 20000; // 20 Gbps bandwidth

        return createPowerDatacenter(name, mips, hostId, ram, storage, bw);
    }

    // Medium resource power datacenter
    public static PowerDatacenter createMediumResourcePowerDatacenter(String name) throws Exception {
        int mips = 1500; // Medium MIPS
        int hostId = 1;
        int ram = 16384; // 16 GB RAM
        long storage = 1000000; // 1 TB storage
        int bw = 10000; // 10 Gbps bandwidth

        return createPowerDatacenter(name, mips, hostId, ram, storage, bw);
    }

    // Low resource power datacenter
    public static PowerDatacenter createLowResourcePowerDatacenter(String name) throws Exception {
        int mips = 1000; // Low MIPS
        int hostId = 2;
        int ram = 8192; // 8 GB RAM
        long storage = 500000; // 500 GB storage
        int bw = 5000; // 5 Gbps bandwidth

        return createPowerDatacenter(name, mips, hostId, ram, storage, bw);
    }

    private static PowerDatacenter createPowerDatacenter(String name, int mips, int hostId, int ram, long storage, int bw) throws Exception {
        List<PowerHost> hostList = new ArrayList<>();
        List<Pe> peList = new ArrayList<>();
        peList.add(new Pe(0, new PeProvisionerSimple(mips)));

        PowerHost host = new PowerHost(
                hostId,
                new RamProvisionerSimple(ram),
                new BwProvisionerSimple(bw),
                storage,
                peList,
                new VmSchedulerTimeShared(peList),
                new PowerModelSpecPowerHpProLiantMl110G4Xeon3040()
        );

        hostList.add(host);

        String arch = "x86"; // system architecture
        String os = "Linux"; // operating system
        String vmm = "Xen";
        double time_zone = 10.0; // time zone this resource located
        double cost = 3.0; // the cost of using processing in this resource
        double costPerMem = 0.05; // the cost of using memory in this resource
        double costPerStorage = 0.001; // the cost of using storage in this resource
        double costPerBw = 0.0; // the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<>(); // we are not adding SAN devices by now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem,
                costPerStorage, costPerBw);

        return new PowerDatacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
    }
}