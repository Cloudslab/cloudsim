package org.cloudbus.cloudsim.container.lists;

import org.cloudbus.cloudsim.container.core.ContainerVm;
import org.cloudbus.cloudsim.core.CloudSim;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by sareh on 28/07/15.
 */
public class PowerContainerVmList extends ContainerVmList {

        /**
         * Sort by cpu utilization.
         *
         * @param vmList the vm list
         */
        public static <T extends ContainerVm> void sortByCpuUtilization(List<T> vmList) {
            Collections.sort(vmList, new Comparator<T>() {

                @Override
                public int compare(T a, T b) throws ClassCastException {
                    Double aUtilization = a.getTotalUtilizationOfCpuMips(CloudSim.clock());
                    Double bUtilization = b.getTotalUtilizationOfCpuMips(CloudSim.clock());
                    return bUtilization.compareTo(aUtilization);
                }
            });
        }

    }


