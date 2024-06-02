/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.List;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.lists.ResCloudletList;

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
		int position = ResCloudletList.getPositionById(getCloudletPausedList(), cloudletId);
		if (position >= 0) {
			ResCloudlet rcl = getCloudletPausedList().remove(position);
			rcl.setCloudletStatus(Cloudlet.CloudletStatus.INEXEC);
			getCloudletExecList().add(rcl);

			// calculate the expected time for cloudlet completion
			// first: how many PEs do we have?
			return getEstimatedFinishTime(rcl, CloudSim.clock());
		}
		return 0.0;
	}

	@Override
	public double cloudletSubmit(Cloudlet cloudlet, double fileTransferTime) {
		ResCloudlet rcl = new ResCloudlet(cloudlet);
		rcl.setCloudletStatus(Cloudlet.CloudletStatus.INEXEC);
		for (int i = 0; i < cloudlet.getNumberOfPes(); i++) {
			rcl.setMachineAndPeId(0, i);
		}

		getCloudletExecList().add(rcl);

		// calculate the expected time for cloudlet completion
		double capacity = getCurrentCapacity(getCurrentMipsShare());
		// use the current capacity to estimate the extra amount of
		// time to file transferring. It must be added to the cloudlet length
		double extraSize = capacity * fileTransferTime;
		long length = (long) (cloudlet.getCloudletLength() + extraSize);
		cloudlet.setCloudletLength(length);

		return cloudlet.getCloudletLength() / capacity;
	}

	// Simple policy, there is no real scheduling involved
	@Override
	public double getTotalCurrentAvailableMipsForCloudlet(ResCloudlet rcl, List<Double> mipsShare) {
		return getCurrentCapacity(mipsShare) * rcl.getNumberOfPes();
	}

	@Override
	public double getTotalCurrentAllocatedMipsForCloudlet(ResCloudlet rcl, double time) {
		return getTotalCurrentAvailableMipsForCloudlet(rcl, getCurrentMipsShare());
	}

	@Override
	public double getTotalCurrentRequestedMipsForCloudlet(ResCloudlet rcl, double time) {
		return getTotalCurrentAvailableMipsForCloudlet(rcl, getCurrentMipsShare());
	}
}
