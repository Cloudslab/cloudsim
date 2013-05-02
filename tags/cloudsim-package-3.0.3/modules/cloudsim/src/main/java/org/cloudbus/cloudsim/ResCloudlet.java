/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import org.cloudbus.cloudsim.core.CloudSim;

/**
 * CloudSim ResCloudlet represents a Cloudlet submitted to CloudResource for processing. This class
 * keeps track the time for all activities in the CloudResource for a specific Cloudlet. Before a
 * Cloudlet exits the CloudResource, it is RECOMMENDED to call this method
 * {@link #finalizeCloudlet()}.
 * <p>
 * It contains a Cloudlet object along with its arrival time and the ID of the machine and the Pe
 * (Processing Element) allocated to it. It acts as a placeholder for maintaining the amount of
 * resource share allocated at various times for simulating any scheduling using internal events.
 * 
 * @author Manzur Murshed
 * @author Rajkumar Buyya
 * @since CloudSim Toolkit 1.0
 */
public class ResCloudlet {

	/** The Cloudlet object. */
	private final Cloudlet cloudlet;

	/** The Cloudlet arrival time for the first time. */
	private double arrivalTime;

	/** The estimation of Cloudlet finished time. */
	private double finishedTime;

	/** The length of Cloudlet finished so far. */
	private long cloudletFinishedSoFar;

	/**
	 * Cloudlet execution start time. This attribute will only hold the latest time since a Cloudlet
	 * can be cancel, paused or resumed.
	 */
	private double startExecTime;

	/** The total time to complete this Cloudlet. */
	private double totalCompletionTime;

	// The below attributes are only be used by the SpaceShared policy.

	/** The machine id this Cloudlet is assigned to. */
	private int machineId;

	/** The Pe id this Cloudlet is assigned to. */
	private int peId;

	/** The an array of machine IDs. */
	private int[] machineArrayId = null;

	/** The an array of Pe IDs. */
	private int[] peArrayId = null;

	/** The index of machine and Pe arrays. */
	private int index;

	// NOTE: Below attributes are related to AR stuff

	/** The Constant NOT_FOUND. */
	private static final int NOT_FOUND = -1;

	/** The reservation start time. */
	private final long startTime;

	/** The reservation duration time. */
	private final int duration;

	/** The reservation id. */
	private final int reservId;

	/** The num Pe needed to execute this Cloudlet. */
	private int pesNumber;

	/**
	 * Allocates a new ResCloudlet object upon the arrival of a Cloudlet object. The arriving time
	 * is determined by {@link gridsim.CloudSim#clock()}.
	 * 
	 * @param cloudlet a cloudlet object
	 * @see gridsim.CloudSim#clock()
	 * @pre cloudlet != null
	 * @post $none
	 */
	public ResCloudlet(Cloudlet cloudlet) {
		// when a new ResCloudlet is created, then it will automatically set
		// the submission time and other properties, such as remaining length
		this.cloudlet = cloudlet;
		startTime = 0;
		reservId = NOT_FOUND;
		duration = 0;

		init();
	}

	/**
	 * Allocates a new ResCloudlet object upon the arrival of a Cloudlet object. Use this
	 * constructor to store reserved Cloudlets, i.e. Cloudlets that done reservation before. The
	 * arriving time is determined by {@link gridsim.CloudSim#clock()}.
	 * 
	 * @param cloudlet a cloudlet object
	 * @param startTime a reservation start time. Can also be interpreted as starting time to
	 *            execute this Cloudlet.
	 * @param duration a reservation duration time. Can also be interpreted as how long to execute
	 *            this Cloudlet.
	 * @param reservID a reservation ID that owns this Cloudlet
	 * @see gridsim.CloudSim#clock()
	 * @pre cloudlet != null
	 * @pre startTime > 0
	 * @pre duration > 0
	 * @pre reservID > 0
	 * @post $none
	 */
	public ResCloudlet(Cloudlet cloudlet, long startTime, int duration, int reservID) {
		this.cloudlet = cloudlet;
		this.startTime = startTime;
		reservId = reservID;
		this.duration = duration;

		init();
	}

	/**
	 * Gets the Cloudlet or reservation start time.
	 * 
	 * @return Cloudlet's starting time
	 * @pre $none
	 * @post $none
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * Gets the reservation duration time.
	 * 
	 * @return reservation duration time
	 * @pre $none
	 * @post $none
	 */
	public int getDurationTime() {
		return duration;
	}

	/**
	 * Gets the number of PEs required to execute this Cloudlet.
	 * 
	 * @return number of Pe
	 * @pre $none
	 * @post $none
	 */
	public int getNumberOfPes() {
		return pesNumber;
	}

	/**
	 * Gets the reservation ID that owns this Cloudlet.
	 * 
	 * @return a reservation ID
	 * @pre $none
	 * @post $none
	 */
	public int getReservationID() {
		return reservId;
	}

	/**
	 * Checks whether this Cloudlet is submitted by reserving or not.
	 * 
	 * @return <tt>true</tt> if this Cloudlet has reserved before, <tt>false</tt> otherwise
	 * @pre $none
	 * @post $none
	 */
	public boolean hasReserved() {
		if (reservId == NOT_FOUND) {
			return false;
		}

		return true;
	}

