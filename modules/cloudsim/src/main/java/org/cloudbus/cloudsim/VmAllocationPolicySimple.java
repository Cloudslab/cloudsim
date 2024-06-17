/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.List;

import org.cloudbus.cloudsim.core.HostEntity;
import org.cloudbus.cloudsim.selectionPolicies.SelectionPolicyLeastFull;
import org.cloudbus.cloudsim.selectionPolicies.SelectionPolicyWorstFit;

/**
 * VmAllocationPolicySimple is an VmAllocationPolicy that chooses, as the host for a VM, the host
 * with less PEs in use. It is therefore a Worst Fit policy, allocating VMs into the 
 * host with most available PE.
 *
 * @author Remo Andreoli
 * @since CloudSim Toolkit 7.0
 */
public class VmAllocationPolicySimple extends VmAllocationWithSelectionPolicy {
	/**
	 * Creates a new VmAllocationPolicySimple object.
	 * 
	 * @param list the list of hosts
	 * @pre $none
	 * @post $none
	 */
	public VmAllocationPolicySimple(List<? extends HostEntity> list) {
		super(list, new SelectionPolicyLeastFull());
	}
}
