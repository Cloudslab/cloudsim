package org.cloudbus.cloudsim.container.containerPlacementPolicies;


import org.cloudbus.cloudsim.container.core.ContainerVm;

import java.util.List;
import java.util.Set;

/**
 * Created by sareh fotuhi Piraghaj on 16/12/15.
 * For container placement First Fit policy.
 */

public class ContainerPlacementPolicyFirstFit extends ContainerPlacementPolicy {

    @Override
    public ContainerVm getContainerVm(List<ContainerVm> vmList, Object obj, Set<? extends ContainerVm> excludedVmList) {
        ContainerVm containerVm = null;
        for (ContainerVm containerVm1 : vmList) {
            if (excludedVmList.contains(containerVm1)) {
                continue;
            }
            containerVm = containerVm1;
            break;
        }
        return containerVm;
    }

}
