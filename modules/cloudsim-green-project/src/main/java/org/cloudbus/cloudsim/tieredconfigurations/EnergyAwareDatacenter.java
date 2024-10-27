package org.cloudbus.cloudsim.tieredconfigurations;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.HostEntity;

import java.util.List;

public class EnergyAwareDatacenter extends Datacenter {

    public EnergyAwareDatacenter(
            String name,
            DatacenterCharacteristics characteristics,
            VmAllocationPolicy vmAllocationPolicy,
            List<Storage> storageList,
            double schedulingInterval) throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
    }

    // Method to estimate energy consumption based on PE utilization
    public double getEstimatedEnergyConsumption() {
        double totalEnergyConsumption = 0;
        for (HostEntity host : getHostList()) {
            totalEnergyConsumption += estimateEnergyConsumptionForHost(host);
        }
        return totalEnergyConsumption;
    }

    // Estimate the energy consumption for each host based on PE utilization
    private double estimateEnergyConsumptionForHost(HostEntity host) {
        double totalUtilization = 0;
        for (Pe pe : host.getPeList()) {
            totalUtilization += pe.getPeProvisioner().getUtilization();
        }

        // Energy consumption = base energy + dynamic energy based on PE utilization
        double baseEnergy = 100; // Assume a base energy consumption in watts
        double dynamicEnergy = 400 * totalUtilization; // Adjust the multiplier as needed
        return baseEnergy + dynamicEnergy;
    }

    @Override
    public void updateCloudletProcessing() {
        super.updateCloudletProcessing();
        double currentEnergyConsumption = getEstimatedEnergyConsumption();
        Log.printLine(String.format("Current Energy Consumption: %.2f W", currentEnergyConsumption));
    }
}
