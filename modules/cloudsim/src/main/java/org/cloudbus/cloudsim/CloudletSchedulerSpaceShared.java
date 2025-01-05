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
import org.cloudbus.cloudsim.lists.CloudletList;

/**
 * CloudletSchedulerSpaceShared implements a policy of scheduling performed by a virtual machine
 * to run its {@link Cloudlet Cloudlets}.
 * Only one cloudlet at a time, per VM, would be executing. Other cloudlets will be in a waiting list.
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

	// wake up one cloudlet from the waiting list
	@Override
	protected void updateWaitingCloudlets(double currentTime, Object info) {
		if(getCloudletWaitingList().isEmpty()) {
			return;
		}

		int i = 0;
		while (i < getCloudletWaitingList().size()) {
			Cloudlet cl = getCloudletWaitingList().get(i);
			if ((getCurrentPEs() - usedPes) >= cl.getNumberOfPes()) {
				getCloudletWaitingList().remove(cl);

				cl.updateStatus(Cloudlet.CloudletStatus.INEXEC);
				getCloudletExecList().add(cl);
				usedPes += cl.getNumberOfPes();
				break;
			}
			i++;
		}
	}

	@Override
	public double cloudletResume(int cloudletId) {
		// look for the cloudlet in the paused list
		int position = CloudletList.getPositionById(getCloudletPausedList(), cloudletId);
		if (position >= 0) {
			Cloudlet cl = getCloudletPausedList().remove(position);

			// it can go to the exec list
			if ((getCurrentPEs() - usedPes) >= cl.getNumberOfPes()) {
				cl.updateStatus(Cloudlet.CloudletStatus.INEXEC);

				long size = cl.getRemainingCloudletLength();
				size *= cl.getNumberOfPes();
				cl.setCloudletLength(size);

				getCloudletExecList().add(cl);
				usedPes += cl.getNumberOfPes();

				// calculate the expected time for cloudlet completion
				return getEstimatedFinishTime(cl, CloudSim.clock());
			} else {// no enough free PEs: go to the waiting queue
				cl.updateStatus(Cloudlet.CloudletStatus.QUEUED);

				long size = cl.getRemainingCloudletLength();
				size *= cl.getNumberOfPes();
				cl.setCloudletLength(size);

				getCloudletWaitingList().add(cl);
				return 0.0;
			}
		}
		// not found in the paused list: either it is in in the queue, executing or not exist
		return 0.0;
	}

	@Override
	public double cloudletSubmit(Cloudlet cl, double fileTransferTime) {
        if ((getCurrentPEs() - usedPes) >= cl.getNumberOfPes()) { // it can go to the exec list
            cl.updateStatus(Cloudlet.CloudletStatus.INEXEC);
			getCloudletExecList().add(cl);
			usedPes += cl.getNumberOfPes();
		} else {// no enough free PEs: go to the waiting queue
            cl.updateStatus(Cloudlet.CloudletStatus.QUEUED);
			getCloudletWaitingList().add(cl);
			return 0.0;
		}

		// calculate the expected time for cloudlet completion
		double capacity = getCurrentCapacity();

		// use the current capacity to estimate the extra amount of
		// time to file transferring. It must be added to the cloudlet length
		double extraSize = capacity * fileTransferTime;
		long length = (long) (cl.getCloudletLength() + extraSize);
		cl.setCloudletLength(length);

		return cl.getCloudletLength() / capacity;
	}

	@Override
	public void cloudletFinish(Cloudlet cl) {
		super.cloudletFinish(cl);
		usedPes -= cl.getNumberOfPes();
	}

	/**
	 * Returns the first cloudlet to migrate to another VM.
	 * 
	 * @return the first running cloudlet
	 * @pre $none
	 * @post $none
         * 
         * //@TODO it doesn't check if the list is empty
	 */
	@Override
	public Cloudlet migrateCloudlet() {
		Cloudlet cl = super.migrateCloudlet();
		usedPes -= cl.getNumberOfPes();
		return cl;
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
