/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2024, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.container.core;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.VmAllocationPolicy.GuestMapping;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.cloudbus.cloudsim.core.*;
import org.cloudbus.cloudsim.lists.CloudletList;
import org.cloudbus.cloudsim.lists.VmList;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by sareh on 15/07/15.
 * Modified by Remo Andreoli (Feb 2024)
 */

public class ContainerDatacenterBroker extends DatacenterBroker {
    /**
     * The container list
     */
    protected List<? extends GuestEntity> containerList;
    /**
     * The containers created list.
     */
    protected List<? extends GuestEntity> containersCreatedList;
    /**
     * The containers acks.
     */
    protected int containersAcks;
    /**
     * The number of created containers
     */

    protected int containersCreated;


 /**
     * The vms to datacenters map.
     */
    protected Map<Integer, Integer> containersToDatacentersMap;


    /**
     * The datacenter characteristics list.
     */
    protected double overBookingfactor;

    protected int numberOfCreatedVMs;

    /**
     * Created a new DatacenterBroker object.
     *
     * @param name name to be associated with this entity (as required by Sim_entity class from
     *             simjava package)
     * @throws Exception the exception
     * @pre name != null
     * @post $none
     */
    public ContainerDatacenterBroker(String name, double overBookingfactor) throws Exception {
        super(name);

        setGuestList(new ArrayList<>());
        setContainerList(new ArrayList<>());
        setGuestsCreatedList(new ArrayList<>());
        setContainersCreatedList(new ArrayList<>());
        setCloudletList(new ArrayList<>());
        setCloudletSubmittedList(new ArrayList<>());
        setCloudletReceivedList(new ArrayList<>());
        cloudletsSubmitted = 0;
        setVmsRequested(0);
        setVmsAcks(0);
        setContainersAcks(0);
        setContainersCreated(0);
        setVmsDestroyed(0);
        setOverBookingfactor(overBookingfactor);
        setDatacenterIdsList(new LinkedList<>());
        setDatacenterRequestedIdsList(new ArrayList<>());
        setVmsToDatacentersMap(new HashMap<>());
        setContainersToDatacentersMap(new HashMap<>());
        setDatacenterCharacteristicsList(new HashMap<>());
        setNumberOfCreatedVMs(0);
    }

    /**
     * Specifies that a given cloudlet must run in a specific virtual machine.
     *
     * @param cloudletId ID of the cloudlet being bount to a vm
     * @param containerId       the vm id
     * @pre cloudletId > 0
     * @pre id > 0
     * @post $none
     */
    public void bindCloudletToContainer(int cloudletId, int containerId) {
        Cloudlet containerCloudlet = CloudletList.getById(getCloudletList(), cloudletId);
        containerCloudlet.setContainerId(containerId);
    }

    /**
     * Processes events available for this Broker.
     *
     * @param ev a SimEvent object
     * @pre ev != null
     * @post $none
     */
    @Override
    public void processEvent(SimEvent ev) {
        CloudSimTags tag = ev.getTag();
        // New VM Creation answer (PowerContainerDatacenterCM only)
        if (tag == ContainerCloudSimTags.VM_NEW_CREATE) {
            processNewVmCreate((GuestMapping)ev.getData());
        } else if (tag == ContainerCloudSimTags.CONTAINER_CREATE_ACK) {
            processContainerCreate(ev);
            // VM Creation answer
        } else if (tag == CloudActionTags.VM_CREATE_ACK) {
            processVmCreateAck(ev);
            // other (potentially unknown tags) are processed by the base class
        } else {
            super.processEvent(ev);
        }
    }

