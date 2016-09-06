package org.cloudbus.cloudsim.googletrace.policies.vmallocation;

import org.cloudbus.cloudsim.Vm;

/**
 * TODO
 * 
 * @author Giovanni Farias
 *
 */
public interface Preemptable {

	public void preempt(Vm vm);
}
