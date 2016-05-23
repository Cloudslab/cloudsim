package org.cloudbus.cloudsim.container.containerPlacementPolicies;

import org.cloudbus.cloudsim.container.core.ContainerVm;
import org.cloudbus.cloudsim.container.utils.RandomGen;
import org.cloudbus.cloudsim.Log;

import java.util.List;
import java.util.Set;

/**
 * Created by sareh fotuhi Piraghaj on 16/12/15.
 * For container placement Random policy.
 */
public class ContainerPlacementPolicyRandomSelection extends ContainerPlacementPolicy {
    @Override
    public ContainerVm getContainerVm(List<ContainerVm> vmList, Object obj, Set<? extends ContainerVm> excludedVmList) {
        ContainerVm containerVm = null;
        while (true) {
            if (vmList.size() > 0) {
                int randomNum = new RandomGen().getNum(vmList.size());
                containerVm = vmList.get(randomNum);
                if (excludedVmList.contains(containerVm)) {
                    continue;
                }
            } else {

                Log.print(String.format("Error: The VM list Size is: %d", vmList.size()));
            }

            return containerVm;
        }
    }
}
