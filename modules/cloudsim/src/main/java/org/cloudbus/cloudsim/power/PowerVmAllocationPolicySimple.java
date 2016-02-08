/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;

/**
 * A simple VM allocation policy that does <b>not</b> perform any optimization on VM allocation.
 * 
 * <br/>If you are using any algorithms, policies or workload included in the power package please cite
 * the following paper:<br/>
 * 
 * <ul>
 * <li><a href="http://dx.doi.org/10.1002/cpe.1867">Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012</a>
 * </ul>
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 3.0
 */
public class PowerVmAllocationPolicySimple extends PowerVmAllocationPolicyAbstract {

	/**
	 * Instantiates a new PowerVmAllocationPolicySimple.
	 * 
	 * @param list the list
	 */
	public PowerVmAllocationPolicySimple(List<? extends Host> list) {
		super(list);
	}

        /**
         * The method doesn't perform any VM allocation optimization
         * and in fact has no effect.
         * @param vmList
         * @return 
         */
	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
                //@todo It is better to return an empty map in order to avoid NullPointerException or extra null checks
		// This policy does not optimize the VM allocation
		return null;
	}

}
