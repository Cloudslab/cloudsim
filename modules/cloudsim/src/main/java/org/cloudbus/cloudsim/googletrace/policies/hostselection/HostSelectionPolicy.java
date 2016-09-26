package org.cloudbus.cloudsim.googletrace.policies.hostselection;

import java.util.SortedSet;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.googletrace.GoogleHost;

/**
 * TODO 
 * 
 * @author Giovanni Farias
 *
 */
public interface HostSelectionPolicy {

	public GoogleHost select(SortedSet<GoogleHost> hosts, Vm vm);
	
}
