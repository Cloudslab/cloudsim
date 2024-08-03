package org.cloudbus.cloudsim.EX;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.*;
import org.cloudbus.cloudsim.EX.delay.ConstantVmBootDelay;
import org.cloudbus.cloudsim.EX.delay.IVmBootDelayDistribution;

import java.util.List;
import java.util.Objects;

/**
 * Extended datacenter, that allows for VM booting delays to be modeled.
 * 
 * @author nikolay.grozev
 * @author Remo Andreoli
 * 
 */
public class DatacenterEX extends Datacenter {
    private IVmBootDelayDistribution delayDistribution = new ConstantVmBootDelay(0);

    public DatacenterEX(String name, DatacenterCharacteristics characteristics, VmAllocationPolicy vmAllocationPolicy,
                        List<Storage> storageList, double schedulingInterval) throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
    }

    public DatacenterEX(String name, DatacenterCharacteristics characteristics, VmAllocationPolicy vmAllocationPolicy,
                        List<Storage> storageList, double schedulingInterval, IVmBootDelayDistribution delayDistribution)
            throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
        this.delayDistribution = delayDistribution;
    }

    public IVmBootDelayDistribution getDelayDistribution() { return delayDistribution; }
    public void setDelayDistribution(IVmBootDelayDistribution delayDistribution) { this.delayDistribution = delayDistribution; }

    @Override
    protected void processOtherEvent(final SimEvent ev) {
        if (ev.getTag() == CloudSimEXTags.DATACENTER_BOOT_VM_TAG) {
            GuestEntity vm = (GuestEntity) ev.getData();
            if (vm.isBeingInstantiated()) {
                vm.setBeingInstantiated(false);
            }
        } else {
            super.processOtherEvent(ev);
        }
    }

    /*
     * Copied and modified from the superclass as we wanted to avoid setting the
     * beingInstantiated flag, before the boot time.
     */
    @Override
    protected void processVmCreate(final SimEvent ev, final boolean ack) {
        GuestEntity vm = (GuestEntity) ev.getData();

        boolean result = getVmAllocationPolicy().allocateHostForGuest(vm);
        double delay = delayDistribution.getDelay(vm);
        if (ack) {
            int[] data = new int[3];
            data[0] = getId();
            data[1] = vm.getId();
            data[2] = result ? CloudSimTags.TRUE : CloudSimTags.FALSE;
            send(vm.getUserId(), delay, CloudActionTags.VM_CREATE_ACK, data);
        }

        if (result) {
            send(getId(), delay, CloudSimEXTags.DATACENTER_BOOT_VM_TAG, vm);

            getVmList().add(vm);

            // May not be instantiated yet

            vm.updateCloudletsProcessing(CloudSim.clock(), getVmAllocationPolicy().getHost(vm).getGuestScheduler()
                    .getAllocatedMipsForGuest(vm));
        }

    }

    @Override
    public String toString() {
        return String.format("DC(%s,%d)", Objects.toString(getName(), "N/A"), getId());
    }
}
