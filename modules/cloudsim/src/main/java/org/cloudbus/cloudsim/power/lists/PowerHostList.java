package org.cloudbus.cloudsim.power.lists;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.lists.HostList;

/**
 *
 * @author Sabir Mohamemdi Taieb
 */
public class PowerHostList extends HostList {
    
    public static <T extends Host> void sortByIncreasingAvailableMips(List<T> hostList) {
		Collections.sort(hostList, new Comparator<T>() {

			@Override
			public int compare(T a, T b) throws ClassCastException {
				Double aUtilization = a.getAvailableMips();
				Double bUtilization = b.getAvailableMips();
				return bUtilization.compareTo(aUtilization); // ascending order
			}
		});
	}
    
    public static <T extends Host> void sortByDecreasingAvailableMips(List<T> hostList) {
		Collections.sort(hostList, new Comparator<T>() {

			@Override
			public int compare(T a, T b) throws ClassCastException {
				Double aUtilization = a.getAvailableMips();
				Double bUtilization = b.getAvailableMips();
				return aUtilization.compareTo(bUtilization); 
			}
		});
	}
}
