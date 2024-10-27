package org.cloudbus.cloudsim.tieredconfigurations;

import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

public class EnergyAwareLoadBalancer {

    public void balanceLoad(List<Cloudlet> cloudlets, List<Vm> vms) {
        // balance based upon green energy credit rating
        for (Cloudlet cloudlet : cloudlets) {
            Vm selectedVm = selectVmBasedOnGreenEnergy(vms);
            cloudlet.setVmId(selectedVm.getId());
        }
    }

    private Vm selectVmBasedOnGreenEnergy(List<Vm> vms) {
        // TODO: Logic to select VM with more green energy availability
        return vms.get(0);
    }
}