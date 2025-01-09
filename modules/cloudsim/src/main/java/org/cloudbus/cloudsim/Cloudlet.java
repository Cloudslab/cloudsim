/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */
package org.cloudbus.cloudsim;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.core.CloudSim;

/**
 * Cloudlet is a user activity to be deployed on a Cloud Resource. It stores, despite all the
 * information encapsulated in the Cloudlet, the ID of the VM running it.
 *
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @author Remo Andreoli
 * @since CloudSim Toolkit 1.0
 */
public class Cloudlet {
    public enum CloudletStatus {
        /** The Cloudlet has been created and added to the CloudletList object. */
        CREATED,

        /** The Cloudlet has been assigned to a CloudResource object to be executed as planned. */
        READY,

        /** The Cloudlet has moved to a Cloud node. */
        QUEUED,

        /** The Cloudlet is in execution in a Cloud node. */
        INEXEC,

        /** The Cloudlet has been executed successfully. */
        SUCCESS,

        /** The Cloudlet has failed. */
        FAILED,

        /** The Cloudlet has been canceled. */
        CANCELED,

        /** The Cloudlet has been paused. It can be resumed by changing the status into RESUMED. */
        PAUSED,

        /** The Cloudlet has been resumed from PAUSED state. */
        RESUMED,

        /** The cloudlet has failed due to a resource failure.*/
        FAILED_RESOURCE_UNAVAILABLE;
        public static final CloudletStatus[] values = values();
        // @NOTE: Convert an int to enum with CloudletStatus.values[i] (mind the array bounds)
    }


    /**
     * The cloudlet ID.
     */
    private final int cloudletId;

    /**
     * The User or Broker ID. It is advisable that broker set this ID with its
     * own ID, so that CloudResource returns to it after the execution.
     */
    private int userId;

    /** The Cloudlet UID */
    private String uid;

    /**
     * The execution length of this Cloudlet (Unit: in Million Instructions
     * (MI)). According to this length and the power of the processor (in
     * Million Instruction Per Second - MIPS) where the cloudlet will be run,
     * the cloudlet will take a given time to finish processing. For instance,
     * for a cloudlet of 10000 MI running on a processor of 2000 MIPS, the
     * cloudlet will spend 5 seconds using the processor in order to be
     * completed (that may be uninterrupted or not, depending on the scheduling
     * policy).
     *
     * @see #setNumberOfPes(int)
     */
    private long cloudletLength;

    /**
     * The input file size of this Cloudlet before execution (unit: in byte).
     * This size has to be considered the program + input data sizes.
     */
    private final long cloudletFileSize;

    /**
     * The output file size of this Cloudlet after execution (unit: in byte).
     * <p>
     * //@TODO See
     * <a href="https://groups.google.com/forum/#!topic/cloudsim/MyZ7OnrXuuI">this
     * discussion</a>
     */
    private final long cloudletOutputSize;

    /**
     * The number of Processing Elements (Pe) required to execute this cloudlet
     * (job).
     *
     * @see #setNumberOfPes(int)
     */
    private int numberOfPes;

    /**
     * The execution status of this Cloudlet.
     */
    private CloudletStatus status;

    /**
     * The execution start time of this Cloudlet. With new functionalities, such
     * as CANCEL, PAUSED and RESUMED, this attribute only stores the latest
     * execution time. Previous execution time are ignored.
     */
    private double execStartTime;

    /**
     * The time where this Cloudlet completes.
     */
    private double execFinishTime;

    /** The total time to complete this Cloudlet. */
    private double totalCompletionTime;
    /**
     * The ID of a reservation made for this cloudlet.
     * <p>
     * //@TODO This attribute doesn't appear to be used
     */
    private int reservationId = -1;

    /**
     * Indicates if transaction history records for this Cloudlet is to be
     * outputted.
     */
    private final boolean record;

    /**
     * Stores the operating system line separator.
     */
    private String newline;

    /**
     * The cloudlet transaction history.
     */
    private StringBuffer history;

    /**
     * The list of every resource where the cloudlet has been executed. In case
     * it starts and finishes executing in a single cloud resource, without
     * being migrated, this list will have only one item.
     */
    private final List<Resource> resList;

    /**
     * The classType or priority of this Cloudlet for scheduling on a resource.
     */
    private int classType;

