package org.cloudbus.cloudsim.auction.vm;

import org.cloudbus.cloudsim.Vm;

import java.util.Comparator;

/**
 * Used to compare VMs based on their weight to each other 
 * 
 * @author Youness Teimoury
 * Blog: http://youness-teimoury.blogspot.com/
 * Email: youness126@gmail.com
 * Created on 2011/8/27
 */
public class VmComparator implements Comparator<Vm> {

	@Override
	public int compare(Vm o1, Vm o2) {
		double m = VmUtils.getWeight(o1);
		double n = VmUtils.getWeight(o2);
		if (m < n) {
			return -1;
		} else if (m > n) {
			return 1;
		}
		return 0;
	}
	
}