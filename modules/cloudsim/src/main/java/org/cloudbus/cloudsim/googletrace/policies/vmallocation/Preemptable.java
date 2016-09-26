package org.cloudbus.cloudsim.googletrace.policies.vmallocation;

import org.cloudbus.cloudsim.googletrace.GoogleVm;

/**
 * TODO
 * 
 * @author Giovanni Farias
 *
 */
public interface Preemptable {

	public boolean preempt(GoogleVm vm);
}
