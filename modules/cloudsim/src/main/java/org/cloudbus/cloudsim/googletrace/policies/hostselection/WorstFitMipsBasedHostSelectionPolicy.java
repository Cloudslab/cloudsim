package org.cloudbus.cloudsim.googletrace.policies.hostselection;

import java.util.NoSuchElementException;
import java.util.SortedSet;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.googletrace.GoogleHost;

/**
 * TODO
 *
 * @author Giovanni Farias
 */
public class WorstFitMipsBasedHostSelectionPolicy implements HostSelectionPolicy {

    @Override
    public Host select(SortedSet<Host> hosts, Vm vm) {

        if (hosts == null)
            throw new IllegalArgumentException("The set of host can not be null.");

        if (vm == null)
            throw new IllegalArgumentException("The Vm can not be null.");

        double requiredMips = vm.getMips();

		/*
         * Only first host is checked because the hosts are sorted from bigger
		 * to smaller available capacity
		 */
        if (!hosts.isEmpty()) {

            Host firstHost = hosts.first();

            if (firstHost.getAvailableMips() >= requiredMips) {
                return firstHost;
            }
        }
        return null;
    }
}
