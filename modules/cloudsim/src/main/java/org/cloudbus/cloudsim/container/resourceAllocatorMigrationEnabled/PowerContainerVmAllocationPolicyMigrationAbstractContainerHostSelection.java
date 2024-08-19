/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2024, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.container.resourceAllocatorMigrationEnabled;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.container.core.*;
import org.cloudbus.cloudsim.core.PowerGuestEntity;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.selectionPolicies.SelectionPolicy;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.core.HostEntity;
import org.cloudbus.cloudsim.power.PowerHost;

import java.util.*;

/**
 * Created by sareh on 11/08/15.
 * Modified by Remo Andreoli (Feb 2024)
 */
public abstract class PowerContainerVmAllocationPolicyMigrationAbstractContainerHostSelection extends PowerContainerVmAllocationPolicyMigrationAbstractContainerAdded {

    private SelectionPolicy<HostEntity> hostSelectionPolicy;

    public PowerContainerVmAllocationPolicyMigrationAbstractContainerHostSelection(List<? extends HostEntity> hostList, SelectionPolicy<GuestEntity> vmSelectionPolicy,
                                                                                   SelectionPolicy<PowerGuestEntity> containerSelectionPolicy, SelectionPolicy<HostEntity> hostSelectionPolicy,
                                                                                   int numberOfVmTypes, int[] vmPes, int[] vmRam, long vmBw, long vmSize, double[] vmMips) {
        super(hostList, vmSelectionPolicy, containerSelectionPolicy, numberOfVmTypes, vmPes, vmRam, vmBw, vmSize, vmMips);
        setHostSelectionPolicy(hostSelectionPolicy);
    }

    @Override
    public GuestMapping findHostForGuest(GuestEntity container, Set<? extends HostEntity> excludedHosts, boolean checkForVM) {
        PowerHost allocatedHost = null;
        ContainerVm allocatedVm = null;
        if (excludedHosts.size() == getHostList().size()){
            return null;
        }
        Set<HostEntity> excludedHost1 = new HashSet<>(excludedHosts);
        while (true) {
            if(getHostList().isEmpty()){
                return null;
            }
            HostEntity host = getHostSelectionPolicy().select(getHostList(), container, excludedHost1);
            boolean findVm = false;
            List<ContainerVm> vmList = host.getGuestList();
            VmList.sortByCpuUtilization(vmList);
            for (int i = 0; i < vmList.size(); i++) {
                ContainerVm vm = vmList.get(vmList.size() - 1 - i);
                if(checkForVM){
                    if(vm.isInWaiting()){
                        continue;
                    }
                }
                if (vm.isSuitableForGuest(container)) {

                    // if vm is overutilized or host would be overutilized after the allocation, this host is not chosen!
                    if (!isVmOverUtilized(vm)) {
                        continue;
                    }
                    if (getUtilizationOfCpuMips((PowerHost) host) != 0 && isHostOverUtilizedAfterContainerAllocation((PowerHost) host, vm, (Container) container)) {
                        continue;
                    }
                    vm.guestCreate(container);
                    allocatedVm = vm;
                    findVm = true;
                    allocatedHost = (PowerHost) host;
                    break;


                }
            }
            if (findVm) {
                excludedHost1.clear();
                return new GuestMapping(allocatedVm, allocatedHost, (Container)container);
            } else {
                excludedHost1.add(host);
                if (getHostList().size() == excludedHost1.size()) {
                    excludedHost1.clear();
                    return null;
                }
            }
        }
    }

    protected Collection<? extends GuestMapping> getContainerMigrationMapFromUnderUtilizedHosts(List<PowerHost> overUtilizedHosts, List<GuestMapping> previouseMap) {
        List<GuestMapping> migrationMap = new LinkedList<>();
        List<PowerHost> switchedOffHosts = getSwitchedOffHosts();

        // over-utilized hosts + hosts that are selected to migrate VMs to from over-utilized hosts
        Set<PowerHost> excludedHostsForFindingUnderUtilizedHost = new HashSet<>();
        excludedHostsForFindingUnderUtilizedHost.addAll(overUtilizedHosts);
        excludedHostsForFindingUnderUtilizedHost.addAll(switchedOffHosts);
        excludedHostsForFindingUnderUtilizedHost.addAll(extractHostListFromMigrationMap(previouseMap));

        // over-utilized + under-utilized hosts
        Set<PowerHost> excludedHostsForFindingNewContainerPlacement = new HashSet<>();
        excludedHostsForFindingNewContainerPlacement.addAll(overUtilizedHosts);
        excludedHostsForFindingNewContainerPlacement.addAll(switchedOffHosts);

        int numberOfHosts = getHostList().size();

        while (true) {
            if (numberOfHosts == excludedHostsForFindingUnderUtilizedHost.size()) {
                break;
            }

            PowerHost underUtilizedHost = getUnderUtilizedHost(excludedHostsForFindingUnderUtilizedHost);
            if (underUtilizedHost == null) {
                break;
            }

            Log.printlnConcat("Under-utilized host: host #", underUtilizedHost.getId(), "\n");

            excludedHostsForFindingUnderUtilizedHost.add(underUtilizedHost);
            excludedHostsForFindingNewContainerPlacement.add(underUtilizedHost);

            List<? extends GuestEntity> containersToMigrateFromUnderUtilizedHost = getContainersToMigrateFromUnderUtilizedHost(underUtilizedHost);
            if (containersToMigrateFromUnderUtilizedHost.isEmpty()) {
                continue;
            }

            Log.print("Reallocation of Containers from the under-utilized host: ");
            if (!Log.isDisabled()) {
                for (GuestEntity container : containersToMigrateFromUnderUtilizedHost) {
                    Log.print(container.getId() + " ");
                }
            }
            Log.println();

            List<GuestMapping> newContainerPlacement = getNewContainerPlacementFromUnderUtilizedHost(
                    containersToMigrateFromUnderUtilizedHost,
                    excludedHostsForFindingNewContainerPlacement);
            //Sareh
            if (newContainerPlacement == null) {
//                Add the host to the placement founder option
                excludedHostsForFindingNewContainerPlacement.remove(underUtilizedHost);

            }

            excludedHostsForFindingUnderUtilizedHost.addAll(extractHostListFromMigrationMap(newContainerPlacement));
            //The migration mapp does not have a value for container since the whole vm would be migrated.
            migrationMap.addAll(newContainerPlacement);
            Log.println();
        }

        switchedOffHosts.clear();
        excludedHostsForFindingUnderUtilizedHost.clear();
        excludedHostsForFindingNewContainerPlacement.clear();


        return migrationMap;


    }