    /**
     * The Type of Service (ToS) of IPv4 for sending Cloudlet over the network.
     */
    private int netToS;

    /**
     * The format of decimal numbers.
     */
    private DecimalFormat num;

    /**
     * The id of the guest entity that is planned to execute the cloudlet.
     */
    protected int guestId;

    /**
     * The id of the container is planned to execute the cloudlet.
     * It may be -1, if containers are not in use
     */
    // @TODO: to be deprecated in favor of guestId
    protected int containerId = -1;
    
    /**
     * The cost of each byte of bandwidth (bw) consumed.
     */
    protected double costPerBw;

    /**
     * The total bandwidth (bw) cost for transferring the cloudlet by the
     * network, according to the {@link #cloudletFileSize}.
     */
    protected double accumulatedBwCost;

    // Utilization
    /**
     * The utilization model that defines how the cloudlet will use the VM's
     * CPU.
     */
    private UtilizationModel utilizationModelCpu;

    /**
     * The utilization model that defines how the cloudlet will use the VM's
     * RAM.
     */
    private UtilizationModel utilizationModelRam;

    /**
     * The utilization model that defines how the cloudlet will use the VM's
     * bandwidth (bw).
     */
    private UtilizationModel utilizationModelBw;

    // Data cloudlet
    /**
     * The required files to be used by the cloudlet (if any). The time to
     * transfer these files by the network is considered when placing the
     * cloudlet inside a given VM
     */
    private List<String> requiredFiles = null;

    /**
     * Allocates a new Cloudlet object. The Cloudlet length, input and output
     * file sizes should be greater than or equal to 1. By default this
     * constructor sets the history of this object.
     *
     * @param cloudletId          the unique ID of this Cloudlet
     * @param cloudletLength      the length or size (in MI) of this cloudlet to be
     *                            executed in a PowerDatacenter
     * @param cloudletFileSize    the file size (in byte) of this cloudlet
     *                            <tt>BEFORE</tt> submitting to a Datacenter
     * @param cloudletOutputSize  the file size (in byte) of this cloudlet
     *                            <tt>AFTER</tt> finish executing by a Datacenter
     * @param pesNumber           the pes number
     * @param utilizationModelCpu the utilization model of cpu
     * @param utilizationModelRam the utilization model of ram
     * @param utilizationModelBw  the utilization model of bw
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
        guestId = -1;
        accumulatedBwCost = 0;
        costPerBw = 0;
        requiredFiles = new LinkedList<>();
    }

    /**
     * Allocates a new Cloudlet object. The Cloudlet length, input and output
     * file sizes should be greater than or equal to 1.
     *
     * @param cloudletId          the unique ID of this cloudlet
     * @param cloudletLength      the length or size (in MI) of this cloudlet to be
     *                            executed in a PowerDatacenter
     * @param cloudletFileSize    the file size (in byte) of this cloudlet
     *                            <tt>BEFORE</tt> submitting to a PowerDatacenter
     * @param cloudletOutputSize  the file size (in byte) of this cloudlet
     *                            <tt>AFTER</tt> finish executing by a PowerDatacenter
     * @param record              record the history of this object or not
     * @param fileList            list of files required by this cloudlet
     * @param pesNumber           the pes number
     * @param utilizationModelCpu the utilization model of cpu
     * @param utilizationModelRam the utilization model of ram
     * @param utilizationModelBw  the utilization model of bw
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
        guestId = -1;
        accumulatedBwCost = 0.0;
        costPerBw = 0.0;

        requiredFiles = fileList;
    }

    /**
     * Allocates a new Cloudlet object. The Cloudlet length, input and output
     * file sizes should be greater than or equal to 1. By default this
     * constructor sets the history of this object.
     *
     * @param cloudletId          the unique ID of this Cloudlet
     * @param cloudletLength      the length or size (in MI) of this cloudlet to be
     *                            executed in a PowerDatacenter
     * @param cloudletFileSize    the file size (in byte) of this cloudlet
     *                            <tt>BEFORE</tt> submitting to a PowerDatacenter
     * @param cloudletOutputSize  the file size (in byte) of this cloudlet
     *                            <tt>AFTER</tt> finish executing by a PowerDatacenter
     * @param fileList            list of files required by this cloudlet
     * @param pesNumber           the pes number
     * @param utilizationModelCpu the utilization model of cpu
     * @param utilizationModelRam the utilization model of ram
     * @param utilizationModelBw  the utilization model of bw
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
        guestId = -1;
        accumulatedBwCost = 0.0;
        costPerBw = 0.0;

        requiredFiles = fileList;
    }

    /**
     * Allocates a new Cloudlet object. The Cloudlet length, input and output
     * file sizes should be greater than or equal to 1.
     *
     * @param cloudletId          the unique ID of this cloudlet
     * @param cloudletLength      the length or size (in MI) of this cloudlet to be
     *                            executed in a PowerDatacenter
     * @param cloudletFileSize    the file size (in byte) of this cloudlet
     *                            <tt>BEFORE</tt> submitting to a PowerDatacenter
     * @param cloudletOutputSize  the file size (in byte) of this cloudlet
     *                            <tt>AFTER</tt> finish executing by a PowerDatacenter
     * @param record              record the history of this object or not
     * @param pesNumber           the pes number
     * @param utilizationModelCpu the utilization model of cpu
     * @param utilizationModelRam the utilization model of ram
     * @param utilizationModelBw  the utilization model of bw
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
        status = CloudletStatus.CREATED;
        this.cloudletId = cloudletId;
        numberOfPes = pesNumber;

        execStartTime = 0.0;
        execFinishTime = -1.0;    // meaning this Cloudlet hasn't finished yet
        classType = 0;
        netToS = 0;

        // Cloudlet length, Input and Output size should be at least 1 byte.
        this.cloudletLength = Math.max(1, cloudletLength);
        this.cloudletFileSize = Math.max(1, cloudletFileSize);
        this.cloudletOutputSize = Math.max(1, cloudletOutputSize);

        // Normally, a Cloudlet is only executed on a resource without being
        // migrated to others. Hence, to reduce memory consumption, set the
        // size of this ArrayList to be less than the default one.
        resList = new LinkedList<>();
        this.record = record;

        guestId = -1;
        accumulatedBwCost = 0.0;
        costPerBw = 0.0;

        requiredFiles = new LinkedList<>();

        setUtilizationModelCpu(utilizationModelCpu);
        setUtilizationModelRam(utilizationModelRam);
        setUtilizationModelBw(utilizationModelBw);
        updateUid();
    }

    /** Backward compatibility with ResCloudlet class in CloudSim6G */
    @Deprecated
    public Cloudlet getCloudlet() { return this; }

