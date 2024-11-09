/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2024, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.container.core;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.PowerGuestEntity;
import org.cloudbus.cloudsim.util.HistoryStat;

import java.util.List;

/**
 * Created by sareh on 23/07/15.
 */
public class PowerContainer extends Container implements PowerGuestEntity {
        /** The utilization history. */
        private final HistoryStat utilizationHistoryStat = new HistoryStat(PowerGuestEntity.HISTORY_LENGTH);

        /** The previous time. */
        private double previousTime;

        /** The scheduling interval to update the processing of cloudlets running in this VM. */
        private double schedulingInterval;

        /**
         * Instantiates a new power vm.
         *
         * @param id the id
         * @param userId the user id
         * @param mips the mips
         * @param pesNumber the pes number
         * @param ram the ram
         * @param bw the bw
         * @param size the size
         * @param vmm the vmm
         * @param cloudletScheduler the cloudlet scheduler
         * @param schedulingInterval the scheduling interval
         */
        public PowerContainer(
                final int id,
                final int userId,
                final double mips,
                final int pesNumber,
                final int ram,
                final long bw,
                final long size,
                final String vmm,
                final CloudletScheduler cloudletScheduler,
                final double schedulingInterval) {
            super(id, userId, mips, pesNumber, ram, bw, size, vmm, cloudletScheduler);
            this.schedulingInterval = schedulingInterval;
        }

        /**
         * Updates the processing of guest entities running on this VM.
         *
         * @param currentTime current simulation time
         * @param mipsShare array with MIPS share of each Pe available to the scheduler
         *
         * @return time predicted completion time of the earliest finishing cloudlet, or 0 if there is
         * 		no next events
         *
         * @pre currentTime >= 0
         * @post $none
         */
        @Override
        public double updateCloudletsProcessing(final double currentTime, final List<Double> mipsShare) {
            double time = super.updateCloudletsProcessing(currentTime, mipsShare);
            if (currentTime - getPreviousTime() >= getSchedulingInterval()) {
                double utilization = getTotalUtilizationOfCpu(getCloudletScheduler().getPreviousTime());
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

    public double getSchedulingInterval() { return schedulingInterval; }
    protected void setSchedulingInterval(double schedulingInterval) { this.schedulingInterval = schedulingInterval; }
    }
