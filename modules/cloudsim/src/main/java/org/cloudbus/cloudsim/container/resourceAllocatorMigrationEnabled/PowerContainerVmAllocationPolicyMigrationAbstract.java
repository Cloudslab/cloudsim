/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2024, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.container.resourceAllocatorMigrationEnabled;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.container.core.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.core.HostEntity;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.selectionPolicies.SelectionPolicy;
import org.cloudbus.cloudsim.util.ExecutionTimeMeasurer;

import java.util.*;

/**
 * Created by sareh on 28/07/15.
 * Modified by Remo Andreoli (Feb 2024)
 */
public abstract class PowerContainerVmAllocationPolicyMigrationAbstract extends VmAllocationPolicySimpler {

    /**
     * The vm selection policy.
     */
    private SelectionPolicy<GuestEntity> vmSelectionPolicy;

    /**
     * The saved allocation.
     */
    private final List<GuestMapping> savedAllocation = new ArrayList<>();

    /**
     * The utilization history.
     */
    private final Map<Integer, List<Double>> utilizationHistory = new HashMap<>();

    /**
     * The metric history.
     */
    private final Map<Integer, List<Double>> metricHistory = new HashMap<>();

    /**
     * The time history.
     */
    private final Map<Integer, List<Double>> timeHistory = new HashMap<>();

    /**
     * The execution time history vm selection.
     */
    private final List<Double> executionTimeHistoryVmSelection = new ArrayList<>();

    /**
     * The execution time history host selection.
     */
    private final List<Double> executionTimeHistoryHostSelection = new ArrayList<>();

    /**
     * The execution time history vm reallocation.
     */
    private final List<Double> executionTimeHistoryVmReallocation = new ArrayList<>();

    /**
     * The execution time history total.
     */
    private final List<Double> executionTimeHistoryTotal = new ArrayList<>();

    /**
     * Instantiates a new power vm allocation policy migration abstract.
     *
     * @param hostList          the host list
     * @param vmSelectionPolicy the vm selection policy
     */
    public PowerContainerVmAllocationPolicyMigrationAbstract(
            List<? extends HostEntity> hostList,
            SelectionPolicy<GuestEntity> vmSelectionPolicy) {
        super(hostList);
        setVmSelectionPolicy(vmSelectionPolicy);
    }

    /**
     * Optimize allocation of the VMs according to current utilization.
     *
     * @param vmList the vm list
     * @return the array list< hash map< string, object>>
     */
    @Override
    public List<GuestMapping> optimizeAllocation(List<? extends GuestEntity> vmList) {
        ExecutionTimeMeasurer.start("optimizeAllocationTotal");

        ExecutionTimeMeasurer.start("optimizeAllocationHostSelection");
        List<PowerHost> overUtilizedHosts = getOverUtilizedHosts();
        getExecutionTimeHistoryHostSelection().add(
                ExecutionTimeMeasurer.end("optimizeAllocationHostSelection"));

        printOverUtilizedHosts(overUtilizedHosts);

        saveAllocation();

        ExecutionTimeMeasurer.start("optimizeAllocationVmSelection");
        List<? extends GuestEntity> vmsToMigrate = getVmsToMigrateFromHosts(overUtilizedHosts);
        getExecutionTimeHistoryVmSelection().add(ExecutionTimeMeasurer.end("optimizeAllocationVmSelection"));

        Log.println("Reallocation of VMs from the over-utilized hosts:");
        ExecutionTimeMeasurer.start("optimizeAllocationVmReallocation");
        List<GuestMapping> migrationMap = getNewVmPlacement(vmsToMigrate, new HashSet<Host>(
                overUtilizedHosts));
        getExecutionTimeHistoryVmReallocation().add(
                ExecutionTimeMeasurer.end("optimizeAllocationVmReallocation"));
        Log.println();

        migrationMap.addAll(getMigrationMapFromUnderUtilizedHosts(overUtilizedHosts, migrationMap));

        restoreAllocation();

        getExecutionTimeHistoryTotal().add(ExecutionTimeMeasurer.end("optimizeAllocationTotal"));

        return migrationMap;
    }