    // ////////////////////// INTERNAL CLASS ///////////////////////////////////

    /**
     * Internal class that keeps track of Cloudlet's movement in different
     * CloudResources. Each time a cloudlet is run on a given VM, the cloudlet's
     * execution history on each VM is registered at {@link Cloudlet#resList}
     */
    public static class Resource {

        /** Cloudlet's submission (arrival) time to a CloudResource. */
        public double arrivalTime = 0.0;

        /**
         * The time this Cloudlet resides in a CloudResource (from arrival time
         * until departure time, that may include waiting time).
         */
        public double wallClockTime = 0.0;

        /** The total time the Cloudlet spent being executed in a CloudResource. */
        public double actualCPUTime = 0.0;

        /** Cost per second a CloudResource charge to execute this Cloudlet. */
        public double costPerSec = 0.0;

        /** Cloudlet's length finished so far on the CloudResource. */
        public long cloudletFinishedSoFar = 0;

        /** CloudResource id. */
        public int resourceId = -1;

        /** CloudResource name. */
        public String resourceName = null;

    }

    // ////////////////////// End of Internal Class //////////////////////////

    /**
     * Update the cloudlet (override this to customise the cloudlet behavior).
     * @param info a custom object class you may need to implement the update logic
     * @return true if the update causes a variation in cloudlet length
     */

    public boolean updateCloudlet(Object info) {
        return true;
    }

