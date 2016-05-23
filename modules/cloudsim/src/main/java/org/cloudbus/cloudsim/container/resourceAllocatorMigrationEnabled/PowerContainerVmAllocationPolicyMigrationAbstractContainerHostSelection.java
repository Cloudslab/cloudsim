package org.cloudbus.cloudsim.container.resourceAllocatorMigrationEnabled;

import org.cloudbus.cloudsim.container.containerSelectionPolicies.PowerContainerSelectionPolicy;
import org.cloudbus.cloudsim.container.core.*;
import org.cloudbus.cloudsim.container.hostSelectionPolicies.HostSelectionPolicy;
import org.cloudbus.cloudsim.container.lists.PowerContainerList;
import org.cloudbus.cloudsim.container.lists.PowerContainerVmList;
import org.cloudbus.cloudsim.container.vmSelectionPolicies.PowerContainerVmSelectionPolicy;
import org.cloudbus.cloudsim.Log;

import java.util.*;

/**
 * Created by sareh on 11/08/15.
 */
public abstract class PowerContainerVmAllocationPolicyMigrationAbstractContainerHostSelection extends PowerContainerVmAllocationPolicyMigrationAbstractContainerAdded {

    private HostSelectionPolicy hostSelectionPolicy;

    public PowerContainerVmAllocationPolicyMigrationAbstractContainerHostSelection(List<? extends ContainerHost> hostList, PowerContainerVmSelectionPolicy vmSelectionPolicy,
    		PowerContainerSelectionPolicy containerSelectionPolicy, HostSelectionPolicy hostSelectionPolicy,
    		int numberOfVmTypes, int[] vmPes, float[] vmRam, long vmBw, long vmSize, double[] vmMips) {
        super(hostList, vmSelectionPolicy, containerSelectionPolicy, numberOfVmTypes, vmPes, vmRam, vmBw, vmSize, vmMips);
        setHostSelectionPolicy(hostSelectionPolicy);
    }

