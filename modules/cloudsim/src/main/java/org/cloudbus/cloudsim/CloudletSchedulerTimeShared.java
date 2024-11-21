/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.List;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.lists.CloudletList;

/**
 * CloudletSchedulerTimeShared implements a policy of scheduling performed by a virtual machine
 * to run its {@link Cloudlet Cloudlets}.
 * Cloudlets execute in time-shared manner in VM.
 * Each VM has to have its own instance of a CloudletScheduler.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @author Remo Andreoli
 * @since CloudSim Toolkit 1.0
 */
public class CloudletSchedulerTimeShared extends CloudletScheduler {
	/**
	 * Creates a new CloudletSchedulerTimeShared object. This method must be invoked before starting
	 * the actual simulation.
	 * 
	 * @pre $none
	 * @post $none
	 */
	public CloudletSchedulerTimeShared() {
		super();
	}

	@Override
	public double cloudletResume(int cloudletId) {
		// look for the cloudlet in the paused list
		int position = CloudletList.getPositionById(getCloudletPausedList(), cloudletId);
		if (position >= 0) {
			Cloudlet cl = getCloudletPausedList().remove(position);
			cl.updateStatus(Cloudlet.CloudletStatus.INEXEC);
			getCloudletExecList().add(cl);

			// calculate the expected time for cloudlet completion
			// first: how many PEs do we have?
			return getEstimatedFinishTime(cl, CloudSim.clock());
		}
		return 0.0;
	}

	@Override
	public double cloudletSubmit(Cloudlet cl, double fileTransferTime) {
		cl.updateStatus(Cloudlet.CloudletStatus.INEXEC);
		getCloudletExecList().add(cl);

		// calculate the expected time for cloudlet completion
		double capacity = getCurrentCapacity();
		// use the current capacity to estimate the extra amount of
		// time to file transferring. It must be added to the cloudlet length
		double extraSize = capacity * fileTransferTime;
		long length = (long) (cl.getCloudletLength() + extraSize);
		cl.setCloudletLength(length);

		return cl.getCloudletLength() / capacity;
	}

	// Simple policy, there is no real scheduling involved
	@Override
	public double getTotalCurrentAvailableMipsForCloudlet(Cloudlet cl, List<Double> mipsShare) {
		return getCurrentCapacity() * cl.getNumberOfPes();
	}

	@Override
	public double getTotalCurrentAllocatedMipsForCloudlet(Cloudlet cl, double time) {
		return getTotalCurrentAvailableMipsForCloudlet(cl, getCurrentMipsShare());
	}

	@Override
	public double getTotalCurrentRequestedMipsForCloudlet(Cloudlet cl, double time) {
		return getTotalCurrentAvailableMipsForCloudlet(cl, getCurrentMipsShare());
	}
}
