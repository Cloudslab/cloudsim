package org.cloudbus.cloudsim.container.resourceAllocators;

import org.cloudbus.cloudsim.container.core.Container;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.core.HostEntity;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyAbstract;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sareh on 16/07/15.
 * Modified by Remo Andreoli (March 2024)
 *
 * TODO: Remo Andreoli: I don't know how to reconcile this file for deduplication
 */
public abstract class PowerContainerAllocationPolicy extends PowerVmAllocationPolicyAbstract {

        /** The container table. */
        private final Map<String, HostEntity> guestTable = new HashMap<>();

        /**
         * Instantiates a new power vm allocation policy abstract.
         *
         */
        public PowerContainerAllocationPolicy(List<? extends HostEntity> list) {
            super(list);
        }

        @Override
        public boolean allocateHostForGuest(GuestEntity guest) {
            return allocateHostForGuest(guest, findHostForGuest(guest));
        }

        /*
         * (non-Javadoc)
         * @see org.cloudbus.cloudsim.VmAllocationPolicy#allocateHostForVm(org.cloudbus.cloudsim.Vm,
         * org.cloudbus.cloudsim.Host)
         */
        @Override
        public boolean allocateHostForGuest(GuestEntity guest, HostEntity containerVm) {
            if (containerVm == null) {
                Log.formatLine("%.2f: No suitable VM found for Container#" + guest.getId() + "\n", CloudSim.clock());
                return false;
            }
            if (containerVm.guestCreate(guest)) { // if vm has been succesfully created in the host
                getGuestTable().put(guest.getUid(), containerVm);
//                container.setVm(containerVm);
                Log.formatLine(
                        "%.2f: Container #" + guest.getId() + " has been allocated to the VM #" + containerVm.getId(),
                        CloudSim.clock());
                return true;
            }
            Log.formatLine(
                    "%.2f: Creation of Container #" + guest.getId() + " on the Vm #" + containerVm.getId() + " failed\n",
                    CloudSim.clock());
            return false;
        }

        /*
         * (non-Javadoc)
         * @see org.cloudbus.cloudsim.VmAllocationPolicy#deallocateHostForVm(org.cloudbus.cloudsim.Vm)
         */
        @Override
        public void deallocateHostForGuest(GuestEntity guest) {
            HostEntity containerVm = getGuestTable().remove(guest.getUid());
            if (containerVm != null) {
                containerVm.guestDestroy(guest);
            }
        }

        /*
         * (non-Javadoc)
         * @see org.cloudbus.cloudsim.VmAllocationPolicy#getHost(org.cloudbus.cloudsim.Vm)
         */
        @Override
        public HostEntity getHost(GuestEntity guest) {
            return getGuestTable().get(guest.getUid());
        }

        /*
         * (non-Javadoc)
         * @see org.cloudbus.cloudsim.VmAllocationPolicy#getHost(int, int)
         */
        @Override
        public HostEntity getHost(int containerId, int userId) {
            return getGuestTable().get(Container.getUid(userId, containerId));
        }

        /**
         * Gets the vm table.
         *
         * @return the vm table
         */
        public Map<String, HostEntity> getGuestTable() {
            return guestTable;
        }

    }