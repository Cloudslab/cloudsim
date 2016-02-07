/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;

/**
 * A power-aware {@link DatacenterBroker}.
 * 
 * <br/>If you are using any algorithms, policies or workload included in the power package please cite
 * the following paper:<br/>
 * 
 * <ul>
 * <li><a href="http://dx.doi.org/10.1002/cpe.1867">Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012</a>
 * </ul>
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class PowerDatacenterBroker extends DatacenterBroker {

	/**
	 * Instantiates a new PowerDatacenterBroker.
	 * 
	 * @param name the name of the broker
	 * @throws Exception the exception
	 */
	public PowerDatacenterBroker(String name) throws Exception {
		super(name);
	}

	@Override
	protected void processVmCreate(SimEvent ev) {
		int[] data = (int[]) ev.getData();
		int result = data[2];

		if (result != CloudSimTags.TRUE) {
			int datacenterId = data[0];
			int vmId = data[1];
			System.out.println(CloudSim.clock() + ": " + getName() + ": Creation of VM #" + vmId
					+ " failed in Datacenter #" + datacenterId);
			System.exit(0);
		}
		super.processVmCreate(ev);
	}

}
