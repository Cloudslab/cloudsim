package org.cloudbus.cloudsim.container.resourceAllocators;

import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.container.containerPlacementPolicies.ContainerPlacementPolicy;
import org.cloudbus.cloudsim.container.core.ContainerVm;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.core.HostEntity;

import java.util.*;

/**
 * Created by sareh on 16/12/15.
 * Modified by Remo Andreoli (March 2024)
 */
public class ContainerAllocationPolicyRS extends VmAllocationPolicySimple {
    /** The vm table. */


    private ContainerPlacementPolicy containerPlacementPolicy;


    public ContainerAllocationPolicyRS(List<? extends HostEntity> list, ContainerPlacementPolicy containerPlacementPolicy1) {
        super(list);
        setContainerPlacementPolicy(containerPlacementPolicy1);
    }


    @Override
    public HostEntity findHostForGuest(GuestEntity guest) {
        Set<ContainerVm> excludedVmList = new HashSet<>();
        int tries = 0;

        do{
            ContainerVm containerVm = getContainerPlacementPolicy().getContainerVm(getHostList(), guest, excludedVmList);
            if(containerVm == null){
                return null;
            }

            if (containerVm.isSuitableForGuest(guest)) {
                return containerVm;
            } else {
                excludedVmList.add(containerVm);
                tries ++;
            }
            } while (tries < getHostList().size());
        return null;
    }



    public ContainerPlacementPolicy getContainerPlacementPolicy() {
        return this.containerPlacementPolicy;
    }

    public void setContainerPlacementPolicy(ContainerPlacementPolicy containerPlacementPolicy) {
        this.containerPlacementPolicy = containerPlacementPolicy;
    }


}
