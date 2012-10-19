/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.lists;

import java.util.List;

import org.cloudbus.cloudsim.ResCloudlet;

/**
 * ResCloudletList is a collection of operations on lists of ResCloudlets.
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class ResCloudletList {

	/**
	 * Returns a given Cloudlet. This method needs a combination of Cloudlet Id and User Id because
	 * each Cloud Users might have exactly same Cloudlet Ids.
	 * 
	 * @param cloudletId a Cloudlet Id
	 * @param userId an User Id
	 * @param list the list
	 * @return the Cloudlet.
	 * @throws IndexOutOfBoundsException - if a Cloudlet with specified Id and user id is not in the
	 *             list.
	 * @pre cloudletId >= 0
	 * @pre userId >= 0
	 * @post $none
	 */
	public static <T extends ResCloudlet> ResCloudlet getByIdAndUserId(
			List<T> list,
			int cloudletId,
			int userId) {
		for (T rcl : list) {
			if (rcl.getCloudletId() == cloudletId && rcl.getUserId() == userId) {
				return rcl;
			}
		}
		return null;
	}

	/**
	 * Finds the index of a Cloudlet inside the list. This method needs a combination of Cloudlet Id
	 * and User Id because each Cloud User might have exactly the same Cloudlet Id.
	 * 
	 * @param cloudletId a Cloudlet Id
	 * @param userId an User Id
	 * @param list the list
	 * @return the index in this list of the first occurrence of the specified Cloudlet, or
	 *         <code>-1</code> if the list does not contain this Cloudlet.
	 * @pre cloudletId >= 0
	 * @pre userId >= 0
	 * @post $none
	 */
	public static <T extends ResCloudlet> int indexOf(List<T> list, int cloudletId, int userId) {
		int i = 0;
		for (T rcl : list) {
			if (rcl.getCloudletId() == cloudletId && rcl.getUserId() == userId) {
				return i;
			}
			i++;
		}
		return -1;
	}

	/**
	 * Move a ResCloudlet object from this linked-list into a specified one.
	 * 
	 * @param listFrom the list from
	 * @param listTo the list to
	 * @param cloudlet the cloudlet
	 * @return <b>true</b> if the moving operation successful, otherwise return <b>false</b>
	 * @pre obj != null
	 * @pre list != null
	 * @post $result == true || $result == false
	 */
	public static <T extends ResCloudlet> boolean move(List<T> listFrom, List<T> listTo, T cloudlet) {
		if (listFrom.remove(cloudlet)) {
			listTo.add(cloudlet);
			return true;
		}
		return false;
	}

	/**
	 * Returns the position of the cloudlet with that id, if it exists. Otherwise -1.
	 * @param cloudletList - the list of cloudlets.
	 * @param id - the id we search for.
	 * @return - the position of the cloudlet with that id, or -1 otherwise.
	 */
	public static <T extends ResCloudlet> int getPositionById(List<T> cloudletList, int id) {
		int i = 0 ;
	        for (T cloudlet : cloudletList) {
			if (cloudlet.getCloudletId() == id) {
				return i;
			}
			i++;
		}
		return -1;
	}
}
