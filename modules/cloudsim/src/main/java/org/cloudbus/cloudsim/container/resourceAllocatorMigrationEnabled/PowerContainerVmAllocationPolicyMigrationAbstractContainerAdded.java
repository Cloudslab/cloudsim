/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2024, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.container.resourceAllocatorMigrationEnabled;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.container.core.*;
import org.cloudbus.cloudsim.container.utils.IDs;
import org.cloudbus.cloudsim.container.utils.RandomGen;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.core.HostEntity;
import org.cloudbus.cloudsim.core.PowerGuestEntity;
import org.cloudbus.cloudsim.lists.HostList;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.selectionPolicies.SelectionPolicy;
import org.cloudbus.cloudsim.util.ExecutionTimeMeasurer;

import java.util.*;


/**
 * Created by sareh on 30/07/15.
 * Modified by Remo Andreoli (Feb 2024)
 */

public abstract class PowerContainerVmAllocationPolicyMigrationAbstractContainerAdded extends PowerContainerVmAllocationPolicyMigrationAbstract {

    private ContainerDatacenter datacenter;
    /**
     * The container selection policy.
     */
    private SelectionPolicy<PowerGuestEntity> containerSelectionPolicy;
    protected int numberOfVmTypes;
    protected int[] vmPes;
    protected int[] vmRam;
    protected long vmBw;
    protected long vmSize;
    protected double[] vmMips;

    public PowerContainerVmAllocationPolicyMigrationAbstractContainerAdded(List<? extends HostEntity> hostList,
                                                                           SelectionPolicy<GuestEntity> vmSelectionPolicy, SelectionPolicy<PowerGuestEntity> containerSelectionPolicy,
                                                                           int numberOfVmTypes, int[] vmPes, int[] vmRam, long vmBw, long vmSize, double[] vmMips) {
        super(hostList, vmSelectionPolicy);
//        setDatacenter(datacenter);
        setContainerSelectionPolicy(containerSelectionPolicy);
        
        this.numberOfVmTypes = numberOfVmTypes;
        this.vmPes = vmPes;
        this.vmRam = vmRam;
        this.vmBw = vmBw;
        this.vmSize = vmSize;
        this.vmMips = vmMips;
    }

    @Override
    public List<GuestMapping> optimizeAllocation(List<? extends GuestEntity> vmList) {

        ExecutionTimeMeasurer.start("optimizeAllocationTotal");

        ExecutionTimeMeasurer.start("optimizeAllocationHostSelection");
        List<PowerHost> overUtilizedHosts = getOverUtilizedHosts();
        getExecutionTimeHistoryHostSelection().add(
                ExecutionTimeMeasurer.end("optimizeAllocationHostSelection"));

        printOverUtilizedHosts(overUtilizedHosts);

        saveAllocation();

        ExecutionTimeMeasurer.start("optimizeAllocationContainerSelection");
        List<? extends GuestEntity> containersToMigrate = getContainersToMigrateFromHosts(overUtilizedHosts);
        getExecutionTimeHistoryVmSelection().add(ExecutionTimeMeasurer.end("optimizeAllocationContainerSelection"));

        Log.println("Reallocation of Containers from the over-utilized hosts:");
        ExecutionTimeMeasurer.start("optimizeAllocationVmReallocation");
        List<GuestMapping> migrationMap = getPlacementForLeftContainers(containersToMigrate, new HashSet<Host>(overUtilizedHosts));


        getExecutionTimeHistoryVmReallocation().add(
                ExecutionTimeMeasurer.end("optimizeAllocationVmReallocation"));
        Log.println();

        migrationMap.addAll(getContainerMigrationMapFromUnderUtilizedHosts(overUtilizedHosts, migrationMap));

        restoreAllocation();

        getExecutionTimeHistoryTotal().add(ExecutionTimeMeasurer.end("optimizeAllocationTotal"));

        return migrationMap;


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

            List<? extends ContainerVm> vmsToMigrateFromUnderUtilizedHost = getVmsToMigrateFromUnderUtilizedHost(underUtilizedHost);
            if (vmsToMigrateFromUnderUtilizedHost.isEmpty()) {
                continue;
            }

            Log.print("Reallocation of Containers from the under-utilized host: ");
            if (!Log.isDisabled()) {
                for (ContainerVm vm : vmsToMigrateFromUnderUtilizedHost) {
                    Log.print(vm.getId() + " ");
                }
            }
            Log.println();

            List<GuestMapping> newVmPlacement = getNewVmPlacementFromUnderUtilizedHost(
                    vmsToMigrateFromUnderUtilizedHost,
                    excludedHostsForFindingNewContainerPlacement);
            //Sareh
            if (newVmPlacement == null) {
//                Add the host to the placement founder option
                excludedHostsForFindingNewContainerPlacement.remove(underUtilizedHost);

            }

            excludedHostsForFindingUnderUtilizedHost.addAll(extractHostListFromMigrationMap(newVmPlacement));
            //The migration mapp does not have a value for container since the whole vm would be migrated.
            migrationMap.addAll(newVmPlacement);
            Log.println();
        }

