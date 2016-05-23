package org.cloudbus.cloudsim.container.containerPlacementPolicies;

import org.cloudbus.cloudsim.container.core.ContainerVm;

import java.util.List;
import java.util.Set;

/**
 *  Created by sareh fotuhi Piraghaj on 16/12/15.
 *  For writing any container placement policies this class should be extend.
 */

public abstract class ContainerPlacementPolicy {
    /**
     * Gets the VM List, and the excluded VMs
     *
     * @param vmList the host
     * @return the destination vm to place container
     */
    public abstract ContainerVm getContainerVm(List<ContainerVm> vmList, Object obj, Set<? extends ContainerVm> excludedVmList);

}
