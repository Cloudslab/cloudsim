package org.cloudbus.cloudsim.container.lists;

import org.cloudbus.cloudsim.container.core.ContainerVm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.lists.VmList;

import java.util.List;

/**
 * Created by sareh on 28/07/15.
 * Modified by Remo Andreoli (Feb 2024)
 */
public class PowerContainerVmList extends VmList {

        /**
         * Sort by cpu utilization.
         *
         * @param vmList the vm list
         */
        public static <T extends ContainerVm> void sortByCpuUtilization(List<T> vmList) {
            vmList.sort((a, b) -> {
                Double aUtilization = a.getTotalUtilizationOfCpuMips(CloudSim.clock());
                Double bUtilization = b.getTotalUtilizationOfCpuMips(CloudSim.clock());
                return bUtilization.compareTo(aUtilization);
            });
        }

    }


