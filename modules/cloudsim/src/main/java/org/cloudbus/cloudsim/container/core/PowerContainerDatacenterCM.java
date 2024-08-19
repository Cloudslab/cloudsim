/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2024, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.container.core;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.VmAllocationPolicy.GuestMapping;
import org.cloudbus.cloudsim.container.utils.CustomCSVWriter;
import org.cloudbus.cloudsim.core.*;
import org.cloudbus.cloudsim.core.predicates.PredicateType;
import org.cloudbus.cloudsim.power.PowerHost;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sareh on 3/08/15.
 * Modified by Remo Andreoli (Feb 2024)
 */
public class PowerContainerDatacenterCM extends PowerContainerDatacenter {
    /**
     * The disable container migrations.
     */
    private boolean disableMigrations;
    public int containerMigrationCount;
    private CustomCSVWriter newlyCreatedVmWriter;
    private int newlyCreatedVms;
    private List<Integer> newlyCreatedVmsList;
    private final double vmStartupDelay;
    private final double containerStartupDelay;


    public PowerContainerDatacenterCM(String name, DatacenterCharacteristics characteristics,
                                      VmAllocationPolicy vmAllocationPolicy,
                                      VmAllocationPolicy containerAllocationPolicy, List<Storage> storageList,
                                      double schedulingInterval, String experimentName, String logAddress,
                                      double vmStartupDelay, double containerStartupDelay) throws Exception {
        super(name, characteristics, vmAllocationPolicy, containerAllocationPolicy, storageList, schedulingInterval, experimentName, logAddress);
        String newlyCreatedVmsAddress;
        int index = getExperimentName().lastIndexOf("_");
        newlyCreatedVmsAddress = String.format("%s/NewlyCreatedVms/%s/%s.csv", getLogAddress(), getExperimentName().substring(0, index), getExperimentName());
        setNewlyCreatedVmWriter(new CustomCSVWriter(newlyCreatedVmsAddress));
        setNewlyCreatedVms(0);
        setDisableMigrations(false);
        setNewlyCreatedVmsList(new ArrayList<>());
        this.vmStartupDelay = vmStartupDelay;
        this.containerStartupDelay = containerStartupDelay;
    }

