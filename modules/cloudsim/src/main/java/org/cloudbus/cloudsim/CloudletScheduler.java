/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.EX.disk.HddCloudlet;
import org.cloudbus.cloudsim.EX.disk.HddResCloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.lists.ResCloudletList;
import org.cloudbus.cloudsim.network.datacenter.NetworkCloudletSpaceSharedScheduler;


/**
 * CloudletScheduler is an abstract class that represents the policy of scheduling performed by a
 * virtual machine to run its {@link Cloudlet Cloudlets}. 
 * So, classes extending this must execute Cloudlets. Also, the interface for
 * cloudlet management is also implemented in this class.
 * Each VM has to have its own instance of a CloudletScheduler.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @author Remo Andreoli
 * @since CloudSim Toolkit 1.0
 */
public abstract class CloudletScheduler {

	/** The previous time. */
	private double previousTime;

	/** The list of current mips share available for the VM using the scheduler. */
	private List<Double> currentMipsShare;

	/** The number of PEs currently available for the VM using the scheduler,
	 * according to the mips share provided to it by
	 * {@link CloudletScheduler#updateCloudletsProcessing(double, List)} method. */
	protected int currentCPUs;

	/** The list of cloudlet waiting to be executed on the VM. */
	protected List<? extends ResCloudlet> cloudletWaitingList;

	/** The list of cloudlets being executed on the VM. */
	protected List<? extends ResCloudlet> cloudletExecList;

	/** The list of paused cloudlets. */
	protected List<? extends ResCloudlet> cloudletPausedList;

	/** The list of finished cloudlets. */
	protected List<? extends ResCloudlet> cloudletFinishedList;

	/** The list of failed cloudlets. */
	protected List<? extends ResCloudlet> cloudletFailedList;

	/**
	 * Creates a new CloudletScheduler object. 
         * A CloudletScheduler must be created before starting the actual simulation.
	 * 
	 * @pre $none
	 * @post $none
	 */
	public CloudletScheduler() {
		setPreviousTime(0.0);
		currentCPUs = 0;

		cloudletWaitingList = new LinkedList<>();
		cloudletExecList = new LinkedList<>();
		cloudletPausedList = new LinkedList<>();
		cloudletFinishedList = new LinkedList<>();
		cloudletFailedList = new LinkedList<>();
	}

	/**
	 * Updates the processing of cloudlets running under management of this scheduler.
	 * 
	 * @param currentTime current simulation time
	 * @param mipsShare list with MIPS share of each Pe available to the scheduler
	 * @return the predicted completion time of the earliest finishing cloudlet, 
         * or 0 if there is no next events
	 * @pre currentTime >= 0
	 * @post $none
	 */
	public abstract double updateCloudletsProcessing(double currentTime, List<Double> mipsShare);

	/**
	 * Receives an cloudlet to be executed in the VM managed by this scheduler.
	 * 
	 * @param cl the submited cloudlet
	 * @param fileTransferTime time required to move the required files from the SAN to the VM
	 * @return expected finish time of this cloudlet, or 0 if it is in a waiting queue
	 * @pre gl != null
	 * @post $none
	 */
	public abstract double cloudletSubmit(Cloudlet cl, double fileTransferTime);

	/**
	 * Receives a cloudlet to be executed in the VM managed by this scheduler.
	 * 
	 * @param cl the submitted cloudlet
	 * @return expected finish time of this cloudlet, or 0 if it is in a waiting queue
	 * @pre gl != null
	 * @post $none
	 */
	public double cloudletSubmit(Cloudlet cl) { return cloudletSubmit(cl, 0); }

