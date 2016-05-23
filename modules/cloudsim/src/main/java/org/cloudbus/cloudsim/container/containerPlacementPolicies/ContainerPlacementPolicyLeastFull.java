package org.cloudbus.cloudsim.container.containerPlacementPolicies;

import org.cloudbus.cloudsim.container.core.ContainerVm;

import java.util.List;
import java.util.Set;

/**
 * Created by sareh fotuhi Piraghaj on 16/12/15.
 * For container placement Least-Full policy.
 */
public class ContainerPlacementPolicyLeastFull extends ContainerPlacementPolicy{

    @Override
    public ContainerVm getContainerVm(List<ContainerVm> vmList, Object obj, Set<? extends ContainerVm> excludedVmList) {
        ContainerVm selectedVm = null;
        double minMips = Double.MAX_VALUE;
        for (ContainerVm containerVm1 : vmList) {
            if (excludedVmList.contains(containerVm1)) {
                continue;
            }
            double containerUsage = containerVm1.getContainerScheduler().getAvailableMips();
            if ( containerUsage < minMips ) {
                minMips = containerUsage;
                selectedVm = containerVm1;

            }
           }
        return selectedVm;
    }
}
