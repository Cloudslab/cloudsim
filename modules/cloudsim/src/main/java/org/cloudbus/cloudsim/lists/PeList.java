/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.lists;

import java.util.List;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;

/**
 * PeList is a collection of operations on lists of PEs.
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class PeList {

	/**
	 * Gets a {@link Pe} with a given id.
	 * 
	 * @param peList the PE list where to get a given PE
	 * @param id the id of the PE to be get
	 * @return the PE with the given id or null if not found
	 * @pre id >= 0
	 * @post $none
	 */
	public static <T extends Pe> Pe getById(List<T> peList, int id) {
                /*@todo such kind of search would be made using a HashMap
                (to avoid always iterating over the list),
                where the key is the id of the object and the value the object
                itself. The same occurs for lists of hosts and VMs.*/
		for (Pe pe : peList) {
			if (pe.getId() == id) {
				return pe;
			}
		}
		return null;
	}

	/**
	 * Gets MIPS Rating of a PE with a given ID.
	 * 
	 * @param peList the PE list where to get a given PE
	 * @param id the id of the PE to be get
	 * @return the MIPS rating of the PE or -1 if the PE was not found
	 * @pre id >= 0
	 * @post $none
	 */
	public static <T extends Pe> int getMips(List<T> peList, int id) {
		Pe pe = getById(peList, id);
		if (pe != null) {
			return pe.getMips();
		}
		return -1;
	}

	/**
	 * Gets total MIPS Rating for all PEs.
	 * 
	 * @param peList the pe list
	 * @return the total MIPS Rating
	 * @pre $none
	 * @post $none
	 */
	public static <T extends Pe> int getTotalMips(List<T> peList) {
		int totalMips = 0;
		for (Pe pe : peList) {
			totalMips += pe.getMips();
		}
		return totalMips;
	}

	/**
	 * Gets the max utilization percentage among all PEs.
	 * 
	 * @param peList the pe list
	 * @return the max utilization percentage
	 */
	public static <T extends Pe> double getMaxUtilization(List<T> peList) {
		double maxUtilization = 0;
		for (Pe pe : peList) {
			double utilization = pe.getPeProvisioner().getUtilization();
			if (utilization > maxUtilization) {
				maxUtilization = utilization;
			}
		}
		return maxUtilization;
	}

	/**
	 * Gets the max utilization percentage among all PEs allocated to a VM.
	 * 
	 * @param vm the vm to get the maximum utilization percentage
	 * @param peList the pe list
	 * @return the max utilization percentage
	 */
	public static <T extends Pe> double getMaxUtilizationAmongVmsPes(List<T> peList, Vm vm) {
		double maxUtilization = 0;
		for (Pe pe : peList) {
			if (pe.getPeProvisioner().getAllocatedMipsForVm(vm) == null) {
				continue;
			}
			double utilization = pe.getPeProvisioner().getUtilization();
			if (utilization > maxUtilization) {
				maxUtilization = utilization;
			}
		}
		return maxUtilization;
	}

	/**
	 * Gets the first <tt>FREE</tt> PE which.
	 * 
	 * @param peList the PE list
	 * @return the first free PE or null if not found
	 * @pre $none
	 * @post $none
	 */
	public static <T extends Pe> Pe getFreePe(List<T> peList) {
		for (Pe pe : peList) {
			if (pe.getStatus() == Pe.FREE) {
				return pe;
			}
		}
		return null;
	}

	/**
	 * Gets the number of <tt>FREE</tt> (non-busy) PEs.
	 * 
	 * @param peList the PE list
	 * @return number of free PEs
	 * @pre $none
	 * @post $result >= 0
	 */
	public static <T extends Pe> int getNumberOfFreePes(List<T> peList) {
		int cnt = 0;
		for (Pe pe : peList) {
			if (pe.getStatus() == Pe.FREE) {
				cnt++;
			}
		}
		return cnt;
	}

	/**
	 * Sets a PE status.
	 * 
	 * @param status the PE status, either <tt>Pe.FREE</tt> or <tt>Pe.BUSY</tt>
	 * @param id the id of the PE to be set
	 * @param peList the PE list
	 * @return <tt>true</tt> if the PE status has been changed, <tt>false</tt> otherwise (PE id might
	 *         not be exist)
	 * @pre peID >= 0
	 * @post $none
	 */
	public static <T extends Pe> boolean setPeStatus(List<T> peList, int id, int status) {
		Pe pe = getById(peList, id);
		if (pe != null) {
			pe.setStatus(status);
			return true;
		}
		return false;
	}

	/**
	 * Gets the number of <tt>BUSY</tt> PEs.
	 * 
	 * @param peList the PE list
	 * @return number of busy PEs
	 * @pre $none
	 * @post $result >= 0
	 */
	public static <T extends Pe> int getNumberOfBusyPes(List<T> peList) {
		int cnt = 0;
		for (Pe pe : peList) {
			if (pe.getStatus() == Pe.BUSY) {
				cnt++;
			}
		}
		return cnt;
	}

	/**
	 * Sets the status of PEs of a host to FAILED or FREE. NOTE: <tt>resName</tt> and
	 * <tt>hostId</tt> are used for debugging purposes, which is <b>ON</b> by default. 
         * Use {@link #setStatusFailed(boolean)} if you do not want this information.
	 * 
         * @param peList the host's PE list to be set as failed or free
	 * @param resName the name of the resource
	 * @param hostId the id of the host
	 * @param failed true if the host's PEs have to be set as FAILED, false
         * if they have to be set as FREE.
         * @see #setStatusFailed(java.util.List, boolean) 
	 */
	public static <T extends Pe> void setStatusFailed(
			List<T> peList,
			String resName,
			int hostId,
			boolean failed) {
		String status = null;
		if (failed) {
			status = "FAILED";
		} else {
			status = "WORKING";
		}

		Log.printConcatLine(resName, " - Machine: ", hostId, " is ", status);

		setStatusFailed(peList, failed);
	}

	/**
	 * Sets the status of PEs of a host to FAILED or FREE.
	 * 
	 * @param peList the host's PE list to be set as failed or free
	 * @param failed true if the host's PEs have to be set as FAILED, false
         * if they have to be set as FREE.
	 */
	public static <T extends Pe> void setStatusFailed(List<T> peList, boolean failed) {
		// a loop to set the status of all the PEs in this machine
		for (Pe pe : peList) {
			if (failed) {
				pe.setStatus(Pe.FAILED);
			} else {
				pe.setStatus(Pe.FREE);
			}
		}
	}

}