    public void processContainerCreate(SimEvent ev) {
        int[] data = (int[]) ev.getData();
        int datacenterId = data[0];
        int containerId = data[1];
        int result = data[2];

        if (result == CloudSimTags.TRUE) {
            GuestEntity guest = VmList.getById(getContainerList(), containerId);
            HostEntity vm = guest.getHost();

            getContainersToVmsMap().put(containerId, vm.getId());
            getContainersCreatedList().add(guest);

            int hostId = VmList.getById(getGuestsCreatedList(), vm.getId()).getHost().getId();
            Log.printlnConcat(CloudSim.clock(), ": ", getName(), ": The Container #", containerId,
                     ", is created on Vm #",vm.getId()
                    , ", On Host#", hostId);
            setContainersCreated(getContainersCreated()+1);
        } else {
            //Container container = VmList.getById(getContainerList(), containerId);
            Log.printlnConcat(CloudSim.clock(), ": ", getName(), ": Failed Creation of Container #", containerId);
        }

        incrementContainersAcks();
        if (getContainersAcks() == getContainerList().size()) {
            //Log.print(getContainersCreatedList().size() + "vs asli"+getContainerList().size());
            submitCloudlets();
            getContainerList().clear();
        }

    }

    protected void processNewVmCreate(GuestMapping map) {
        int datacenterId = map.datacenterId();
        GuestEntity containerVm = (ContainerVm) map.vm();
        int vmId = containerVm.getId();
        GuestEntity guest = VmList.getById(getGuestsCreatedList(), vmId);

        getGuestList().add(containerVm);
        getVmsToDatacentersMap().put(vmId, datacenterId);
        getGuestsCreatedList().add(containerVm);
        Log.printlnConcat(CloudSim.clock(), ": ", getName(), ": ", guest.getClassName(), " #", vmId,
                    " has been created in Datacenter #", datacenterId, ", Host #",
                    guest.getHost().getId());
    }

    /**
     * Process the ack received due to a request for VM creation.
     *
     * @param ev a SimEvent object
     * @pre ev != null
     * @post $none
     */
    protected void processVmCreateAck(SimEvent ev) {
        int[] data = (int[]) ev.getData();
        int datacenterId = data[0];
        int vmId = data[1];
        int result = data[2];

        if (result == CloudSimTags.TRUE) {
            GuestEntity guest = VmList.getById(getGuestList(), vmId);

            getVmsToDatacentersMap().put(vmId, datacenterId);
            getGuestsCreatedList().add(guest);
            Log.printlnConcat(CloudSim.clock(), ": ", getName(), ": ", guest.getClassName(), " #", vmId,
                    " has been created in Datacenter #", datacenterId, ", Host #",
                    guest.getHost().getId());
            setNumberOfCreatedVMs(getNumberOfCreatedVMs()+1);
        } else {
            Log.printlnConcat(CloudSim.clock(), ": ", getName(), ": Creation of VM #", vmId,
                    " failed in Datacenter #", datacenterId);
        }

        incrementVmsAcks();
//        if (getVmsCreatedList().size() == getVmList().size() - getVmsDestroyed()) {
//        If we have tried creating all of the vms in the data center, we submit the containers.
        if(getGuestList().size() == vmsAcks){

            submitContainers();
        }
//        // all the requested VMs have been created
//        if (getVmsCreatedList().size() == getVmList().size() - getVmsDestroyed()) {
//            submitCloudlets();
//        } else {
//            // all the acks received, but some VMs were not created
//            if (getVmsRequested() == getVmsAcks()) {
//                // find id of the next datacenter that has not been tried
//                for (int nextDatacenterId : getDatacenterIdsList()) {
//                    if (!getDatacenterRequestedIdsList().contains(nextDatacenterId)) {
//                        createVmsInDatacenter(nextDatacenterId);
//                        return;
//                    }
//                }
//
//                // all datacenters already queried
//                if (getVmsCreatedList().size() > 0) { // if some vm were created
//                    submitCloudlets();
//                } else { // no vms created. abort
//                    Log.printLine(CloudSim.clock() + ": " + getName()
//                            + ": none of the required VMs could be created. Aborting");
//                    finishExecution();
//                }
//            }
//        }
    }

