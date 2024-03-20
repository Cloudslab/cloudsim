package org.cloudbus.cloudsim.container.resourceAllocatorMigrationEnabled;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.container.containerProvisioners.ContainerRamProvisionerSimple;
import org.cloudbus.cloudsim.container.containerSelectionPolicies.PowerContainerSelectionPolicy;
import org.cloudbus.cloudsim.container.core.*;
import org.cloudbus.cloudsim.container.lists.*;
import org.cloudbus.cloudsim.container.resourceAllocators.PowerContainerAllocationPolicy;
import org.cloudbus.cloudsim.container.schedulers.ContainerSchedulerTimeSharedOverSubscription;
import org.cloudbus.cloudsim.container.utils.IDs;
import org.cloudbus.cloudsim.container.utils.RandomGen;
import org.cloudbus.cloudsim.container.vmSelectionPolicies.PowerContainerVmSelectionPolicy;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.core.HostEntity;
import org.cloudbus.cloudsim.lists.HostList;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerHostUtilizationHistory;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
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
    private PowerContainerSelectionPolicy containerSelectionPolicy;
    protected int numberOfVmTypes;
    protected int[] vmPes;
    protected int[] vmRam;
    protected long vmBw;
    protected long vmSize;
    protected double[] vmMips;

    public PowerContainerVmAllocationPolicyMigrationAbstractContainerAdded(List<? extends Host> hostList,
    		PowerContainerVmSelectionPolicy vmSelectionPolicy, PowerContainerSelectionPolicy containerSelectionPolicy,
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
    public List<Map<String, Object>> optimizeAllocation(List<? extends GuestEntity> vmList) {

        ExecutionTimeMeasurer.start("optimizeAllocationTotal");

        ExecutionTimeMeasurer.start("optimizeAllocationHostSelection");
        List<PowerHostUtilizationHistory> overUtilizedHosts = getOverUtilizedHosts();
        getExecutionTimeHistoryHostSelection().add(
                ExecutionTimeMeasurer.end("optimizeAllocationHostSelection"));

        printOverUtilizedHosts(overUtilizedHosts);

        saveAllocation();

        ExecutionTimeMeasurer.start("optimizeAllocationContainerSelection");
        List<? extends Container> containersToMigrate = getContainersToMigrateFromHosts(overUtilizedHosts);
        getExecutionTimeHistoryVmSelection().add(ExecutionTimeMeasurer.end("optimizeAllocationContainerSelection"));

        Log.printLine("Reallocation of Containers from the over-utilized hosts:");
        ExecutionTimeMeasurer.start("optimizeAllocationVmReallocation");
        List<Map<String, Object>> migrationMap = getPlacementForLeftContainers(containersToMigrate, new HashSet<Host>(overUtilizedHosts));


        getExecutionTimeHistoryVmReallocation().add(
                ExecutionTimeMeasurer.end("optimizeAllocationVmReallocation"));
        Log.printLine();

        migrationMap.addAll(getContainerMigrationMapFromUnderUtilizedHosts(overUtilizedHosts, migrationMap));

        restoreAllocation();

        getExecutionTimeHistoryTotal().add(ExecutionTimeMeasurer.end("optimizeAllocationTotal"));

        return migrationMap;


    }

    protected Collection<? extends Map<String, Object>> getContainerMigrationMapFromUnderUtilizedHosts(List<PowerHostUtilizationHistory> overUtilizedHosts, List<Map<String, Object>> previouseMap) {

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

            Log.printConcatLine("Under-utilized host: host #", underUtilizedHost.getId(), "\n");

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
            Log.printLine();

            List<Map<String, Object>> newVmPlacement = getNewVmPlacementFromUnderUtilizedHost(
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
            Log.printLine();
        }

        excludedHostsForFindingUnderUtilizedHost.clear();
        excludedHostsForFindingNewContainerPlacement.clear();
        return migrationMap;
    }

    private List<? extends Container> getContainersToMigrateFromHosts(List<PowerHostUtilizationHistory> overUtilizedHosts) {
        List<Container> containersToMigrate = new LinkedList<>();
        for (PowerHostUtilizationHistory host : overUtilizedHosts) {
            while (true) {
                Container container = getContainerSelectionPolicy().getContainerToMigrate(host);
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


    private List<Map<String, Object>> getNewContainerPlacement(List<? extends Container> containersToMigrate, Set<? extends Host> excludedHosts) {

        List<Map<String, Object>> migrationMap = new LinkedList<>();

        PowerContainerList.sortByCpuUtilization(containersToMigrate);
        for (Container container : containersToMigrate) {
            Map<String, Object> allocationMap = findHostForGuest(container, excludedHosts, false);

            if (allocationMap.get("host") != null && allocationMap.get("vm") != null) {
                ContainerVm vm = (ContainerVm) allocationMap.get("vm");
                Log.printConcatLine("Container #", container.getId(), " allocated to host #", ((PowerHost) allocationMap.get("host")).getId(), "The VM ID is #", vm.getId());
                Map<String, Object> migrate = new HashMap<>();
                migrate.put("container", container);
                migrate.put("vm", vm);
                migrate.put("host", allocationMap.get("host"));
                migrationMap.add(migrate);
            } else {
                Map<String, Object> migrate = new HashMap<>();
                migrate.put("NewVmRequired", container);
                migrationMap.add(migrate);

            }

        }
        containersToMigrate.clear();
        return migrationMap;
    }

    private List<Map<String, Object>> getPlacementForLeftContainers(List<? extends Container> containersToMigrate, Set<? extends Host> excludedHostsList) {
        List<Map<String, Object>> newMigrationMap = new LinkedList<>();

        if (containersToMigrate.size() == 0) {
            return newMigrationMap;
        }
        HashSet<Host> excludedHostsforOverUtilized = new HashSet<>();
        excludedHostsforOverUtilized.addAll(getSwitchedOffHosts());
        excludedHostsforOverUtilized.addAll(excludedHostsList);
        List<Map<String, Object>> migrationMap = getNewContainerPlacement(containersToMigrate, excludedHostsforOverUtilized);
        if (migrationMap.size() == 0) {
            return migrationMap;
        }
//        List<Container> containerList = getExtraContainers(migrationMap);


        List<Container> containerList = new ArrayList<>();
        for (Map<String, Object> map : migrationMap) {
            if (map.containsKey("NewVmRequired")) {
                containerList.add((Container) map.get("NewVmRequired"));

            } else {
                newMigrationMap.add(map);
            }

        }
        if (containerList.size() == 0) {
            return newMigrationMap;
        }

        List<Host> underUtilizedHostList = getUnderUtilizedHostList(excludedHostsList);

        List<Map<String, Object>> migrationMapUnderUtilized = findMapInUnderUtilizedHosts(underUtilizedHostList,containerList);
        newMigrationMap.addAll(migrationMapUnderUtilized);
        containerList.removeAll(getAssignedContainers(migrationMapUnderUtilized));
        if(containerList.size()!= 0){
            List<Map<String, Object>> migrationMapSwitchedOff= findMapInSwitchedOffHosts(containerList);
            newMigrationMap.addAll(migrationMapSwitchedOff);

        }


// Now Check if there are any containers left without VMs.
        //firsthost chosen


        return newMigrationMap;
    }
    protected List<Map<String, Object>> findMapInUnderUtilizedHosts(List<Host> underUtilizedHostList, List<Container> containerList){
        List<Map<String, Object>> newMigrationMap = new ArrayList<>();
        //        Create new Vms on underUtilized hosts;
        List<Map<String, Object>> createdVmMap = new ArrayList<>();
        if (underUtilizedHostList.size() != 0) {
            for (Host host : underUtilizedHostList) {
//                   We try to create the largest Vm possible
                List<ContainerVm> VmList = createVms(host, true);
                if(VmList.size() != 0){
                    for(ContainerVm vm:VmList){
                        Map<String, Object> map = new HashMap<>();
                        map.put("host",host);
                        map.put("vm",vm);
                        createdVmMap.add(map);

                    }}
            }
            if(createdVmMap.size() ==0){

                return newMigrationMap;

            }

            //        if there are any new Vms on the underUtilized Hosts we assign the containers to them first!
            // Sort the underUtilized host by the utilization, so that we first assign vms to the more utilized ones
            for (Container container : containerList) {
                Map<String, Object> allocationMap = findAvailableHostForContainer(container, createdVmMap);
                if (allocationMap.get("host") != null && allocationMap.get("vm") != null) {
                    ContainerVm vm = (ContainerVm) allocationMap.get("vm");
                    Log.printConcatLine("Container #", container.getId(), " allocated to host #", ((PowerHost) allocationMap.get("host")).getId(), "The VM ID is #", vm.getId());
                    Map<String, Object> migrate = new HashMap<>();
//                    vm.setInWaiting(true);
                    migrate.put("NewEventRequired", container);
                    migrate.put("container", container);
                    migrate.put("vm", vm);
                    migrate.put("host", allocationMap.get("host"));
                    newMigrationMap.add(migrate);

                }
            }
        }

      return newMigrationMap;

    }
    protected List<Container> getAssignedContainers(List<Map<String, Object>> migrationMap){
        List<Container> assignedContainers = new ArrayList<>();
        for(Map<String, Object> map:migrationMap){
            if(map.containsKey("container")){
                assignedContainers.add((Container) map.get("container"));
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
                Log.printLine("The vm ID #" + vm.getId() + "will be created ");
                vm.setInWaiting(vmStatus);
                return vm;
            }
        }

        return null;
    }

    protected List<Map<String, Object>> findMapInSwitchedOffHosts(List<Container> containerList) {
        Log.print(String.format(" %s :  Find Placement in the switched of hosts", CloudSim.clock()));
        List<PowerHost> switchedOffHostsList = getSwitchedOffHosts();
        List<Map<String, Object>> newMigrationMap = new ArrayList<>();

        if (containerList.size() == 0) {

            return newMigrationMap;
        }

        Host previouseHost = null;
        ContainerVm previouseVm = null;
        while (containerList.size() != 0) {
            if (switchedOffHostsList.size() == 0) {

                Log.print("There is no hosts to create VMs");
                break;
            }
            List<Container> assignedContainer = new ArrayList<>();
            //choose a random host
            if (previouseHost == null && previouseVm == null) {
                if(switchedOffHostsList.size() ==0 ){
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
                        Map<String, Object> migrate = new HashMap<>();
//                        previouseVm.setInWaiting(true);
                        migrate.put("NewEventRequired", container);
                        migrate.put("container", container);
                        migrate.put("vm", previouseVm);
                        migrate.put("host", previouseHost);
                        newMigrationMap.add(migrate);
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
                        Map<String, Object> migrate = new HashMap<>();
//                        previouseVm.setInWaiting(true);
                        migrate.put("NewEventRequired", container);
                        migrate.put("container", container);
                        migrate.put("vm", previouseVm);
                        migrate.put("host", previouseHost);
                        newMigrationMap.add(migrate);

                    }
                }

                containerList.removeAll(assignedContainer);
            } else {

                for (Container container : containerList) {
                    if (previouseVm.isSuitableForGuest(container)) {
                        previouseVm.guestCreate(container);
                        assignedContainer.add(container);
                        Map<String, Object> migrate = new HashMap<>();
//                        previouseVm.setInWaiting(true);
                        migrate.put("NewEventRequired", container);
                        migrate.put("container", container);
                        migrate.put("vm", previouseVm);
                        migrate.put("host", previouseHost);
                        newMigrationMap.add(migrate);
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
                        Map<String, Object> migrate = new HashMap<>();
//                        previouseVm.setInWaiting(true);
                        migrate.put("NewEventRequired", container);
                        migrate.put("container", container);
                        migrate.put("vm", previouseVm);
                        migrate.put("host", previouseHost);
                        newMigrationMap.add(migrate);

                    }
                }

                containerList.removeAll(assignedContainer);


            }
        }
        return newMigrationMap;


    }

    //    This method should be re written!
    protected Map<String, Object> findAvailableHostForContainer(Container
                                                                        container, List<Map<String, Object>> createdVm){

        double minPower = Double.MAX_VALUE;
        PowerHost allocatedHost = null;
        ContainerVm allocatedVm = null;
        List<Host> underUtilizedHostList = new ArrayList<>();
        List<ContainerVm> vmList = new ArrayList<>();
        for(Map<String, Object> map:createdVm){
            underUtilizedHostList.add((Host) map.get("host"));

        }
        HostList.sortByCpuUtilization(underUtilizedHostList);
        for (Host host1 : underUtilizedHostList) {

            PowerHost host = (PowerHost) host1;
            for(Map<String, Object> map:createdVm){
                if(map.get("host") == host1){
                vmList.add((ContainerVm) map.get("vm"));
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


        Map<String, Object> map = new HashMap<>();
        map.put("vm", allocatedVm);
        map.put("host", allocatedHost);

        return map;


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
        PowerContainerVm vm = new PowerContainerVm(IDs.pollId(ContainerVm.class), brokerId, vmMips[vmType],
                (int) vmRam[vmType],
                vmBw, vmSize, "Xen",
                new ContainerSchedulerTimeSharedOverSubscription(peList),
                new RamProvisionerSimple(vmRam[vmType]),
                new BwProvisionerSimple(vmBw), peList, 300);
        return vm;

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

    public Map<String, Object> findHostForGuest(GuestEntity container, Set<? extends HostEntity> excludedHosts, boolean checkForVM) {
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
        Map<String, Object> map = new HashMap<>();
        map.put("vm", allocatedVm);
        map.put("host", allocatedHost);

        return map;
    }

    protected boolean isVmOverUtilized(ContainerVm vm) {
        boolean isOverUtilized = true;
        double util = 0;
//        Log.printConcatLine("Checking if the vm is over utilized or not!");
        for (GuestEntity container : vm.getGuestList()) {
            util += container.getTotalUtilizationOfCpuMips(CloudSim.clock());
        }
        if (util > 1.0*vm.getHost().getTotalMips() / vm.getHost().getNumberOfPes() * vm.getNumberOfPes()) {
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
        double pePotentialUtilization = hostPotentialUtilizationMips / host.getTotalMips();
        return pePotentialUtilization;
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
                    Map<String, Object> map = new HashMap<>();
                    map.put("host", host);
                    map.put("vm", vm);
                    map.put("container", container);
                    getSavedAllocation().add(map);
                }
            }
        }
        Log.printLine(String.format("The length of the saved map is ....%d", getSavedAllocation().size()));

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
        for (Map<String, Object> map : getSavedAllocation()) {
            PowerContainerVm vm = (PowerContainerVm) map.get("vm");

            PowerHost host = (PowerHost) map.get("host");
            if (!host.getGuestList().contains(vm)) {
                if (!host.guestCreate(vm)) {
                    Log.printConcatLine("Couldn't restore VM #", vm.getId(), " on host #", host.getId());
                    System.exit(0);
                }

                getVmTable().put(vm.getUid(), host);
            }
//            vm.containerDestroyAll();
//            vm.reallocateMigratingInContainers();
        }
//        List<ContainerVm > restoredVms = new ArrayList<>();
        for (Map<String, Object> map : getSavedAllocation()) {
            PowerContainerVm vm = (PowerContainerVm) map.get("vm");
            if (map.get("container") != null && map.containsKey("container")) {
                Container container = (Container) map.get("container");
//                Log.print(container);

                if (!vm.getGuestList().contains(container)) {
                    if (!vm.guestCreate(container)) {
                        Log.printConcatLine("Couldn't restore Container #", container.getId(), " on vm #", vm.getId());
                        System.exit(0);
                    }
                } else {

                    Log.print("The Container is in the VM already");
                }

                if (container.getHost() == null) {
                    Log.print("The Vm is null");

                }
                ((PowerContainerAllocationPolicy) getDatacenter().getContainerAllocationPolicy()).
                        getContainerTable().put(container.getUid(), vm);
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

    public void setDatacenter(ContainerDatacenter datacenter) {
        this.datacenter = datacenter;
    }

    public PowerContainerSelectionPolicy getContainerSelectionPolicy() {
        return containerSelectionPolicy;
    }

    public void setContainerSelectionPolicy(PowerContainerSelectionPolicy containerSelectionPolicy) {
        this.containerSelectionPolicy = containerSelectionPolicy;
    }

}