	/**
	 * Cancels execution of a cloudlet.
	 * 
	 * @param cloudletId ID of the cloudlet being canceled
	 * @return the canceled cloudlet, $null if not found
	 * @pre $none
	 * @post $none
	 */
	public Cloudlet cloudletCancel(final int cloudletId) {
		// First, looks in the finished queue
		int position = ResCloudletList.getPositionById(getCloudletFinishedList(), cloudletId);
		if (position >= 0) {
			return getCloudletFinishedList().remove(position).getCloudlet();
		}

		// Then searches in the exec list
		position = ResCloudletList.getPositionById(getCloudletExecList(), cloudletId);
		if (position >= 0) {
			ResCloudlet rcl = getCloudletExecList().remove(position);
			if (rcl.getRemainingCloudletLength() == 0) {
				cloudletFinish(rcl);
			} else {
				rcl.setCloudletStatus(Cloudlet.CloudletStatus.CANCELED);
			}
			return rcl.getCloudlet();
		}

		// Now, looks in the paused queue
		position = ResCloudletList.getPositionById(getCloudletPausedList(), cloudletId);
		if (position >= 0) {
			return getCloudletPausedList().remove(position).getCloudlet();
		}

		// Finally, looks in the waiting list
		position = ResCloudletList.getPositionById(getCloudletWaitingList(), cloudletId);
		if (position >= 0) {
			return getCloudletWaitingList().remove(position).getCloudlet();
		}
		return null;
	}

	/**
	 * Pauses execution of a cloudlet.
	 * 
	 * @param cloudletId ID of the cloudlet being paused
	 * @return $true if cloudlet paused, $false otherwise
	 * @pre $none
	 * @post $none
	 */
	public boolean cloudletPause(int cloudletId) {
		// first, looks for the cloudlet in the exec list
		int position = ResCloudletList.getPositionById(getCloudletExecList(), cloudletId);
		if (position >= 0) {
			// moves to the paused list
			ResCloudlet rcl = getCloudletExecList().remove(position);
			if (rcl.getRemainingCloudletLength() == 0) {
				cloudletFinish(rcl);
			} else {
				rcl.setCloudletStatus(Cloudlet.CloudletStatus.PAUSED);
				getCloudletPausedList().add(rcl);
			}
			return true;
		}

		// now, look for the cloudlet in the waiting list
		position = ResCloudletList.getPositionById(getCloudletWaitingList(), cloudletId);
		if (position >= 0) {
			// moves to the paused list
			ResCloudlet rcl = getCloudletWaitingList().remove(position);
			if (rcl.getRemainingCloudletLength() == 0) {
				cloudletFinish(rcl);
			} else {
				rcl.setCloudletStatus(Cloudlet.CloudletStatus.PAUSED);
				getCloudletPausedList().add(rcl);
			}
			return true;
		}
		return false;
	}

	/**
	 * Resumes execution of a paused cloudlet.
	 * 
	 * @param clId ID of the cloudlet being resumed
	 * @return expected finish time of the cloudlet, 0.0 if queued
	 * @pre $none
	 * @post $none
	 */
	public abstract double cloudletResume(int clId);

	/**
	 * Processes a finished cloudlet.
	 * 
	 * @param rcl finished cloudlet
	 * @pre rgl != $null
	 * @post $none
	 */
	public void cloudletFinish(ResCloudlet rcl) {
		rcl.setCloudletStatus(Cloudlet.CloudletStatus.SUCCESS);
		rcl.finalizeCloudlet();
		getCloudletFinishedList().add(rcl);
	}

	/**
	 * Gets the status of a cloudlet.
	 * 
	 * @param cloudletId ID of the cloudlet
	 * @return status of the cloudlet, -1 if cloudlet not found
	 * @pre $none
	 * @post $none
	 *
	 */
	public Cloudlet.CloudletStatus getCloudletStatus(final int cloudletId) {
		int position = ResCloudletList.getPositionById(getCloudletExecList(), cloudletId);
		if (position >= 0) {
			return getCloudletExecList().get(position).getCloudletStatus();
		}

		position = ResCloudletList.getPositionById(getCloudletPausedList(), cloudletId);
		if (position >= 0) {
			return getCloudletPausedList().get(position).getCloudletStatus();
		}

		position = ResCloudletList.getPositionById(getCloudletWaitingList(), cloudletId);
		if (position >= 0) {
			return getCloudletWaitingList().get(position).getCloudletStatus();
		}

		throw new RuntimeException("cloudlet doesn't not exist");
	}

	/**
	 * Informs if there is any cloudlet that finished to execute in the VM managed by this scheduler.
	 * 
	 * @return $true if there is at least one finished cloudlet; $false otherwise
	 * @pre $none
	 * @post $none
         * //TODO the method name would be isThereFinishedCloudlets to be clearer
	 */
	public boolean isFinishedCloudlets() { return !getCloudletFinishedList().isEmpty(); }

