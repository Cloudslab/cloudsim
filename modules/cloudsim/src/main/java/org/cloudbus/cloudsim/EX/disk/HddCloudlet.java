package org.cloudbus.cloudsim.EX.disk;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Consts;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.EX.util.Id;
import org.cloudbus.cloudsim.EX.util.TextUtil;

/**
 * A cloudlet that requires disk operations in addition to CPU ones. Besides
 * disk operations this cloudlet also defines RAM, which is constantly requred
 * while it is running.
 * 
 * @author nikolay.grozev
 * 
 */
public class HddCloudlet extends Cloudlet {

    private static final UtilizationModelFull UTIL_MODEL_FULL = new UtilizationModelFull();

    /** The length of Cloudlet finished so far. */
    private long cloudletIOFinishedSoFar;

    private int numberOfHddPes = 1;
    protected long cloudletIOLength;
    protected final double ram;
    protected final DataItem data;
    protected boolean dataModifying;

    /**
     * Constr.
     * 
     * @param cloudletLength
     * @param cloudletIOLength
     * @param pesNumber
     * @param numberOfHddPes
     * @param ram
     * @param userId
     * @param data
     * @param dataModifying
     *            - whether this cloudlets modifies the data it accesses.
     * @param record
     */
    public HddCloudlet(final long cloudletLength, final long cloudletIOLength, final int pesNumber,
            final int numberOfHddPes, final double ram, final int userId, final boolean dataModifying,
            final DataItem data, final boolean record) {

        super(Id.pollId(Cloudlet.class), cloudletLength, pesNumber, 0, 0, UTIL_MODEL_FULL, UTIL_MODEL_FULL,
                UTIL_MODEL_FULL, record);
        this.ram = ram;
        this.cloudletIOLength = cloudletIOLength;
        this.data = data;
        this.numberOfHddPes = numberOfHddPes;
        this.dataModifying = dataModifying;
        setUserId(userId);
    }

    /**
     * Constr.
     * 
     * @param cloudletLength
     * @param cloudletIOLength
     * @param ram
     * @param userId
     * @param dataModifying
     *            - whether this cloudlets modifies the data it accesses.
     * @param data
     * @param record
     */
    public HddCloudlet(final long cloudletLength, final long cloudletIOLength, final double ram, final int userId,
            final boolean dataModifying, final DataItem data, final boolean record) {
        this(cloudletLength, cloudletIOLength, 1, 1, ram, userId, dataModifying, data, record);
    }

    /**
     * Constr.
     * 
     * @param cloudletLength
     * @param cloudletIOLength
     * @param ram
     * @param userId
     * @param dataModifying
     *            - whether this cloudlets modifies the data it accesses.
     * @param data
     */
    public HddCloudlet(final long cloudletLength, final long cloudletIOLength, final double ram, final int userId,
            final boolean dataModifying, final DataItem data) {
        this(cloudletLength, cloudletIOLength, 1, 1, ram, userId, dataModifying, data, false);
    }

    /**
     * Updates the state of the aggreagted cloudlet.
     *
     * @param cpuFinishedSoFar
     *            - the number of served CPU instructions.
     * @param ioFinishedSoFar
     *            - the number of served IO instructions.
     */
    public void updateCloudletFinishedSoFar(final long cpuFinishedSoFar, final long ioFinishedSoFar) {
        updateCloudletFinishedSoFar(cpuFinishedSoFar);
        // Do not allow addition of mips to the IOFinishedSoFar variable, if the
        // IO part of the cloudlet is finished. This is needed to avoid
        // overflow of the variable.
        if (getRemainingCloudletIOLength() > 0) {
            cloudletIOFinishedSoFar += ioFinishedSoFar;
        }
    }

    public void updateCloudletFinishedSoFar(final long miLength) {
        // Do not allow addition of mips to the finishedSoFar variable, if the
        // CPU part of the cloudlet is finished. This is needed to avoid
        // overflow of the variable
        if (getRemainingCloudletLength() > 0) {
            super.updateCloudletFinishedSoFar(miLength);
        }
    }

    /**
     * Returns how many instructions are left from the aggregated cloudlet.
     *
     * @return how many instructions are left from the aggregated cloudlet.
     */
    public long getRemainingCloudletIOLength() {
        long length = getCloudletTotalIOLength() * Consts.MILLION - cloudletIOFinishedSoFar;
        return length <= 0 ? 0 : length / Consts.MILLION;
    }

    /**
     * Returns if the aggregated cloudlet has served all its operations in terms
     * of both CPU and IO.
     *
     * @return if the aggregated cloudlet has served all its operations in terms
     *         of both CPU and IO.
     */
    public boolean isDone() {
        return getRemainingCloudletIOLength() == 0 && getRemainingCloudletLength() == 0;
    }

    /**
     * Returns the amount of ram memory used in megabytes.
     * 
     * @return the amount of ram memory used in megabytes.
     */
    public double getRam() {
        return ram;
    }

    /**
     * Returns the number of Harddisks this host has.
     * 
     * @return the number of Harddisks this host has.
     */
    public int getNumberOfHddPes() {
        return numberOfHddPes;
    }

    /**
     * Sets the number of Harddisks this host has.
     * 
     * @param numberOfHddPes
     *            - the number of Harddisks this host has. Must not be negative
     *            or 0.
     */
    public void setNumberOfHddPes(final int numberOfHddPes) {
        this.numberOfHddPes = numberOfHddPes;
    }

    /**
     * Returns the total length of this cloudlet in terms of IO operations.
     * 
     * @return the total length of this cloudlet in terms of IO operations.
     */
    public long getCloudletTotalIOLength() {
        return getCloudletIOLength() * getNumberOfHddPes();
    }

    public long getCloudletIOLength() {
        return cloudletIOLength;
    }

    /**
     * Sets the total length of this cloudlet in terms of IO operations.
     * 
     * @param cloudletIOLength
     *            - total length of this cloudlet in terms of IO operations.
     *            Must be a positive number.
     */
    public void setCloudletIOLength(final long cloudletIOLength) {
        this.cloudletIOLength = cloudletIOLength;
    }

    /**
     * Returns the data used by this cloudlet.
     * 
     * @return - the data used by this cloudlet.
     */
    public DataItem getData() {
        return data;
    }

    /**
     * Returns if this cloudlet modifies the data it accesses.
     * 
     * @return if this cloudlet modifies the data it accesses.
     */
    public boolean isDataModifying() {
        return dataModifying;
    }

    /**
     * Sets if this cloudlet modifies the data it accesses.
     * 
     * @param dataModifying
     *            - if this cloudlet modifies the data it accesses.
     */
    public void setDataModifying(final boolean dataModifying) {
        this.dataModifying = dataModifying;
    }

    @Override
    public String toString() {
        return TextUtil.getTxtLine(this, "\t", null, true);
    }

}