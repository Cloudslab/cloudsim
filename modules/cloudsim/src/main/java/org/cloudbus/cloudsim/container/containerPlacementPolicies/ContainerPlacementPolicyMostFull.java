package org.cloudbus.cloudsim.container.containerPlacementPolicies;

import org.cloudbus.cloudsim.container.core.ContainerVm;

import java.util.List;
import java.util.Set;

/**
 * Created by sareh fotuhi Piraghaj on 16/12/15.
 * For container placement Most-Full policy.
 */
public class ContainerPlacementPolicyMostFull extends ContainerPlacementPolicy {

    @Override
    public ContainerVm getContainerVm(List<ContainerVm> vmList, Object obj, Set<? extends ContainerVm> excludedVmList) {
        ContainerVm selectedVm = null;
        double leastAvailable = Double.MAX_VALUE;

        for (ContainerVm containerVm1 : vmList) {
            if (excludedVmList.contains(containerVm1)) {
                continue;
            }

            double containerAvailable = containerVm1.getContainerScheduler().getAvailableMips();
            if ( containerAvailable < leastAvailable) {
                leastAvailable = containerAvailable;
                selectedVm = containerVm1;
            }
        }

        return selectedVm;
    }
}
