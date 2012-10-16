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
	 * Gets MIPS Rating for a specified Pe ID.
	 * 
	 * @param id the Pe ID
	 * @param peList the pe list
	 * @return the MIPS rating if exists, otherwise returns -1
	 * @pre id >= 0
	 * @post $none
	 */
	public static <T extends Pe> Pe getById(List<T> peList, int id) {
		for (Pe pe : peList) {
			if (pe.getId() == id) {
				return pe;
			}
		}
		return null;
	}

	/**
	 * Gets MIPS Rating for a specified Pe ID.
	 * 
	 * @param id the Pe ID
	 * @param peList the pe list
	 * @return the MIPS rating if exists, otherwise returns -1
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
	 * Gets the max utilization among by all PEs.
	 * 
	 * @param peList the pe list
	 * @return the utilization
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
	 * Gets the max utilization among by all PEs allocated to the VM.
	 * 
	 * @param vm the vm
	 * @param peList the pe list
	 * @return the utilization
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
	 * Gets a Pe ID which is FREE.
	 * 
	 * @param peList the pe list
	 * @return a Pe ID if it is FREE, otherwise returns -1
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
	 * Gets the number of <tt>FREE</tt> or non-busy Pe.
	 * 
	 * @param peList the pe list
	 * @return number of Pe
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
	 * Sets the Pe status.
	 * 
	 * @param status Pe status, either <tt>Pe.FREE</tt> or <tt>Pe.BUSY</tt>
	 * @param id the id
	 * @param peList the pe list
	 * @return <tt>true</tt> if the Pe status has been changed, <tt>false</tt> otherwise (Pe id might
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
	 * Gets the number of <tt>BUSY</tt> Pe.
	 * 
	 * @param peList the pe list
	 * @return number of Pe
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
	 * Sets the status of PEs of this machine to FAILED. NOTE: <tt>resName</tt> and
	 * <tt>machineID</tt> are used for debugging purposes, which is <b>ON</b> by default. Use
	 * {@link #setStatusFailed(boolean)} if you do not want this information.
	 * 
	 * @param resName the name of the resource
	 * @param hostId the id of this machine
	 * @param failed the new value for the "failed" parameter
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

		Log.printLine(resName + " - Machine: " + hostId + " is " + status);

		setStatusFailed(peList, failed);
	}

	/**
	 * Sets the status of PEs of this machine to FAILED.
	 * 
	 * @param failed the new value for the "failed" parameter
	 * @param peList the pe list
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