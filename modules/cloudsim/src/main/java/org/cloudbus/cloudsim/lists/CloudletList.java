/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.lists;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;

/**
 * CloudletList is a collection of operations on lists of Cloudlets.
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class CloudletList {

	/**
	 * Gets a {@link Cloudlet} with a given id.
	 * 
	 * @param cloudletList the list of existing Cloudlets
	 * @param id the Cloudlet id
	 * @return a Cloudlet with the given ID or $null if not found
	 */
	public static <T extends Cloudlet> T getById(List<T> cloudletList, int id) {
		for (T cloudlet : cloudletList) {
			if (cloudlet.getCloudletId() == id) {
				return cloudlet;
			}
		}
		return null;
	}

	/**
	 * Gets the position of a cloudlet with a given id.
         * 
	 * @param cloudletList the list of existing cloudlets
	 * @param id the cloudlet id
	 * @return the position of the cloudlet with the given id or -1 if not found
	 */
	public static <T extends Cloudlet> int getPositionById(List<T> cloudletList, int id) {
		int i = 0 ;
	        for (T cloudlet : cloudletList) {
			if (cloudlet.getCloudletId() == id) {
				return i;
			}
			i++;
		}
		return -1;
	}
	
	/**
	 * Sorts the Cloudlets in a list based on their lengths.
	 * 
	 * @param cloudletList the cloudlet list
	 * @pre $none
	 * @post $none
	 */
	public static <T extends Cloudlet> void sort(List<T> cloudletList) {
		Collections.sort(cloudletList, new Comparator<T>() {

			/**
			 * Compares two Cloudlets.
			 * 
			 * @param a the first Cloudlet to be compared
			 * @param b the second Cloudlet to be compared
			 * @return the value 0 if both Cloudlets are numerically equal; a value less than 0 if the
			 *         first Object is numerically less than the second Cloudlet; and a value greater
			 *         than 0 if the first Cloudlet is numerically greater than the second Cloudlet.
			 * @throws ClassCastException <tt>a</tt> and <tt>b</tt> are expected to be of type
			 *             <tt>Cloudlet</tt>
			 * @pre a != null
			 * @pre b != null
			 * @post $none
			 */
			@Override
			public int compare(T a, T b) throws ClassCastException {
				Double cla = Double.valueOf(a.getCloudletTotalLength());
				Double clb = Double.valueOf(b.getCloudletTotalLength());
				return cla.compareTo(clb);
			}
		});
	}

}
