package org.cloudbus.cloudsim.power;

import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;

/**
 * The Class PowerDatacenterBroker.
 */
public class PowerDatacenterBroker extends DatacenterBroker {

	/**
	 * Instantiates a new power datacenter broker.
	 * 
	 * @param name the name
	 * @throws Exception the exception
	 */
	public PowerDatacenterBroker(String name) throws Exception {
		super(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.cloudbus.cloudsim.DatacenterBroker#processVmCreate(org.cloudbus.cloudsim.core.SimEvent)
	 */
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