	/**
	 * Initialises all local attributes.
	 * 
	 * @pre $none
	 * @post $none
	 */
	private void init() {
		// get number of PEs required to run this Cloudlet
		pesNumber = cloudlet.getNumberOfPes();

		// if more than 1 Pe, then create an array
		if (pesNumber > 1) {
			machineArrayId = new int[pesNumber];
			peArrayId = new int[pesNumber];
		}

		arrivalTime = CloudSim.clock();
		cloudlet.setSubmissionTime(arrivalTime);

		// default values
		finishedTime = NOT_FOUND;  // Cannot finish in this hourly slot.
		machineId = NOT_FOUND;
		peId = NOT_FOUND;
		index = 0;
		totalCompletionTime = 0.0;
		startExecTime = 0.0;

		// In case a Cloudlet has been executed partially by some other grid
		// hostList.
		cloudletFinishedSoFar = cloudlet.getCloudletFinishedSoFar() * Consts.MILLION;
	}

	/**
	 * Gets this Cloudlet entity Id.
	 * 
	 * @return the Cloudlet entity Id
	 * @pre $none
	 * @post $none
	 */
	public int getCloudletId() {
		return cloudlet.getCloudletId();
	}

	/**
	 * Gets the user or owner of this Cloudlet.
	 * 
	 * @return the Cloudlet's user Id
	 * @pre $none
	 * @post $none
	 */
	public int getUserId() {
		return cloudlet.getUserId();
	}

	/**
	 * Gets the Cloudlet's length.
	 * 
	 * @return Cloudlet's length
	 * @pre $none
	 * @post $none
	 */
	public long getCloudletLength() {
		return cloudlet.getCloudletLength();
	}

	/**
	 * Gets the total Cloudlet's length (across all PEs).
	 * 
	 * @return total Cloudlet's length
	 * @pre $none
	 * @post $none
	 */
	public long getCloudletTotalLength() {
		return cloudlet.getCloudletTotalLength();
	}

	/**
	 * Gets the Cloudlet's class type.
	 * 
	 * @return class type of the Cloudlet
	 * @pre $none
	 * @post $none
	 */
	public int getCloudletClassType() {
		return cloudlet.getClassType();
	}

	/**
	 * Sets the Cloudlet status.
	 * 
	 * @param status the Cloudlet status
	 * @return <tt>true</tt> if the new status has been set, <tt>false</tt> otherwise
	 * @pre status >= 0
	 * @post $none
	 */
	public boolean setCloudletStatus(int status) {
		// gets Cloudlet's previous status
		int prevStatus = cloudlet.getCloudletStatus();

		// if the status of a Cloudlet is the same as last time, then ignore
		if (prevStatus == status) {
			return false;
		}

		boolean success = true;
		try {
			double clock = CloudSim.clock();   // gets the current clock

			// sets Cloudlet's current status
			cloudlet.setCloudletStatus(status);

			// if a previous Cloudlet status is INEXEC
			if (prevStatus == Cloudlet.INEXEC) {
				// and current status is either CANCELED, PAUSED or SUCCESS
				if (status == Cloudlet.CANCELED || status == Cloudlet.PAUSED || status == Cloudlet.SUCCESS) {
					// then update the Cloudlet completion time
					totalCompletionTime += (clock - startExecTime);
					index = 0;
					return true;
				}
			}

			if (prevStatus == Cloudlet.RESUMED && status == Cloudlet.SUCCESS) {
				// then update the Cloudlet completion time
				totalCompletionTime += (clock - startExecTime);
				return true;
			}

			// if a Cloudlet is now in execution
			if (status == Cloudlet.INEXEC || (prevStatus == Cloudlet.PAUSED && status == Cloudlet.RESUMED)) {
				startExecTime = clock;
				cloudlet.setExecStartTime(startExecTime);
			}

		} catch (Exception e) {
			success = false;
		}

		return success;
	}

	/**
	 * Gets the Cloudlet's execution start time.
	 * 
	 * @return Cloudlet's execution start time
	 * @pre $none
	 * @post $none
	 */
	public double getExecStartTime() {
		return cloudlet.getExecStartTime();
	}

	/**
	 * Sets this Cloudlet's execution parameters. These parameters are set by the CloudResource
	 * before departure or sending back to the original Cloudlet's owner.
	 * 
	 * @param wallClockTime the time of this Cloudlet resides in a CloudResource (from arrival time
	 *            until departure time).
	 * @param actualCPUTime the total execution time of this Cloudlet in a CloudResource.
	 * @pre wallClockTime >= 0.0
	 * @pre actualCPUTime >= 0.0
	 * @post $none
	 */
	public void setExecParam(double wallClockTime, double actualCPUTime) {
		cloudlet.setExecParam(wallClockTime, actualCPUTime);
	}

