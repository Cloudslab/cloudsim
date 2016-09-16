package org.cloudbus.cloudsim.googletrace.policies.hostselection;

import java.util.SortedSet;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.googletrace.PriorityHostSkin;

/**
 * TODO 
 * 
 * @author Giovanni Farias
 *
 */
public interface HostSelectionPolicy {

	public PriorityHostSkin select(SortedSet<PriorityHostSkin> hosts, Vm vm);
	
}
