/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2024, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.container.resourceAllocatorMigrationEnabled;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.container.core.*;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.selectionPolicies.SelectionPolicy;
import org.cloudbus.cloudsim.core.HostEntity;
import org.cloudbus.cloudsim.lists.HostList;
import org.cloudbus.cloudsim.power.PowerHost;

import java.util.*;

/**
 * Created by sareh on 17/11/15.
 * Modified by Remo Andreoli (Feb 2024)
 */
public class PowerContainerVmAllocationPolicyMigrationAbstractHostSelection extends PowerContainerVmAllocationPolicyMigrationAbstract {

    private SelectionPolicy<HostEntity> hostSelectionPolicy;
    private double utilizationThreshold = 0.9;
    private double underUtilizationThreshold = 0.7;

    /**
     * Instantiates a new power vm allocation policy migration abstract.
     *
     * @param hostSelectionPolicy
     * @param hostList            the host list
     * @param vmSelectionPolicy   the vm selection policy
     */
    public PowerContainerVmAllocationPolicyMigrationAbstractHostSelection(List<? extends HostEntity> hostList, SelectionPolicy<GuestEntity> vmSelectionPolicy, SelectionPolicy<HostEntity> hostSelectionPolicy, double OlThreshold, double UlThreshold) {
        super(hostList, vmSelectionPolicy);
        setHostSelectionPolicy(hostSelectionPolicy);
        setUtilizationThreshold(OlThreshold);
        setUnderUtilizationThreshold(UlThreshold);
    }

    @Override
    /**
     * Find host for vm.
     *
     * @param vm            the vm
     * @param excludedHosts the excluded hosts
     * @return the power host
     */
    public PowerHost findHostForGuest(GuestEntity vm, Set<? extends HostEntity> excludedHosts) {
        PowerHost allocatedHost = null;
        boolean find = false;
        Set<HostEntity> excludedHost1 = new HashSet<>(excludedHosts);
        while (!find) {
            HostEntity host = getHostSelectionPolicy().select(getHostList(), vm, excludedHost1);
            if (host == null) {
                return allocatedHost;
            }
            if (host.isSuitableForGuest(vm)) {
                find = true;
                allocatedHost = (PowerHost) host;
            } else {
                excludedHost1.add(host);
                if (getHostList().size() == excludedHost1.size()) {

                    return null;

                }
            }

        }
        return allocatedHost;
    }


    public SelectionPolicy<HostEntity> getHostSelectionPolicy() {
        return hostSelectionPolicy;
    }

    public void setHostSelectionPolicy(SelectionPolicy<HostEntity> hostSelectionPolicy) {
        this.hostSelectionPolicy = hostSelectionPolicy;
    }


    /**
     * Checks if is host over utilized.
     *
     * @param host the _host
     * @return true, if is host over utilized
     */
    @Override
    protected boolean isHostOverUtilized(PowerHost host) {
        addHistoryEntry(host, getUtilizationThreshold());
        double totalRequestedMips = 0;
        for (ContainerVm vm : host.<ContainerVm>getGuestList()) {
            totalRequestedMips += vm.getCurrentRequestedTotalMips();
        }
        double utilization = totalRequestedMips / host.getTotalMips();
        return utilization > getUtilizationThreshold();
    }

    @Override
    protected boolean isHostUnderUtilized(PowerHost host) {
        return false;
    }

    /**
     * Sets the utilization threshold.
     *
     * @param utilizationThreshold the new utilization threshold
     */
    protected void setUtilizationThreshold(double utilizationThreshold) {
        this.utilizationThreshold = utilizationThreshold;
    }

    /**
     * Gets the utilization threshold.
     *
     * @return the utilization threshold
     */
    protected double getUtilizationThreshold() {
        return utilizationThreshold;
    }

    public double getUnderUtilizationThreshold() {
        return underUtilizationThreshold;
    }

    public void setUnderUtilizationThreshold(double underUtilizationThreshold) {
        this.underUtilizationThreshold = underUtilizationThreshold;
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

        return (PowerHost) underUtilizedHostList.get(0);
    }


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
            if (!areAllVmsMigratingOutOrAnyVmMigratingIn(host) && utilization < getUnderUtilizationThreshold() && !areAllContainersMigratingOutOrAnyContainersMigratingIn(host)) {
                underUtilizedHostList.add(host);
            }
        }
        return underUtilizedHostList;
    }
}
