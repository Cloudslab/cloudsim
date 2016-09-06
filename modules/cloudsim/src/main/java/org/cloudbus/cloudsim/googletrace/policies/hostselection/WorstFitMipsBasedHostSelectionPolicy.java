package org.cloudbus.cloudsim.googletrace.policies.hostselection;

import java.util.SortedSet;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;

/**
 * TODO
 * 
 * @author Giovanni Farias
 *
 */
public class WorstFitMipsBasedHostSelectionPolicy implements HostSelectionPolicy {

	@Override
	public Host select(SortedSet<Host> hosts, Vm vm) {

		double requiredMips = vm.getMips();

		/*
		 * Only first host is checked because the hosts are sorted from bigger
		 * to smaller available capacity
		 */
		Host firstHost = hosts.first();
		if (firstHost.getAvailableMips() >= requiredMips) {
			return firstHost;
		}
		return null;
	}
}
