package org.cloudbus.cloudsim.geoweb.web;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.plus.disk.HddCloudlet;
import org.cloudbus.cloudsim.plus.disk.HddPe;
import org.cloudbus.cloudsim.plus.disk.HddVm;
import org.cloudbus.cloudsim.plus.util.CustomLog;

import java.util.*;
import java.util.logging.Level;

/**
 * Implements round robin among the suitable DB servers.
 * 
 * @author nikolay.grozev
 * 
 */
public class RoundRobinDBBalancer extends BaseDBLoadBalancer {

    private Map<Integer, Integer> dataItemToCounter = new HashMap<>();

    /**
     * Constr.
     * 
     * @param dbVms
     *            - The list of DB vms to distribute cloudlets among.
     */
    public RoundRobinDBBalancer(final List<HddVm> dbVms) {
        super(dbVms);
    }

    /**
     * Constr.
     * 
     * @param dbVms
     *            - The list of DB vms to distribute cloudlets among.
     */
    public RoundRobinDBBalancer(final HddVm... dbVms) {
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
        List<HddVm> suitable = new ArrayList<>();
        for (HddVm vm : getVMs()) {
            for (HddPe hdd : vm.getHost().getHddList()) {
                if (vm.getHddsIds().contains(hdd.getId()) && hdd.containsDataItem(cloudlet.getData().getId())) {
                    suitable.add(vm);
                }
            }
        }

        if (suitable.isEmpty()) {
            CustomLog
                    .printf("[RoundRobinDBBalancer:] Cloudlet %d could not be assigned a DB VM, since no VM has its data item %d",
                            cloudlet.getCloudletId(), cloudlet.getData().getId());

            try {
                cloudlet.setCloudletStatus(Cloudlet.FAILED);
            } catch (Exception e) {
                CustomLog.logError(Level.SEVERE, "Unexpected error occurred", e);
            }

        } else {
            // Get the next one...
            if (!dataItemToCounter.containsKey(cloudlet.getData().getId())) {
                dataItemToCounter.put(cloudlet.getData().getId(), 0);
            }
            int idx = dataItemToCounter.get(cloudlet.getData().getId());
            idx = idx < suitable.size() ? idx : 0;
            cloudlet.setVmId(suitable.get(idx).getId());
            dataItemToCounter.put(cloudlet.getData().getId(), idx + 1);

            // If the cloudlet has not yet been assigned a VM
            if (cloudlet.getVmId() == -1) {
                CustomLog.printf("Cloudlet %d could not be assigned a DB VM, since no VM has its data item %d",
                        cloudlet.getCloudletId(), cloudlet.getData().getId());

                try {
                    cloudlet.setCloudletStatus(Cloudlet.FAILED);
                } catch (Exception e) {
                    CustomLog.logError(Level.SEVERE, "Unexpected error occurred", e);
                }
            }
        }
    }
}
