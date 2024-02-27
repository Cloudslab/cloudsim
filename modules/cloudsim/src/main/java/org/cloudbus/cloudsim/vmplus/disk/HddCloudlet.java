package org.cloudbus.cloudsim.vmplus.disk;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.vmplus.util.Id;
import org.cloudbus.cloudsim.vmplus.util.TextUtil;

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