	/**
	 * Returns the next cloudlet in the finished list.
	 * 
	 * @return a finished cloudlet or $null if the respective list is empty
	 * @pre $none
	 * @post $none
	 */
	public Cloudlet getNextFinishedCloudlet() {
		if (!getCloudletFinishedList().isEmpty()) {
			return getCloudletFinishedList().remove(0).getCloudlet();
		}
		return null;
	}

	/**
	 * Returns the number of cloudlets running in the virtual machine.
	 * 
	 * @return number of cloudlets running
	 * @pre $none
	 * @post $none
	 */
	public int runningCloudlets(){ return getCloudletExecList().size(); }

	/**
	 * Returns one cloudlet to migrate to another vm.
	 * 
	 * @return one running cloudlet
	 * @pre $none
	 * @post $none
	 */
	public Cloudlet migrateCloudlet() {
		ResCloudlet rcl = getCloudletExecList().remove(0);
		rcl.finalizeCloudlet();
		return rcl.getCloudlet();
	}

	/**
	 * Get the estimated completion time of a given cloudlet.
	 *
	 * @param rcl the cloudlet
	 * @param time the time
	 * @return the estimated finish time
	 */
	public double getEstimatedFinishTime(ResCloudlet rcl, double time) {
		return time
				+ ((rcl.getRemainingCloudletLength()) / (getCPUCapacity(getCurrentMipsShare()) * rcl.getNumberOfPes()));
	}

	/**
	 * Gets the individual MIPS capacity available for each PE available for the scheduler,
	 * considering that all PEs have the same capacity. The default implementation is suitable for space-shared policies.
	 *
	 * @param mipsShare list with MIPS share of each PE available to the scheduler
	 * @return the capacity of each PE
	 */
	public double getCPUCapacity(final List<Double> mipsShare) {
		double capacity = 0.0;
		int cpus = 0;

		for (Double mips : mipsShare) { // count the CPUs available to the VMM
			capacity += mips;
			if (mips > 0) {
				cpus++;
			}
		}
		currentCPUs = cpus;
		capacity /= cpus; // average capacity of each cpu

		return capacity;
	}

	/**
	 * Gets total CPU utilization percentage of all cloudlets, according to CPU UtilizationModel of 
         * each one.
	 * 
	 * @param time the time to get the current CPU utilization
	 * @return total utilization
	 */
	public double getTotalUtilizationOfCpu(double time) {
		double totalUtilization = 0;
		for (ResCloudlet rcl : getCloudletExecList()) {
			totalUtilization += rcl.getCloudlet().getUtilizationOfCpu(time);
		}
		return totalUtilization;
	}

	/**
	 * Gets the current requested mips.
	 * 
	 * @return the current mips
	 */
	public List<Double> getCurrentRequestedMips() {
		List<Double> mipsShare = new ArrayList<>();
		if (getCurrentMipsShare() != null) {
			mipsShare.addAll(getCurrentMipsShare());
		}
		return mipsShare;
	}

	/**
	 * Gets the total current available mips for the Cloudlet.
	 * 
	 * @param rcl the rcl
	 * @param mipsShare the mips share
	 * @return the total current mips
         * //TODO In fact, this method is returning different data depending
         * of the subclass. It is expected that the way the method use to compute
         * the resulting value can be different in every subclass,
         * but is not supposed that each subclass returns a complete different 
         * result for the same method of the superclass.
         * In some class such as {@link NetworkCloudletSpaceSharedScheduler},
         * the method returns the average MIPS for the available PEs,
         * in other classes such as {@link CloudletSchedulerDynamicWorkload} it returns
         * the MIPS' sum of all PEs.
	 */
	public abstract double getTotalCurrentAvailableMipsForCloudlet(ResCloudlet rcl, List<Double> mipsShare);

	/**
	 * Gets the total current requested mips for a given cloudlet.
	 * 
	 * @param rcl the rcl
	 * @param time the time
	 * @return the total current requested mips for the given cloudlet
	 */
	public abstract double getTotalCurrentRequestedMipsForCloudlet(ResCloudlet rcl, double time);

