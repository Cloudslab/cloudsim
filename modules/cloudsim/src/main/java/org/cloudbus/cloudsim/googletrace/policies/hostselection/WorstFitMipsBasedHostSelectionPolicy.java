package org.cloudbus.cloudsim.googletrace.policies.hostselection;

import java.util.SortedSet;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.googletrace.PriorityHostSkin;

/**
 * TODO
 * 
 * @author Giovanni Farias
 */
public class WorstFitMipsBasedHostSelectionPolicy implements HostSelectionPolicy {

	@Override
	public PriorityHostSkin select(SortedSet<PriorityHostSkin> hosts, Vm vm) {

		if (hosts == null)
			throw new IllegalArgumentException(
					"The set of host can not be null.");

		if (vm == null)
			throw new IllegalArgumentException("The Vm can not be null.");

		/*
		 * Only first host is checked because the hosts are sorted from bigger
		 * to smaller available capacity
		 */
		if (!hosts.isEmpty()) {
//			for (PriorityHostSkin firstHost : hosts) {
				PriorityHostSkin firstHost = hosts.first();
				if (firstHost.getHost().isSuitableForVm(vm)) {
					return firstHost;
				}
				
//			}
		}
		return null;
	}
}
