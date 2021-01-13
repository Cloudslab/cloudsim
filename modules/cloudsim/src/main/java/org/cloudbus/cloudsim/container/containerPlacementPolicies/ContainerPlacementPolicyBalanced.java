package org.cloudbus.cloudsim.container.containerPlacementPolicies;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.container.core.Container;
import org.cloudbus.cloudsim.container.core.ContainerVm;

import java.util.List;
import java.util.Set;

/**
 * Created by hvandenb
 * BalancedResourceAllocation favors nodes with balanced resource usage rate.
 * BalancedResourceAllocation should **NOT** be used alone, and **MUST** be used together with LeastRequestedPriority.
 * It calculates the difference between the cpu and memory fracion of capacity, and prioritizes the host based on how
 * close the two metrics are to each other.
 * Detail: score = 10 - abs(cpuFraction-memoryFraction)*10. The algorithm is partly inspired by:
 * "Wei Huang et al. An Energy Efficient Virtual Machine Placement Algorithm with Balanced Resource Utilization"
 */
public class ContainerPlacementPolicyBalanced extends ContainerPlacementPolicy {

    @Override
    public ContainerVm getContainerVm(List<ContainerVm> vmList, Object obj, Set<? extends ContainerVm> excludedVmList) {
        ContainerVm selectedVm = null;
        Container container = (Container)obj;
        int maxScore = 0;

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


            double cpuFraction = fractionOfCapacity(totalMips, allocatableResourceMips);
            double memoryFraction = fractionOfCapacity(totalMemory, allocatableResourceMemory);
            int score = 0;
            if (cpuFraction >= 1 || memoryFraction >= 1) {
                // if requested >= capacity, the corresponding host should never be preferred.
                score = 0;
            } else {
                // Upper and lower boundary of difference between cpuFraction and memoryFraction are -1 and 1
                // respectively. Multilying the absolute value of the difference by 10 scales the value to
                // 0-10 with 0 representing well balanced allocation and 10 poorly balanced. Subtracting it from
                // 10 leads to the score which also scales from 0 to 10 while 10 representing well balanced.
                double diff = Math.abs(cpuFraction - memoryFraction);
                score = (int)(10 - diff*10);
            }

            Log.formatLine(container.getId() + " -> " + containerVm1.getId() + " :" +
                    "Balanced Resource Allocation, capacity %.2f millicores %.2f memory bytes, total request %.2f millicores %.2f memory bytes, score %d",
                    allocatableResourceMips, allocatableResourceMemory,
                    totalMips, totalMemory,score);

            // Select the VM with the max score
            if ( score > maxScore) {
                maxScore = score;
                selectedVm = containerVm1;
            }
        }

        return selectedVm;
    }

    private double fractionOfCapacity(double requested, double capacity) {
        if (capacity == 0) {
            return 1;
        }
        return (requested) / (capacity);
    }
}
