package org.cloudbus.cloudsim.container.resourceAllocatorMigrationEnabled;

import org.cloudbus.cloudsim.container.core.ContainerHost;
import org.cloudbus.cloudsim.container.core.ContainerHostList;
import org.cloudbus.cloudsim.container.core.PowerContainerHost;
import org.cloudbus.cloudsim.container.containerSelectionPolicies.PowerContainerSelectionPolicy;
import org.cloudbus.cloudsim.container.hostSelectionPolicies.HostSelectionPolicy;
import org.cloudbus.cloudsim.container.vmSelectionPolicies.PowerContainerVmSelectionPolicy;

import java.util.*;

/**
 * Created by sareh on 13/08/15.
 */
public abstract class PowerContainerVmAllocationPolicyMigrationAbstractContainerHostSelectionUnderUtilizedAdded extends PowerContainerVmAllocationPolicyMigrationAbstractContainerHostSelection {

    private double underUtilizationThr;

    public PowerContainerVmAllocationPolicyMigrationAbstractContainerHostSelectionUnderUtilizedAdded(
            List<? extends ContainerHost> hostList, PowerContainerVmSelectionPolicy vmSelectionPolicy,
            PowerContainerSelectionPolicy containerSelectionPolicy, HostSelectionPolicy hostSelectionPolicy,
            double underUtilizationThr,
            int numberOfVmTypes, int[] vmPes, float[] vmRam, long vmBw, long vmSize, double[] vmMips) {
        super(hostList, vmSelectionPolicy, containerSelectionPolicy, hostSelectionPolicy,
        		 numberOfVmTypes, vmPes, vmRam, vmBw, vmSize, vmMips);
        setUnderUtilizationThr(underUtilizationThr);
    }


    @Override
    /**
     * Gets the under utilized host.
     *Checks if the utilization is under the threshold then counts it as underUtilized :)
     * @param excludedHosts the excluded hosts
     * @return the under utilized host
     */
    protected PowerContainerHost getUnderUtilizedHost(Set<? extends ContainerHost> excludedHosts) {

        List<ContainerHost> underUtilizedHostList = getUnderUtilizedHostList(excludedHosts);
        if (underUtilizedHostList.size() == 0) {

            return null;
        }
        ContainerHostList.sortByCpuUtilizationDescending(underUtilizedHostList);
//        Log.print(String.format("The under Utilized Hosts are %d", underUtilizedHostList.size()));
        PowerContainerHost underUtilizedHost = (PowerContainerHost) underUtilizedHostList.get(0);

        return underUtilizedHost;
    }

    @Override
    /**
     * Gets the under utilized host.
     *
     * @param excludedHosts the excluded hosts
     * @return the under utilized host
     */
    protected List<ContainerHost> getUnderUtilizedHostList(Set<? extends ContainerHost> excludedHosts) {
        List<ContainerHost> underUtilizedHostList = new ArrayList<>();
        for (PowerContainerHost host : this.<PowerContainerHost>getContainerHostList()) {
            if (excludedHosts.contains(host)) {
                continue;
            }
            double utilization = host.getUtilizationOfCpu();
            if (!areAllVmsMigratingOutOrAnyVmMigratingIn(host) && utilization < getUnderUtilizationThr() && !areAllContainersMigratingOutOrAnyContainersMigratingIn(host)) {
                underUtilizedHostList.add(host);
            }
        }
        return underUtilizedHostList;
    }

    public double getUnderUtilizationThr() {
        return underUtilizationThr;
    }

    public void setUnderUtilizationThr(double underUtilizationThr) {
        this.underUtilizationThr = underUtilizationThr;
    }
}
