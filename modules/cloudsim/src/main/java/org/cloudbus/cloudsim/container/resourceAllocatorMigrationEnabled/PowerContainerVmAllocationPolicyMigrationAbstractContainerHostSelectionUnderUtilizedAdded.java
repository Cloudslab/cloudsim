/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2024, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.container.resourceAllocatorMigrationEnabled;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.core.HostEntity;
import org.cloudbus.cloudsim.core.PowerGuestEntity;
import org.cloudbus.cloudsim.selectionPolicies.SelectionPolicy;
import org.cloudbus.cloudsim.lists.HostList;
import org.cloudbus.cloudsim.power.PowerHost;

import java.util.*;

/**
 * Created by sareh on 13/08/15.
 * Modified by Remo Andreoli (Feb 2024)
 */
public abstract class PowerContainerVmAllocationPolicyMigrationAbstractContainerHostSelectionUnderUtilizedAdded extends PowerContainerVmAllocationPolicyMigrationAbstractContainerHostSelection {

    private double underUtilizationThr;

    public PowerContainerVmAllocationPolicyMigrationAbstractContainerHostSelectionUnderUtilizedAdded(
            List<? extends HostEntity> hostList, SelectionPolicy<GuestEntity> vmSelectionPolicy,
            SelectionPolicy<PowerGuestEntity> containerSelectionPolicy, SelectionPolicy<HostEntity> hostSelectionPolicy,
            double underUtilizationThr,
            int numberOfVmTypes, int[] vmPes, int[] vmRam, long vmBw, long vmSize, double[] vmMips) {
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
    protected PowerHost getUnderUtilizedHost(Set<? extends Host> excludedHosts) {

        List<Host> underUtilizedHostList = getUnderUtilizedHostList(excludedHosts);
        if (underUtilizedHostList.isEmpty()) {

            return null;
        }
        HostList.sortByCpuUtilizationDescending(underUtilizedHostList);
//        Log.print(String.format("The under Utilized Hosts are %d", underUtilizedHostList.size()));

        return (PowerHost) underUtilizedHostList.getFirst();
    }

    @Override
    /**
     * Gets the under utilized host.
     *
     * @param excludedHosts the excluded hosts
     * @return the under utilized host
     */
    protected List<Host> getUnderUtilizedHostList(Set<? extends Host> excludedHosts) {
        List<Host> underUtilizedHostList = new ArrayList<>();
        for (PowerHost host : this.<PowerHost>getHostList()) {
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
