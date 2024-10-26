package TieredConfigurations;

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
        List<Pe> peList = new ArrayList<>();
        int mips = 2000; // High MIPS (high processing power)

        // Create 8 PEs for each host
        for (int i = 0; i < 8; i++) {
            peList.add(new Pe(i, new PeProvisionerSimple(mips)));
        }

        // Host characteristics
        int ram = 32768; // 32 GB RAM
        long storage = 2000000; // 2 TB storage
        int bw = 20000; // 20 Gbps bandwidth

        List<Host> hostList = new ArrayList<>();
        hostList.add(new Host(0,
                new RamProvisionerSimple(ram),
                new BwProvisionerSimple(bw),
                storage,
                peList,
                new VmSchedulerTimeShared(peList)));

        // Datacenter characteristics
        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                "x86", "Linux", "Xen", hostList, 10.0, 0.01, 0.05, 0.001, 0.0);

        return new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), new LinkedList<>(), 0);
    }

    // Medium resource datacenter
    public static Datacenter createMediumResourceDatacenter(String name) throws Exception {
        List<Pe> peList = new ArrayList<>();
        int mips = 1500; // Medium MIPS

        // Create 6 PEs for each host
        for (int i = 0; i < 6; i++) {
            peList.add(new Pe(i, new PeProvisionerSimple(mips)));
        }

        // Host characteristics
        int ram = 16384; // 16 GB RAM
        long storage = 1000000; // 1 TB storage
        int bw = 10000; // 10 Gbps bandwidth

        List<Host> hostList = new ArrayList<>();
        hostList.add(new Host(1,
                new RamProvisionerSimple(ram),
                new BwProvisionerSimple(bw),
                storage,
                peList,
                new VmSchedulerTimeShared(peList)));

        // Datacenter characteristics
        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                "x86", "Linux", "Xen", hostList, 10.0, 0.01, 0.05, 0.001, 0.0);

        return new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), new LinkedList<>(), 0);
    }

    // Low resource datacenter
    public static Datacenter createLowResourceDatacenter(String name) throws Exception {
        List<Pe> peList = new ArrayList<>();
        int mips = 1000; // Low MIPS

        // Create 4 PEs for each host
        for (int i = 0; i < 4; i++) {
            peList.add(new Pe(i, new PeProvisionerSimple(mips)));
        }

        // Host characteristics
        int ram = 8192; // 8 GB RAM
        long storage = 500000; // 500 GB storage
        int bw = 5000; // 5 Gbps bandwidth

        List<Host> hostList = new ArrayList<>();
        hostList.add(new Host(2,
                new RamProvisionerSimple(ram),
                new BwProvisionerSimple(bw),
                storage,
                peList,
                new VmSchedulerTimeShared(peList)));

        // Datacenter characteristics
        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                "x86", "Linux", "Xen", hostList, 10.0, 0.01, 0.05, 0.001, 0.0);

        return new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), new LinkedList<>(), 0);
    }
}