    /**
     * Gets the vms to migrate from under utilized host.
     *
     * @param host the host
     * @return the vms to migrate from under utilized host
     */
    protected List<? extends GuestEntity> getContainersToMigrateFromUnderUtilizedHost(PowerHost host) {
        List<GuestEntity> containersToMigrate = new LinkedList<>();
        for (ContainerVm vm : host.<ContainerVm>getGuestList()) {
            if (!vm.isInMigration()) {
                for (GuestEntity container : vm.getGuestList()) {
                    if (!container.isInMigration()) {
                        containersToMigrate.add(container);
                    }
                }
            }
        }
        return containersToMigrate;
    }

    /**
     * Gets the new vm placement from under utilized host.
     *
     * @param containersToMigrate the vms to migrate
     * @param excludedHosts the excluded hosts
     * @return the new vm placement from under utilized host
     */
    protected List<GuestMapping> getNewContainerPlacementFromUnderUtilizedHost(
            List<? extends GuestEntity> containersToMigrate,
            Set<? extends HostEntity> excludedHosts) {
        List<GuestMapping> migrationMap = new LinkedList<>();
        VmList.sortByCpuUtilization(containersToMigrate);
        for (GuestEntity container : containersToMigrate) {
            GuestMapping allocatedMap = findHostForGuest(container, excludedHosts, true);
            if (allocatedMap != null && allocatedMap.vm() != null && allocatedMap.host() != null) {
                Log.printlnConcat("Container# ",container.getId(),"allocated to VM # ", (allocatedMap.vm()).getId()
                        , " on host# ", (allocatedMap.host()).getId());
                migrationMap.add(allocatedMap);
            } else {
                Log.println("Not all Containers can be reallocated from the host, reallocation cancelled");
                migrationMap.clear();
                break;
            }
        }
        return migrationMap;
    }



    @Override
    protected GuestMapping findAvailableHostForContainer(Container container,
                                                                List<GuestMapping> createdVm) {


    PowerHost allocatedHost = null;
    ContainerVm allocatedVm = null;
    Set<HostEntity> excludedHost1 = new HashSet<>();
    List<HostEntity> underUtilizedHostList = new ArrayList<>();
    for(GuestMapping map1 : createdVm){
        Host host=(Host) map1.host();
        if(!underUtilizedHostList.contains(host)) {
            underUtilizedHostList.add(host);
        }
    }

    while (true) {
        HostEntity host = getHostSelectionPolicy().select(underUtilizedHostList, container, excludedHost1);
        List<ContainerVm> vmList = new ArrayList<>();

        for (GuestMapping map2 : createdVm) {
            if (map2.host() == host) {
                vmList.add((ContainerVm) map2.vm());
            }
        }

        boolean findVm = false;

        VmList.sortByCpuUtilization(vmList);
        for (int i = 0; i < vmList.size(); i++) {

            ContainerVm vm = vmList.get(vmList.size() - 1 - i);
            if (vm.isSuitableForGuest(container)) {

                // if vm is overutilized or host would be overutilized after the allocation, this host is not chosen!
                if (!isVmOverUtilized(vm)) {
                    continue;
                }

                vm.guestCreate(container);
                allocatedVm = vm;
                findVm = true;
                allocatedHost = (PowerHost) host;
                break;


            }
        }
        if (findVm) {
            excludedHost1.clear();
            return new GuestMapping(allocatedVm, allocatedHost, container);
        } else {
            if (host != null){
                excludedHost1.add(host);
            }
            if (underUtilizedHostList.size() == excludedHost1.size()) {
                excludedHost1.clear();
                return null;
            }
        }

    }


    }

    public void setHostSelectionPolicy(SelectionPolicy<HostEntity> hostSelectionPolicy) {
        this.hostSelectionPolicy = hostSelectionPolicy;
    }

    public SelectionPolicy<HostEntity> getHostSelectionPolicy() {
        return hostSelectionPolicy;
    }
}
