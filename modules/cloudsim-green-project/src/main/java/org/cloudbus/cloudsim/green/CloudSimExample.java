package org.cloudbus.cloudsim.green;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

// Define power models for different Tiers
class RenewableEnergyPowerModel implements PowerModel {
    @Override
    public double getPower(double utilization) {
        // Assuming energy is available based on time (e.g., day/night for solar energy)
        // Power varies with renewable energy availability
        if (isRenewableEnergyAvailable()) {
            return 50 + utilization * 50; // low power usage for renewable energy
        } else {
            return 0; // No energy available if renewable energy is depleted
        }
    }
    
    private boolean isRenewableEnergyAvailable() {
        // Simulation logic to check renewable energy availability (e.g., based on time of day)
        return CloudSim.clock() % 24 < 12; // e.g., renewable energy available during first 12 hours of the day
    }
}

class EnergyEfficientPowerModel implements PowerModel {
    @Override
    public double getPower(double utilization) {
        return 100 + utilization * 100; // Power-efficient hardware consumes moderate power
    }
}

class HighPerformancePowerModel implements PowerModel {
    @Override
    public double getPower(double utilization) {
        return 200 + utilization * 300; // High-performance hardware consumes more power
    }
}

// Define Tiers
public class CloudSimExample {

    public static void main(String[] args) throws Exception {
        // Initialize CloudSim
        CloudSim.init(1, Calendar.getInstance(), false);

        // Create Tier 1: Renewable Energy Servers
        List<Host> renewableEnergyHosts = createHosts(new RenewableEnergyPowerModel(), 5); // 5 renewable energy hosts

        // Create Tier 2: Energy-Efficient Servers
        List<Host> energyEfficientHosts = createHosts(new EnergyEfficientPowerModel(), 10); // 10 energy-efficient hosts

        // Create Tier 3: High-Performance Servers
        List<Host> highPerformanceHosts = createHosts(new HighPerformancePowerModel(), 8); // 8 high-performance hosts

        // Add them to the Datacenter
        Datacenter dc = createDatacenter(renewableEnergyHosts, energyEfficientHosts, highPerformanceHosts);
        
        // Proceed with orchestrator and workload scheduling...
        System.out.println(dc);
    }

    private static List<Host> createHosts(PowerModel powerModel, int count) {
        List<Host> hostList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            // Define hosts (servers) with different resource configurations
            List<Pe> peList = new ArrayList<>();
            peList.add(new Pe(0, new PeProvisionerSimple(1000))); // Each host has 1000 MIPS capacity

            Host host = new PowerHost(i, new RamProvisionerSimple(4096), new BwProvisionerSimple(10000),
                                      1000000, peList, new VmSchedulerTimeShared(peList), powerModel);
            hostList.add(host);
        }
        return hostList;
    }

    private static Datacenter createDatacenter(List<Host>... hostLists) throws Exception {
        List<Host> allHosts = new ArrayList<>();
        for (List<Host> hosts : hostLists) {
            allHosts.addAll(hosts);
        }
        return new Datacenter("Datacenter", (DatacenterCharacteristics) allHosts, new VmAllocationPolicySimple(allHosts), new LinkedList<>(), 0);
    }
}