    /**
     * Gets the migration map from under utilized hosts.
     *
     * @param overUtilizedHosts the over utilized hosts
     * @return the migration map from under utilized hosts
     */
    protected List<GuestMapping> getMigrationMapFromUnderUtilizedHosts(
            List<PowerHost> overUtilizedHosts, List<GuestMapping> previouseMap) {
        List<GuestMapping> migrationMap = new LinkedList<>();
        List<PowerHost> switchedOffHosts = getSwitchedOffHosts();

        // over-utilized hosts + hosts that are selected to migrate VMs to from over-utilized hosts
        Set<PowerHost> excludedHostsForFindingUnderUtilizedHost = new HashSet<>();
        excludedHostsForFindingUnderUtilizedHost.addAll(overUtilizedHosts);
        excludedHostsForFindingUnderUtilizedHost.addAll(switchedOffHosts);
        excludedHostsForFindingUnderUtilizedHost.addAll(extractHostListFromMigrationMap(previouseMap));

        // over-utilized + under-utilized hosts
        Set<PowerHost> excludedHostsForFindingNewVmPlacement = new HashSet<>();
        excludedHostsForFindingNewVmPlacement.addAll(overUtilizedHosts);
        excludedHostsForFindingNewVmPlacement.addAll(switchedOffHosts);

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
            excludedHostsForFindingNewVmPlacement.add(underUtilizedHost);

            List<? extends ContainerVm> vmsToMigrateFromUnderUtilizedHost = getVmsToMigrateFromUnderUtilizedHost(underUtilizedHost);
            if (vmsToMigrateFromUnderUtilizedHost.isEmpty()) {
                continue;
            }

            Log.print("Reallocation of VMs from the under-utilized host: ");
            if (!Log.isDisabled()) {
                for (ContainerVm vm : vmsToMigrateFromUnderUtilizedHost) {
                    Log.print(vm.getId() + " ");
                }
            }
            Log.println();

            List<GuestMapping> newVmPlacement = getNewVmPlacementFromUnderUtilizedHost(
                    vmsToMigrateFromUnderUtilizedHost,
                    excludedHostsForFindingNewVmPlacement);

            excludedHostsForFindingUnderUtilizedHost.addAll(extractHostListFromMigrationMap(newVmPlacement));

            migrationMap.addAll(newVmPlacement);
            Log.println();
        }

