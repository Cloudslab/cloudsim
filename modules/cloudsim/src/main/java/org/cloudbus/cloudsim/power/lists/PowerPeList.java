/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Parallel and Distributed Systems such as Clusters and Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2002, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power.lists;

import java.util.List;

import org.cloudbus.cloudsim.lists.PeList;
import org.cloudbus.cloudsim.power.PowerPe;

/**
 * CloudSim PowerPeList maintains a list of PEs (Processing Elements) that make up
 * a machine.
 *
 * @author       Manzur Murshed and Rajkumar Buyya
 * @since        CloudSim Toolkit 1.0
 * @invariant $none
 */
public class PowerPeList extends PeList {

	/**
	 * Gets the power. For this moment only consumed by all PEs.
	 *
	 * @param peList the pe list
	 *
	 * @return the power
	 */
	public static <T extends PowerPe> double getPower(List<PowerPe> peList) {
		double power = 0;
		for (PowerPe pe : peList) {
			try {
				power += pe.getPowerModel().getPower(pe.getPeProvisioner().getUtilization());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return power;
	}

	/**
	 * Gets the maximum power. For this moment only consumed by all PEs.
	 *
	 * @param peList the pe list
	 *
	 * @return the power
	 */
	public static <T extends PowerPe> double getMaxPower(List<PowerPe> peList) {
    	double power = 0;
    	for (PowerPe pe : peList) {
    		try {
    			power += pe.getPowerModel().getPower(1.0);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}
    	return power;
	}

}