    /**
     * Finalizes all relevant information before <tt>exiting</tt> the CloudResource entity. This
     * method sets the final data of:
     * <ul>
     * <li>wall clock time, i.e. the time of this Cloudlet resides in a CloudResource (from arrival
     * time until departure time).
     * <li>actual CPU time, i.e. the total execution time of this Cloudlet in a CloudResource.
     * <li>Cloudlet's finished time so far
     * </ul>
     *
     * @pre $none
     * @post $none
     */
    public void finalizeCloudlet() {
        // Sets the wall clock time and actual CPU time
        double wallClockTime = CloudSim.clock() - getSubmissionTime();
        setExecParam(wallClockTime, totalCompletionTime);

        //if (cloudlet.getCloudletTotalLength() * Consts.MILLION < cloudletFinishedSoFar) {
        if (getStatus() == Cloudlet.CloudletStatus.SUCCESS) {
            setCloudletFinishedSoFar(getCloudletLength());
        }
    }

    /**
     * Sets the id of the reservation made for this cloudlet.
     *
     * @param resId the reservation ID
     * @return <tt>true</tt> if the ID has successfully been set or
     * <tt>false</tt> otherwise.
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
     * @return <tt>true</tt> if this Cloudlet has reserved before,
     * <tt>false</tt> otherwise
     */
    public boolean hasReserved() {
        return reservationId != -1;
    }

    /**
     * Sets the length or size (in MI) of this Cloudlet to be executed in a
     * CloudResource. It has to be the length for each individual Pe,
     * <tt>not</tt> the total length (the sum of length to be executed by each
     * Pe).
     *
     * @param cloudletLength the length or size (in MI) of this Cloudlet to be
     *                       executed in a CloudResource
     * @return <tt>true</tt> if it is successful, <tt>false</tt> otherwise
     * @pre cloudletLength > 0
     * @post $none
     * @see #getCloudletTotalLength() }
     */
    public boolean setCloudletLength(final long cloudletLength) {
        if (cloudletLength <= 0) {
            return false;
        }

        this.cloudletLength = cloudletLength;
        return true;
    }

    /**
     * Sets the network service level (ToS) for sending this cloudlet over a
     * network.
     *
     * @param netServiceLevel determines the type of service (ToS) this cloudlet
     *                        receives in the network (applicable to selected PacketScheduler class
     *                        only)
     * @return <code>true</code> if successful.
     * @pre netServiceLevel >= 0
     * @post $none
     * <p>
     * //@TODO The name of the setter is inconsistent with the attribute name,
     * what might be misinterpreted by other developers.
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
     * Gets the network service level (ToS) for sending this cloudlet over a
     * network.
     *
     * @return the network service level
     * @pre $none
     * @post $none
     * //@TODO The name of the getter is inconsistent with the attribute name,
     * what might be misinterpreted by other developers.
     */
    public int getNetServiceLevel() {
        return netToS;
    }

    /**
     * Gets the time the cloudlet had to wait before start executing on a
     * resource.
     *
     * @return the waiting time
     * @pre $none
     * @post $none
     */
    public double getWaitingTime() {
        if (resList.isEmpty()) {
            return 0;
        }

        // use the latest resource submission time
        final double subTime = resList.getLast().arrivalTime;
        return execStartTime - subTime;
    }

    /**
     * Sets the classType or priority of this Cloudlet for scheduling on a
     * resource.
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
     * Gets the classtype or priority of this Cloudlet for scheduling on a
     * resource.
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
     * For example, consider a Cloudlet that has a length of 500 MI and requires
     * 2 PEs. This means each Pe will execute 500 MI of this Cloudlet.
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
     * Gets the transaction history of this Cloudlet. The layout of this history
     * is in a readable table column with <tt>time</tt> and <tt>description</tt>
     * as headers.
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
     * Gets the length of this Cloudlet that has been executed so far from the
     * latest CloudResource. This method is useful when trying to move this
     * Cloudlet into different CloudResources or to cancel it.
     *
     * @return the length of a partially executed Cloudlet or the full Cloudlet
     * length if it is completed
     * @pre $none
     * @post $result >= 0.0
     */
    public long getCloudletFinishedSoFar() {
        if (resList.isEmpty()) {
            return 0;
        }

        return Math.min(resList.getLast().cloudletFinishedSoFar, getCloudletTotalLength()*Consts.MILLION);
    }

