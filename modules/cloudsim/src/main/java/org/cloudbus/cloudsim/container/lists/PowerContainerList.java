package org.cloudbus.cloudsim.container.lists;

import org.cloudbus.cloudsim.container.core.Container;
import org.cloudbus.cloudsim.core.CloudSim;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by sareh on 31/07/15.
 */
public class PowerContainerList {


    /**
     * Sort by cpu utilization.
     *
     * @param containerList the vm list
     */
    public static <T extends Container> void sortByCpuUtilization(List<T> containerList) {
        Collections.sort(containerList, new Comparator<T>() {

            @Override
            public int compare(T a, T b) throws ClassCastException {
                Double aUtilization = a.getTotalUtilizationOfCpuMips(CloudSim.clock());
                Double bUtilization = b.getTotalUtilizationOfCpuMips(CloudSim.clock());
                return bUtilization.compareTo(aUtilization);
            }
        });
    }

}
