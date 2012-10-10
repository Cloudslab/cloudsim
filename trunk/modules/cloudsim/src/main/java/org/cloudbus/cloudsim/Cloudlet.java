/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.core.CloudSim;

/**
 * Cloudlet is an extension to the cloudlet. It stores, despite all the information encapsulated in
 * the Cloudlet, the ID of the VM running it.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class Cloudlet {

	/**
	 * The User or Broker ID. It is advisable that broker set this ID with its own ID, so that
	 * CloudResource returns to it after the execution.
	 **/
	private int userId;

	/**
	 * The size of this Cloudlet to be executed in a CloudResource (unit: in MI).
	 */
	private long cloudletLength;

	/**
	 * The input file size of this Cloudlet before execution (unit: in byte). in byte = program +
	 * input data size
	 */
	private final long cloudletFileSize;

	/** The output file size of this Cloudlet after execution (unit: in byte). */
	private final long cloudletOutputSize;

	/** The num of Pe required to execute this job. */
	private int numberOfPes;

	/** The cloudlet ID. */
	private final int cloudletId;

	/** The status of this Cloudlet. */
	private int status;

	/** The format of decimal numbers. */
	private DecimalFormat num;

	/** The time where this Cloudlet completes. */
	private double finishTime;

	/**
	 * Start time of executing this Cloudlet. With new functionalities, such as CANCEL, PAUSED and
	 * RESUMED, this attribute only stores the latest execution time. Previous execution time are
	 * ignored.
	 */
	private double execStartTime;

	/** The ID of a reservation made for this cloudlet. */
	private int reservationId = -1;

	/** The records the transaction history for this Cloudlet. */
	private final boolean record;

	/** The newline. */
	private String newline;

	/** The history. */
	private StringBuffer history;

	/** The res list. */
	private final List<Resource> resList;

	/** The index. */
	private int index;

	/** The class type of Cloudlet for resource scheduling. */
	private int classType;

	/** The ToS for sending Cloudlet over the network. */
	private int netToS;

	// //////////////////////////////////////////
	// Below are CONSTANTS attributes
	/** The Cloudlet has been created and added to the CloudletList object. */
	public static final int CREATED = 0;

	/** The Cloudlet has been assigned to a CloudResource object as planned. */
	public static final int READY = 1;

	/** The Cloudlet has moved to a Cloud node. */
	public static final int QUEUED = 2;

	/** The Cloudlet is in execution in a Cloud node. */
	public static final int INEXEC = 3;

	/** The Cloudlet has been executed successfully. */
	public static final int SUCCESS = 4;

	/** The Cloudlet is failed. */
	public static final int FAILED = 5;

	/** The Cloudlet has been canceled. */
	public static final int CANCELED = 6;

	/**
	 * The Cloudlet has been paused. It can be resumed by changing the status into <tt>RESUMED</tt>.
	 */
	public static final int PAUSED = 7;

	/** The Cloudlet has been resumed from <tt>PAUSED</tt> state. */
	public static final int RESUMED = 8;

	/** The cloudlet has failed due to a resource failure. */
	public static final int FAILED_RESOURCE_UNAVAILABLE = 9;

	/** The vm id. */
	protected int vmId;

	/** The cost per bw. */
	protected double costPerBw;

	/** The accumulated bw cost. */
	protected double accumulatedBwCost;

	// Utilization

	/** The utilization of cpu model. */
	private UtilizationModel utilizationModelCpu;

	/** The utilization of memory model. */
	private UtilizationModel utilizationModelRam;

	/** The utilization of bw model. */
	private UtilizationModel utilizationModelBw;

	// Data cloudlet
	/** The required files. */
	private List<String> requiredFiles = null;   // list of required filenames

	/**
	 * Allocates a new Cloudlet object. The Cloudlet length, input and output file sizes should be
	 * greater than or equal to 1. By default this constructor sets the history of this object.
	 * 
	 * @param cloudletId the unique ID of this Cloudlet
	 * @param cloudletLength the length or size (in MI) of this cloudlet to be executed in a
	 *            PowerDatacenter
	 * @param cloudletFileSize the file size (in byte) of this cloudlet <tt>BEFORE</tt> submitting
	 *            to a PowerDatacenter
	 * @param cloudletOutputSize the file size (in byte) of this cloudlet <tt>AFTER</tt> finish
	 *            executing by a PowerDatacenter
	 * @param pesNumber the pes number
	 * @param utilizationModelCpu the utilization model cpu
	 * @param utilizationModelRam the utilization model ram
	 * @param utilizationModelBw the utilization model bw
	 * @pre cloudletID >= 0
	 * @pre cloudletLength >= 0.0
	 * @pre cloudletFileSize >= 1
	 * @pre cloudletOutputSize >= 1
	 * @post $none
	 */
	public Cloudlet(
			final int cloudletId,
			final long cloudletLength,
			final int pesNumber,
			final long cloudletFileSize,
			final long cloudletOutputSize,
			final UtilizationModel utilizationModelCpu,
			final UtilizationModel utilizationModelRam,
			final UtilizationModel utilizationModelBw) {
		this(
				cloudletId,
				cloudletLength,
				pesNumber,
				cloudletFileSize,
				cloudletOutputSize,
				utilizationModelCpu,
				utilizationModelRam,
				utilizationModelBw,
				false);
		vmId = -1;
		accumulatedBwCost = 0.0;
		costPerBw = 0.0;

		requiredFiles = new LinkedList<String>();
	}

	/**
	 * Allocates a new Cloudlet object. The Cloudlet length, input and output file sizes should be
	 * greater than or equal to 1.
	 * 
	 * @param cloudletId the unique ID of this cloudlet
	 * @param cloudletLength the length or size (in MI) of this cloudlet to be executed in a
	 *            PowerDatacenter
	 * @param cloudletFileSize the file size (in byte) of this cloudlet <tt>BEFORE</tt> submitting
	 *            to a PowerDatacenter
	 * @param cloudletOutputSize the file size (in byte) of this cloudlet <tt>AFTER</tt> finish
	 *            executing by a PowerDatacenter
	 * @param record record the history of this object or not
	 * @param fileList list of files required by this cloudlet
	 * @param pesNumber the pes number
	 * @param utilizationModelCpu the utilization model cpu
	 * @param utilizationModelRam the utilization model ram
	 * @param utilizationModelBw the utilization model bw
	 * @pre cloudletID >= 0
	 * @pre cloudletLength >= 0.0
	 * @pre cloudletFileSize >= 1
	 * @pre cloudletOutputSize >= 1
	 * @post $none
	 */
	public Cloudlet(
			final int cloudletId,
			final long cloudletLength,
			final int pesNumber,
			final long cloudletFileSize,
			final long cloudletOutputSize,
			final UtilizationModel utilizationModelCpu,
			final UtilizationModel utilizationModelRam,
			final UtilizationModel utilizationModelBw,
			final boolean record,
			final List<String> fileList) {
		this(
				cloudletId,
				cloudletLength,
				pesNumber,
				cloudletFileSize,
				cloudletOutputSize,
				utilizationModelCpu,
				utilizationModelRam,
				utilizationModelBw,
				record);
		vmId = -1;
		accumulatedBwCost = 0.0;
		costPerBw = 0.0;

		requiredFiles = fileList;
	}

	/**
	 * Allocates a new Cloudlet object. The Cloudlet length, input and output file sizes should be
	 * greater than or equal to 1. By default this constructor sets the history of this object.
	 * 
	 * @param cloudletId the unique ID of this Cloudlet
	 * @param cloudletLength the length or size (in MI) of this cloudlet to be executed in a
	 *            PowerDatacenter
	 * @param cloudletFileSize the file size (in byte) of this cloudlet <tt>BEFORE</tt> submitting
	 *            to a PowerDatacenter
	 * @param cloudletOutputSize the file size (in byte) of this cloudlet <tt>AFTER</tt> finish
	 *            executing by a PowerDatacenter
	 * @param fileList list of files required by this cloudlet
	 * @param pesNumber the pes number
	 * @param utilizationModelCpu the utilization model cpu
	 * @param utilizationModelRam the utilization model ram
	 * @param utilizationModelBw the utilization model bw
	 * @pre cloudletID >= 0
	 * @pre cloudletLength >= 0.0
	 * @pre cloudletFileSize >= 1
	 * @pre cloudletOutputSize >= 1
	 * @post $none
	 */
	public Cloudlet(
			final int cloudletId,
			final long cloudletLength,
			final int pesNumber,
			final long cloudletFileSize,
			final long cloudletOutputSize,
			final UtilizationModel utilizationModelCpu,
			final UtilizationModel utilizationModelRam,
			final UtilizationModel utilizationModelBw,
			final List<String> fileList) {
		this(
				cloudletId,
				cloudletLength,
				pesNumber,
				cloudletFileSize,
				cloudletOutputSize,
				utilizationModelCpu,
				utilizationModelRam,
				utilizationModelBw,
				false);
		vmId = -1;
		accumulatedBwCost = 0.0;
		costPerBw = 0.0;

		requiredFiles = fileList;
	}

	/**
	 * Allocates a new Cloudlet object. The Cloudlet length, input and output file sizes should be
	 * greater than or equal to 1.
	 * 
	 * @param cloudletId the unique ID of this cloudlet
	 * @param cloudletLength the length or size (in MI) of this cloudlet to be executed in a
	 *            PowerDatacenter
	 * @param cloudletFileSize the file size (in byte) of this cloudlet <tt>BEFORE</tt> submitting
	 *            to a PowerDatacenter
	 * @param cloudletOutputSize the file size (in byte) of this cloudlet <tt>AFTER</tt> finish
	 *            executing by a PowerDatacenter
	 * @param record record the history of this object or not
	 * @param pesNumber the pes number
	 * @param utilizationModelCpu the utilization model cpu
	 * @param utilizationModelRam the utilization model ram
	 * @param utilizationModelBw the utilization model bw
	 * @pre cloudletID >= 0
	 * @pre cloudletLength >= 0.0
	 * @pre cloudletFileSize >= 1
	 * @pre cloudletOutputSize >= 1
	 * @post $none
	 */
	public Cloudlet(
			final int cloudletId,
			final long cloudletLength,
			final int pesNumber,
			final long cloudletFileSize,
			final long cloudletOutputSize,
			final UtilizationModel utilizationModelCpu,
			final UtilizationModel utilizationModelRam,
			final UtilizationModel utilizationModelBw,
			final boolean record) {
		userId = -1;          // to be set by a Broker or user
		status = CREATED;
		this.cloudletId = cloudletId;
		numberOfPes = pesNumber;
		execStartTime = 0.0;
		finishTime = -1.0;    // meaning this Cloudlet hasn't finished yet
		classType = 0;
		netToS = 0;

		// Cloudlet length, Input and Output size should be at least 1 byte.
		this.cloudletLength = Math.max(1, cloudletLength);
		this.cloudletFileSize = Math.max(1, cloudletFileSize);
		this.cloudletOutputSize = Math.max(1, cloudletOutputSize);

		// Normally, a Cloudlet is only executed on a resource without being
		// migrated to others. Hence, to reduce memory consumption, set the
		// size of this ArrayList to be less than the default one.
		resList = new ArrayList<Resource>(2);
		index = -1;
		this.record = record;

		vmId = -1;
		accumulatedBwCost = 0.0;
		costPerBw = 0.0;

		requiredFiles = new LinkedList<String>();

		setUtilizationModelCpu(utilizationModelCpu);
		setUtilizationModelRam(utilizationModelRam);
		setUtilizationModelBw(utilizationModelBw);
	}

	// ////////////////////// INTERNAL CLASS ///////////////////////////////////

	/**
	 * Internal class that keeps track Cloudlet's movement in different CloudResources.
	 */
	private static class Resource {

		/** Cloudlet's submission time to a CloudResource. */
		public double submissionTime = 0.0;

		/**
		 * The time of this Cloudlet resides in a CloudResource (from arrival time until departure
		 * time).
		 */
		public double wallClockTime = 0.0;

		/** The total execution time of this Cloudlet in a CloudResource. */
		public double actualCPUTime = 0.0;

		/** Cost per second a CloudResource charge to execute this Cloudlet. */
		public double costPerSec = 0.0;

		/** Cloudlet's length finished so far. */
		public long finishedSoFar = 0;

		/** a CloudResource id. */
		public int resourceId = -1;

		/** a CloudResource name. */
		public String resourceName = null;

	} // end of internal class

	// ////////////////////// End of Internal Class //////////////////////////

	/**
	 * Sets the id of the reservation made for this cloudlet.
	 * 
	 * @param resId the reservation ID
	 * @return <tt>true</tt> if the ID has successfully been set or <tt>false</tt> otherwise.
	 */
	public boolean setReservationId(final int resId) {
		if (resId <= 0) {
			return false;
		}
		reservationId = resId;
		return true;
	}

	/**
	 * Gets the reservation ID that owns this Cloudlet.
	 * 
	 * @return a reservation ID
	 * @pre $none
	 * @post $none
	 */
	public int getReservationId() {
		return reservationId;
	}

	/**
	 * Checks whether this Cloudlet is submitted by reserving or not.
	 * 
	 * @return <tt>true</tt> if this Cloudlet has reserved before, <tt>false</tt> otherwise
	 */
	public boolean hasReserved() {
		if (reservationId == -1) {
			return false;
		}
		return true;
	}

	/**
	 * Sets the length or size (in MI) of this Cloudlet to be executed in a CloudResource. This
	 * Cloudlet length is calculated for 1 Pe only <tt>not</tt> the total length.
	 * 
	 * @param cloudletLength the length or size (in MI) of this Cloudlet to be executed in a
	 *            CloudResource
	 * @return <tt>true</tt> if it is successful, <tt>false</tt> otherwise
	 * @pre cloudletLength > 0
	 * @post $none
	 */
	public boolean setCloudletLength(final long cloudletLength) {
		if (cloudletLength <= 0) {
			return false;
		}

		this.cloudletLength = cloudletLength;
		return true;
	}

	/**
	 * Sets the network service level for sending this cloudlet over a network.
	 * 
	 * @param netServiceLevel determines the kind of service this cloudlet receives in the network
	 *            (applicable to selected PacketScheduler class only)
	 * @return <code>true</code> if successful.
	 * @pre netServiceLevel >= 0
	 * @post $none
	 */
	public boolean setNetServiceLevel(final int netServiceLevel) {
		boolean success = false;
		if (netServiceLevel > 0) {
			netToS = netServiceLevel;
			success = true;
		}

		return success;
	}

	/**
	 * Gets the network service level for sending this cloudlet over a network.
	 * 
	 * @return the network service level
	 * @pre $none
	 * @post $none
	 */
	public int getNetServiceLevel() {
		return netToS;
	}

	/**
	 * Gets the waiting time of this cloudlet executed on a resource.
	 * 
	 * @return the waiting time
	 * @pre $none
	 * @post $none
	 */
	public double getWaitingTime() {
		if (index == -1) {
			return 0;
		}

		// use the latest resource submission time
		final double subTime = resList.get(index).submissionTime;
		return execStartTime - subTime;
	}

	/**
	 * Sets the classType or priority of this Cloudlet for scheduling on a resource.
	 * 
	 * @param classType classType of this Cloudlet
	 * @return <tt>true</tt> if it is successful, <tt>false</tt> otherwise
	 * @pre classType > 0
	 * @post $none
	 */
	public boolean setClassType(final int classType) {
		boolean success = false;
		if (classType > 0) {
			this.classType = classType;
			success = true;
		}

		return success;
	}

	/**
	 * Gets the classtype or priority of this Cloudlet for scheduling on a resource.
	 * 
	 * @return classtype of this cloudlet
	 * @pre $none
	 * @post $none
	 */
	public int getClassType() {
		return classType;
	}

	/**
	 * Sets the number of PEs required to run this Cloudlet. <br>
	 * NOTE: The Cloudlet length is computed only for 1 Pe for simplicity. <br>
	 * For example, this Cloudlet has a length of 500 MI and requires 2 PEs. This means each Pe will
	 * execute 500 MI of this Cloudlet.
	 * 
	 * @param numberOfPes number of Pe
	 * @return <tt>true</tt> if it is successful, <tt>false</tt> otherwise
	 * @pre numPE > 0
	 * @post $none
	 */
	public boolean setNumberOfPes(final int numberOfPes) {
		if (numberOfPes > 0) {
			this.numberOfPes = numberOfPes;
			return true;
		}
		return false;
	}

	/**
	 * Gets the number of PEs required to run this Cloudlet.
	 * 
	 * @return number of PEs
	 * @pre $none
	 * @post $none
	 */
	public int getNumberOfPes() {
		return numberOfPes;
	}

	/**
	 * Gets the history of this Cloudlet. The layout of this history is in a readable table column
	 * with <tt>time</tt> and <tt>description</tt> as headers.
	 * 
	 * @return a String containing the history of this Cloudlet object.
	 * @pre $none
	 * @post $result != null
	 */
	public String getCloudletHistory() {
		String msg = null;
		if (history == null) {
			msg = "No history is recorded for Cloudlet #" + cloudletId;
		} else {
			msg = history.toString();
		}

		return msg;
	}

	/**
	 * Gets the length of this Cloudlet that has been executed so far from the latest CloudResource.
	 * This method is useful when trying to move this Cloudlet into different CloudResources or to
	 * cancel it.
	 * 
	 * @return the length of a partially executed Cloudlet or the full Cloudlet length if it is
	 *         completed
	 * @pre $none
	 * @post $result >= 0.0
	 */
	public long getCloudletFinishedSoFar() {
		if (index == -1) {
			return cloudletLength;
		}

		final long finish = resList.get(index).finishedSoFar;
		if (finish > cloudletLength) {
			return cloudletLength;
		}

		return finish;
	}

	/**
	 * Checks whether this Cloudlet has finished execution or not.
	 * 
	 * @return <tt>true</tt> if this Cloudlet has finished execution, <tt>false</tt> otherwise
	 * @pre $none
	 * @post $none
	 */
	public boolean isFinished() {
		if (index == -1) {
			return false;
		}

		boolean completed = false;

		// if result is 0 or -ve then this Cloudlet has finished
		final long finish = resList.get(index).finishedSoFar;
		final long result = cloudletLength - finish;
		if (result <= 0.0) {
			completed = true;
		}
		return completed;
	}

	/**
	 * Sets the length of this Cloudlet that has been executed so far. This method is used by
	 * ResCloudlet class when an application is decided to cancel or to move this Cloudlet into
	 * different CloudResources.
	 * 
	 * @param length length of this Cloudlet
	 * @see gridsim.AllocPolicy
	 * @see gridsim.ResCloudlet
	 * @pre length >= 0.0
	 * @post $none
	 */
	public void setCloudletFinishedSoFar(final long length) {
		// if length is -ve then ignore
		if (length < 0.0 || index < 0) {
			return;
		}

		final Resource res = resList.get(index);
		res.finishedSoFar = length;

		if (record) {
			write("Sets the length's finished so far to " + length);
		}
	}

	/**
	 * Sets the user or owner ID of this Cloudlet. It is <tt>VERY</tt> important to set the user ID,
	 * otherwise this Cloudlet will not be executed in a CloudResource.
	 * 
	 * @param id the user ID
	 * @pre id >= 0
	 * @post $none
	 */
	public void setUserId(final int id) {
		userId = id;
		if (record) {
			write("Assigns the Cloudlet to " + CloudSim.getEntityName(id) + " (ID #" + id + ")");
		}
	}

	/**
	 * Gets the user or owner ID of this Cloudlet.
	 * 
	 * @return the user ID or <tt>-1</tt> if the user ID has not been set before
	 * @pre $none
	 * @post $result >= -1
	 */
	public int getUserId() {
		return userId;
	}

	/**
	 * Gets the latest resource ID that processes this Cloudlet.
	 * 
	 * @return the resource ID or <tt>-1</tt> if none
	 * @pre $none
	 * @post $result >= -1
	 */
	public int getResourceId() {
		if (index == -1) {
			return -1;
		}
		return resList.get(index).resourceId;
	}

	/**
	 * Gets the input file size of this Cloudlet <tt>BEFORE</tt> submitting to a CloudResource.
	 * 
	 * @return the input file size of this Cloudlet
	 * @pre $none
	 * @post $result >= 1
	 */
	public long getCloudletFileSize() {
		return cloudletFileSize;
	}

	/**
	 * Gets the output size of this Cloudlet <tt>AFTER</tt> submitting and executing to a
	 * CloudResource.
	 * 
	 * @return the Cloudlet output file size
	 * @pre $none
	 * @post $result >= 1
	 */
	public long getCloudletOutputSize() {
		return cloudletOutputSize;
	}

	/**
	 * Sets the resource parameters for which this Cloudlet is going to be executed. <br>
	 * NOTE: This method <tt>should</tt> be called only by a resource entity, not the user or owner
	 * of this Cloudlet.
	 * 
	 * @param resourceID the CloudResource ID
	 * @param cost the cost running this CloudResource per second
	 * @pre resourceID >= 0
	 * @pre cost > 0.0
	 * @post $none
	 */
	public void setResourceParameter(final int resourceID, final double cost) {
		final Resource res = new Resource();
		res.resourceId = resourceID;
		res.costPerSec = cost;
		res.resourceName = CloudSim.getEntityName(resourceID);

		// add into a list if moving to a new grid resource
		resList.add(res);

		if (index == -1 && record) {
			write("Allocates this Cloudlet to " + res.resourceName + " (ID #" + resourceID
					+ ") with cost = $" + cost + "/sec");
		} else if (record) {
			final int id = resList.get(index).resourceId;
			final String name = resList.get(index).resourceName;
			write("Moves Cloudlet from " + name + " (ID #" + id + ") to " + res.resourceName + " (ID #"
					+ resourceID + ") with cost = $" + cost + "/sec");
		}

		index++;  // initially, index = -1
	}

	/**
	 * Sets the submission or arrival time of this Cloudlet into a CloudResource.
	 * 
	 * @param clockTime the submission time
	 * @pre clockTime >= 0.0
	 * @post $none
	 */
	public void setSubmissionTime(final double clockTime) {
		if (clockTime < 0.0 || index < 0) {
			return;
		}

		final Resource res = resList.get(index);
		res.submissionTime = clockTime;

		if (record) {
			write("Sets the submission time to " + num.format(clockTime));
		}
	}

	/**
	 * Gets the submission or arrival time of this Cloudlet from the latest CloudResource.
	 * 
	 * @return the submission time or <tt>0.0</tt> if none
	 * @pre $none
	 * @post $result >= 0.0
	 */
	public double getSubmissionTime() {
		if (index == -1) {
			return 0.0;
		}
		return resList.get(index).submissionTime;
	}

	/**
	 * Sets the execution start time of this Cloudlet inside a CloudResource. <b>NOTE:</b> With new
	 * functionalities, such as being able to cancel / to pause / to resume this Cloudlet, the
	 * execution start time only holds the latest one. Meaning, all previous execution start time
	 * are ignored.
	 * 
	 * @param clockTime the latest execution start time
	 * @pre clockTime >= 0.0
	 * @post $none
	 */
	public void setExecStartTime(final double clockTime) {
		execStartTime = clockTime;
		if (record) {
			write("Sets the execution start time to " + num.format(clockTime));
		}
	}

	/**
	 * Gets the latest execution start time.
	 * 
	 * @return the latest execution start time
	 * @pre $none
	 * @post $result >= 0.0
	 */
	public double getExecStartTime() {
		return execStartTime;
	}

	/**
	 * Sets this Cloudlet's execution parameters. These parameters are set by the CloudResource
	 * before departure or sending back to the original Cloudlet's owner.
	 * 
	 * @param wallTime the time of this Cloudlet resides in a CloudResource (from arrival time until
	 *            departure time).
	 * @param actualTime the total execution time of this Cloudlet in a CloudResource.
	 * @pre wallTime >= 0.0
	 * @pre actualTime >= 0.0
	 * @post $none
	 */
	public void setExecParam(final double wallTime, final double actualTime) {
		if (wallTime < 0.0 || actualTime < 0.0 || index < 0) {
			return;
		}

		final Resource res = resList.get(index);
		res.wallClockTime = wallTime;
		res.actualCPUTime = actualTime;

		if (record) {
			write("Sets the wall clock time to " + num.format(wallTime) + " and the actual CPU time to "
					+ num.format(actualTime));
		}
	}

	/**
	 * Sets the status code of this Cloudlet.
	 * 
	 * @param newStatus the status code of this Cloudlet
	 * @throws Exception Invalid range of Cloudlet status
	 * @pre newStatus >= 0 && newStatus <= 8
	 * @post $none
	 */
	public void setCloudletStatus(final int newStatus) throws Exception {
		// if the new status is same as current one, then ignore the rest
		if (status == newStatus) {
			return;
		}

		// throws an exception if the new status is outside the range
		if (newStatus < Cloudlet.CREATED || newStatus > Cloudlet.FAILED_RESOURCE_UNAVAILABLE) {
			throw new Exception(
					"Cloudlet.setCloudletStatus() : Error - Invalid integer range for Cloudlet status.");
		}

		if (newStatus == Cloudlet.SUCCESS) {
			finishTime = CloudSim.clock();
		}

		if (record) {
			write("Sets Cloudlet status from " + getCloudletStatusString() + " to "
					+ Cloudlet.getStatusString(newStatus));
		}

		status = newStatus;
	}

	/**
	 * Gets the status code of this Cloudlet.
	 * 
	 * @return the status code of this Cloudlet
	 * @pre $none
	 * @post $result >= 0
	 */
	public int getCloudletStatus() {
		return status;
	}

	/**
	 * Gets the string representation of the current Cloudlet status code.
	 * 
	 * @return the Cloudlet status code as a string or <tt>null</tt> if the status code is unknown
	 * @pre $none
	 * @post $none
	 */
	public String getCloudletStatusString() {
		return Cloudlet.getStatusString(status);
	}

	/**
	 * Gets the string representation of the given Cloudlet status code.
	 * 
	 * @param status the Cloudlet status code
	 * @return the Cloudlet status code as a string or <tt>null</tt> if the status code is unknown
	 * @pre $none
	 * @post $none
	 */
	public static String getStatusString(final int status) {
		String statusString = null;
		switch (status) {
			case Cloudlet.CREATED:
				statusString = "Created";
				break;

			case Cloudlet.READY:
				statusString = "Ready";
				break;

			case Cloudlet.INEXEC:
				statusString = "InExec";
				break;

			case Cloudlet.SUCCESS:
				statusString = "Success";
				break;

			case Cloudlet.QUEUED:
				statusString = "Queued";
				break;

			case Cloudlet.FAILED:
				statusString = "Failed";
				break;

			case Cloudlet.CANCELED:
				statusString = "Canceled";
				break;

			case Cloudlet.PAUSED:
				statusString = "Paused";
				break;

			case Cloudlet.RESUMED:
				statusString = "Resumed";
				break;

			case Cloudlet.FAILED_RESOURCE_UNAVAILABLE:
				statusString = "Failed_resource_unavailable";
				break;

			default:
				break;
		}

		return statusString;
	}

	/**
	 * Gets the length of this Cloudlet.
	 * 
	 * @return the length of this Cloudlet
	 * @pre $none
	 * @post $result >= 0.0
	 */
	public long getCloudletLength() {
		return cloudletLength;
	}

	/**
	 * Gets the total length (across all PEs) of this Cloudlet.
	 * 
	 * @return the total length of this Cloudlet
	 * @pre $none
	 * @post $result >= 0.0
	 */
	public long getCloudletTotalLength() {
		return getCloudletLength() * getNumberOfPes();
	}

	/**
	 * Gets the cost running this Cloudlet in the latest CloudResource.
	 * 
	 * @return the cost associated with running this Cloudlet or <tt>0.0</tt> if none
	 * @pre $none
	 * @post $result >= 0.0
	 */
	public double getCostPerSec() {
		if (index == -1) {
			return 0.0;
		}
		return resList.get(index).costPerSec;
	}

	/**
	 * Gets the time of this Cloudlet resides in the latest CloudResource (from arrival time until
	 * departure time).
	 * 
	 * @return the time of this Cloudlet resides in a CloudResource
	 * @pre $none
	 * @post $result >= 0.0
	 */
	public double getWallClockTime() {
		if (index == -1) {
			return 0.0;
		}
		return resList.get(index).wallClockTime;
	}

	/**
	 * Gets all the CloudResource names that executed this Cloudlet.
	 * 
	 * @return an array of CloudResource names or <tt>null</tt> if it has none
	 * @pre $none
	 * @post $none
	 */
	public String[] getAllResourceName() {
		final int size = resList.size();
		String[] data = null;

		if (size > 0) {
			data = new String[size];
			for (int i = 0; i < size; i++) {
				data[i] = resList.get(i).resourceName;
			}
		}

		return data;
	}

	/**
	 * Gets all the CloudResource IDs that executed this Cloudlet.
	 * 
	 * @return an array of CloudResource IDs or <tt>null</tt> if it has none
	 * @pre $none
	 * @post $none
	 */
	public int[] getAllResourceId() {
		final int size = resList.size();
		int[] data = null;

		if (size > 0) {
			data = new int[size];
			for (int i = 0; i < size; i++) {
				data[i] = resList.get(i).resourceId;
			}
		}

		return data;
	}

	/**
	 * Gets the total execution time of this Cloudlet in a given CloudResource ID.
	 * 
	 * @param resId a CloudResource entity ID
	 * @return the total execution time of this Cloudlet in a CloudResource or <tt>0.0</tt> if not
	 *         found
	 * @pre resId >= 0
	 * @post $result >= 0.0
	 */
	public double getActualCPUTime(final int resId) {
		Resource resource = getResourceById(resId);
		if (resource != null) {
			return resource.actualCPUTime;
		}
		return 0.0;
	}

	/**
	 * Gets the cost running this Cloudlet in a given CloudResource ID.
	 * 
	 * @param resId a CloudResource entity ID
	 * @return the cost associated with running this Cloudlet or <tt>0.0</tt> if not found
	 * @pre resId >= 0
	 * @post $result >= 0.0
	 */
	public double getCostPerSec(final int resId) {
		Resource resource = getResourceById(resId);
		if (resource != null) {
			return resource.costPerSec;
		}
		return 0.0;
	}

	/**
	 * Gets the length of this Cloudlet that has been executed so far in a given CloudResource ID.
	 * This method is useful when trying to move this Cloudlet into different CloudResources or to
	 * cancel it.
	 * 
	 * @param resId a CloudResource entity ID
	 * @return the length of a partially executed Cloudlet or the full Cloudlet length if it is
	 *         completed or <tt>0.0</tt> if not found
	 * @pre resId >= 0
	 * @post $result >= 0.0
	 */
	public long getCloudletFinishedSoFar(final int resId) {
		Resource resource = getResourceById(resId);
		if (resource != null) {
			return resource.finishedSoFar;
		}
		return 0;
	}

	/**
	 * Gets the submission or arrival time of this Cloudlet in the given CloudResource ID.
	 * 
	 * @param resId a CloudResource entity ID
	 * @return the submission time or <tt>0.0</tt> if not found
	 * @pre resId >= 0
	 * @post $result >= 0.0
	 */
	public double getSubmissionTime(final int resId) {
		Resource resource = getResourceById(resId);
		if (resource != null) {
			return resource.submissionTime;
		}
		return 0.0;
	}

	/**
	 * Gets the time of this Cloudlet resides in a given CloudResource ID (from arrival time until
	 * departure time).
	 * 
	 * @param resId a CloudResource entity ID
	 * @return the time of this Cloudlet resides in the CloudResource or <tt>0.0</tt> if not found
	 * @pre resId >= 0
	 * @post $result >= 0.0
	 */
	public double getWallClockTime(final int resId) {
		Resource resource = getResourceById(resId);
		if (resource != null) {
			return resource.wallClockTime;
		}
		return 0.0;
	}

	/**
	 * Gets the CloudResource name based on its ID.
	 * 
	 * @param resId a CloudResource entity ID
	 * @return the CloudResource name or <tt>null</tt> if not found
	 * @pre resId >= 0
	 * @post $none
	 */
	public String getResourceName(final int resId) {
		Resource resource = getResourceById(resId);
		if (resource != null) {
			return resource.resourceName;
		}
		return null;
	}

	/**
	 * Gets the resource by id.
	 * 
	 * @param resourceId the resource id
	 * @return the resource by id
	 */
	public Resource getResourceById(final int resourceId) {
		for (Resource resource : resList) {
			if (resource.resourceId == resourceId) {
				return resource;
			}
		}
		return null;
	}

	/**
	 * Gets the finish time of this Cloudlet in a CloudResource.
	 * 
	 * @return the finish or completion time of this Cloudlet or <tt>-1</tt> if not finished yet.
	 * @pre $none
	 * @post $result >= -1
	 */
	public double getFinishTime() {
		return finishTime;
	}

	// //////////////////////// PROTECTED METHODS //////////////////////////////

	/**
	 * Writes this particular history transaction of this Cloudlet into a log.
	 * 
	 * @param str a history transaction of this Cloudlet
	 * @pre str != null
	 * @post $none
	 */
	protected void write(final String str) {
		if (!record) {
			return;
		}

		if (num == null || history == null) { // Creates the history or
												// transactions of this Cloudlet
			newline = System.getProperty("line.separator");
			num = new DecimalFormat("#0.00#"); // with 3 decimal spaces
			history = new StringBuffer(1000);
			history.append("Time below denotes the simulation time.");
			history.append(System.getProperty("line.separator"));
			history.append("Time (sec)       Description Cloudlet #" + cloudletId);
			history.append(System.getProperty("line.separator"));
			history.append("------------------------------------------");
			history.append(System.getProperty("line.separator"));
			history.append(num.format(CloudSim.clock()));
			history.append("   Creates Cloudlet ID #" + cloudletId);
			history.append(System.getProperty("line.separator"));
		}

		history.append(num.format(CloudSim.clock()));
		history.append("   " + str + newline);
	}

	/**
	 * Get the status of the Cloudlet.
	 * 
	 * @return status of the Cloudlet
	 * @pre $none
	 * @post $none
	 */
	public int getStatus() {
		return getCloudletStatus();
	}

	/**
	 * Gets the ID of this Cloudlet.
	 * 
	 * @return Cloudlet Id
	 * @pre $none
	 * @post $none
	 */
	public int getCloudletId() {
		return cloudletId;
	}

	/**
	 * Gets the ID of the VM that will run this Cloudlet.
	 * 
	 * @return VM Id, -1 if the Cloudlet was not assigned to a VM
	 * @pre $none
	 * @post $none
	 */
	public int getVmId() {
		return vmId;
	}

	/**
	 * Sets the ID of the VM that will run this Cloudlet.
	 * 
	 * @param vmId the vm id
	 * @pre id >= 0
	 * @post $none
	 */
	public void setVmId(final int vmId) {
		this.vmId = vmId;
	}

	/**
	 * Returns the time the Cloudlet actually run.
	 * 
	 * @return time in which the Cloudlet was running
	 * @pre $none
	 * @post $none
	 */
	public double getActualCPUTime() {
		return getFinishTime() - getExecStartTime();
	}

	/**
	 * Sets the resource parameters for which this Cloudlet is going to be executed. <br>
	 * NOTE: This method <tt>should</tt> be called only by a resource entity, not the user or owner
	 * of this Cloudlet.
	 * 
	 * @param resourceID the CloudResource ID
	 * @param costPerCPU the cost running this Cloudlet per second
	 * @param costPerBw the cost of data transfer to this PowerDatacenter
	 * @pre resourceID >= 0
	 * @pre cost > 0.0
	 * @post $none
	 */
	public void setResourceParameter(final int resourceID, final double costPerCPU, final double costPerBw) {
		setResourceParameter(resourceID, costPerCPU);
		this.costPerBw = costPerBw;
		accumulatedBwCost = costPerBw * getCloudletFileSize();

	}

	/**
	 * Gets the total cost of processing or executing this Cloudlet
	 * <tt>Processing Cost = input data transfer + processing cost + output transfer cost</tt> .
	 * 
	 * @return the total cost of processing Cloudlet
	 * @pre $none
	 * @post $result >= 0.0
	 */
	public double getProcessingCost() {
		// cloudlet cost: execution cost...
		// double cost = getProcessingCost();
		double cost = 0;
		// ...plus input data transfer cost...
		cost += accumulatedBwCost;
		// ...plus output cost
		cost += costPerBw * getCloudletOutputSize();
		return cost;
	}

	// Data cloudlet

	/**
	 * Gets the required files.
	 * 
	 * @return the required files
	 */
	public List<String> getRequiredFiles() {
		return requiredFiles;
	}

	/**
	 * Sets the required files.
	 * 
	 * @param requiredFiles the new required files
	 */
	protected void setRequiredFiles(final List<String> requiredFiles) {
		this.requiredFiles = requiredFiles;
	}

	/**
	 * Adds the required filename to the list.
	 * 
	 * @param fileName the required filename
	 * @return <tt>true</tt> if succesful, <tt>false</tt> otherwise
	 */
	public boolean addRequiredFile(final String fileName) {
		// if the list is empty
		if (getRequiredFiles() == null) {
			setRequiredFiles(new LinkedList<String>());
		}

		// then check whether filename already exists or not
		boolean result = false;
		for (int i = 0; i < getRequiredFiles().size(); i++) {
			final String temp = getRequiredFiles().get(i);
			if (temp.equals(fileName)) {
				result = true;
				break;
			}
		}

		if (!result) {
			getRequiredFiles().add(fileName);
		}

		return result;
	}

	/**
	 * Deletes the given filename from the list.
	 * 
	 * @param filename the given filename to be deleted
	 * @return <tt>true</tt> if succesful, <tt>false</tt> otherwise
	 */
	public boolean deleteRequiredFile(final String filename) {
		boolean result = false;
		if (getRequiredFiles() == null) {
			return result;
		}

		for (int i = 0; i < getRequiredFiles().size(); i++) {
			final String temp = getRequiredFiles().get(i);

			if (temp.equals(filename)) {
				getRequiredFiles().remove(i);
				result = true;

				break;
			}
		}

		return result;
	}

	/**
	 * Checks whether this cloudlet requires any files or not.
	 * 
	 * @return <tt>true</tt> if required, <tt>false</tt> otherwise
	 */
	public boolean requiresFiles() {
		boolean result = false;
		if (getRequiredFiles() != null && getRequiredFiles().size() > 0) {
			result = true;
		}

		return result;
	}

	/**
	 * Gets the utilization model cpu.
	 * 
	 * @return the utilization model cpu
	 */
	public UtilizationModel getUtilizationModelCpu() {
		return utilizationModelCpu;
	}

	/**
	 * Sets the utilization model cpu.
	 * 
	 * @param utilizationModelCpu the new utilization model cpu
	 */
	public void setUtilizationModelCpu(final UtilizationModel utilizationModelCpu) {
		this.utilizationModelCpu = utilizationModelCpu;
	}

	/**
	 * Gets the utilization model ram.
	 * 
	 * @return the utilization model ram
	 */
	public UtilizationModel getUtilizationModelRam() {
		return utilizationModelRam;
	}

	/**
	 * Sets the utilization model ram.
	 * 
	 * @param utilizationModelRam the new utilization model ram
	 */
	public void setUtilizationModelRam(final UtilizationModel utilizationModelRam) {
		this.utilizationModelRam = utilizationModelRam;
	}

	/**
	 * Gets the utilization model bw.
	 * 
	 * @return the utilization model bw
	 */
	public UtilizationModel getUtilizationModelBw() {
		return utilizationModelBw;
	}

	/**
	 * Sets the utilization model bw.
	 * 
	 * @param utilizationModelBw the new utilization model bw
	 */
	public void setUtilizationModelBw(final UtilizationModel utilizationModelBw) {
		this.utilizationModelBw = utilizationModelBw;
	}

	/**
	 * Gets the total utilization of cpu.
	 * 
	 * @param time the time
	 * @return the utilization of cpu
	 */
	public double getUtilizationOfCpu(final double time) {
		return getUtilizationModelCpu().getUtilization(time);
	}

	/**
	 * Gets the utilization of memory.
	 * 
	 * @param time the time
	 * @return the utilization of memory
	 */
	public double getUtilizationOfRam(final double time) {
		return getUtilizationModelRam().getUtilization(time);
	}

	/**
	 * Gets the utilization of bw.
	 * 
	 * @param time the time
	 * @return the utilization of bw
	 */
	public double getUtilizationOfBw(final double time) {
		return getUtilizationModelBw().getUtilization(time);
	}

}
