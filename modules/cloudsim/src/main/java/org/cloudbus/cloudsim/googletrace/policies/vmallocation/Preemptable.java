package org.cloudbus.cloudsim.googletrace.policies.vmallocation;

import org.cloudbus.cloudsim.Vm;

/**
 * TODO
 * 
 * @author Giovanni Farias
 *
 */
public interface Preemptable {

	public boolean preempt(Vm vm);
}
