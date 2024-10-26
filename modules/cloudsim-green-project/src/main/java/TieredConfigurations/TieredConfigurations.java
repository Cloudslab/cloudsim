package TieredConfigurations;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.power.models.PowerModelLinear;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;

import java.util.ArrayList;
import java.util.List;

public class TieredConfigurations {

    public static Datacenter createTier1(CloudSim simulation) throws Exception {
        List<Host> hostList = new ArrayList<>();
        hostList.add(createHostWithRenewableEnergy());

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                "x86",          // architecture
                "Linux",        // operating system
                "Xen",          // virtual machine manager
                hostList,       // list of hosts
                10.0,           // time zone (arbitrary)
                0.05,           // cost per second
                0.001,          // cost per MB of RAM
                0.001,          // cost per storage
                0.002           // cost per bandwidth
        );

        return new Datacenter("Tier1", characteristics, new VmAllocationPolicySimple(hostList), new ArrayList<Storage>(), 0);
    }

    public static Datacenter createTier2(CloudSim simulation) throws Exception {
        List<Host> hostList = new ArrayList<>();
        hostList.add(createEnergyEfficientHost());

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                "x86",          // architecture
                "Linux",        // operating system
                "Xen",          // virtual machine manager
                hostList,       // list of hosts
                10.0,           // time zone (arbitrary)
                0.05,           // cost per second
                0.001,          // cost per MB of RAM
                0.001,          // cost per storage
                0.002           // cost per bandwidth
        );

        return new Datacenter("Tier2", characteristics, new VmAllocationPolicySimple(hostList), new ArrayList<Storage>(), 0);
    }

    public static Datacenter createTier3(CloudSim simulation) throws Exception {
        List<Host> hostList = new ArrayList<>();
        hostList.add(createHighPerformanceHost());

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                "x86",          // architecture
                "Linux",        // operating system
                "Xen",          // virtual machine manager
                hostList,       // list of hosts
                10.0,           // time zone (arbitrary)
                0.05,           // cost per second
                0.001,          // cost per MB of RAM
                0.001,          // cost per storage
                0.002           // cost per bandwidth
        );

        return new Datacenter("Tier3", characteristics, new VmAllocationPolicySimple(hostList), new ArrayList<Storage>(), 0);
    }

    // Custom methods to create hosts for each tier
    private static Host createHostWithRenewableEnergy() {
        List<Pe> peList = new ArrayList<>();
        peList.add(new Pe(0, new PeProvisionerSimple(1000))); // Processing Element (PE) with 1000 MIPS

        return new PowerHost(
                2048, new RamProvisionerSimple(2048), new BwProvisionerSimple(10000), 100000, peList,
                new VmSchedulerSpaceShared(peList), new PowerModelLinear(50, 250) // Renewable energy power model
        );
    }

    private static Host createEnergyEfficientHost() {
        List<Pe> peList = new ArrayList<>();
        peList.add(new Pe(0, new PeProvisionerSimple(2000))); // Processing Element (PE) with 2000 MIPS

        return new PowerHost(
                4096, new RamProvisionerSimple(4096), new BwProvisionerSimple(20000), 200000, peList,
                new VmSchedulerSpaceShared(peList), new PowerModelLinear(100, 500) // Energy-efficient power model
        );
    }

    private static Host createHighPerformanceHost() {
        List<Pe> peList = new ArrayList<>();
        peList.add(new Pe(0, new PeProvisionerSimple(3000))); // Processing Element (PE) with 3000 MIPS

        return new PowerHost(
                8192, new RamProvisionerSimple(8192), new BwProvisionerSimple(40000), 500000, peList,
                new VmSchedulerSpaceShared(peList), new PowerModelLinear(200, 800) // High-performance power model
        );
    }
}