        excludedHostsForFindingUnderUtilizedHost.clear();
        excludedHostsForFindingNewContainerPlacement.clear();
        return migrationMap;
    }

    private List<? extends GuestEntity> getContainersToMigrateFromHosts(List<PowerHost> overUtilizedHosts) {
        List<GuestEntity> containersToMigrate = new LinkedList<>();
        for (PowerHost host : overUtilizedHosts) {
            while (true) {
                GuestEntity container = getContainerSelectionPolicy().select(host.getMigrableContainers(), host, Set.of());
                if (container == null) {
                    break;
                }
                containersToMigrate.add(container);
                container.getHost().guestDestroy(container);
                if (!isHostOverUtilized(host)) {
                    break;
                }
            }
        }
        return containersToMigrate;
    }


    private List<GuestMapping> getNewContainerPlacement(List<? extends GuestEntity> containersToMigrate, Set<? extends Host> excludedHosts) {
        List<GuestMapping> migrationMap = new LinkedList<>();

        VmList.sortByCpuUtilization(containersToMigrate);
        for (GuestEntity container : containersToMigrate) {
            GuestMapping allocationMap = findHostForGuest(container, excludedHosts, false);

            if (allocationMap != null && allocationMap.host() != null && allocationMap.vm() != null) {
                ContainerVm vm = (ContainerVm) allocationMap.vm();
                Log.printlnConcat("Container #", container.getId(), " allocated to host #", (allocationMap.host()).getId(), "The VM ID is #", vm.getId());
                migrationMap.add(new GuestMapping(vm, allocationMap.host(), (Container)container));
            } else {
                migrationMap.add(new GuestMapping(null, null, null, 0, false, true));
            }
        }
        containersToMigrate.clear();
        return migrationMap;
    }

    private List<GuestMapping> getPlacementForLeftContainers(List<? extends GuestEntity> containersToMigrate, Set<? extends Host> excludedHostsList) {
        List<GuestMapping> newMigrationMap = new LinkedList<>();

        if (containersToMigrate.isEmpty()) {
            return newMigrationMap;
        }
        HashSet<Host> excludedHostsforOverUtilized = new HashSet<>();
        excludedHostsforOverUtilized.addAll(getSwitchedOffHosts());
        excludedHostsforOverUtilized.addAll(excludedHostsList);
        List<GuestMapping> migrationMap = getNewContainerPlacement(containersToMigrate, excludedHostsforOverUtilized);
        if (migrationMap.isEmpty()) {
            return migrationMap;
        }
//        List<Container> containerList = getExtraContainers(migrationMap);


        List<Container> containerList = new ArrayList<>();
        for (GuestMapping map : migrationMap) {
            if (map.NewVmRequired()) {
                containerList.add(map.container());
            } else {
                newMigrationMap.add(map);
            }

        }
        if (containerList.isEmpty()) {
            return newMigrationMap;
        }

        List<Host> underUtilizedHostList = getUnderUtilizedHostList(excludedHostsList);

        List<GuestMapping> migrationMapUnderUtilized = findMapInUnderUtilizedHosts(underUtilizedHostList,containerList);
        newMigrationMap.addAll(migrationMapUnderUtilized);
        containerList.removeAll(getAssignedContainers(migrationMapUnderUtilized));
        if(!containerList.isEmpty()){
            List<GuestMapping> migrationMapSwitchedOff = findMapInSwitchedOffHosts(containerList);
            newMigrationMap.addAll(migrationMapSwitchedOff);
        }


// Now Check if there are any containers left without VMs.
        //firsthost chosen


        return newMigrationMap;
    }
    protected List<GuestMapping> findMapInUnderUtilizedHosts(List<Host> underUtilizedHostList, List<Container> containerList){
        List<GuestMapping> newMigrationMap = new ArrayList<>();
        //        Create new Vms on underUtilized hosts;
        List<GuestMapping> createdVmMap = new ArrayList<>();
        if (!underUtilizedHostList.isEmpty()) {
            for (Host host : underUtilizedHostList) {
//                   We try to create the largest Vm possible
                List<ContainerVm> VmList = createVms(host, true);
                if(!VmList.isEmpty()) {
                    for(ContainerVm vm:VmList) {
                        GuestMapping map = new GuestMapping(vm, host);
                        createdVmMap.add(map);
                    }
                }
            }
            if (createdVmMap.isEmpty()) {
                return newMigrationMap;
            }

            //        if there are any new Vms on the underUtilized Hosts we assign the containers to them first!
            // Sort the underUtilized host by the utilization, so that we first assign vms to the more utilized ones
            for (Container container : containerList) {
                GuestMapping allocationMap = findAvailableHostForContainer(container, createdVmMap);
                if (allocationMap != null && allocationMap.host() != null && allocationMap.vm() != null) {
                    ContainerVm vm = (ContainerVm) allocationMap.vm();
                    Log.printlnConcat("Container #", container.getId(), " allocated to host #", (allocationMap.host()).getId(), "The VM ID is #", vm.getId());
                    // vm.setInWaiting(true);
                    newMigrationMap.add(new GuestMapping(vm, allocationMap.host(), container, true, false));
                }
            }
        }

      return newMigrationMap;

    }
    protected List<Container> getAssignedContainers(List<GuestMapping> migrationMap) {
        List<Container> assignedContainers = new ArrayList<>();
        for (GuestMapping map : migrationMap) {
            if (map.container() != null) {
                assignedContainers.add((Container) map.container());
            }
        }
        return assignedContainers;
    }

    protected ContainerVm createVMinHost(Host host, boolean vmStatus) {

        for (int i=0; i<numberOfVmTypes; i++) {
            ContainerVm vm = getNewVm(i);
            if (getUtilizationOfCpuMips((PowerHost) host) != 0 && isHostOverUtilizedAfterAllocation((PowerHost) host, vm)) {
                continue;
            }
            
            if(allocateHostForGuest(vm, host)){
                Log.println("The vm ID #" + vm.getId() + "will be created ");
                vm.setInWaiting(vmStatus);
                return vm;
            }
        }

        return null;
    }

    protected List<GuestMapping> findMapInSwitchedOffHosts(List<Container> containerList) {
        Log.print(String.format(" %s :  Find Placement in the switched of hosts", CloudSim.clock()));
        List<PowerHost> switchedOffHostsList = getSwitchedOffHosts();
        List<GuestMapping> newMigrationMap = new ArrayList<>();

        if (containerList.isEmpty()) {
            return newMigrationMap;
        }

        Host previouseHost = null;
        ContainerVm previouseVm = null;
        while (!containerList.isEmpty()) {
            if (switchedOffHostsList.isEmpty()) {

                Log.print("There is no hosts to create VMs");
                break;
            }
            List<Container> assignedContainer = new ArrayList<>();
            //choose a random host
            if (previouseHost == null && previouseVm == null) {
                if(switchedOffHostsList.isEmpty()){
                    return newMigrationMap;
                }
                int hostIndex = new RandomGen().getNum(switchedOffHostsList.size());
                previouseHost = switchedOffHostsList.get(hostIndex);
                switchedOffHostsList.remove(previouseHost);
                previouseVm = createVMinHost(previouseHost, true);
                previouseHost.guestCreate(previouseVm);

                for (Container container : containerList) {
                    if (previouseVm.isSuitableForGuest(container)) {
                        previouseVm.guestCreate(container);
                        assignedContainer.add(container);
//                        previouseVm.setInWaiting(true);
                        newMigrationMap.add(new GuestMapping(previouseVm, previouseHost, container, true, false));
                    } else {
                        previouseVm = createVMinHost(previouseHost, true);
                        if (previouseVm == null) {
                            switchedOffHostsList.remove(previouseHost);
                            previouseHost = null;
                            containerList.removeAll(assignedContainer);
                            break;
                        }
                        previouseVm.guestCreate(container);
                        assignedContainer.add(container);
//                        previouseVm.setInWaiting(true);
                        newMigrationMap.add(new GuestMapping(previouseVm, previouseHost, container, true, false));
                    }
                }

                containerList.removeAll(assignedContainer);
            } else {

                for (Container container : containerList) {
                    if (previouseVm.isSuitableForGuest(container)) {
                        previouseVm.guestCreate(container);
                        assignedContainer.add(container);
//                        previouseVm.setInWaiting(true);
                        newMigrationMap.add(new GuestMapping(previouseVm, previouseHost, container, true, false));
                    } else {
                        previouseVm = createVMinHost(previouseHost, true);
                        if (previouseVm == null) {
                            switchedOffHostsList.remove(previouseHost);
                            previouseHost = null;
                            containerList.removeAll(assignedContainer);
                            break;
                        }
                        previouseVm.guestCreate(container);
                        assignedContainer.add(container);
//                        previouseVm.setInWaiting(true);
                        newMigrationMap.add(new GuestMapping(previouseVm, previouseHost, container, true, false));
                    }
                }

                containerList.removeAll(assignedContainer);


            }
        }
        return newMigrationMap;


    }

    //    This method should be re written!
    protected GuestMapping findAvailableHostForContainer(Container
                                                                        container, List<GuestMapping> createdVm) {
        double minPower = Double.MAX_VALUE;
        PowerHost allocatedHost = null;
        ContainerVm allocatedVm = null;
        List<Host> underUtilizedHostList = new ArrayList<>();
        List<ContainerVm> vmList = new ArrayList<>();
        for(GuestMapping map : createdVm){
            underUtilizedHostList.add((Host) map.host());
        }
        HostList.sortByCpuUtilization(underUtilizedHostList);
        for (Host host1 : underUtilizedHostList) {

            PowerHost host = (PowerHost) host1;
            for(GuestMapping map : createdVm) {
                if (map.host() == host1) {
                    vmList.add((ContainerVm) map.vm());
                }
            }
            for (ContainerVm vm : vmList) {
//                if vm is not created no need for checking!

                if (vm.isSuitableForGuest(container)) {
                    // if vm is overutilized or host would be overutilized after the allocation, this host is not chosen!
                    if (!isVmOverUtilized(vm)) {
                        continue;
                    }
                    if (getUtilizationOfCpuMips(host) != 0 && isHostOverUtilizedAfterContainerAllocation(host, vm, container)) {
                        continue;
                    }

                    try {
                        double powerAfterAllocation = getPowerAfterContainerAllocation(host, container, vm);
                        if (powerAfterAllocation != -1) {
                            double powerDiff = powerAfterAllocation - host.getPower();
                            if (powerDiff < minPower) {
                                minPower = powerDiff;
                                allocatedHost = host;
                                allocatedVm = vm;
                            }
                        }
                    } catch (Exception e) {
                        Log.print("Error: Exception in powerDiff algorithm containerAdded");
                    }
                }
            }
        }

        return new GuestMapping(allocatedVm, allocatedHost);
    }

    private ContainerVm getNewVm(int vmType) {

        ArrayList<Pe> peList = new ArrayList<>();
//        int vmType = new RandomGen().getNum(ConstantsEx.VM_TYPES);
//            int vmType = i / (int) Math.ceil((double) containerVmsNumber / 4.0D);
//            int vmType = 1;
//        Log.print(vmType);
        for (int j = 0; j < vmPes[vmType]; ++j) {
            peList.add(new Pe(j, new PeProvisionerSimple(vmMips[vmType])));
        }
        int brokerId = 2;
        return new PowerContainerVm(IDs.pollId(ContainerVm.class), brokerId, vmMips[vmType],
                vmRam[vmType],
                vmBw, vmSize, "Xen",
                new VmSchedulerTimeSharedOverSubscription(peList),
                new RamProvisionerSimple(vmRam[vmType]),
                new BwProvisionerSimple(vmBw), peList, 300);
    }


    /**
     * Gets the under utilized host.
     *
     * @param excludedHosts the excluded hosts
     * @return the under utilized host
     */
    protected List<Host> getUnderUtilizedHostList(Set<? extends Host> excludedHosts) {
        List<Host> underUtilizedHostList = new ArrayList<>();
        double minUtilization = 1;
        for (PowerHost host : this.<PowerHost>getHostList()) {
            if (excludedHosts.contains(host)) {
                continue;
            }
            double utilization = host.getUtilizationOfCpu();
            if (utilization > 0 && utilization < minUtilization
                    && !areAllVmsMigratingOutOrAnyVmMigratingIn(host) && !areAllContainersMigratingOutOrAnyContainersMigratingIn(host)) {
                minUtilization = utilization;
                underUtilizedHostList.add(host);
            }
        }
        return underUtilizedHostList;
    }

    public GuestMapping findHostForGuest(GuestEntity container, Set<? extends HostEntity> excludedHosts, boolean checkForVM) {
        double minPower = Double.MAX_VALUE;
        PowerHost allocatedHost = null;
        ContainerVm allocatedVm = null;

        for (PowerHost host : this.<PowerHost>getHostList()) {
            if (excludedHosts.contains(host)) {
                continue;
            }
            for (ContainerVm vm : host.<ContainerVm>getGuestList()) {
                if (checkForVM) {
                    if (vm.isInWaiting()) {
                        continue;
                    }
                }
                if (vm.isSuitableForGuest(container)) {
                    // if vm is overutilized or host would be overutilized after the allocation, this host is not chosen!
                    if (!isVmOverUtilized(vm)) {
                        continue;
                    }
                    if (getUtilizationOfCpuMips(host) != 0 && isHostOverUtilizedAfterContainerAllocation(host, vm, (Container) container)) {
                        continue;
                    }

                    try {
                        double powerAfterAllocation = getPowerAfterContainerAllocation(host, (Container) container, vm);
                        if (powerAfterAllocation != -1) {
                            double powerDiff = powerAfterAllocation - host.getPower();
                            if (powerDiff < minPower) {
                                minPower = powerDiff;
                                allocatedHost = host;
                                allocatedVm = vm;
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }

        if (allocatedVm == null || allocatedHost == null) {
            return null;
        }
        return new GuestMapping(allocatedVm, allocatedHost);
    }

    protected boolean isVmOverUtilized(ContainerVm vm) {
        boolean isOverUtilized = true;
        double util = 0;
//        Log.printConcatLine("Checking if the vm is over utilized or not!");
        for (GuestEntity container : vm.getGuestList()) {
            util += container.getTotalUtilizationOfCpuMips(CloudSim.clock());
        }
        if (util > vm.getHost().getTotalMips() / vm.getHost().getNumberOfPes() * vm.getNumberOfPes()) {
            return false;
        }


        return isOverUtilized;
    }

    /**
     * Gets the power after allocation.
     *
     * @param host      the host
     * @param container the vm
     * @return the power after allocation
     */
    protected double getPowerAfterContainerAllocation(PowerHost host, Container container, ContainerVm vm) {
        double power = 0;
        try {
            power = host.getPowerModel().getPower(getMaxUtilizationAfterContainerAllocation(host, container, vm));
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
     * @param host      the host
     * @param container the vm
     * @return the power after allocation
     */
    protected double getMaxUtilizationAfterContainerAllocation(PowerHost host, Container container, ContainerVm containerVm) {
        double requestedTotalMips = container.getCurrentRequestedTotalMips();
        if (requestedTotalMips > containerVm.getMips()) {
            requestedTotalMips = containerVm.getMips();
        }
        double hostUtilizationMips = getUtilizationOfCpuMips(host);
        double hostPotentialUtilizationMips = hostUtilizationMips + requestedTotalMips;
        return hostPotentialUtilizationMips / host.getTotalMips();
    }

    /**
     * Gets the utilization of the CPU in MIPS for the current potentially allocated VMs.
     *
     * @param containerVm the host
     * @return the utilization of the CPU in MIPS
     */
    protected double getUtilizationOfCpuMipsofVm(ContainerVm containerVm) {
        double vmUtilizationMips = 0;
        for (GuestEntity container : containerVm.getGuestList()) {

            vmUtilizationMips += containerVm.getTotalAllocatedMipsForGuest(container);
        }
        return vmUtilizationMips;
    }

    /**
     * Checks if is host over utilized after allocation.
     *
     * @param host      the host
     * @param container the vm
     * @return true, if is host over utilized after allocation
     */
    protected boolean isHostOverUtilizedAfterContainerAllocation(PowerHost host, ContainerVm vm, Container container) {
        boolean isHostOverUtilizedAfterAllocation = true;
        if (vm.guestCreate(container)) {
            isHostOverUtilizedAfterAllocation = isHostOverUtilized(host);
            vm.guestDestroy(container);
        }
        return isHostOverUtilizedAfterAllocation;
    }


    /**
     * Save allocation.
     */
    @Override
    protected void saveAllocation() {
        getSavedAllocation().clear();
        for (Host host : this.<Host>getHostList()) {
            for (ContainerVm vm : host.<ContainerVm>getGuestList()) {
                if (host.getGuestsMigratingIn().contains(vm)) {
                    continue;
                }
                for (GuestEntity container : vm.getGuestList()) {
                    if (vm.getGuestsMigratingIn().contains(container)) {
                        continue;
                    }
                    getSavedAllocation().add(new GuestMapping((GuestEntity) vm, (HostEntity) host, (Container) container));
                }
            }
        }
        Log.println(String.format("The length of the saved map is ....%d", getSavedAllocation().size()));

    }


    /**
     * Restore allocation.
     */
    protected void restoreAllocation() {
        for (Host host : this.<Host>getHostList()) {
            for (ContainerVm vm : host.<ContainerVm>getGuestList()) {
                vm.guestDestroyAll();
                vm.reallocateMigratingInGuests();
            }

            host.guestDestroyAll();
            host.reallocateMigratingInGuests();
        }
        for (GuestMapping map : getSavedAllocation()) {
            PowerContainerVm vm = (PowerContainerVm) map.vm();

            PowerHost host = (PowerHost) map.host();
            if (!host.getGuestList().contains(vm)) {
                if (!host.guestCreate(vm)) {
                    Log.printlnConcat("Couldn't restore VM #", vm.getId(), " on host #", host.getId());
                    System.exit(0);
                }

                getGuestTable().put(vm.getUid(), host);
            }
//            vm.containerDestroyAll();
//            vm.reallocateMigratingInContainers();
        }
//        List<ContainerVm > restoredVms = new ArrayList<>();
        for (GuestMapping map : getSavedAllocation()) {
            PowerContainerVm vm = (PowerContainerVm) map.vm();
            if (map.container() != null) {
                Container container = map.container();
//                Log.print(container);

                if (!vm.getGuestList().contains(container)) {
                    if (!vm.guestCreate(container)) {
                        Log.printlnConcat("Couldn't restore Container #", container.getId(), " on vm #", vm.getId());
                        System.exit(0);
                    }
                } else {

                    Log.print("The Container is in the VM already");
                }

                if (container.getHost() == null) {
                    Log.print("The Vm is null");

                }
                getDatacenter().getContainerAllocationPolicy().
                        getGuestTable().put(container.getUid(), vm);
//            container.setVm(vm);

            }
        }


    }

    protected List<ContainerVm> createVms(Host host, boolean vmStatus) {
        List<ContainerVm> vmList = new ArrayList<>();
        while (true) {
            ContainerVm vm = createVMinHost(host, vmStatus);
            if (vm == null) {
                break;
            }
            vmList.add(vm);
        }
        return vmList;


    }

    public ContainerDatacenter getDatacenter() {
        return datacenter;
    }

    @Override
    public <T extends Datacenter> void setDatacenter(T datacenter) {
        this.datacenter = (ContainerDatacenter) datacenter;
    }

    public SelectionPolicy<PowerGuestEntity> getContainerSelectionPolicy() {
        return containerSelectionPolicy;
    }

    public void setContainerSelectionPolicy(SelectionPolicy<PowerGuestEntity> containerSelectionPolicy) {
        this.containerSelectionPolicy = containerSelectionPolicy;
    }

}
