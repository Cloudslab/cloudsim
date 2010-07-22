package org.cloudbus.cloudsim.power.lists;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.lists.VmList;

/**
 * The Class PowerVmList.
 */
public class PowerVmList extends VmList {

	/**
	 * Sort by cpu utilization.
	 *
	 * @param vmList the vm list
	 */
	public static <T extends Vm> void sortByCpuUtilization(List<T> vmList) {
    	Collections.sort(vmList, new Comparator<T>() {
           public int compare(T a, T b) throws ClassCastException {
               Double aUtilization = a.getTotalUtilizationOfCpuMips(CloudSim.clock());
               Double bUtilization = b.getTotalUtilizationOfCpuMips(CloudSim.clock());
               return bUtilization.compareTo(aUtilization);
           }
		});
	}

}
