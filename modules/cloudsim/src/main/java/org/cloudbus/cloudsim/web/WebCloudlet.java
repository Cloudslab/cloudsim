package org.cloudbus.cloudsim.web;

import org.cloudbus.cloudsim.EX.disk.DataItem;
import org.cloudbus.cloudsim.EX.disk.HddCloudlet;
import org.cloudbus.cloudsim.EX.util.Textualize;

/**
 * A web cloudlet is a cloudlet, which is a part of a web session. Usually it is
 * small in terms of RAM and CPU. Each web cloudlet is contained within a web
 * session.
 * 
 * <br/>
 * 
 * A distinct property of a web cloudlet is that it has ideal start time. This
 * ideal start time can be met, but can also be postponed if the previous web
 * cloudlets in the web session are delayed. Thus there is a difference between
 * the ideal start time and the actual start time.
 * 
 * @author nikolay.grozev
 * 
 */
@Textualize(properties = { "CloudletId", "SessionId", "Ram", "VmId", "Delay", "IdealStartTime", "ExecStartTime",
        "CloudletLength", "CloudletIOLength", "ActualCPUTime", "FinishTime", "CloudletStatusString", "Finished" })
public class WebCloudlet extends HddCloudlet {

    private final double idealStartTime;
    private int sessionId;

    /**
     * Constructs a new cloudlet.
     * 
     * @param idealStartTime
     *            - the ideal start time.
     * @param cloudletLength
     *            - number of processor instructions (MIPS) required.
     * @param ram
     *            - amount of ram in megabytes.
     * @param userId
     *            - the userId.
     * @param data
     *            - the data used by this cloudlet.
     * @param dataModifying
     *            - whether this cloudlets modifies the data it accesses.
     * @param record
     *            - the record flag, as specified by the parent class.
     */
    public WebCloudlet(final double idealStartTime, final long cloudletLength, final long cloudletIOLength,
            final double ram, final int userId, final boolean dataModifying, final DataItem data, final boolean record) {
        super(cloudletLength, cloudletIOLength, 1, 1, ram, userId, dataModifying, data, record);
        this.idealStartTime = idealStartTime;
    }

    /**
     * Constructs a new cloudlet.
     * 
     * @param idealStartTime
     *            - the ideal start time.
     * @param cloudletLength
     *            - number of processor instructions (MIPS) required
     * @param ram
     *            - amount of ram in megabytes.
     * @param userId
     *            - the userId.
     * @param data
     *            - the data used by this cloudlet.
     * @param dataModifying
     *            - whether this cloudlets modifies the data it accesses.
     */
    public WebCloudlet(final double idealStartTime, final long cloudletLength, final long cloudletIOLength,
            final double ram, final int userId, final boolean dataModifying, final DataItem data) {
        this(idealStartTime, cloudletLength, cloudletIOLength, ram, userId, dataModifying, data, false);
    }

    /**
     * Returns the ideal start time of this web cloudlet.
     * 
     * @return the ideal start time of this web cloudlet.
     */
    public double getIdealStartTime() {
        return idealStartTime;
    }

    /**
     * Returns the id of the session this cloudlet belongs to.
     * 
     * @return the id of the session this cloudlet belongs to.
     */
    public int getSessionId() {
        return sessionId;
    }

    /**
     * Sets the session id of the session this cloudlet belongs to.
     * 
     * @param sessionId
     *            - the new session id.
     */
    public void setSessionId(final int sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Returns the delay in the start time. This is equal to actual_start_time -
     * ideal_start_time. In case some of them is unknown -1 is return.
     * 
     * @return the delay in the start time, as described above.
     */
    public double getDelay() {
        double delay = getExecStartTime() - getIdealStartTime();
        return delay < 0 ? -1 : delay;
    }

}
