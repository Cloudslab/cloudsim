package org.cloudbus.cloudsim.vmplus.disk;

import org.cloudbus.cloudsim.Consts;
import org.cloudbus.cloudsim.ResCloudlet;

/**
 * 
 * Extension of {@link ResCloudlet} that allows modeling the used I/O
 * operations.
 * 
 * @author nikolay.grozev
 * 
 */
public class HddResCloudlet extends ResCloudlet {

    /** The length of Cloudlet finished so far. */
    private long cloudletIOFinishedSoFar;

    /**
     * Allocates a new ResCloudlet object upon the arrival of a Cloudlet object.
     * 
     * @param cloudlet
     *            - the new cloudlet.
     */
    public HddResCloudlet(final HddCloudlet cloudlet) {
        super(cloudlet);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cloudbus.cloudsim.ResCloudlet#getCloudlet()
     */
    @Override
    public HddCloudlet getCloudlet() {
        return (HddCloudlet) super.getCloudlet();
    }

    /**
     * Returns the number of harddisks utilized by the cloudlet.
     * 
     * @return the number of harddisks utilized by the cloudlet.
     */
    public double getNumberOfHdds() {
        return getCloudlet().getNumberOfHddPes();
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

    /*
     * (non-Javadoc)
     * 
     * @see org.cloudbus.cloudsim.ResCloudlet#updateCloudletFinishedSoFar(long)
     */
    @Override
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
        long length = getCloudlet().getCloudletTotalIOLength() * Consts.MILLION - cloudletIOFinishedSoFar;
        return length < 0 ? 0 : (long) Math.floor(1.0*length / Consts.MILLION);
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

}