    /**
     * Checks whether this Cloudlet has finished execution or not.
     *
     * @return <tt>true</tt> if this Cloudlet has finished execution,
     * <tt>false</tt> otherwise
     * @pre $none
     * @post $none
     */
    public boolean isFinished() {
        if (resList.isEmpty()) {
            return false;
        }
        // if result is 0 or -ve then this Cloudlet has finished
        return getRemainingCloudletLength() <= 0.0;
    }
    /**
     * Updates the length of cloudlet that has already been completed.
     *
     * @param miLength cloudlet length in Instructions (I)
     * @pre miLength >= 0.0
     * @post $none
     */
    public void updateCloudletFinishedSoFar(long miLength) {
        setCloudletFinishedSoFar(getCloudletFinishedSoFar() + miLength);
    }

    /**
     * Sets the length of this Cloudlet that has been executed so far. This
     * method is used by ResCloudlet class when an application is decided to
     * cancel or to move this Cloudlet into different CloudResources.
     *
     * @param length length of this Cloudlet
     * @pre length >= 0.0
     * @post $none
     * @see VmAllocationPolicy
     * @see Cloudlet
     */
    public void setCloudletFinishedSoFar(final long length) {
        // if length is -ve then ignore
        if (length < 0.0 || resList.isEmpty()) {
            return;
        }

        resList.getLast().cloudletFinishedSoFar = length;

        if (record) {
            write("Sets the length's finished so far to " + length);
        }
    }

    /**
     * Sets the user or owner ID of this Cloudlet. It is <tt>VERY</tt> important
     * to set the user ID, otherwise this Cloudlet will not be executed in a
     * CloudResource.
     *
     * @param id the user ID
     * @pre id >= 0
     * @post $none
     */
    public void setUserId(final int id) {
        userId = id;
        updateUid();
        if (record) {
            write("Assigns the Cloudlet to " + CloudSim.getEntityName(id) + " (ID #" + id + ")");
        }
    }

    /**
     * Gets the latest resource ID that processes this Cloudlet.
     *
     * @return the resource ID or <tt>-1</tt> if none
     * @pre $none
     * @post $result >= -1
     */
    public int getResourceId() {
        if (resList.isEmpty()) {
            return -1;
        }
        return resList.getLast().resourceId;
    }

    public double getExecFinishTime() {
        return execFinishTime;
    }

    @Deprecated
    public double getFinishTime() { return getExecFinishTime(); }

    /**
     * Gets the input file size of this Cloudlet <tt>BEFORE</tt> submitting to a
     * CloudResource.
     *
     * @return the input file size of this Cloudlet
     * @pre $none
     * @post $result >= 1
     */
    public long getCloudletFileSize() {
        return cloudletFileSize;
    }

    /**
     * Gets the output size of this Cloudlet <tt>AFTER</tt> submitting and
     * executing to a CloudResource.
     *
     * @return the Cloudlet output file size
     * @pre $none
     * @post $result >= 1
     */
    public long getCloudletOutputSize() {
        return cloudletOutputSize;
    }

    /**
     * Sets the resource parameters for which the Cloudlet is going to be
     * executed. From the second time this method is called, every call make the
     * cloudlet to be migrated to the indicated resource.<br>
     * <p>
     * NOTE: This method <tt>should</tt> be called only by a resource entity,
     * not the user or owner of this Cloudlet.
     *
     * @param resourceID the CloudResource ID
     * @param cost       the cost running this CloudResource per second
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

        if (resList.size() == 1 && record) {
            write("Allocates this Cloudlet to " + res.resourceName + " (ID #" + resourceID
                    + ") with cost = $" + cost + "/sec");
        } else if (record) {
            final int id = resList.getLast().resourceId;
            final String name = resList.getLast().resourceName;
            write("Moves Cloudlet from " + name + " (ID #" + id + ") to " + res.resourceName + " (ID #"
                    + resourceID + ") with cost = $" + cost + "/sec");
        }

        setSubmissionTime(CloudSim.clock());
    }

    /**
     * Sets the submission (arrival) time of this Cloudlet into a CloudResource.
     *
     * @param clockTime the submission time
     * @pre clockTime >= 0.0
     * @post $none
     */
    public void setSubmissionTime(final double clockTime) {
        if (clockTime < 0.0 || resList.isEmpty()) {
            return;
        }

        resList.getLast().arrivalTime = clockTime;

        if (record) {
            write("Sets the submission time to " + num.format(clockTime));
        }
    }

