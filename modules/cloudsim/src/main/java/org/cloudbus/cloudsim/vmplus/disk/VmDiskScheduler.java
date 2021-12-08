package org.cloudbus.cloudsim.vmplus.disk;

import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.VmSchedulerTimeSharedOverSubscription;
import org.cloudbus.cloudsim.vmplus.VmSchedulerWithIndependentPes;

import java.util.Arrays;
import java.util.List;

/**
 * 
 * Schedules harddisks between VMs on a host.
 * 
 * @author nikolay.grozev
 * 
 */
public class VmDiskScheduler extends VmSchedulerWithIndependentPes<HddPe> {

    public VmDiskScheduler(final List<HddPe> pelist) {
        super(pelist);
    }

    @Override
    protected VmScheduler createSchedulerFroPe(final HddPe pe) {
        return new VmSchedulerTimeSharedOverSubscription(List.of(pe));
    }

    @Override
    protected boolean doesVmUse(final Vm vm, final Pe pe) {
        return vm instanceof HddVm && ((HddVm) vm).getHddsIds().contains(pe.getId());
    }

}
