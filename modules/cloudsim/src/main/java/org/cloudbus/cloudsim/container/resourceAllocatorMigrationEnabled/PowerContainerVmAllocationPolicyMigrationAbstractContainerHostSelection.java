package org.cloudbus.cloudsim.container.resourceAllocatorMigrationEnabled;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.container.containerSelectionPolicies.PowerContainerSelectionPolicy;
import org.cloudbus.cloudsim.container.core.*;
import org.cloudbus.cloudsim.selectionPolicies.SelectionPolicy;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.core.HostEntity;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.lists.PowerVmList;

import java.util.*;

/**
 * Created by sareh on 11/08/15.
 * Modified by Remo Andreoli (Feb 2024)
 */
public abstract class PowerContainerVmAllocationPolicyMigrationAbstractContainerHostSelection extends PowerContainerVmAllocationPolicyMigrationAbstractContainerAdded {

    private SelectionPolicy<HostEntity> hostSelectionPolicy;

    public PowerContainerVmAllocationPolicyMigrationAbstractContainerHostSelection(List<? extends HostEntity> hostList, SelectionPolicy<GuestEntity> vmSelectionPolicy,
                                                                                   PowerContainerSelectionPolicy containerSelectionPolicy, SelectionPolicy<HostEntity> hostSelectionPolicy,
                                                                                   int numberOfVmTypes, int[] vmPes, int[] vmRam, long vmBw, long vmSize, double[] vmMips) {
        super(hostList, vmSelectionPolicy, containerSelectionPolicy, numberOfVmTypes, vmPes, vmRam, vmBw, vmSize, vmMips);
        setHostSelectionPolicy(hostSelectionPolicy);
    }

    @Override
    public Map<String, Object> findHostForGuest(GuestEntity container, Set<? extends HostEntity> excludedHosts, boolean checkForVM) {

        PowerHost allocatedHost = null;
        ContainerVm allocatedVm = null;
        Map<String, Object> map = new HashMap<>();
        if(excludedHosts.size() == getHostList().size()){
            return map;}
        Set<HostEntity> excludedHost1 = new HashSet<>(excludedHosts);
        while (true) {
            if(getHostList().isEmpty()){
                return map;
            }
            HostEntity host = getHostSelectionPolicy().select(getHostList(), container, excludedHost1);
            boolean findVm = false;
            List<ContainerVm> vmList = host.getGuestList();
            PowerVmList.sortByCpuUtilization(vmList);
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

                map.put("vm", allocatedVm);
                map.put("host", allocatedHost);
                map.put("container", container);
                excludedHost1.clear();
                return map;


            } else {
                excludedHost1.add(host);
                if (getHostList().size() == excludedHost1.size()) {
                    excludedHost1.clear();
                    return map;
                }
            }

        }


    }

    protected Collection<? extends Map<String, Object>> getContainerMigrationMapFromUnderUtilizedHosts(List<PowerHost> overUtilizedHosts, List<Map<String, Object>> previouseMap) {


        List<Map<String, Object>> migrationMap = new LinkedList<>();
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

            List<Map<String, Object>> newContainerPlacement = getNewContainerPlacementFromUnderUtilizedHost(
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
    protected List<Map<String, Object>> getNewContainerPlacementFromUnderUtilizedHost(
            List<? extends GuestEntity> containersToMigrate,
            Set<? extends HostEntity> excludedHosts) {
        List<Map<String, Object>> migrationMap = new LinkedList<>();
        PowerVmList.sortByCpuUtilization(containersToMigrate);
        for (GuestEntity container : containersToMigrate) {
            Map<String, Object> allocatedMap = findHostForGuest(container, excludedHosts, true);
            if (allocatedMap.get("vm") != null && allocatedMap.get("host")!= null) {

                Log.printlnConcat("Container# ",container.getId(),"allocated to VM # ", ((ContainerVm)allocatedMap.get("vm")).getId()
                        , " on host# ", ((Host)allocatedMap.get("host")).getId());
                migrationMap.add(allocatedMap);
            } else {
                Log.println("Not all Containers can be reallocated from the host, reallocation cancelled");
                allocatedMap.clear();
                migrationMap.clear();
                break;
            }
        }
        return migrationMap;
    }



@Override
    protected Map<String, Object> findAvailableHostForContainer(Container container,
                                                                List<Map<String, Object>> createdVm) {


    PowerHost allocatedHost = null;
    ContainerVm allocatedVm = null;
    Map<String, Object> map = new HashMap<>();
    Set<HostEntity> excludedHost1 = new HashSet<>();
    List<HostEntity> underUtilizedHostList = new ArrayList<>();
    for(Map<String, Object> map1:createdVm){
        Host host=(Host) map1.get("host");
        if(!underUtilizedHostList.contains(host)){
            underUtilizedHostList.add(host);}

    }


    while (true) {

        HostEntity host = getHostSelectionPolicy().select(underUtilizedHostList, container, excludedHost1);
        List<ContainerVm> vmList = new ArrayList<>();

        for(Map<String, Object> map2:createdVm){
            if(map2.get("host")== host){
                vmList.add((ContainerVm) map2.get("vm"));
            }

        }



        boolean findVm = false;

        PowerVmList.sortByCpuUtilization(vmList);
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

            map.put("vm", allocatedVm);
            map.put("host", allocatedHost);
            map.put("container", container);
            excludedHost1.clear();
            return map;


        } else {
            if(host != null){
            excludedHost1.add(host);
            }
            if (underUtilizedHostList.size() == excludedHost1.size()) {
                excludedHost1.clear();
                return map;
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
