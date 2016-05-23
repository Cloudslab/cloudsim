package org.cloudbus.cloudsim.container.core;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.ResCloudlet;

/**
 * Created by sareh on 10/07/15.
 */
public class ResContainerCloudlet extends ResCloudlet {
    public ResContainerCloudlet(Cloudlet cloudlet) {
        super(cloudlet);
    }

    public ResContainerCloudlet(Cloudlet cloudlet, long startTime, int duration, int reservID) {
        super(cloudlet, startTime, duration, reservID);
    }


    public int getContainerId(){return((ContainerCloudlet)getCloudlet()).getContainerId();}
}