    /**
     * Gets the submission (arrival) time of this Cloudlet from the latest
     * CloudResource.
     *
     * @return the submission time or <tt>0.0</tt> if none
     * @pre $none
     * @post $result >= 0.0
     */
    public double getSubmissionTime() {
        if (resList.isEmpty()) {
            return 0.0;
        }
        return resList.getLast().arrivalTime;
    }

    @Deprecated
    public double getCloudletArrivalTime() { return getSubmissionTime(); }
    @Deprecated
    public double getArrivalTime() { return getSubmissionTime(); }

    /**
     * Sets the execution start time of this Cloudlet inside a CloudResource.
     * <br/>
     * <b>NOTE:</b> With new functionalities, such as being able to cancel / to
     * pause / to resume this Cloudlet, the execution start time only holds the
     * latest one. Meaning, all previous execution start time are ignored.
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
     * Sets the Cloudlet's execution parameters. These parameters are set by the
     * CloudResource before departure or sending back to the original Cloudlet's
     * owner.
     *
     * @param wallTime   the time of this Cloudlet resides in a CloudResource
     *                   (from arrival time until departure time).
     * @param actualTime the total execution time of this Cloudlet in a
     *                   CloudResource.
     * @pre wallTime >= 0.0
     * @pre actualTime >= 0.0
     * @post $none
     * @see Resource#wallClockTime
     * @see Resource#actualCPUTime
     */
    public void setExecParam(final double wallTime, final double actualTime) {
        if (wallTime < 0.0 || actualTime < 0.0 || resList.isEmpty()) {
            return;
        }

        final Resource res = resList.getLast();
        res.wallClockTime = wallTime;
        res.actualCPUTime = actualTime;

        if (record) {
            write("Sets the wall clock time to " + num.format(wallTime) + " and the actual CPU time to "
                    + num.format(actualTime));
        }
    }

    public CloudletStatus getStatus() { return status; }

    /**
     * Sets the execution status of the Cloudlet.
     *
     * @param status the Cloudlet status
     * @return <tt>true</tt> if the new status has been set, <tt>false</tt> otherwise
     * @pre status >= 0
     * @post $none
     */
    public boolean updateStatus(final CloudletStatus status) {
        // gets Cloudlet's previous status
        Cloudlet.CloudletStatus prevStatus = getStatus();

        // if the status of a Cloudlet is the same as last time, then ignore
        if (prevStatus == status) {
            return false;
        }

        // sets Cloudlet's current status
        if (status == CloudletStatus.SUCCESS) {
            execFinishTime = CloudSim.clock();
        }

        double clock = CloudSim.clock();
        this.status = status;

        if (record) {
            write("Sets Cloudlet status from " + getCloudletStatusString() + " to " + status.toString());
        }

        // if a previous Cloudlet status is INEXEC
        if (prevStatus == Cloudlet.CloudletStatus.INEXEC) {
            // and current status is either CANCELED, PAUSED or SUCCESS
            if (status == Cloudlet.CloudletStatus.CANCELED || status == Cloudlet.CloudletStatus.PAUSED || status == Cloudlet.CloudletStatus.SUCCESS) {
                // then update the Cloudlet completion time
                totalCompletionTime += (clock - execStartTime);
                return true;
            }
        }

        if (prevStatus == Cloudlet.CloudletStatus.RESUMED && status == Cloudlet.CloudletStatus.SUCCESS) {
            // then update the Cloudlet completion time
            totalCompletionTime += (clock - execStartTime);
            return true;
        }

        // if a Cloudlet is now in execution
        if (status == Cloudlet.CloudletStatus.INEXEC || (prevStatus == Cloudlet.CloudletStatus.PAUSED && status == Cloudlet.CloudletStatus.RESUMED)) {
            setExecStartTime(clock);
            return true;
        }

        return false;
    }

    @Deprecated
    public boolean setStatus(CloudletStatus newStatus) { return updateStatus(newStatus); }
    @Deprecated
    public void setCloudletStatus(final CloudletStatus newStatus) { updateStatus(newStatus); }

    public int getUserId() { return userId; }

    public int getCloudletId() { return cloudletId; }

    public int getContainerId() { return containerId; }
    public void setContainerId(int containerId) { this.containerId = containerId; }

