/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power.lists;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.lists.VmList;

/**
 * PowerVmList is a collection of operations on lists of power-enabled VMs.
 * 
 * If you are using any algorithms, policies or workload included in the power package, please cite
 * the following paper:
 * 
 * Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012
 * 
 * @author Anton Beloglazov
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class PowerVmList extends VmList {

	/**
	 * Sort by cpu utilization.
	 * 
	 * @param vmList the vm list
	 */
	public static <T extends Vm> void sortByCpuUtilization(List<T> vmList) {
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
