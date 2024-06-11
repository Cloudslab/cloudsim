package org.cloudbus.cloudsim.container.core;

import org.cloudbus.cloudsim.core.CloudSimTags;

public enum ContainerCloudSimTags implements CloudSimTags {
    /**
     * Denotes the receiving of a cloudlet  in the data center broker
     * entity.
     */
    FIND_VM_FOR_CLOUDLET,

    /**
     * Denotes the creating a new VM is required in the data center.
     * Invoked in the data center broker.
     */
    CREATE_NEW_VM,

    /**
     * Denotes the containers are submitted to the data center.
     * Invoked in the data center broker.
     */
    CONTAINER_SUBMIT,

    /**
     * Denotes the containers are created in the data center.
     * Invoked in the data center.
     */
    CONTAINER_CREATE_ACK,

    /**
     * Denotes the containers are migrated to another Vm.
     * Invoked in the data center.
     */
    CONTAINER_MIGRATE,

    /**
     * Denotes a new VM is created in data center by the local scheduler
     * Invoked in the data center.
     */
    VM_NEW_CREATE,
}