    @Override
    protected void updateCloudletProcessing() {
        //        Log.printLine("Power data center is Updating the cloudlet processing");
        if (getCloudletSubmitted() == -1 || getCloudletSubmitted() == CloudSim.clock()) {
            CloudSim.cancelAll(getId(), new PredicateType(CloudActionTags.VM_DATACENTER_EVENT));
            schedule(getId(), getSchedulingInterval(), CloudActionTags.VM_DATACENTER_EVENT);
            return;
        }
        double currentTime = CloudSim.clock();

        // if some time passed since last processing
        if (currentTime > getLastProcessTime()) {
            Log.print(currentTime + " ");

            double minTime = updateCloudetProcessingWithoutSchedulingFutureEventsForce();

            if (!isDisableMigrations()) {
                List<GuestMapping> migrationMap = getVmAllocationPolicy().optimizeAllocation(
                        getVmList());
                int previousContainerMigrationCount = getContainerMigrationCount();
                int previousVmMigrationCount = getVmMigrationCount();
                if (migrationMap != null) {
                    List<HostEntity> vmList = new ArrayList<>();
                    for (GuestMapping migrate : migrationMap) {
                        if (migrate.container() != null) {
                            Container container = migrate.container();
                            HostEntity targetVm = (HostEntity) migrate.vm();
                            HostEntity oldVm = container.getHost();
                            if (oldVm == null) {
                                Log.formatLine(
                                        "%.2f: Migration of Container #%d to Vm #%d is started",
                                        currentTime,
                                        container.getId(),
                                        targetVm.getId());
                            } else {
                                Log.formatLine(
                                        "%.2f: Migration of Container #%d from Vm #%d to VM #%d is started",
                                        currentTime,
                                        container.getId(),
                                        oldVm.getId(),
                                        targetVm.getId());
                            }
                            incrementContainerMigrationCount();
                            targetVm.addMigratingInGuest(container);


                            if (migrate.NewEventRequired()) {
                                if (!vmList.contains(targetVm)) {
                                    // A new VM is created  send a vm create request with delay :)
//                                Send a request to create Vm after 100 second
//                                            create a new event for this. or overright the vm create
                                    Log.formatLine(
                                            "%.2f: Migration of Container #%d to newly created Vm #%d is started",
                                            currentTime,
                                            container.getId(),
                                            targetVm.getId());
                                    targetVm.guestDestroyAll();
                                    send(
                                            getId(),
                                            vmStartupDelay,
                                            CloudActionTags.VM_CREATE,
                                            migrate);
                                    vmList.add(targetVm);

                                    send(
                                            getId(),
                                            containerStartupDelay + vmStartupDelay
                                            , ContainerCloudSimTags.CONTAINER_MIGRATE,
                                            migrate);

                                } else {
                                    Log.formatLine(
                                            "%.2f: Migration of Container #%d to newly created Vm #%d is started",
                                            currentTime,
                                            container.getId(),
                                            targetVm.getId());
//                                    send a request for container migration after the vm is created
//                                    it would be 100.4
                                    send(
                                            getId(),
                                            containerStartupDelay + vmStartupDelay
                                            , ContainerCloudSimTags.CONTAINER_MIGRATE,
                                            migrate);


                                }


                            } else {
                                send(
                                        getId(),
                                        containerStartupDelay,
                                        ContainerCloudSimTags.CONTAINER_MIGRATE,
                                        migrate);


                            }
                        } else {
                            GuestEntity vm = migrate.vm();
                            PowerHost targetHost = (PowerHost) migrate.host();
                            PowerHost oldHost = vm.getHost();

                            if (oldHost == null) {
                                Log.formatLine(
                                        "%.2f: Migration of VM #%d to Host #%d is started",
                                        currentTime,
                                        vm.getId(),
                                        targetHost.getId());
                            } else {
                                Log.formatLine(
                                        "%.2f: Migration of VM #%d from Host #%d to Host #%d is started",
                                        currentTime,
                                        vm.getId(),
                                        oldHost.getId(),
                                        targetHost.getId());
                            }

                            targetHost.addMigratingInGuest(vm);
                            incrementMigrationCount();

                            /** VM migration delay = RAM / bandwidth **/
                            // we use BW / 2 to model BW available for migration purposes, the other
                            // half of BW is for VM communication
                            // around 16 seconds for 1024 MB using 1 Gbit/s network
                            send(
                                    getId(),
                                    vm.getRam() / ((double) targetHost.getBw() / (2 * 8000)),
                                    CloudActionTags.VM_MIGRATE,
                                    migrate);
                        }


                    }


                    migrationMap.clear();
                    vmList.clear();
                }
                getContainerMigrationList().add((double) (getContainerMigrationCount() - previousContainerMigrationCount));

                Log.printlnConcat(CloudSim.clock(), ": The Number Container of Migrations is:  ", getContainerMigrationCount() - previousContainerMigrationCount);
                Log.printlnConcat(CloudSim.clock(), ": The Number of VM Migrations is:  ", getVmMigrationCount() - previousVmMigrationCount);
                String[] vmMig = {Double.toString(CloudSim.clock()), Integer.toString(getVmMigrationCount() - previousVmMigrationCount)};                   // <--declared statement
                String[] msg = {Double.toString(CloudSim.clock()), Integer.toString(getContainerMigrationCount() - previousContainerMigrationCount)};                   // <--declared statement
                try {
                    getContainerMigrationWriter().writeTofile(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    getVmMigrationWriter().writeTofile(vmMig);
                } catch (IOException e) {
                    e.printStackTrace();
                }


                int numberOfNewVms = getNewlyCreatedVms();
                getNewlyCreatedVmsList().add(numberOfNewVms);
                String[] msg1 = {Double.toString(CloudSim.clock()), Integer.toString(numberOfNewVms)};                   // <--declared statement
                try {
                    getNewlyCreatedVmWriter().writeTofile(msg1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            // schedules an event to the next time
            if (minTime != Double.MAX_VALUE) {
                CloudSim.cancelAll(getId(), new PredicateType(CloudActionTags.VM_DATACENTER_EVENT));
                send(getId(), getSchedulingInterval(), CloudActionTags.VM_DATACENTER_EVENT);
            }

            setLastProcessTime(currentTime);

        }

    }

    @Override
    protected void processVmCreate(SimEvent ev, boolean ack) {

//    here we override the method
        if (ev.getData() instanceof GuestMapping map) {
            VirtualEntity containerVm = (Vm) map.vm();
            HostEntity host = map.host();
            boolean result = getVmAllocationPolicy().allocateHostForGuest(containerVm, host);
//                set the containerVm in waiting state
            containerVm.setInWaiting(true);
//                containerVm.addMigratingInContainer((Container) map.container());

            if (result) {
                GuestMapping data = new GuestMapping(containerVm, null, null, getId(), false, false);
                send(2, CloudSim.getMinTimeBetweenEvents(), ContainerCloudSimTags.VM_NEW_CREATE, data);
                Log.println(String.format("%s VM ID #%d is created on Host #%d", CloudSim.clock(), containerVm.getId(), host.getId()));
                incrementNewlyCreatedVmsCount();
                getVmList().add(containerVm);


                if (containerVm.isBeingInstantiated()) {
                    containerVm.setBeingInstantiated(false);
                }

                containerVm.updateCloudletsProcessing(CloudSim.clock(), getVmAllocationPolicy().getHost(containerVm).getGuestScheduler()
                        .getAllocatedMipsForGuest(containerVm));
            }

        } else {
            super.processVmCreate(ev, ack);
        }

    }

    /**
     * Increment migration count.
     */
    protected void incrementContainerMigrationCount() {
        setContainerMigrationCount(getContainerMigrationCount() + 1);
    }

    /**
     * Increment migration count.
     */
    protected void incrementNewlyCreatedVmsCount() {
        setNewlyCreatedVms(getNewlyCreatedVms() + 1);
    }

    /**
     * Checks if is disable migrations.
     *
     * @return true, if is disable migrations
     */
    public boolean isDisableMigrations() {
        return disableMigrations;
    }

    /**
     * Sets the disable migrations.
     *
     * @param disableMigrations the new disable migrations
     */
    public void setDisableMigrations(boolean disableMigrations) {
        this.disableMigrations = disableMigrations;
    }


    public int getContainerMigrationCount() {
        return containerMigrationCount;
    }

    public void setContainerMigrationCount(int containerMigrationCount) {
        this.containerMigrationCount = containerMigrationCount;
    }

    public CustomCSVWriter getNewlyCreatedVmWriter() {
        return newlyCreatedVmWriter;
    }

    public void setNewlyCreatedVmWriter(CustomCSVWriter newlyCreatedVmWriter) {
        this.newlyCreatedVmWriter = newlyCreatedVmWriter;
    }

    public int getNewlyCreatedVms() {
        return newlyCreatedVms;
    }

    public void setNewlyCreatedVms(int newlyCreatedVms) {
        this.newlyCreatedVms = newlyCreatedVms;
    }

    public List<Integer> getNewlyCreatedVmsList() {
        return newlyCreatedVmsList;
    }

    public void setNewlyCreatedVmsList(List<Integer> newlyCreatedVmsList) {
        this.newlyCreatedVmsList = newlyCreatedVmsList;
    }


}