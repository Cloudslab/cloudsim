package org.cloudbus.cloudsim.container.resourceAllocators;


import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.container.core.ContainerVm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyAbstract;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sareh on 14/07/15.
 * Modified by Remo Andreoli (Feb 2024)
 */
public abstract  class PowerContainerVmAllocationAbstract extends PowerVmAllocationPolicyAbstract {
    /**
     * Instantiates a new PowerVmAllocationPolicyAbstract.
     *
     * @param list the list
     */
    public PowerContainerVmAllocationAbstract(List<? extends Host> list) {
        super(list);
    }

    public List<ContainerVm> getOverUtilizedVms() {
        List<ContainerVm> vmList = new ArrayList<>();
        List<Host> hostList = getHostList();
        for (Host host : hostList) {
            List<ContainerVm> containerVms = host.getVmList();
            for (ContainerVm vm : containerVms) {
                if (vm.getTotalUtilizationOfCpuMips(CloudSim.clock()) > vm.getTotalMips()) {
                    vmList.add(vm);
                }

            }

        }
        return vmList;
    }


}