        excludedHostsForFindingUnderUtilizedHost.clear();
        excludedHostsForFindingNewVmPlacement.clear();
        return migrationMap;
    }

    /**
     * Prints the over utilized hosts.
     *
     * @param overUtilizedHosts the over utilized hosts
     */
    protected void printOverUtilizedHosts(List<PowerHost> overUtilizedHosts) {
        if (!Log.isDisabled()) {
            Log.println("Over-utilized hosts:");
            for (PowerHost host : overUtilizedHosts) {
                Log.printlnConcat("Host #", host.getId());
            }
            Log.println();
        }
    }

    /**
     * Find host for vm.
     *
     * @param vm            the vm
     * @param excludedHosts the excluded hosts
     * @return the power host
     */
    public PowerHost findHostForGuest(GuestEntity vm, Set<? extends HostEntity> excludedHosts) {
        double minPower = Double.MAX_VALUE;
        PowerHost allocatedHost = null;

        for (PowerHost host : this.<PowerHost>getHostList()) {
            if (excludedHosts.contains(host)) {
                continue;
            }
            if (host.isSuitableForGuest(vm)) {
                if (getUtilizationOfCpuMips(host) != 0 && isHostOverUtilizedAfterAllocation(host, vm)) {
                    continue;
                }

                try {
                    double powerAfterAllocation = getPowerAfterAllocation(host, vm);
                    if (powerAfterAllocation != -1) {
                        double powerDiff = powerAfterAllocation - host.getPower();
                        if (powerDiff < minPower) {
                            minPower = powerDiff;
                            allocatedHost = host;
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
        return allocatedHost;
    }

    /**
     * Checks if is host over utilized after allocation.
     *
     * @param host the host
     * @param vm   the vm
     * @return true, if is host over utilized after allocation
     */
    protected boolean isHostOverUtilizedAfterAllocation(PowerHost host, GuestEntity vm) {
        boolean isHostOverUtilizedAfterAllocation = true;
        if (host.guestCreate(vm)) {
            isHostOverUtilizedAfterAllocation = isHostOverUtilized(host);
            host.guestDestroy(vm);
        }

        return isHostOverUtilizedAfterAllocation;
    }

    /**
     * Find host for vm.
     * @TODO: this is supposed to be an Override from PowerVmAllocationPolicyAbstract, fix
     * @param vm the vm
     * @return the power host
     */
    public PowerHost findHostForGuest(ContainerVm vm) {
        Set<Host> excludedHosts = new HashSet<>();
        if (vm.getHost() != null) {
            excludedHosts.add((Host) vm.getHost());
        }
        PowerHost hostForVm = findHostForGuest(vm, excludedHosts);
        excludedHosts.clear();

        return hostForVm;
    }

    /**
     * Extract host list from migration map.
     *
     * @param migrationMap the migration map
     * @return the list
     */
    protected List<PowerHost> extractHostListFromMigrationMap(List<GuestMapping> migrationMap) {
        List<PowerHost> hosts = new LinkedList<>();
        for (GuestMapping map : migrationMap) {
            hosts.add((PowerHost) map.host());
        }

        return hosts;
    }

    /**
     * Gets the new vm placement.
     *
     * @param vmsToMigrate  the vms to migrate
     * @param excludedHosts the excluded hosts
     * @return the new vm placement
     */
    protected List<GuestMapping> getNewVmPlacement(
            List<? extends GuestEntity> vmsToMigrate,
            Set<? extends HostEntity> excludedHosts) {
        List<GuestMapping> migrationMap = new LinkedList<>();
        VmList.sortByCpuUtilization(vmsToMigrate);
        for (GuestEntity vm : vmsToMigrate) {
            PowerHost allocatedHost = findHostForGuest(vm, excludedHosts);
            if (allocatedHost != null) {
                allocatedHost.guestCreate(vm);
                Log.printlnConcat("VM #", vm.getId(), " allocated to host #", allocatedHost.getId());

                GuestMapping migrate = new GuestMapping(vm, allocatedHost);
                migrationMap.add(migrate);
            }
        }
        return migrationMap;
    }

    /**
     * Gets the new vm placement from under utilized host.
     *
     * @param vmsToMigrate  the vms to migrate
     * @param excludedHosts the excluded hosts
     * @return the new vm placement from under utilized host
     */
    protected List<GuestMapping> getNewVmPlacementFromUnderUtilizedHost(
            List<? extends ContainerVm> vmsToMigrate,
            Set<? extends Host> excludedHosts) {
        List<GuestMapping> migrationMap = new LinkedList<>();
        VmList.sortByCpuUtilization(vmsToMigrate);
        for (ContainerVm vm : vmsToMigrate) {
            PowerHost allocatedHost = findHostForGuest(vm, excludedHosts);
            if (allocatedHost != null) {
                allocatedHost.guestCreate(vm);
                Log.printlnConcat("VM #", vm.getId(), " allocated to host #", allocatedHost.getId());

                GuestMapping migrate = new GuestMapping(vm, allocatedHost);
                migrationMap.add(migrate);
            } else {
                Log.println("Not all VMs can be reallocated from the host, reallocation cancelled");
                for (GuestMapping map : migrationMap) {
                    (map.host()).guestDestroy(map.vm());
                }
                migrationMap.clear();
                break;
            }
        }
        return migrationMap;
    }

    /**
     * Gets the vms to migrate from hosts.
     *
     * @param overUtilizedHosts the over utilized hosts
     * @return the vms to migrate from hosts
     */
    protected List<? extends GuestEntity> getVmsToMigrateFromHosts(List<PowerHost> overUtilizedHosts) {
        List<GuestEntity> vmsToMigrate = new LinkedList<>();
        for (PowerHost host : overUtilizedHosts) {
            while (true) {
                GuestEntity vm = getVmSelectionPolicy().select(host.getMigrableVms(), host, Set.of());
                if (vm == null) {
                    break;
                }
                vmsToMigrate.add(vm);
                host.guestDestroy(vm);
                if (!isHostOverUtilized(host)) {
                    break;
                }
            }
        }
        return vmsToMigrate;
    }


    /**
     * Gets the vms to migrate from under utilized host.
     *
     * @param host the host
     * @return the vms to migrate from under utilized host
     */
    protected List<? extends ContainerVm> getVmsToMigrateFromUnderUtilizedHost(PowerHost host) {
        List<ContainerVm> vmsToMigrate = new LinkedList<>();
        for (ContainerVm vm : host.<ContainerVm>getGuestList()) {
            if (!vm.isInMigration()) {
                vmsToMigrate.add(vm);
            }
        }
        return vmsToMigrate;
    }

    /**
     * Gets the over utilized hosts.
     *
     * @return the over utilized hosts
     */
    protected List<PowerHost> getOverUtilizedHosts() {
        List<PowerHost> overUtilizedHosts = new LinkedList<>();
        for (PowerHost host : this.<PowerHost>getHostList()) {
            if (isHostOverUtilized(host)) {
                overUtilizedHosts.add(host);
            }
            }
        return overUtilizedHosts;
    }

    /**
     * Gets the switched off host.
     *
     * @return the switched off host
     */
    protected List<PowerHost> getSwitchedOffHosts() {
        List<PowerHost> switchedOffHosts = new LinkedList<>();
        for (PowerHost host : this.<PowerHost>getHostList()) {
            if (host.getUtilizationOfCpu() == 0) {
                switchedOffHosts.add(host);
            }
        }
        return switchedOffHosts;
    }

    /**
     * Gets the under utilized host.
     *
     * @param excludedHosts the excluded hosts
     * @return the under utilized host
     */
    protected PowerHost getUnderUtilizedHost(Set<? extends Host> excludedHosts) {
        double minUtilization = 1;
        PowerHost underUtilizedHost = null;
        for (PowerHost host : this.<PowerHost>getHostList()) {
            if (excludedHosts.contains(host)) {
                continue;
            }

            double utilization = host.getUtilizationOfCpu();
            if (utilization > 0 && utilization < minUtilization
                    && !areAllVmsMigratingOutOrAnyVmMigratingIn(host)&& !areAllContainersMigratingOutOrAnyContainersMigratingIn(host)) {
                minUtilization = utilization;
                underUtilizedHost = host;
            }
        }
        return underUtilizedHost;
    }






    /**
     * Checks whether all vms are in migration.
     *
     * @param host the host
     * @return true, if successful
     */
    protected boolean areAllVmsMigratingOutOrAnyVmMigratingIn(PowerHost host) {
        for (PowerContainerVm vm : host.<PowerContainerVm>getGuestList()) {
            if (!vm.isInMigration()) {
                return false;
            }
            if (host.getGuestsMigratingIn().contains(vm)) {
                return true;
            }
        }
        return true;
    }

    /**
     * Checks whether all vms are in migration.
     *
     * @param host the host
     * @return true, if successful
     */
    protected boolean areAllContainersMigratingOutOrAnyContainersMigratingIn(PowerHost host) {
        for (PowerContainerVm vm : host.<PowerContainerVm>getGuestList()) {
           if(!vm.getGuestsMigratingIn().isEmpty()){
               return true;
           }
            for(GuestEntity container:vm.getGuestList()){
                if(!container.isInMigration()){
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks if is host over utilized.
     *
     * @param host the host
     * @return true, if is host over utilized
     */
    protected abstract boolean isHostOverUtilized(PowerHost host);


    /**
     * Checks if is host over utilized.
     *
     * @param host the host
     * @return true, if is host over utilized
     */
    protected abstract boolean isHostUnderUtilized(PowerHost host);


    /**
     * Adds the history value.
     *
     * @param host   the host
     * @param metric the metric
     * @TODO: I had to change this (ContainerHostDynamicWorkload -> PowerHost); I don't know if it's a problem
     */
    protected void addHistoryEntry(PowerHost host, double metric) {
        int hostId = host.getId();
        if (!getTimeHistory().containsKey(hostId)) {
            getTimeHistory().put(hostId, new LinkedList<>());
        }
        if (!getUtilizationHistory().containsKey(hostId)) {
            getUtilizationHistory().put(hostId, new LinkedList<>());
        }
        if (!getMetricHistory().containsKey(hostId)) {
            getMetricHistory().put(hostId, new LinkedList<>());
        }
        if (!getTimeHistory().get(hostId).contains(CloudSim.clock())) {
            getTimeHistory().get(hostId).add(CloudSim.clock());
            getUtilizationHistory().get(hostId).add(host.getUtilizationOfCpu());
            getMetricHistory().get(hostId).add(metric);
        }
    }

    /**
     * Save allocation.
     */
    protected void saveAllocation() {
        getSavedAllocation().clear();
        for (Host host : this.<Host>getHostList()) {
            for (ContainerVm vm : host.<ContainerVm>getGuestList()) {
                if (host.getGuestsMigratingIn().contains(vm)) {
                    continue;
                }
                getSavedAllocation().add(new GuestMapping(vm, host));
            }
        }
    }

    /**
     * Restore allocation.
     */
    protected void restoreAllocation() {
        for (Host host : this.<Host>getHostList()) {
            host.guestDestroyAll();
            host.reallocateMigratingInGuests();
        }
        for (GuestMapping map : getSavedAllocation()) {
            ContainerVm vm = (ContainerVm) map.vm();
            PowerHost host = (PowerHost) map.host();
            if (!host.guestCreate(vm)) {
                Log.printlnConcat("Couldn't restore VM #", vm.getId(), " on host #", host.getId());
                System.exit(0);
            }
            getGuestTable().put(vm.getUid(), host);
        }
    }

    /**
     * Gets the power after allocation.
     *
     * @param host the host
     * @param vm   the vm
     * @return the power after allocation
     */
    protected double getPowerAfterAllocation(PowerHost host, GuestEntity vm) {
        double power = 0;
        try {
            power = host.getPowerModel().getPower(getMaxUtilizationAfterAllocation(host, vm));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        return power;
    }

    /**
     * Gets the power after allocation. We assume that load is balanced between PEs. The only
     * restriction is: VM's max MIPS < PE's MIPS
     *
     * @param host the host
     * @param vm   the vm
     * @return the power after allocation
     */
    protected double getMaxUtilizationAfterAllocation(PowerHost host, GuestEntity vm) {
        double requestedTotalMips = vm.getCurrentRequestedTotalMips();
        double hostUtilizationMips = getUtilizationOfCpuMips(host);
        double hostPotentialUtilizationMips = hostUtilizationMips + requestedTotalMips;
        double pePotentialUtilization = hostPotentialUtilizationMips / host.getTotalMips();
        return pePotentialUtilization;
    }

    /**
     * Gets the utilization of the CPU in MIPS for the current potentially allocated VMs.
     *
     * @param host the host
     * @return the utilization of the CPU in MIPS
     */
    protected double getUtilizationOfCpuMips(PowerHost host) {
        double hostUtilizationMips = 0;
        for (ContainerVm vm2 : host.<ContainerVm>getGuestList()) {
            if (host.getGuestsMigratingIn().contains(vm2)) {
                // calculate additional potential CPU usage of a migrating in VM
                hostUtilizationMips += host.getTotalAllocatedMipsForGuest(vm2) * 0.9 / 0.1;
            }
            hostUtilizationMips += host.getTotalAllocatedMipsForGuest(vm2);
        }
        return hostUtilizationMips;
    }

    /**
     * Gets the saved allocation.
     *
     * @return the saved allocation
     */
    protected List<GuestMapping> getSavedAllocation() {
        return savedAllocation;
    }

    /**
     * Sets the vm selection policy.
     *
     * @param vmSelectionPolicy the new vm selection policy
     */
    protected void setVmSelectionPolicy(SelectionPolicy<GuestEntity> vmSelectionPolicy) {
        this.vmSelectionPolicy = vmSelectionPolicy;
    }

    /**
     * Gets the vm selection policy.
     *
     * @return the vm selection policy
     */
    protected SelectionPolicy<GuestEntity> getVmSelectionPolicy() {
        return vmSelectionPolicy;
    }

    /**
     * Gets the utilization history.
     *
     * @return the utilization history
     */
    public Map<Integer, List<Double>> getUtilizationHistory() {
        return utilizationHistory;
    }

    /**
     * Gets the metric history.
     *
     * @return the metric history
     */
    public Map<Integer, List<Double>> getMetricHistory() {
        return metricHistory;
    }

    /**
     * Gets the time history.
     *
     * @return the time history
     */
    public Map<Integer, List<Double>> getTimeHistory() {
        return timeHistory;
    }

    /**
     * Gets the execution time history vm selection.
     *
     * @return the execution time history vm selection
     */
    public List<Double> getExecutionTimeHistoryVmSelection() {
        return executionTimeHistoryVmSelection;
    }

    /**
     * Gets the execution time history host selection.
     *
     * @return the execution time history host selection
     */
    public List<Double> getExecutionTimeHistoryHostSelection() {
        return executionTimeHistoryHostSelection;
    }

    /**
     * Gets the execution time history vm reallocation.
     *
     * @return the execution time history vm reallocation
     */
    public List<Double> getExecutionTimeHistoryVmReallocation() {
        return executionTimeHistoryVmReallocation;
    }

    /**
     * Gets the execution time history total.
     *
     * @return the execution time history total
     */
    public List<Double> getExecutionTimeHistoryTotal() {
        return executionTimeHistoryTotal;
    }

//    public abstract List<? extends Container> getContainersToMigrateFromHosts(List<PowerHost> overUtilizedHosts);
}