package org.cloudbus.cloudsim.container.containerPlacementPolicies;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.container.core.Container;
import org.cloudbus.cloudsim.container.core.ContainerVm;

import java.util.List;
import java.util.Set;

/**
 * Created by hvandenb
 * LeastRequestedPriority is a policy function that favors nodes with fewer requested resources.
 * It calculates the percentage of memory and CPU requested by pods scheduled on the node, and prioritizes
 * based on the minimum of the average of the fraction of requested to capacity.
 * Details: cpu((capacity - sum(requested)) * 10 / capacity) + memory((capacity - sum(requested)) * 10 / capacity) / 2
 **/
public class ContainerPlacementPolicyLeastRequested extends ContainerPlacementPolicy {

    @Override
    public ContainerVm getContainerVm(List<ContainerVm> vmList, Object obj, Set<? extends ContainerVm> excludedVmList) {
        ContainerVm selectedVm = null;
        Container container = (Container)obj;
        int minScore = Integer.MAX_VALUE;

        // Look at what's requested
        //double totalMips = containerVm1.getMips();
        double totalMips = container.getCurrentRequestedTotalMips();
//            double totalMemory = containerVm1.getRam();
        double totalMemory = container.getCurrentRequestedRam();

        for (ContainerVm containerVm1 : vmList) {
            if (excludedVmList.contains(containerVm1)) {
                continue;
            }

            // Check Capacity
            double allocatableResourceMips = containerVm1.getAvailableMips();
//            double allocatableResourceMemory = containerVm1.getRam() - containerVm1.getCurrentAllocatedRam();
            double allocatableResourceMemory = containerVm1.getContainerRamProvisioner().getAvailableVmRam();


            double cpuScore = calculateUnusedScore(totalMips, allocatableResourceMips, Integer.toString(containerVm1.getId()));
            double memoryScore = calculateUnusedScore(totalMemory, allocatableResourceMemory, Integer.toString(containerVm1.getId()));
            int score = 0;
            score = (int)(cpuScore + memoryScore) / 2;

            Log.formatLine(container.getId() + " -> " + containerVm1.getId() + " :" +
                      "Least Requested Priority, capacity %.2f millicores %.2f memory bytes, total request %.2f millicores %.2f memory bytes, score %.2f CPU %.2f memory",
                    allocatableResourceMips, allocatableResourceMemory,
                    totalMips, totalMemory, cpuScore, memoryScore);

            // Select the VM with the max score
            if ( score < minScore) {
                minScore = score;
                selectedVm = containerVm1;
            }
        }

        return selectedVm;
    }

    private double calculateUnusedScore(double requested, double capacity, String vm)  {
        if (capacity == 0) {
            return 0;
        }
        if (requested > capacity) {
            Log.formatLine("Combined requested resources %.2f from existing VM exceeds capacity %.2f on node %s",
                    requested, capacity, vm);

            return 0;
        }
        return ((capacity - requested) * 10) / capacity;
    }

}
