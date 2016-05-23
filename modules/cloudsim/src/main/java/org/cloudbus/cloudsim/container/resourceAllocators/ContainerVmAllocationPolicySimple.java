package org.cloudbus.cloudsim.container.resourceAllocators;

import org.cloudbus.cloudsim.container.core.ContainerDatacenter;
import org.cloudbus.cloudsim.container.core.ContainerHost;
import org.cloudbus.cloudsim.container.core.ContainerVm;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sareh on 14/07/15.
 */
public class ContainerVmAllocationPolicySimple extends ContainerVmAllocationPolicy {

    /**
     * The vm table.
     */
    private Map<String, ContainerHost> vmTable;

    /**
     * The used pes.
     */
    private Map<String, Integer> usedPes;

    /**
     * The free pes.
     */
    private List<Integer> freePes;

    /**
     * Creates the new VmAllocationPolicySimple object.
     *
     * @param list the list
     * @pre $none
     * @post $none
     */
    public ContainerVmAllocationPolicySimple(List<? extends ContainerHost> list) {
        super(list);

        setFreePes(new ArrayList<Integer>());
        for (ContainerHost host : getContainerHostList()) {
            getFreePes().add(host.getNumberOfPes());

        }

        setVmTable(new HashMap<String, ContainerHost>());
        setUsedPes(new HashMap<String, Integer>());
    }

    @Override
    public boolean allocateHostForVm(ContainerVm containerVm) {
        int requiredPes = containerVm.getNumberOfPes();
        boolean result = false;
        int tries = 0;
        List<Integer> freePesTmp = new ArrayList<>();
        for (Integer freePes : getFreePes()) {
            freePesTmp.add(freePes);
        }

        if (!getVmTable().containsKey(containerVm.getUid())) { // if this vm was not created
            do {// we still trying until we find a host or until we try all of them
                int moreFree = Integer.MIN_VALUE;
                int idx = -1;

                // we want the host with less pes in use
                for (int i = 0; i < freePesTmp.size(); i++) {
                    if (freePesTmp.get(i) > moreFree) {
                        moreFree = freePesTmp.get(i);
                        idx = i;
                    }
                }

                ContainerHost host = getContainerHostList().get(idx);
                result = host.containerVmCreate(containerVm);

                if (result) { // if vm were succesfully created in the host
                    getVmTable().put(containerVm.getUid(), host);
                    getUsedPes().put(containerVm.getUid(), requiredPes);
                    getFreePes().set(idx, getFreePes().get(idx) - requiredPes);
                    result = true;
                    break;
                } else {
                    freePesTmp.set(idx, Integer.MIN_VALUE);
                }
                tries++;
            } while (!result && tries < getFreePes().size());

        }

        freePesTmp.clear();
        return result;
    }

    @Override
    public boolean allocateHostForVm(ContainerVm containerVm, ContainerHost host) {
        if (host.containerVmCreate(containerVm)) { // if vm has been succesfully created in the host
            getVmTable().put(containerVm.getUid(), host);

            int requiredPes = containerVm.getNumberOfPes();
            int idx = getContainerHostList().indexOf(host);
            getUsedPes().put(containerVm.getUid(), requiredPes);
            getFreePes().set(idx, getFreePes().get(idx) - requiredPes);

            Log.formatLine(
                    "%.2f: VM #" + containerVm.getId() + " has been allocated to the host #" + host.getId(),
                    CloudSim.clock());
            return true;
        }

        return false;
    }


    @Override
    public List<Map<String, Object>> optimizeAllocation(List<? extends ContainerVm> vmList) {
        return null;
    }

    @Override
    public void deallocateHostForVm(ContainerVm containerVm) {
        ContainerHost host = getVmTable().remove(containerVm.getUid());
        int idx = getContainerHostList().indexOf(host);
        int pes = getUsedPes().remove(containerVm.getUid());
        if (host != null) {
            host.containerVmDestroy(containerVm);
            getFreePes().set(idx, getFreePes().get(idx) + pes);
        }
    }

    @Override
    public ContainerHost getHost(ContainerVm containerVm) {
        return getVmTable().get(containerVm.getUid());
    }

    @Override
    public ContainerHost getHost(int vmId, int userId) {
        return getVmTable().get(ContainerVm.getUid(userId, vmId));
    }

    @Override
    public void setDatacenter(ContainerDatacenter datacenter) {

    }


    public Map<String, ContainerHost> getVmTable() {
        return vmTable;
    }

    public void setVmTable(Map<String, ContainerHost> vmTable) {
        this.vmTable = vmTable;
    }

    public Map<String, Integer> getUsedPes() {
        return usedPes;
    }

    public void setUsedPes(Map<String, Integer> usedPes) {
        this.usedPes = usedPes;
    }

    public List<Integer> getFreePes() {
        return freePes;
    }

    public void setFreePes(List<Integer> freePes) {
        this.freePes = freePes;
    }
}