    /**
     * Submit cloudlets to the created VMs.
     *
     * @pre $none
     * @post $none
     */
    protected void submitCloudlets() {
        int containerIndex = 0;
        List<Cloudlet> successfullySubmitted = new ArrayList<>();
        for (Cloudlet cloudlet : this.<Cloudlet>getCloudletList()) {
            //Log.printLine("Containers Created" + getContainersCreated());
            if (containerIndex < getContainersCreated()) {
                    //Log.printLine("Container Index" + containerIndex);
//                    int containerId = getContainersCreatedList().get(containerIndex).getId();
//                    bindCloudletToContainer(cloudlet.getCloudletId(), containerId);
                if(getContainersToVmsMap().get(cloudlet.getContainerId()) != null) {
                    int vmId = getContainersToVmsMap().get(cloudlet.getContainerId());
//                    bindCloudletToVm(cloudlet.getCloudletId(), guestId);
                    cloudlet.setGuestId(vmId);
//                    if(cloudlet.getGuestId() != guestId){
//                        Log.printConcatLine("The cloudlet Vm Id is ", cloudlet.getGuestId(), "It should be", guestId);
//
//                    }
                    containerIndex++;
                    sendNow(getDatacenterIdsList().get(0), CloudActionTags.CLOUDLET_SUBMIT, cloudlet);
                    cloudletsSubmitted++;
                    getCloudletSubmittedList().add(cloudlet);
                    successfullySubmitted.add(cloudlet);
                }


                //Log.printLine("Container Id" + containerId);

                //Log.printConcatLine("VM ID is: ",cloudlet.getGuestId(), "Container ID:", cloudlet.getContainerId(), "cloudletId:", cloudlet.getCloudletId());

//                cloudlet.setGuestId(v.getId());
                // if user didn't bind this cloudlet and it has not been executed yet
//            if (cloudlet.getContainerId() == -1) {
//                Log.print("User has forgotten to bound the cloudlet to container");
//            } else { // submit to the specific vm
//                vm = VmList.getById(getVmsCreatedList(), cloudlet.getGuestId());
//                if (vm == null) { // vm was not created
//                    if (!Log.isDisabled()) {
//                        Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Postponing execution of cloudlet ",
//                                cloudlet.getCloudletId(), ": bount VM not available");
//                    }
//                    continue;
//                }
//            }
//
            if (!Log.isDisabled()) {
                Log.printlnConcat(CloudSim.clock(), ": ", getName(), ": Sending ", cloudlet.getClass().getSimpleName(),
                        " #", cloudlet.getCloudletId(), " to VM #", cloudlet.getContainerId());
            }


//            containerIndex = (containerIndex + 1) % getVmsCreatedList().size();

//          int vmIndex = 0;
//        List<Cloudlet> successfullySubmitted = new ArrayList<Cloudlet>();
//        for (Cloudlet cloudlet : getCloudletList()) {
//            ContainerVm vm;
//            // if user didn't bind this cloudlet and it has not been executed yet
//            if (cloudlet.getGuestId() == -1) {
//                vm = getVmsCreatedList().get(vmIndex);
//            } else { // submit to the specific vm
//                vm = VmList.getById(getVmsCreatedList(), cloudlet.getGuestId());
//                if (vm == null) { // vm was not created
//                    if (!Log.isDisabled()) {
//                        Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Postponing execution of cloudlet ",
//                                cloudlet.getCloudletId(), ": bount VM not available");
//                    }
//                    continue;
//                }
//            }
//
//            if (!Log.isDisabled()) {cloudlet.getCloudletId()
//                Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Sending cloudlet ",
//                        cloudlet.getCloudletId(), " to VM #", vm.getId());
//            }
//
//            cloudlet.setGuestId(vm.getId());
//            sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudActionTags.CLOUDLET_SUBMIT, cloudlet);
//            cloudletsSubmitted++;
//            vmIndex = (vmIndex + 1) % getVmsCreatedList().size();
//            getCloudletSubmittedList().add(cloudlet);
//            successfullySubmitted.add(cloudlet);
            }
        }

        // remove submitted cloudlets from waiting list
        getCloudletList().removeAll(successfullySubmitted);
        successfullySubmitted.clear();
    }

