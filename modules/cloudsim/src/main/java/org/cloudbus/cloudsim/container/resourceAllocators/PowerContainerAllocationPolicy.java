package org.cloudbus.cloudsim.container.resourceAllocators;

import org.cloudbus.cloudsim.container.core.Container;
import org.cloudbus.cloudsim.container.core.ContainerVm;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sareh on 16/07/15.
 */
public abstract class PowerContainerAllocationPolicy extends ContainerAllocationPolicy{

        /** The container table. */
        private final Map<String, ContainerVm> containerTable = new HashMap<>();

        /**
         * Instantiates a new power vm allocation policy abstract.
         *
         */
        public PowerContainerAllocationPolicy() {
            super();
        }

        /*
         * (non-Javadoc)
         * @see org.cloudbus.cloudsim.VmAllocationPolicy#allocateHostForVm(org.cloudbus.cloudsim.Vm)
         */
        @Override
        public boolean allocateVmForContainer(Container container, List<ContainerVm> containerVmList) {
            setContainerVmList(containerVmList);
            return allocateVmForContainer(container, findVmForContainer(container));
        }

        /*
         * (non-Javadoc)
         * @see org.cloudbus.cloudsim.VmAllocationPolicy#allocateHostForVm(org.cloudbus.cloudsim.Vm,
         * org.cloudbus.cloudsim.Host)
         */
        @Override
        public boolean allocateVmForContainer(Container container, ContainerVm containerVm) {
            if (containerVm == null) {
                Log.formatLine("%.2f: No suitable VM found for Container#" + container.getId() + "\n", CloudSim.clock());
                return false;
            }
            if (containerVm.containerCreate(container)) { // if vm has been succesfully created in the host
                getContainerTable().put(container.getUid(), containerVm);
//                container.setVm(containerVm);
                Log.formatLine(
                        "%.2f: Container #" + container.getId() + " has been allocated to the VM #" + containerVm.getId(),
                        CloudSim.clock());
                return true;
            }
            Log.formatLine(
                    "%.2f: Creation of Container #" + container.getId() + " on the Vm #" + containerVm.getId() + " failed\n",
                    CloudSim.clock());
            return false;
        }

        /**
         * Find host for vm.
         *
         * @param container the vm
         * @return the power host
         */
        public ContainerVm findVmForContainer(Container container) {
            for (ContainerVm containerVm : getContainerVmList()) {
//                Log.printConcatLine("Trying vm #",containerVm.getId(),"For container #", container.getId());
                if (containerVm.isSuitableForContainer(container)) {
                    return containerVm;
                }
            }
            return null;
        }

        /*
         * (non-Javadoc)
         * @see org.cloudbus.cloudsim.VmAllocationPolicy#deallocateHostForVm(org.cloudbus.cloudsim.Vm)
         */
        @Override
        public void deallocateVmForContainer(Container container) {
            ContainerVm containerVm = getContainerTable().remove(container.getUid());
            if (containerVm != null) {
                containerVm.containerDestroy(container);
            }
        }

        /*
         * (non-Javadoc)
         * @see org.cloudbus.cloudsim.VmAllocationPolicy#getHost(org.cloudbus.cloudsim.Vm)
         */
        @Override
        public ContainerVm getContainerVm(Container container) {
            return getContainerTable().get(container.getUid());
        }

        /*
         * (non-Javadoc)
         * @see org.cloudbus.cloudsim.VmAllocationPolicy#getHost(int, int)
         */
        @Override
        public ContainerVm getContainerVm(int containerId, int userId) {
            return getContainerTable().get(Container.getUid(userId, containerId));
        }

        /**
         * Gets the vm table.
         *
         * @return the vm table
         */
        public Map<String, ContainerVm> getContainerTable() {
            return containerTable;
        }

    }