	/**
	 * Gets the total current allocated mips for cloudlet.
	 * 
	 * @param rcl the rcl
	 * @param time the time
	 * @return the total current allocated mips for cloudlet
	 */
	public abstract double getTotalCurrentAllocatedMipsForCloudlet(ResCloudlet rcl, double time);

	/**
	 * Gets the current requested ram.
	 * 
	 * @return the current requested ram
	 */
	public double getCurrentRequestedUtilizationOfRam() {
		double ram = 0;
		for (ResCloudlet rcl : cloudletExecList) {
			ram += rcl.getCloudlet().getUtilizationOfRam(CloudSim.clock());
		}
		return ram;
	}
	/**
	 * Gets the current requested bw.
	 * 
	 * @return the current requested bw
	 */
	public double getCurrentRequestedUtilizationOfBw() {
		double bw = 0;
		for (ResCloudlet rcl : cloudletExecList) {
			bw += rcl.getCloudlet().getUtilizationOfBw(CloudSim.clock());
		}
		return bw;
	}
	/**
	 * Gets the previous time.
	 * 
	 * @return the previous time
	 */
	public double getPreviousTime() {
		return previousTime;
	}

	/**
	 * Sets the previous time.
	 * 
	 * @param previousTime the new previous time
	 */
	protected void setPreviousTime(double previousTime) {
		this.previousTime = previousTime;
	}

	/**
	 * Sets the current mips share.
	 * 
	 * @param currentMipsShare the new current mips share
	 */
	protected void setCurrentMipsShare(List<Double> currentMipsShare) {
		this.currentMipsShare = currentMipsShare;
	}

	/**
	 * Gets the current mips share.
	 * 
	 * @return the current mips share
	 */
	public List<Double> getCurrentMipsShare() {
		return currentMipsShare;
	}

	/**
	 * Gets the cloudlet waiting list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet waiting list
	 */
	@SuppressWarnings("unchecked")
	public <T extends ResCloudlet> List<T> getCloudletWaitingList() {
		return (List<T>) cloudletWaitingList;
	}

	/**
	 * Cloudlet waiting list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletWaitingList the cloudlet waiting list
	 */
	protected <T extends ResCloudlet> void setCloudletWaitingList(List<T> cloudletWaitingList) {
		this.cloudletWaitingList = cloudletWaitingList;
	}

	/**
	 * Gets the cloudlet exec list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet exec list
	 */
	@SuppressWarnings("unchecked")
	public <T extends ResCloudlet> List<T> getCloudletExecList() {
		return (List<T>) cloudletExecList;
	}

	/**
	 * Sets the cloudlet exec list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletExecList the new cloudlet exec list
	 */
	protected <T extends ResCloudlet> void setCloudletExecList(List<T> cloudletExecList) {
		this.cloudletExecList = cloudletExecList;
	}

	/**
	 * Gets the cloudlet paused list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet paused list
	 */
	@SuppressWarnings("unchecked")
	public <T extends ResCloudlet> List<T> getCloudletPausedList() {
		return (List<T>) cloudletPausedList;
	}

	/**
	 * Sets the cloudlet paused list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletPausedList the new cloudlet paused list
	 */
	protected <T extends ResCloudlet> void setCloudletPausedList(List<T> cloudletPausedList) {
		this.cloudletPausedList = cloudletPausedList;
	}

	/**
	 * Gets the cloudlet finished list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet finished list
	 */
	@SuppressWarnings("unchecked")
	public <T extends ResCloudlet> List<T> getCloudletFinishedList() {
		return (List<T>) cloudletFinishedList;
	}

	/**
	 * Sets the cloudlet finished list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletFinishedList the new cloudlet finished list
	 */
	protected <T extends ResCloudlet> void setCloudletFinishedList(List<T> cloudletFinishedList) {
		this.cloudletFinishedList = cloudletFinishedList;
	}

	/**
	 * Gets the cloudlet failed list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet failed list.
	 */
	@SuppressWarnings("unchecked")
	public <T extends ResCloudlet> List<T>  getCloudletFailedList() {
		return (List<T>) cloudletFailedList;
	}

	/**
	 * Sets the cloudlet failed list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletFailedList the new cloudlet failed list.
	 */
	protected <T extends ResCloudlet> void setCloudletFailedList(List<T> cloudletFailedList) {
		this.cloudletFailedList = cloudletFailedList;
	}

}
