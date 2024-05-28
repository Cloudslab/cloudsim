/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.lists.ResCloudletList;

/**
 * CloudletSchedulerSpaceShared implements a policy of scheduling performed by a virtual machine
 * to run its {@link Cloudlet Cloudlets}.
 * It consider there will be only one cloudlet per VM. Other cloudlets will be in a waiting list.
 * We consider that file transfer from cloudlets waiting happens before cloudlet execution. I.e.,
 * even though cloudlets must wait for CPU, data transfer happens as soon as cloudlets are
 * submitted.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @author Remo Andreoli
 * @since CloudSim Toolkit 1.0
 */
public class CloudletSchedulerSpaceShared extends CloudletScheduler {
	/** The number of used PEs. */
	protected int usedPes;

	/**
	 * Creates a new CloudletSchedulerSpaceShared object. This method must be invoked before
	 * starting the actual simulation.
	 * 
	 * @pre $none
	 * @post $none
	 */
	public CloudletSchedulerSpaceShared() {
		super();
		usedPes = 0;
	}

	@Override
	public double updateCloudletsProcessing(double currentTime, List<Double> mipsShare) {
		setCurrentMipsShare(mipsShare);
		double timeSpan = currentTime - getPreviousTime(); // time since last update

		// each machine in the exec list has the same amount of cpu
		for (ResCloudlet rcl : getCloudletExecList()) {
			rcl.updateCloudletFinishedSoFar((long) (timeSpan *
					getTotalCurrentAllocatedMipsForCloudlet(rcl, currentTime) * Consts.MILLION));
		}

		// check finished cloudlets
		List<ResCloudlet> toRemove = new ArrayList<>();
		for (ResCloudlet rcl : getCloudletExecList()) {
			if (rcl.getRemainingCloudletLength() == 0) { // finished: remove from the list
				toRemove.add(rcl);
				cloudletFinish(rcl);
			}
		}
		getCloudletExecList().removeAll(toRemove);

		// no more cloudlets in this scheduler
		if (getCloudletExecList().isEmpty() && getCloudletWaitingList().isEmpty()) {
			setPreviousTime(currentTime);
			return 0.0;
		}

		// for each finished cloudlet, add a new one from the waiting list
		if (!getCloudletWaitingList().isEmpty()) {
			int finished = toRemove.size();
			List<ResCloudlet> toUnpause = getCloudletWaitingList().stream()
							.filter(rcl -> (currentCPUs - usedPes) >= rcl.getNumberOfPes()).limit(finished).toList();

			for (ResCloudlet rcl : toUnpause) {
				rcl.setCloudletStatus(Cloudlet.CloudletStatus.INEXEC);
				for (int k = 0; k < rcl.getNumberOfPes(); k++) {
					rcl.setMachineAndPeId(0, k);
				}
				getCloudletExecList().add(rcl);
				usedPes += rcl.getNumberOfPes();
			}

			getCloudletWaitingList().removeAll(toUnpause);
		}

		// estimate finish time of cloudlets in the execution queue
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

	@Override
	public double cloudletResume(int cloudletId) {
		// look for the cloudlet in the paused list
		int position = ResCloudletList.getPositionById(getCloudletPausedList(), cloudletId);
		if (position >= 0) {
			ResCloudlet rcl = getCloudletPausedList().remove(position);

			// it can go to the exec list
			if ((currentCPUs - usedPes) >= rcl.getNumberOfPes()) {
				rcl.setCloudletStatus(Cloudlet.CloudletStatus.INEXEC);
				for (int i = 0; i < rcl.getNumberOfPes(); i++) {
					rcl.setMachineAndPeId(0, i);
				}

				long size = rcl.getRemainingCloudletLength();
				size *= rcl.getNumberOfPes();
				rcl.getCloudlet().setCloudletLength(size);

				getCloudletExecList().add(rcl);
				usedPes += rcl.getNumberOfPes();

				// calculate the expected time for cloudlet completion
				return getEstimatedFinishTime(rcl, CloudSim.clock());
			} else {// no enough free PEs: go to the waiting queue
				rcl.setCloudletStatus(Cloudlet.CloudletStatus.QUEUED);

				long size = rcl.getRemainingCloudletLength();
				size *= rcl.getNumberOfPes();
				rcl.getCloudlet().setCloudletLength(size);

				getCloudletWaitingList().add(rcl);
				return 0.0;
			}
		}
		// not found in the paused list: either it is in in the queue, executing or not exist
		return 0.0;
	}

	@Override
	public double cloudletSubmit(Cloudlet cloudlet, double fileTransferTime) {
        ResCloudlet rcl = new ResCloudlet(cloudlet);
        if ((currentCPUs - usedPes) >= cloudlet.getNumberOfPes()) { // it can go to the exec list
            rcl.setCloudletStatus(Cloudlet.CloudletStatus.INEXEC);
			for (int i = 0; i < cloudlet.getNumberOfPes(); i++) {
				rcl.setMachineAndPeId(0, i);
			}
			getCloudletExecList().add(rcl);
			usedPes += cloudlet.getNumberOfPes();
		} else {// no enough free PEs: go to the waiting queue
            rcl.setCloudletStatus(Cloudlet.CloudletStatus.QUEUED);
			getCloudletWaitingList().add(rcl);
			return 0.0;
		}

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
	public void cloudletFinish(ResCloudlet rcl) {
		super.cloudletFinish(rcl);
		usedPes -= rcl.getNumberOfPes();
	}

	/**
	 * Returns the first cloudlet to migrate to another VM.
	 * 
	 * @return the first running cloudlet
	 * @pre $none
	 * @post $none
         * 
         * //TODO it doesn't check if the list is empty
	 */
	@Override
	public Cloudlet migrateCloudlet() {
		Cloudlet cl = super.migrateCloudlet();
		usedPes -= cl.getNumberOfPes();
		return cl;
	}

	// Simple policy, there is no real scheduling involved
	@Override
	public double getTotalCurrentAvailableMipsForCloudlet(ResCloudlet rcl, List<Double> mipsShare) {
		return getCPUCapacity(mipsShare) * rcl.getNumberOfPes();
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
