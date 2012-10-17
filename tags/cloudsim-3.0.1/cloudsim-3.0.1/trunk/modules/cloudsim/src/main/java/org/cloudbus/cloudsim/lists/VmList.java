/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.lists;

import java.util.List;

import org.cloudbus.cloudsim.Vm;

/**
 * VmList is a collection of operations on lists of VMs.
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class VmList {

	/**
	 * Return a reference to a Vm object from its ID.
	 * 
	 * @param id ID of required VM
	 * @param vmList the vm list
	 * @return Vm with the given ID, $null if not found
	 * @pre $none
	 * @post $none
	 */
	public static <T extends Vm> T getById(List<T> vmList, int id) {
		for (T vm : vmList) {
			if (vm.getId() == id) {
				return vm;
			}
		}
		return null;
	}

	/**
	 * Return a reference to a Vm object from its ID and user ID.
	 * 
	 * @param id ID of required VM
	 * @param userId the user ID
	 * @param vmList the vm list
	 * @return Vm with the given ID, $null if not found
	 * @pre $none
	 * @post $none
	 */
	public static <T extends Vm> T getByIdAndUserId(List<T> vmList, int id, int userId) {
		for (T vm : vmList) {
			if (vm.getId() == id && vm.getUserId() == userId) {
				return vm;
			}
		}
		return null;
	}

}
