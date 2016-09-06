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
public interface HostSelectionPolicy {

	public Host select(SortedSet<Host> hosts, Vm vm);
	
}