    @Override
    public Map<String, Object> findHostForContainer(Container container, Set<? extends ContainerHost> excludedHosts, boolean checkForVM) {

        PowerContainerHost allocatedHost = null;
        ContainerVm allocatedVm = null;
        Map<String, Object> map = new HashMap<>();
        Set<ContainerHost> excludedHost1 = new HashSet<>();
        if(excludedHosts.size() == getContainerHostList().size()){
            return map;}
        excludedHost1.addAll(excludedHosts);
        while (true) {
            if(getContainerHostList().size()==0){
                return map;
            }
            ContainerHost host = getHostSelectionPolicy().getHost(getContainerHostList(), container, excludedHost1);
            boolean findVm = false;
            List<ContainerVm> vmList = host.getVmList();
            PowerContainerVmList.sortByCpuUtilization(vmList);
            for (int i = 0; i < vmList.size(); i++) {
                ContainerVm vm = vmList.get(vmList.size() - 1 - i);
                if(checkForVM){
                    if(vm.isInWaiting()){

                        continue;
                    }

                }
                if (vm.isSuitableForContainer(container)) {

                    // if vm is overutilized or host would be overutilized after the allocation, this host is not chosen!
                    if (!isVmOverUtilized(vm)) {
                        continue;
                    }
                    if (getUtilizationOfCpuMips((PowerContainerHost) host) != 0 && isHostOverUtilizedAfterContainerAllocation((PowerContainerHost) host, vm, container)) {
                        continue;
                    }
                    vm.containerCreate(container);
                    allocatedVm = vm;
                    findVm = true;
                    allocatedHost = (PowerContainerHost) host;
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
                if (getContainerHostList().size() == excludedHost1.size()) {
                    excludedHost1.clear();
                    return map;
                }
            }

        }


    }

    @Override
    protected Collection<? extends Map<String, Object>> getContainerMigrationMapFromUnderUtilizedHosts(List<PowerContainerHostUtilizationHistory> overUtilizedHosts, List<Map<String, Object>> previouseMap) {


        List<Map<String, Object>> migrationMap = new LinkedList<Map<String, Object>>();
        List<PowerContainerHost> switchedOffHosts = getSwitchedOffHosts();

        // over-utilized hosts + hosts that are selected to migrate VMs to from over-utilized hosts
        Set<PowerContainerHost> excludedHostsForFindingUnderUtilizedHost = new HashSet<>();
        excludedHostsForFindingUnderUtilizedHost.addAll(overUtilizedHosts);
        excludedHostsForFindingUnderUtilizedHost.addAll(switchedOffHosts);
        excludedHostsForFindingUnderUtilizedHost.addAll(extractHostListFromMigrationMap(previouseMap));

        // over-utilized + under-utilized hosts
        Set<PowerContainerHost> excludedHostsForFindingNewContainerPlacement = new HashSet<PowerContainerHost>();
        excludedHostsForFindingNewContainerPlacement.addAll(overUtilizedHosts);
        excludedHostsForFindingNewContainerPlacement.addAll(switchedOffHosts);

        int numberOfHosts = getContainerHostList().size();

        while (true) {
            if (numberOfHosts == excludedHostsForFindingUnderUtilizedHost.size()) {
                break;
            }

            PowerContainerHost underUtilizedHost = getUnderUtilizedHost(excludedHostsForFindingUnderUtilizedHost);
            if (underUtilizedHost == null) {
                break;
            }

            Log.printConcatLine("Under-utilized host: host #", underUtilizedHost.getId(), "\n");

            excludedHostsForFindingUnderUtilizedHost.add(underUtilizedHost);
            excludedHostsForFindingNewContainerPlacement.add(underUtilizedHost);

            List<? extends Container> containersToMigrateFromUnderUtilizedHost = getContainersToMigrateFromUnderUtilizedHost(underUtilizedHost);
            if (containersToMigrateFromUnderUtilizedHost.isEmpty()) {
                continue;
            }

            Log.print("Reallocation of Containers from the under-utilized host: ");
            if (!Log.isDisabled()) {
                for (Container container : containersToMigrateFromUnderUtilizedHost) {
                    Log.print(container.getId() + " ");
                }
            }
            Log.printLine();

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
            Log.printLine();
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
    protected List<? extends Container> getContainersToMigrateFromUnderUtilizedHost(PowerContainerHost host) {
        List<Container> containersToMigrate = new LinkedList<>();
        for (ContainerVm vm : host.getVmList()) {
            if (!vm.isInMigration()) {
                for (Container container : vm.getContainerList()) {
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
            List<? extends Container> containersToMigrate,
            Set<? extends ContainerHost> excludedHosts) {
        List<Map<String, Object>> migrationMap = new LinkedList<Map<String, Object>>();
        PowerContainerList.sortByCpuUtilization(containersToMigrate);
        for (Container container : containersToMigrate) {
            Map<String, Object> allocatedMap = findHostForContainer(container, excludedHosts, true);
            if (allocatedMap.get("vm") != null && allocatedMap.get("host")!= null) {

                Log.printConcatLine("Container# ",container.getId(),"allocated to VM # ", ((ContainerVm)allocatedMap.get("vm")).getId()
                        , " on host# ", ((ContainerHost)allocatedMap.get("host")).getId());
                migrationMap.add(allocatedMap);
            } else {
                Log.printLine("Not all Containers can be reallocated from the host, reallocation cancelled");
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


    PowerContainerHost allocatedHost = null;
    ContainerVm allocatedVm = null;
    Map<String, Object> map = new HashMap<>();
    Set<ContainerHost> excludedHost1 = new HashSet<>();
    List<ContainerHost> underUtilizedHostList = new ArrayList<>();
    for(Map<String, Object> map1:createdVm){
        ContainerHost host=(ContainerHost) map1.get("host");
        if(!underUtilizedHostList.contains(host)){
            underUtilizedHostList.add(host);}

    }


    while (true) {

        ContainerHost host = getHostSelectionPolicy().getHost(underUtilizedHostList, container, excludedHost1);
        List<ContainerVm> vmList = new ArrayList<>();

        for(Map<String, Object> map2:createdVm){
            if(map2.get("host")== host){
                vmList.add((ContainerVm) map2.get("vm"));
            }

        }



        boolean findVm = false;

        PowerContainerVmList.sortByCpuUtilization(vmList);
        for (int i = 0; i < vmList.size(); i++) {

            ContainerVm vm = vmList.get(vmList.size() - 1 - i);
            if (vm.isSuitableForContainer(container)) {

                // if vm is overutilized or host would be overutilized after the allocation, this host is not chosen!
                if (!isVmOverUtilized(vm)) {
                    continue;
                }

                vm.containerCreate(container);
                allocatedVm = vm;
                findVm = true;
                allocatedHost = (PowerContainerHost) host;
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

    public void setHostSelectionPolicy(HostSelectionPolicy hostSelectionPolicy) {
        this.hostSelectionPolicy = hostSelectionPolicy;
    }

    public HostSelectionPolicy getHostSelectionPolicy() {
        return hostSelectionPolicy;
    }
}
