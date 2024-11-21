/**
 * 
 */
package org.cloudbus.cloudsim.web;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.EX.disk.HddCloudlet;
import org.cloudbus.cloudsim.EX.disk.HddPe;
import org.cloudbus.cloudsim.EX.disk.HddVm;
import org.cloudbus.cloudsim.EX.util.CustomLog;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/**
 * A simple DB balancer, that allocates a DB cloudlet to the first VM server
 * that has the data to serve it.
 * 
 * @author nikolay.grozev
 * 
 */
public class SimpleDBBalancer extends BaseDBLoadBalancer implements IDBBalancer {

    /**
     * Constr.
     * 
     * @param dbVms
     *            - The list of DB vms to distribute cloudlets among.
     */
    public SimpleDBBalancer(final List<HddVm> dbVms) {
        super(dbVms);
    }

    /**
     * Constr.
     * 
     * @param dbVms
     *            - The list of DB vms to distribute cloudlets among.
     */
    public SimpleDBBalancer(final HddVm... dbVms) {
        super(Arrays.asList(dbVms));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cloudbus.cloudsim.incubator.web.IDBBalancer#allocateToServer(org.
     * cloudbus.cloudsim.incubator.disk.HddCloudlet)
     */
    @Override
    public void allocateToServer(final HddCloudlet cloudlet) {
        label: for (HddVm vm : getVMs()) {
            for (HddPe hdd : vm.getHost().getHddList()) {
                if (vm.getHddsIds().contains(hdd.getId()) && hdd.containsDataItem(cloudlet.getData().getId())) {
                    cloudlet.setGuestId(vm.getId());
                    break label;
                }
            }
        }

        // If the cloudlet has not yet been assigned a VM
        if (cloudlet.getGuestId() == -1) {
            CustomLog.printf("Cloudlet %d could not be assigned a DB VM, since no VM has its data item %d",
                    cloudlet.getCloudletId(), cloudlet.getData().getId());

            try {
                cloudlet.updateStatus(Cloudlet.CloudletStatus.FAILED);
            } catch (Exception e) {
                CustomLog.logError(Level.SEVERE, "Unexpected error occurred", e);
            }
        }
    }
}
