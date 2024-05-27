/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.ArrayList;
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
	public double updateCloudletsProcessing(double currentTime, List<Double> mipsShare) {
		setCurrentMipsShare(mipsShare);
		double timeSpan = currentTime - getPreviousTime(); // time since last update
		double capacity = getCPUCapacity(mipsShare);

		// each machine in the exec list has the same amount of cpu
		for (ResCloudlet rcl : getCloudletExecList()) {
			rcl.updateCloudletFinishedSoFar((long) (capacity * timeSpan * rcl.getNumberOfPes() * Consts.MILLION));
		}

		// no more cloudlets in this scheduler
		if (getCloudletExecList().isEmpty() && getCloudletWaitingList().isEmpty()) {
			setPreviousTime(currentTime);
			return 0.0;
		}

		// check finished cloudlets
		List<ResCloudlet> toRemove = new ArrayList<>();
		for (ResCloudlet rcl : getCloudletExecList()) {
			if (rcl.getRemainingCloudletLength() == 0) {// finished: remove from the list
				toRemove.add(rcl);
				cloudletFinish(rcl);
			}
		}
		getCloudletExecList().removeAll(toRemove);

		// estimate finish time of cloudlets
		double nextEvent = Double.MAX_VALUE;
		for (ResCloudlet rcl : getCloudletExecList()) {
			double estimatedFinishTime = getEstimatedFinishTime(rcl, currentTime);
			if (estimatedFinishTime - currentTime < CloudSim.getMinTimeBetweenEvents()) {
				estimatedFinishTime = currentTime + CloudSim.getMinTimeBetweenEvents();
			}

			if (estimatedFinishTime < nextEvent) {
				nextEvent = estimatedFinishTime;
			}
		}

		setPreviousTime(currentTime);
		return nextEvent;
	}

	/**
	 * Gets the individual MIPS capacity available for each PE available for the scheduler,
         * considering that all PEs have the same capacity.
	 * 
	 * @param mipsShare list with MIPS share of each PE available to the scheduler
	 * @return the capacity of each PE
	 */
	@Override
	public double getCPUCapacity(final List<Double> mipsShare) {
		double capacity = 0.0;
		int cpus = 0;
		for (Double mips : mipsShare) {
			capacity += mips;
			if (mips > 0.0) {
				cpus++;
			}
		}
		currentCPUs = cpus;

		int pesInUse = 0;
		for (ResCloudlet rcl : getCloudletExecList()) {
			if (rcl.getRemainingCloudletLength() != 0) {
				pesInUse += rcl.getNumberOfPes();
			}
		}

		capacity /= Math.max(pesInUse, currentCPUs);
		return capacity;
	}

	@Override
	public Cloudlet cloudletCancel(int cloudletId) {
		// First, looks in the finished queue
		for (ResCloudlet rcl : getCloudletFinishedList()) {
			if (rcl.getCloudletId() == cloudletId) {
				getCloudletFinishedList().remove(rcl);
				return rcl.getCloudlet();
			}
		}

		// Then searches in the exec list
		for (ResCloudlet rcl : getCloudletExecList()) {
			if (rcl.getCloudletId() == cloudletId) {
				getCloudletExecList().remove(rcl);
				if (rcl.getRemainingCloudletLength() == 0) {
					cloudletFinish(rcl);
				} else {
					rcl.setCloudletStatus(Cloudlet.CloudletStatus.CANCELED);
				}
				return rcl.getCloudlet();
			}
		}

		// Now, looks in the paused queue
		for (ResCloudlet rcl : getCloudletPausedList()) {
			if (rcl.getCloudletId() == cloudletId) {
				rcl.setCloudletStatus(Cloudlet.CloudletStatus.CANCELED);
				getCloudletPausedList().remove(rcl);
				return rcl.getCloudlet();
			}
		}

		// Finally, looks in the waiting list
		for (ResCloudlet rcl : getCloudletWaitingList()) {
			if (rcl.getCloudletId() == cloudletId) {
				rcl.setCloudletStatus(Cloudlet.CloudletStatus.CANCELED);
				getCloudletWaitingList().remove(rcl);
				return rcl.getCloudlet();
			}
		}

		return null;
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
		double capacity = getCPUCapacity(getCurrentMipsShare());

		// use the current capacity to estimate the extra amount of
		// time to file transferring. It must be added to the cloudlet length
		double extraSize = capacity * fileTransferTime;
		long length = (long) (cloudlet.getCloudletLength() + extraSize);
		cloudlet.setCloudletLength(length);

		return cloudlet.getCloudletLength() / capacity;
	}

	@Override
	public double getTotalCurrentAvailableMipsForCloudlet(ResCloudlet rcl, List<Double> mipsShare) {
            /*//TODO It isn't being used any the the given parameters.*/
            return getCPUCapacity(getCurrentMipsShare());
	}

	@Override
	public double getTotalCurrentAllocatedMipsForCloudlet(ResCloudlet rcl, double time) {
                ////TODO The method is not implemented, in fact
		return 0.0;
	}

	@Override
	public double getTotalCurrentRequestedMipsForCloudlet(ResCloudlet rcl, double time) {
                ////TODO The method is not implemented, in fact
		// TODO Auto-generated method stub
		return 0.0;
	}
}
