package org.cloudbus.cloudsim.green;


import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;

class CustomCloudlet extends Cloudlet {
    public static final int HIGH_PRIORITY = 1;
    public static final int LOW_PRIORITY = 0;

    private int priority;

    public CustomCloudlet(
            final int cloudletId,
            int priority,
            final long cloudletLength,
            final int pesNumber,
            final long cloudletFileSize,
            final long cloudletOutputSize,
            final UtilizationModel utilizationModelCpu,
            final UtilizationModel utilizationModelRam,
            final UtilizationModel utilizationModelBw) {
        super(cloudletId, cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize, utilizationModelCpu, utilizationModelRam, utilizationModelBw);
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}