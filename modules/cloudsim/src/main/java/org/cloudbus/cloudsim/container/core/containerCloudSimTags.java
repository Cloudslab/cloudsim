package org.cloudbus.cloudsim.container.core;

public class containerCloudSimTags {
    /**
     * Starting constant value for network-related tags
     **/
    private static final int ContainerSimBASE = 400;
    /**
     * Denotes the receiving of a cloudlet  in the data center broker
     * entity.
     */
    public static final int FIND_VM_FOR_CLOUDLET = ContainerSimBASE + 1;

    /**
     * Denotes the creating a new VM is required in the data center.
     * Invoked in the data center broker.
     */
    public static final int CREATE_NEW_VM = ContainerSimBASE + 2;
    /**
     * Denotes the containers are submitted to the data center.
     * Invoked in the data center broker.
     */
    public static final int CONTAINER_SUBMIT = ContainerSimBASE + 3;

    /**
     * Denotes the containers are created in the data center.
     * Invoked in the data center.
     */
    public static final int CONTAINER_CREATE_ACK = ContainerSimBASE + 4;
    /**
     * Denotes the containers are migrated to another Vm.
     * Invoked in the data center.
     */
    public static final int CONTAINER_MIGRATE = ContainerSimBASE + 10;
    /**
     * Denotes a new VM is created in data center by the local scheduler
     * Invoked in the data center.
     */
    public static final int VM_NEW_CREATE = ContainerSimBASE + 11;


    private containerCloudSimTags() {
        // TODO Auto-generated constructor stub
        /** Private Constructor */
        throw new UnsupportedOperationException("ContainerCloudSim Tags cannot be instantiated");

    }
}
