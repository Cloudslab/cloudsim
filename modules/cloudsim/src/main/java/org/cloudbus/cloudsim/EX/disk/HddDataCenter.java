package org.cloudbus.cloudsim.EX.disk;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudActionTags;
import org.cloudbus.cloudsim.core.HostEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.EX.DatacenterEX;
import org.cloudbus.cloudsim.EX.delay.IVmBootDelayDistribution;
import org.cloudbus.cloudsim.EX.util.CustomLog;

import java.util.List;
import java.util.logging.Level;

/**
 * 
 * A data center capable of executing harddisk cloudlets. The main difference
 * between it and a {@link Datacenter} is that it marks VMs as disabled if their
 * RAM consumption is exceeded.
 * 
 * @author nikolay.grozev
 * 
 */
public class HddDataCenter extends DatacenterEX {

    /**
     * Constr.
     * 
     * @param name
     * @param characteristics
     * @param vmAllocationPolicy
     * @param storageList
     * @param schedulingInterval
     * @throws Exception
     */
    public HddDataCenter(final String name, final DatacenterCharacteristics characteristics,
                         final VmAllocationPolicy vmAllocationPolicy, final List<Storage> storageList,
                         final double schedulingInterval) throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
    }

    public HddDataCenter(final String name, final DatacenterCharacteristics characteristics,
                         final VmAllocationPolicy vmAllocationPolicy, final List<Storage> storageList,
                         final double schedulingInterval, IVmBootDelayDistribution delayDistribution) throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval, delayDistribution);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cloudbus.cloudsim.Datacenter#processCloudletSubmit(org.cloudbus.cloudsim
     * .plus.SimEvent, boolean)
     */
    @Override
    protected void processCloudletSubmit(final SimEvent ev, final boolean ack) {
        try {
            HddCloudlet cl = (HddCloudlet) ev.getData();

            int userId = cl.getUserId();
            int vmId = cl.getGuestId();

            HddHost host = (HddHost) getVmAllocationPolicy().getHost(vmId, userId);
            HddVm vm = (HddVm) host.getGuest(vmId, userId);
            HddCloudletSchedulerTimeShared scheduler = vm.getCloudletScheduler();

            if (!vm.isOutOfMemory()) {
                List<HddCloudlet> cloudletExecList = scheduler.getCloudletExecList();

                int vmUsedRam = 0;
                for (HddCloudlet hddCl : cloudletExecList) {
                    vmUsedRam += (int) hddCl.getRam();
                }

                // If we have used all of the resources of this VM
                if (vmUsedRam + cl.getRam() > vm.getRam()) {
                    scheduler.failAllCloudlets();
                    scheduler.addFailedCloudlet(cl);
                    vm.setOutOfMemory(true);

                    CustomLog.printf("VM/Server %d on host %d in data center %s(%d) is out of memory. "
                            + "It will not be further available", vm.getId(), host.getId(), getName(), getId());
                } else {
                    super.processCloudletSubmit(ev, ack);
                }
            } else {
                scheduler.addFailedCloudlet(cl);
                CustomLog.printf("Cloudlet %d could not be submited because "
                        + "VM/Server %d on host %d in data center %s(%d) is out of memory. ", cl.getCloudletId(),
                        vm.getId(), host.getId(), getName(), getId());
            }
        } catch (Exception e) {
            CustomLog.logError(Level.SEVERE, "An error occurred when processing cloudlet sbmission", e);
        }
    }

    @Override
    protected void checkCloudletCompletion() {
        super.checkCloudletCompletion();

        // Return the failed cloudlets as well.
        for (HostEntity host : getVmAllocationPolicy().getHostList()) {
            for (HddVm vm : ((HddHost) host).getGuestList()) {
                while (vm.getCloudletScheduler().isFailedCloudlets()) {
                    Cloudlet cl = vm.getCloudletScheduler().getNextFailedCloudlet();
                    if (cl != null) {
                        sendNow(cl.getUserId(), CloudActionTags.CLOUDLET_RETURN, cl);
                    }
                }
            }
        }
    }

}