    /**
     *
     */
    protected void submitContainers(){
        int i = 0;
        for(Container container:getContainerList()) {
            Cloudlet cloudlet = (Cloudlet) getCloudletList().get(i);
                //Log.printLine("Containers Created" + getContainersCreated());

                if (cloudlet.getUtilizationModelCpu() instanceof UtilizationModelPlanetLabInMemory temp) {
                    double[] cloudletUsage = temp.getData();
                    Percentile percentile = new Percentile();
                    double percentileUsage = percentile.evaluate(cloudletUsage, getOverBookingfactor());
                    //Log.printLine("Container Index" + containerIndex);
                    double newmips = percentileUsage * container.getMips();
//                    double newmips = percentileUsage * container.getMips();
//                    double maxUsage = Doubles.max(cloudletUsage);
//                    double newmips = maxUsage * container.getMips();
                    container.setMips(newmips);
                }

                if (cloudlet.getContainerId() != container.getId()) {
                    bindCloudletToContainer(cloudlet.getCloudletId(), container.getId());
                    Log.printlnConcat("Binding Cloudlet: ", cloudlet.getCloudletId(), " to Container: ", container.getId());
                }
            i++;

        }

        List<Container> successfullySubmitted = new ArrayList<>(getContainerList());
        sendNow(getDatacenterIdsList().get(0), ContainerCloudSimTags.CONTAINER_SUBMIT, successfullySubmitted);

//        List<Container> successfullySubmitted = new ArrayList<>();
//        for (Container container : getContainerList()) {
//
//            if (!Log.isDisabled()) {
//                Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Sending container ",
//                        container.getId(), " to Datacenter");
//            }
//            cloudletsSubmitted++;
//            vmIndex = (vmIndex + 1) % getVmsCreatedList().size();
//            getCloudletSubmittedList().add(cloudlet);
//            successfullySubmitted.add(cloudlet);
//        }

        // remove submitted cloudlets from waiting list
    }

    /**
     * Increment vms acks.
     */
    protected void incrementContainersAcks() {
        setContainersAcks(getContainersAcks()+1);
    }

//------------------------------------------------

    public <T extends Container> List<T> getContainerList() {

        return (List<T>) containerList;
    }

    public void setContainerList(List<? extends Container> containerList) {
        this.containerList = containerList;
    }
    /**
     * This method is used to send to the broker the list with virtual machines that must be
     * created.
     *
     * @param list the list
     * @pre list !=null
     * @post $none
     */
    public void submitContainerList(List<? extends Container> list) {
        getContainerList().addAll(list);
    }


    public Map<Integer, Integer> getContainersToVmsMap() {
        return containersToDatacentersMap;
    }

    public void setContainersToDatacentersMap(Map<Integer, Integer> containersToDatacentersMap) {
        this.containersToDatacentersMap = containersToDatacentersMap;
    }

    public <T extends GuestEntity> List<T> getContainersCreatedList() {
        return (List<T>) containersCreatedList;
    }

    public void setContainersCreatedList(List<? extends GuestEntity> containersCreatedList) {
        this.containersCreatedList = containersCreatedList;
    }

    public int getContainersAcks() {
        return containersAcks;
    }

    public void setContainersAcks(int containersAcks) {
        this.containersAcks = containersAcks;
    }

    public int getContainersCreated() {
        return containersCreated;
    }

    public void setContainersCreated(int containersCreated) {
        this.containersCreated = containersCreated;
    }

    public double getOverBookingfactor() {
        return overBookingfactor;
    }

    public void setOverBookingfactor(double overBookingfactor) {
        this.overBookingfactor = overBookingfactor;
    }

    public int getNumberOfCreatedVMs() {
        return numberOfCreatedVMs;
    }

    public void setNumberOfCreatedVMs(int numberOfCreatedVMs) {
        this.numberOfCreatedVMs = numberOfCreatedVMs;
    }
}