    /**
     * Gets the string representation of the current Cloudlet status code.
     *
     * @return the Cloudlet status code as a string or <tt>null</tt> if the
     * status code is unknown
     * @pre $none
     * @post $none
     */
    public String getCloudletStatusString() {
        return status.toString();
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
     * Gets the total length (across all PEs) of this Cloudlet. It considers the
     * {@link #cloudletLength} of the cloudlet to be executed in each Pe and the
     * {@link #numberOfPes}.<br/>
     * <p>
     * For example, setting the cloudletLenght as 10000 MI and
     * {@link #numberOfPes} to 4, each Pe will execute 10000 MI. Thus, the
     * entire cloudlet has a total length of 40000 MI.
     *
     * @return the total length of this Cloudlet
     * @pre $none
     * @post $result >= 0.0
     * @see #setCloudletLength(long)
     */
    public long getCloudletTotalLength() {
        return getCloudletLength() * getNumberOfPes();
    }

    public double getExecStartTime() { return execStartTime; }

    /**
     * Gets the cost/sec of running the Cloudlet in the latest CloudResource.
     *
     * @return the cost associated with running this Cloudlet or <tt>0.0</tt> if
     * none
     * @pre $none
     * @post $result >= 0.0
     */
    public double getCostPerSec() {
        if (resList.isEmpty()) {
            return 0.0;
        }
        return resList.getLast().costPerSec;
    }

    /**
     * Gets the time of this Cloudlet resides in the latest CloudResource (from
     * arrival time until departure time).
     *
     * @return the time of this Cloudlet resides in a CloudResource
     * @pre $none
     * @post $result >= 0.0
     */
    public double getWallClockTime() {
        if (resList.isEmpty()) {
            return 0.0;
        }
        return resList.getLast().wallClockTime;
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
     * Gets the total execution time of this Cloudlet in a given CloudResource
     * ID.
     *
     * @param resId a CloudResource entity ID
     * @return the total execution time of this Cloudlet in a CloudResource or
     * <tt>0.0</tt> if not found
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
     * @return the cost associated with running this Cloudlet or <tt>0.0</tt> if
     * not found
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
     * Gets the length of this Cloudlet that has been executed so far in a given
     * CloudResource ID. This method is useful when trying to move this Cloudlet
     * into different CloudResources or to cancel it.
     *
     * @param resId a CloudResource entity ID
     * @return the length of a partially executed Cloudlet or the full Cloudlet
     * length if it is completed or <tt>0.0</tt> if not found
     * @pre resId >= 0
     * @post $result >= 0.0
     */
    public long getCloudletFinishedSoFar(final int resId) {
        Resource resource = getResourceById(resId);
        if (resource != null) {
            return resource.cloudletFinishedSoFar;
        }
        return 0;
    }

    /**
     * Gets the remaining cloudlet length that has to be execute yet,
     * considering the {@link #getCloudletTotalLength()}.
     *
     * @return cloudlet length
     * @pre $none
     * @post $result >= 0
     */
    public long getRemainingCloudletLength() {
        long length = getCloudletTotalLength()*Consts.MILLION - getCloudletFinishedSoFar();

        // Remaining Cloudlet length can't be negative number.
        if (length <= 0) {
            return 0;
        }
        return length/Consts.MILLION;
    }

    /**
     * Gets the submission (arrival) time of this Cloudlet in the given
     * CloudResource ID.
     *
     * @param resId a CloudResource entity ID
     * @return the submission time or <tt>0.0</tt> if not found
     * @pre resId >= 0
     * @post $result >= 0.0
     */
    public double getSubmissionTime(final int resId) {
        Resource resource = getResourceById(resId);
        if (resource != null) {
            return resource.arrivalTime;
        }
        return 0.0;
    }

    /**
     * Gets the time of this Cloudlet resides in a given CloudResource ID (from
     * arrival time until departure time).
     *
     * @param resId a CloudResource entity ID
     * @return the time of this Cloudlet resides in the CloudResource or
     * <tt>0.0</tt> if not found
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
            history.append("Time (sec)       Description Cloudlet #").append(cloudletId);
            history.append(System.getProperty("line.separator"));
            history.append("------------------------------------------");
            history.append(System.getProperty("line.separator"));
            history.append(num.format(CloudSim.clock()));
            history.append("   Creates Cloudlet ID #").append(cloudletId);
            history.append(System.getProperty("line.separator"));
        }

        history.append(num.format(CloudSim.clock()));
        history.append("   ").append(str).append(newline);
    }

    /**
     * Returns the execution time of the Cloudlet.
     *
     * @return time in which the Cloudlet was running
     * @pre $none
     * @post $none
     */
    public double getActualCPUTime() {
        return getExecFinishTime() - getExecStartTime();
    }

    /**
     * Sets the resource parameters for which this Cloudlet is going to be
     * executed. <br>
     * NOTE: This method <tt>should</tt> be called only by a resource entity,
     * not the user or owner of this Cloudlet.
     *
     * @param resourceID the CloudResource ID
     * @param costPerCPU the cost per second of running this Cloudlet
     * @param costPerBw  the cost per byte of data transfer to the Datacenter
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
     * <tt>Processing Cost = input data transfer + processing cost + output
     * transfer cost</tt> .
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
     * Adds the required filename to the list.
     *
     * @param fileName the required filename
     * @return <tt>true</tt> if succesful, <tt>false</tt> otherwise
     */
    public boolean addRequiredFile(final String fileName) {
        // if the list is empty
        if (getRequiredFiles() == null) {
            setRequiredFiles(new LinkedList<>());
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
        return getRequiredFiles() != null && !getRequiredFiles().isEmpty();
    }

    public int getGuestId() { return guestId; }

    @Deprecated
    public int getVmId() {return getGuestId(); }

    public void setGuestId(int guestId) { this.guestId = guestId; }

    @Deprecated
    public void setVmId(int vmId) { setGuestId(vmId); }

    /**
     * Gets the utilization model of cpu.
     *
     * @return the utilization model cpu
     */
    public UtilizationModel getUtilizationModelCpu() {
        return utilizationModelCpu;
    }

    /**
     * Sets the utilization model of cpu.
     *
     * @param utilizationModelCpu the new utilization model of cpu
     */
    public void setUtilizationModelCpu(final UtilizationModel utilizationModelCpu) {
        this.utilizationModelCpu = utilizationModelCpu;
    }

    /**
     * Gets the utilization model of ram.
     *
     * @return the utilization model of ram
     */
    public UtilizationModel getUtilizationModelRam() {
        return utilizationModelRam;
    }

    /**
     * Sets the utilization model of ram.
     *
     * @param utilizationModelRam the new utilization model of ram
     */
    public void setUtilizationModelRam(final UtilizationModel utilizationModelRam) {
        this.utilizationModelRam = utilizationModelRam;
    }

    /**
     * Gets the utilization model of bw.
     *
     * @return the utilization model of bw
     */
    public UtilizationModel getUtilizationModelBw() {
        return utilizationModelBw;
    }

    /**
     * Sets the utilization model of bw.
     *
     * @param utilizationModelBw the new utilization model of bw
     */
    public void setUtilizationModelBw(final UtilizationModel utilizationModelBw) {
        this.utilizationModelBw = utilizationModelBw;
    }

    /**
     * Get am Unique Identifier (UID) of the cloudlet.
     *
     * @return The UID
     */
    public String getUid() {
        return uid;
    }

    /** update uid after change of cloudletId or userId */
    private void updateUid() {
        uid = getUserId() + "-" + getCloudletId();
    }

    /**
     * Gets the utilization percentage of cpu.
     *
     * @param time the time
     * @return the utilization of cpu
     */
    public double getUtilizationOfCpu(final double time) {
        return getUtilizationModelCpu().getUtilization(time);
    }

    /**
     * Gets the utilization percentage of memory.
     *
     * @param time the time
     * @return the utilization of memory
     */
    public double getUtilizationOfRam(final double time) {
        return getUtilizationModelRam().getUtilization(time);
    }

    /**
     * Gets the utilization percentage of bw.
     *
     * @param time the time
     * @return the utilization of bw
     */
    public double getUtilizationOfBw(final double time) {
        return getUtilizationModelBw().getUtilization(time);
    }

    public List<String> getRequiredFiles() { return requiredFiles; }

    protected void setRequiredFiles(List<String> requiredFiles) { this.requiredFiles = requiredFiles; }
}