	/**
	 * Sets the machine and Pe (Processing Element) ID.
	 * 
	 * @param machineId machine ID
	 * @param peId Pe ID
	 * @pre machineID >= 0
	 * @pre peID >= 0
	 * @post $none
	 */
	public void setMachineAndPeId(int machineId, int peId) {
		// if this job only requires 1 Pe
		this.machineId = machineId;
		this.peId = peId;

		// if this job requires many PEs
		if (peArrayId != null && pesNumber > 1) {
			machineArrayId[index] = machineId;
			peArrayId[index] = peId;
			index++;
		}
	}

	/**
	 * Gets machine ID.
	 * 
	 * @return machine ID or <tt>-1</tt> if it is not specified before
	 * @pre $none
	 * @post $result >= -1
	 */
	public int getMachineId() {
		return machineId;
	}

	/**
	 * Gets Pe ID.
	 * 
	 * @return Pe ID or <tt>-1</tt> if it is not specified before
	 * @pre $none
	 * @post $result >= -1
	 */
	public int getPeId() {
		return peId;
	}

	/**
	 * Gets a list of Pe IDs. <br>
	 * NOTE: To get the machine IDs corresponding to these Pe IDs, use {@link #getMachineIdList()}.
	 * 
	 * @return an array containing Pe IDs.
	 * @pre $none
	 * @post $none
	 */
	public int[] getPeIdList() {
		return peArrayId;
	}

	/**
	 * Gets a list of Machine IDs. <br>
	 * NOTE: To get the Pe IDs corresponding to these machine IDs, use {@link #getPeIdList()}.
	 * 
	 * @return an array containing Machine IDs.
	 * @pre $none
	 * @post $none
	 */
	public int[] getMachineIdList() {
		return machineArrayId;
	}

	/**
	 * Gets the remaining cloudlet length.
	 * 
	 * @return cloudlet length
	 * @pre $none
	 * @post $result >= 0
	 */
	public long getRemainingCloudletLength() {
		long length = cloudlet.getCloudletTotalLength() * Consts.MILLION - cloudletFinishedSoFar;

		// Remaining Cloudlet length can't be negative number.
		if (length < 0) {
			return 0;
		}

		return (long) Math.floor(length / Consts.MILLION);
	}

	/**
	 * Finalizes all relevant information before <tt>exiting</tt> the CloudResource entity. This
	 * method sets the final data of:
	 * <ul>
	 * <li>wall clock time, i.e. the time of this Cloudlet resides in a CloudResource (from arrival
	 * time until departure time).
	 * <li>actual CPU time, i.e. the total execution time of this Cloudlet in a CloudResource.
	 * <li>Cloudlet's finished so far
	 * </ul>
	 * 
	 * @pre $none
	 * @post $none
	 */
	public void finalizeCloudlet() {
		// Sets the wall clock time and actual CPU time
		double wallClockTime = CloudSim.clock() - arrivalTime;
		cloudlet.setExecParam(wallClockTime, totalCompletionTime);

		long finished = 0;
		//if (cloudlet.getCloudletTotalLength() * Consts.MILLION < cloudletFinishedSoFar) {
		if (cloudlet.getCloudletStatus()==Cloudlet.SUCCESS) {
			finished = cloudlet.getCloudletLength();
		} else {
			finished = cloudletFinishedSoFar / Consts.MILLION;
		}

		cloudlet.setCloudletFinishedSoFar(finished);
	}

	/**
	 * A method that updates the length of cloudlet that has been completed.
	 * 
	 * @param miLength cloudlet length in Instructions (I)
	 * @pre miLength >= 0.0
	 * @post $none
	 */
	public void updateCloudletFinishedSoFar(long miLength) {
		cloudletFinishedSoFar += miLength;
	}

	/**
	 * Gets arrival time of a cloudlet.
	 * 
	 * @return arrival time
	 * @pre $none
	 * @post $result >= 0.0
	 */
	public double getCloudletArrivalTime() {
		return arrivalTime;
	}

	/**
	 * Sets the finish time for this Cloudlet. If time is negative, then it is being ignored.
	 * 
	 * @param time finish time
	 * @pre time >= 0.0
	 * @post $none
	 */
	public void setFinishTime(double time) {
		if (time < 0.0) {
			return;
		}

		finishedTime = time;
	}

	/**
	 * Gets the Cloudlet's finish time.
	 * 
	 * @return finish time of a cloudlet or <tt>-1.0</tt> if it cannot finish in this hourly slot
	 * @pre $none
	 * @post $result >= -1.0
	 */
	public double getClouddletFinishTime() {
		return finishedTime;
	}

	/**
	 * Gets this Cloudlet object.
	 * 
	 * @return cloudlet object
	 * @pre $none
	 * @post $result != null
	 */
	public Cloudlet getCloudlet() {
		return cloudlet;
	}

	/**
	 * Gets the Cloudlet status.
	 * 
	 * @return Cloudlet status
	 * @pre $none
	 * @post $none
	 */
	public int getCloudletStatus() {
		return cloudlet.getCloudletStatus();
	}

	/**
	 * Get unique string identificator of the VM.
	 * 
	 * @return string uid
	 */
	public String getUid() {
		return getUserId() + "-" + getCloudletId();
	}

}
