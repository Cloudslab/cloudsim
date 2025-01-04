/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2024, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.container.core;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sareh on 10/07/15.
 * Modified by Remo Andreoli (Feb 2024)
 */
public class ContainerDatacenter extends Datacenter {
    /**
     * The container provisioner.
     */
    private VmAllocationPolicy containerAllocationPolicy;

    /**
     * The container list.
     */
    private List<? extends Container> containerList;

    /**
     * The scheduling interval.
     */
    private String experimentName;
    /**
     * The log address.
     */
    private String logAddress;


    /**
     * Allocates a new PowerDatacenter object.
     * @param name
     * @param characteristics
     * @param vmAllocationPolicy
     * @param containerAllocationPolicy
     * @param storageList
     * @param schedulingInterval
     * @param experimentName
     * @param logAddress
     * @throws Exception
     */
    public ContainerDatacenter(
            String name,
            DatacenterCharacteristics characteristics,
            VmAllocationPolicy vmAllocationPolicy,
            VmAllocationPolicy containerAllocationPolicy,
            List<Storage> storageList,
            double schedulingInterval, String experimentName, String logAddress) throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);

        setContainerAllocationPolicy(containerAllocationPolicy);
        setContainerList(new ArrayList<>());
        setExperimentName(experimentName);
        setLogAddress(logAddress);
    }

    /**
     * Overrides this method when making a new and different type of resource. <br>
     * <b>NOTE:</b> You do not need to override {} method, if you use this method.
     *
     * @pre $none
     * @post $none
     */
    protected void registerOtherEntity() {
        // empty. This should be override by a child class
    }

    /**
     * Processes events or services that are available for this PowerDatacenter.
     *
     * @param ev a Sim_event object
     * @pre ev != null
     * @post $none
     */
    @Override
    public void processEvent(SimEvent ev) {
        CloudSimTags tag = ev.getTag();
        if (tag == ContainerCloudSimTags.CONTAINER_SUBMIT) {
            processContainerSubmit(ev, true);
        } else if (tag == ContainerCloudSimTags.CONTAINER_MIGRATE) {
            processContainerMigrate(ev, false);

            // other (potentially unknown tags) are processed by the base class
        } else {
            super.processEvent(ev);
        }
    }

    public void processContainerSubmit(SimEvent ev, boolean ack) {
        List<Container> containerList = (List<Container>) ev.getData();

        for (Container container : containerList) {
            boolean result = getContainerAllocationPolicy().allocateHostForGuest(container);
            if (ack) {
                int[] data = new int[3];
                data[0] = getId();
                data[1] = container.getId();
                if (result) {
                    data[2] = CloudSimTags.TRUE;
                } else {
                    data[2] = CloudSimTags.FALSE;
                }

                send(ev.getSourceId(), CloudSim.getMinTimeBetweenEvents(), ContainerCloudSimTags.CONTAINER_CREATE_ACK, data);
            }
            if (result) {
                getContainerList().add(container);

                if (container.isBeingInstantiated()) {
                    container.setBeingInstantiated(false);
                }

                container.updateCloudletsProcessing(CloudSim.clock(), getContainerAllocationPolicy().getHost(container).getGuestScheduler().getAllocatedMipsForGuest(container));
            } else {
                Log.println(String.format("Datacenter.containerAllocator: Couldn't find a vm to host the container #%s", container.getUid()));
            }
        }
    }

    /**
     * Process the event for an User/Broker who wants to know the status of a Cloudlet. This
     * PowerDatacenter will then send the status back to the User/Broker.
     *
     * @param ev a Sim_event object
     * @pre ev != null
     * @post $none
     */
    protected void processCloudletStatus(SimEvent ev) {
        int cloudletId = 0;
        int userId = 0;
        int vmId = 0;
        int containerId = 0;
        Cloudlet.CloudletStatus status;
        HostEntity containerVm;

        try {
            // if a sender using cloudletXXX() methods
            int[] data = (int[]) ev.getData();
            cloudletId = data[0];
            userId = data[1];
            vmId = data[2];
            containerId = data[3];
            //Log.printLine("Data Center is processing the cloudletStatus Event ");

            containerVm = (HostEntity) getVmAllocationPolicy().getHost(vmId, userId).getGuest(vmId, userId);
            status = containerVm.getGuest(containerId, userId).getCloudletScheduler().getCloudletStatus(cloudletId);
        }

        // if a sender using normal send() methods
        catch (ClassCastException c) {
            try {
                Cloudlet cl = (Cloudlet) ev.getData();
                cloudletId = cl.getCloudletId();
                userId = cl.getUserId();
                containerId = cl.getContainerId();

                containerVm = (HostEntity) getVmAllocationPolicy().getHost(vmId, userId).getGuest(vmId, userId);
                status = containerVm.getGuest(containerId, userId).getCloudletScheduler().getCloudletStatus(cloudletId);
            } catch (Exception e) {
                Log.printlnConcat(getName(), ": Error in processing CloudActionTags.CLOUDLET_STATUS");
                Log.println(e.getMessage());
                return;
            }
        } catch (Exception e) {
            Log.printlnConcat(getName(), ": Error in processing CloudActionTags.CLOUDLET_STATUS");
            Log.println(e.getMessage());
            return;
        }

        int[] array = new int[3];
        array[0] = getId();
        array[1] = cloudletId;
        array[2] = status.ordinal();

        sendNow(userId, CloudActionTags.CLOUDLET_STATUS, array);
    }

    /**
     * Process the event for a User/Broker who wants to migrate a VM. This PowerDatacenter will
     * then send the status back to the User/Broker.
     *
     * @param ev a Sim_event object
     * @pre ev != null
     * @post $none
     */
    protected void processContainerMigrate(SimEvent ev, boolean ack) {
        Object tmp = ev.getData();
        if (!(tmp instanceof VmAllocationPolicy.GuestMapping migrate)) {
            throw new ClassCastException("The data object must be GuestMapping");
        }

        Container container = migrate.container();
        HostEntity containerVm = (HostEntity) migrate.vm();

        getContainerAllocationPolicy().deallocateHostForGuest(container);
        if(containerVm.getGuestsMigratingIn().contains(container)){
            containerVm.removeMigratingInGuest(container);}
        boolean result = getContainerAllocationPolicy().allocateHostForGuest(container, containerVm);
        if (!result) {
            Log.println("[Datacenter.processContainerMigrate]Container allocation to the destination vm failed");
            System.exit(0);
        }
        if (containerVm.isInWaiting()){
            containerVm.setInWaiting(false);

        }

        if (ack) {
            int[] data = new int[3];
            data[0] = getId();
            data[1] = container.getId();

            if (result) {
                data[2] = CloudSimTags.TRUE;
            } else {
                data[2] = CloudSimTags.FALSE;
            }
            sendNow(ev.getSourceId(), ContainerCloudSimTags.CONTAINER_CREATE_ACK, data);
        }

        Log.formatLine(
                "%.2f: Migration of container #%d to Vm #%d is completed",
                CloudSim.clock(),
                container.getId(),
                container.getHost().getId());
        container.setInMigration(false);
    }

    /**
     * Processes a Cloudlet based on the event type.
     *
     * @param ev  a Sim_event object
     * @param tag event type
     * @pre ev != null
     * @pre type > 0
     * @post $none
     */
    protected void processCloudlet(SimEvent ev, CloudActionTags tag) {
        int cloudletId = 0;
        int userId = 0;
        int vmId = 0;
        int containerId = 0;

        try { // if the sender using cloudletXXX() methods
            int[] data = (int[]) ev.getData();
            cloudletId = data[0];
            userId = data[1];
            vmId = data[2];
            containerId = data[3];
        }

        // if the sender using normal send() methods
        catch (ClassCastException c) {
            try {
                Cloudlet cl = (Cloudlet) ev.getData();
                cloudletId = cl.getCloudletId();
                userId = cl.getUserId();
                vmId = cl.getGuestId();
                containerId = cl.getContainerId();
            } catch (Exception e) {
                Log.printlnConcat(super.getName(), ": Error in processing Cloudlet");
                Log.println(e.getMessage());
                return;
            }
        } catch (Exception e) {
            Log.printlnConcat(super.getName(), ": Error in processing a Cloudlet.");
            Log.println(e.getMessage());
            return;
        }

        // begins executing ....
        switch (tag) {
            case CLOUDLET_CANCEL -> processCloudletCancel(cloudletId, userId, vmId, containerId);
            case CLOUDLET_PAUSE -> processCloudletPause(cloudletId, userId, vmId, containerId, false);
            case CLOUDLET_PAUSE_ACK -> processCloudletPause(cloudletId, userId, vmId, containerId, true);
            case CLOUDLET_RESUME -> processCloudletResume(cloudletId, userId, vmId, containerId, false);
            case CLOUDLET_RESUME_ACK -> processCloudletResume(cloudletId, userId, vmId, containerId, true);
            default -> {
            }
        }

    }

    /**
     * Process the event for a User/Broker who wants to move a Cloudlet.
     *
     * @param receivedData information about the migration
     * @param tag          event tag
     * @pre receivedData != null
     * @pre type > 0
     * @post $none
     */
    protected void processCloudletMove(int[] receivedData, CloudActionTags tag) {
        updateCloudletProcessing();

        int[] array = receivedData;
        int cloudletId = array[0];
        int userId = array[1];
        int vmId = array[2];
        int containerId = array[3];
        int vmDestId = array[4];
        int containerDestId = array[5];
        int destId = array[6];
        HostEntity containerVm;

        // get the cloudlet
        containerVm = (HostEntity) getVmAllocationPolicy().getHost(vmId, userId).getGuest(vmId, userId);
        Cloudlet cl = containerVm.getGuest(containerId, userId)
                                 .getCloudletScheduler().cloudletCancel(cloudletId);

        boolean failed = false;
        if (cl == null) {// cloudlet doesn't exist
            failed = true;
        } else {
            // has the cloudlet already finished?
            if (cl.getStatus() == Cloudlet.CloudletStatus.SUCCESS) {// if yes, send it back to user
                int[] data = new int[3];
                data[0] = getId();
                data[1] = cloudletId;
                data[2] = 0;
                sendNow(cl.getUserId(), CloudActionTags.CLOUDLET_SUBMIT_ACK, data);
                sendNow(cl.getUserId(), CloudActionTags.CLOUDLET_RETURN, cl);
            }

            // prepare cloudlet for migration
            cl.setGuestId(vmDestId);

            // the cloudlet will migrate from one vm to another does the destination VM exist?
            if (destId == getId()) {
                containerVm = (HostEntity) getVmAllocationPolicy().getHost(vmDestId, userId).getGuest(vmDestId, userId);
                if (containerVm == null) {
                    failed = true;
                } else {
                    // time to transfer the files
                    double fileTransferTime = predictFileTransferTime(cl.getRequiredFiles());
                    containerVm.getGuest(containerDestId, userId).getCloudletScheduler().cloudletSubmit(cl, fileTransferTime);
                }
            } else {// the cloudlet will migrate from one resource to another
                CloudActionTags newTag = ((tag == CloudActionTags.CLOUDLET_MOVE_ACK) ? CloudActionTags.CLOUDLET_SUBMIT_ACK
                        : CloudActionTags.CLOUDLET_SUBMIT);
                sendNow(destId, newTag, cl);
            }
        }

        if (tag == CloudActionTags.CLOUDLET_MOVE_ACK) {// send ACK if requested
            int[] data = new int[3];
            data[0] = getId();
            data[1] = cloudletId;
            if (failed) {
                data[2] = 0;
            } else {
                data[2] = 1;
            }
            sendNow(cl.getUserId(), CloudActionTags.CLOUDLET_SUBMIT_ACK, data);
        }
    }

    /**
     * Processes a Cloudlet submission.
     *
     * @param ev  a SimEvent object
     * @param ack an acknowledgement
     * @pre ev != null
     * @post $none
     */
    protected void processCloudletSubmit(SimEvent ev, boolean ack) {
        updateCloudletProcessing();

        try {
            Cloudlet cl = (Cloudlet) ev.getData();

            // checks whether this Cloudlet has finished or not
            if (cl.isFinished()) {
                String name = CloudSim.getEntityName(cl.getUserId());
                Log.printlnConcat(getName(), ": Warning - Cloudlet #", cl.getCloudletId(), " owned by ", name,
                        " is already completed/finished.");
                Log.println("Therefore, it is not being executed again");
                Log.println();

                // NOTE: If a Cloudlet has finished, then it won't be processed.
                // So, if ack is required, this method sends back a result.
                // If ack is not required, this method don't send back a result.
                // Hence, this might cause CloudSim to be hanged since waiting
                // for this Cloudlet back.
                if (ack) {
                    int[] data = new int[3];
                    data[0] = getId();
                    data[1] = cl.getCloudletId();
                    data[2] = CloudSimTags.FALSE;

                    sendNow(cl.getUserId(), CloudActionTags.CLOUDLET_SUBMIT_ACK, data);
                }

                sendNow(cl.getUserId(), CloudActionTags.CLOUDLET_RETURN, cl);

                return;
            }

            // process this Cloudlet to this CloudResource
            cl.setResourceParameter(getId(), getCharacteristics().getCostPerSecond(), getCharacteristics()
                    .getCostPerBw());

            int userId = cl.getUserId();
            int vmId = cl.getGuestId();
            int containerId = cl.getContainerId();

            // time to transfer the files
            double fileTransferTime = predictFileTransferTime(cl.getRequiredFiles());

            HostEntity host = getVmAllocationPolicy().getHost(vmId, userId);
            VirtualEntity vm = (VirtualEntity) host.getGuest(vmId, userId);
            Container container = (Container) vm.getGuest(containerId, userId);
            double estimatedFinishTime = container.getCloudletScheduler().cloudletSubmit(cl, fileTransferTime);

            // if this cloudlet is in the exec queue
            if (estimatedFinishTime > 0.0 && !Double.isInfinite(estimatedFinishTime)) {
                estimatedFinishTime += fileTransferTime;
                send(getId(), estimatedFinishTime, CloudActionTags.VM_DATACENTER_EVENT);
            }

            if (ack) {
                int[] data = new int[3];
                data[0] = getId();
                data[1] = cl.getCloudletId();
                data[2] = CloudSimTags.TRUE;

                sendNow(cl.getUserId(), CloudActionTags.CLOUDLET_SUBMIT_ACK, data);
            }
        } catch (ClassCastException c) {
            Log.println(String.format("%s.processCloudletSubmit(): ClassCastException error.", getName()));
            c.printStackTrace();
        } catch (Exception e) {
            Log.println(String.format("%s.processCloudletSubmit(): Exception error.", getName()));
            e.printStackTrace();
        }

        checkCloudletCompletion();
    }

    /**
     * Processes a Cloudlet resume request.
     *
     * @param cloudletId resuming cloudlet ID
     * @param userId     ID of the cloudlet's owner
     * @param ack        $true if an ack is requested after operation
     * @param vmId       the vm id
     * @pre $none
     * @post $none
     */
    protected void processCloudletResume(int cloudletId, int userId, int vmId, int containerId, boolean ack) {
        double eventTime = ((VirtualEntity) getVmAllocationPolicy().getHost(vmId, userId)
                            .getGuest(vmId, userId))
                            .getGuest(containerId, userId)
                            .getCloudletScheduler().cloudletResume(cloudletId);

        boolean status = false;
        if (eventTime > 0.0) { // if this cloudlet is in the exec queue
            status = true;
            if (eventTime > CloudSim.clock()) {
                schedule(getId(), eventTime, CloudActionTags.VM_DATACENTER_EVENT);
            }
        }

        if (ack) {
            int[] data = new int[3];
            data[0] = getId();
            data[1] = cloudletId;
            if (status) {
                data[2] = CloudSimTags.TRUE;
            } else {
                data[2] = CloudSimTags.FALSE;
            }
            sendNow(userId, CloudActionTags.CLOUDLET_RESUME_ACK, data);
        }
    }

    /**
     * Processes a Cloudlet pause request.
     *
     * @param cloudletId resuming cloudlet ID
     * @param userId     ID of the cloudlet's owner
     * @param ack        $true if an ack is requested after operation
     * @param vmId       the vm id
     * @pre $none
     * @post $none
     */
    protected void processCloudletPause(int cloudletId, int userId, int vmId, int containerId, boolean ack) {
        VirtualEntity containerVm = (VirtualEntity) getVmAllocationPolicy().getHost(vmId, userId).getGuest(vmId, userId);
        boolean status = containerVm.getGuest(containerId, userId)
                .getCloudletScheduler().cloudletPause(cloudletId);

        if (ack) {
            int[] data = new int[3];
            data[0] = getId();
            data[1] = cloudletId;
            if (status) {
                data[2] = CloudSimTags.TRUE;
            } else {
                data[2] = CloudSimTags.FALSE;
            }
            sendNow(userId, CloudActionTags.CLOUDLET_PAUSE_ACK, data);
        }
    }

    /**
     * Processes a Cloudlet cancel request.
     *
     * @param cloudletId resuming cloudlet ID
     * @param userId     ID of the cloudlet's owner
     * @param vmId       the vm id
     * @pre $none
     * @post $none
     */
    protected void processCloudletCancel(int cloudletId, int userId, int vmId, int containerId) {
        HostEntity containerVm = (HostEntity) getVmAllocationPolicy().getHost(vmId, userId).getGuest(vmId, userId);
        Cloudlet cl = containerVm.getGuest(containerId, userId)
                .getCloudletScheduler().cloudletCancel(cloudletId);
        sendNow(userId, CloudActionTags.CLOUDLET_CANCEL, cl);
    }

    /**
     * Verifies if some cloudlet inside this PowerDatacenter already finished. If yes, send it to
     * the User/Broker
     *
     * @pre $none
     * @post $none
     *
     * @TODO: Generalise this to work with every hybrid host-guest entity
     */
    protected void checkCloudletCompletion() {
        for (HostEntity host : getVmAllocationPolicy().getHostList()) {
            for (VirtualEntity vm : host.<VirtualEntity>getGuestList()) {
                for (GuestEntity container : vm.getGuestList()) {
                    while (container.getCloudletScheduler().isFinishedCloudlets()) {
                        Cloudlet cl = container.getCloudletScheduler().getNextFinishedCloudlet();
                        if (cl != null) {
                            sendNow(cl.getUserId(), CloudActionTags.CLOUDLET_RETURN, cl);
                        }
                    }
                }
            }
        }
    }

    public VmAllocationPolicy getContainerAllocationPolicy() { return containerAllocationPolicy; }
    public void setContainerAllocationPolicy(VmAllocationPolicy containerAllocationPolicy) {
        this.containerAllocationPolicy = containerAllocationPolicy;
    }

    public <T extends Container> List<T> getContainerList() {
        return (List<T>) containerList;
    }
    public void setContainerList(List<? extends Container> containerList) {
        this.containerList = containerList;
    }


    public String getExperimentName() {
        return experimentName;
    }
    public void setExperimentName(String experimentName) {
        this.experimentName = experimentName;
    }

    public String getLogAddress() {
        return logAddress;
    }

    public void setLogAddress(String logAddress) {
        this.logAddress = logAddress;
    }
}


