/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2024, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.container.core;

import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.core.PowerGuestEntity;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;
import org.cloudbus.cloudsim.util.HistoryStat;

import java.util.List;

/**
 * Created by sareh on 14/07/15.
 * Modified by Remo Andreoli (March 2024)
 */
public class PowerContainerVm extends ContainerVm implements PowerGuestEntity {
    /**
     * The utilization history.
     */
    private final HistoryStat utilizationHistoryStat = new HistoryStat(PowerGuestEntity.HISTORY_LENGTH);

    /**
     * The previous time.
     */
    private double previousTime;

    /**
     * The scheduling interval.
     */
    private double schedulingInterval;

    /**
     * Instantiates a new power vm.
     *
     * @param id                 the id
     * @param userId             the user id
     * @param mips               the mips
     * @param ram                the ram
     * @param bw                 the bw
     * @param size               the size
     * @param vmm                the vmm
     * @param containerScheduler the cloudlet scheduler
     * @param schedulingInterval the scheduling interval
     */
    public PowerContainerVm(
            final int id,
            final int userId,
            final double mips,
            final int ram,
            final long bw,
            final long size,
            final String vmm,
            final VmScheduler containerScheduler,
            final RamProvisioner containerRamProvisioner,
            final BwProvisioner containerBwProvisioner,
            List<? extends Pe> peList,
            final double schedulingInterval) {
        super(id, userId, mips, ram, bw, size, vmm, containerScheduler, containerRamProvisioner, containerBwProvisioner, peList);
        setSchedulingInterval(schedulingInterval);
    }

    /**
     * Updates the processing of cloudlets running on this VM.
     *
     * @param currentTime current simulation time
     * @param mipsShare   array with MIPS share of each Pe available to the scheduler
     * @return time predicted completion time of the earliest finishing cloudlet, or 0 if there is
     * no next events
     * @pre currentTime >= 0
     * @post $none
     */
    @Override
    public double updateCloudletsProcessing(final double currentTime, final List<Double> mipsShare) {
        double time = super.updateCloudletsProcessing(currentTime, mipsShare);
        if (currentTime - getPreviousTime() >= getSchedulingInterval()) {
            double utilization = 0;

            for (GuestEntity container : getGuestList()) {
                // The containers which are going to migrate to the vm shouldn't be added to the utilization
                if(!getGuestsMigratingIn().contains(container)) {
                    time = container.getCloudletScheduler().getPreviousTime();
                    utilization += container.getTotalUtilizationOfCpu(time);
                }
                }

            if (CloudSim.clock() != 0 || utilization != 0) {
                addUtilizationHistoryValue(utilization);
            }
            setPreviousTime(currentTime);
        }
        return time;
    }

    /**
     * Gets the utilization history.
     *
     * @return the utilization history
     */
    public HistoryStat getUtilizationHistory() {
        return utilizationHistoryStat;
    }

    /**
     * Gets the previous time.
     *
     * @return the previous time
     */
    public double getPreviousTime() {
        return previousTime;
    }

    /**
     * Sets the previous time.
     *
     * @param previousTime the new previous time
     */
    public void setPreviousTime(final double previousTime) {
        this.previousTime = previousTime;
    }

    /**
     * Gets the scheduling interval.
     *
     * @return the schedulingInterval
     */
    public double getSchedulingInterval() {
        return schedulingInterval;
    }

    /**
     * Sets the scheduling interval.
     *
     * @param schedulingInterval the schedulingInterval to set
     */
    protected void setSchedulingInterval(final double schedulingInterval) {
        this.schedulingInterval = schedulingInterval;
    }
}