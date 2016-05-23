package org.cloudbus.cloudsim.container.schedulers;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.ResCloudlet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sareh on 10/07/15.
 */
public abstract class ContainerCloudletScheduler {
        /** The previous time. */
        private double previousTime;

        /** The current mips share. */
        private List<Double> currentMipsShare;

        /** The cloudlet waiting list. */
        protected List<? extends ResCloudlet> cloudletWaitingList;

        /** The cloudlet exec list. */
        protected List<? extends ResCloudlet> cloudletExecList;

        /** The cloudlet paused list. */
        protected List<? extends ResCloudlet> cloudletPausedList;

        /** The cloudlet finished list. */
        protected List<? extends ResCloudlet> cloudletFinishedList;

        /** The cloudlet failed list. */
        protected List<? extends ResCloudlet> cloudletFailedList;

        /**
         * Creates a new CloudletScheduler object. This method must be invoked before starting the
         * actual simulation.
         *
         * @pre $none
         * @post $none
         */
        public ContainerCloudletScheduler() {
            setPreviousTime(0.0);
            cloudletWaitingList = new ArrayList<ResCloudlet>();
            cloudletExecList = new ArrayList<ResCloudlet>();
            cloudletPausedList = new ArrayList<ResCloudlet>();
            cloudletFinishedList = new ArrayList<ResCloudlet>();
            cloudletFailedList = new ArrayList<ResCloudlet>();
        }

        /**
         * Updates the processing of cloudlets running under management of this scheduler.
         *
         * @param currentTime current simulation time
         * @param mipsShare array with MIPS share of each processor available to the scheduler
         * @return time predicted completion time of the earliest finishing cloudlet, or 0 if there is no
         *         next events
         * @pre currentTime >= 0
         * @post $none
         */
        public abstract double updateContainerProcessing(double currentTime, List<Double> mipsShare);

        /**
         * Receives an cloudlet to be executed in the VM managed by this scheduler.
         *
         * @param gl the submited cloudlet
         * @param fileTransferTime time required to move the required files from the SAN to the VM
         * @return expected finish time of this cloudlet, or 0 if it is in a waiting queue
         * @pre gl != null
         * @post $none
         */
        public abstract double cloudletSubmit(Cloudlet gl, double fileTransferTime);

        /**
         * Receives an cloudlet to be executed in the VM managed by this scheduler.
         *
         * @param gl the submited cloudlet
         * @return expected finish time of this cloudlet, or 0 if it is in a waiting queue
         * @pre gl != null
         * @post $none
         */
        public abstract double cloudletSubmit(Cloudlet gl);

        /**
         * Cancels execution of a cloudlet.
         *
         * @param clId ID of the cloudlet being cancealed
         * @return the canceled cloudlet, $null if not found
         * @pre $none
         * @post $none
         */
        public abstract Cloudlet cloudletCancel(int clId);

        /**
         * Pauses execution of a cloudlet.
         *
         * @param clId ID of the cloudlet being paused
         * @return $true if cloudlet paused, $false otherwise
         * @pre $none
         * @post $none
         */
        public abstract boolean cloudletPause(int clId);

        /**
         * Resumes execution of a paused cloudlet.
         *
         * @param clId ID of the cloudlet being resumed
         * @return expected finish time of the cloudlet, 0.0 if queued
         * @pre $none
         * @post $none
         */
        public abstract double cloudletResume(int clId);

        /**
         * Processes a finished cloudlet.
         *
         * @param rcl finished cloudlet
         * @pre rgl != $null
         * @post $none
         */
        public abstract void cloudletFinish(ResCloudlet rcl);

        /**
         * Gets the status of a cloudlet.
         *
         * @param clId ID of the cloudlet
         * @return status of the cloudlet, -1 if cloudlet not found
         * @pre $none
         * @post $none
         */
        public abstract int getCloudletStatus(int clId);

        /**
         * Informs about completion of some cloudlet in the VM managed by this scheduler.
         *
         * @return $true if there is at least one finished cloudlet; $false otherwise
         * @pre $none
         * @post $none
         */
        public abstract boolean isFinishedCloudlets();

        /**
         * Returns the next cloudlet in the finished list, $null if this list is empty.
         *
         * @return a finished cloudlet
         * @pre $none
         * @post $none
         */
        public abstract Cloudlet getNextFinishedCloudlet();

        /**
         * Returns the number of cloudlets runnning in the virtual machine.
         *
         * @return number of cloudlets runnning
         * @pre $none
         * @post $none
         */
        public abstract int runningCloudlets();

        /**
         * Returns one cloudlet to migrate to another vm.
         *
         * @return one running cloudlet
         * @pre $none
         * @post $none
         */
        public abstract Cloudlet migrateCloudlet();

        /**
         * Get utilization created by all cloudlets.
         *
         * @param time the time
         * @return total utilization
         */
        public abstract double getTotalUtilizationOfCpu(double time);

        /**
         * Gets the current requested mips.
         *
         * @return the current mips
         */
        public abstract List<Double> getCurrentRequestedMips();

        /**
         * Gets the total current mips for the Cloudlet.
         *
         * @param rcl the rcl
         * @param mipsShare the mips share
         * @return the total current mips
         */
        public abstract double getTotalCurrentAvailableMipsForCloudlet(ResCloudlet rcl, List<Double> mipsShare);

        /**
         * Gets the total current requested mips for cloudlet.
         *
         * @param rcl the rcl
         * @param time the time
         * @return the total current requested mips for cloudlet
         */
        public abstract double getTotalCurrentRequestedMipsForCloudlet(ResCloudlet rcl, double time);

        /**
         * Gets the total current allocated mips for cloudlet.
         *
         * @param rcl the rcl
         * @param time the time
         * @return the total current allocated mips for cloudlet
         */
        public abstract double getTotalCurrentAllocatedMipsForCloudlet(ResCloudlet rcl, double time);

        /**
         * Gets the current requested ram.
         *
         * @return the current requested ram
         */
        public abstract double getCurrentRequestedUtilizationOfRam();

        /**
         * Gets the current requested bw.
         *
         * @return the current requested bw
         */
        public abstract double getCurrentRequestedUtilizationOfBw();

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
        protected void setPreviousTime(double previousTime) {
            this.previousTime = previousTime;
        }

        /**
         * Sets the current mips share.
         *
         * @param currentMipsShare the new current mips share
         */
        protected void setCurrentMipsShare(List<Double> currentMipsShare) {
            this.currentMipsShare = currentMipsShare;
        }

        /**
         * Gets the current mips share.
         *
         * @return the current mips share
         */
        public List<Double> getCurrentMipsShare() {
            return currentMipsShare;
        }

        /**
         * Gets the cloudlet waiting list.
         *
         * @param <T> the generic type
         * @return the cloudlet waiting list
         */
        @SuppressWarnings("unchecked")
        public <T extends ResCloudlet> List<T> getCloudletWaitingList() {
            return (List<T>) cloudletWaitingList;
        }

        /**
         * Cloudlet waiting list.
         *
         * @param <T> the generic type
         * @param cloudletWaitingList the cloudlet waiting list
         */
        protected <T extends ResCloudlet> void setCloudletWaitingList(List<T> cloudletWaitingList) {
            this.cloudletWaitingList = cloudletWaitingList;
        }

        /**
         * Gets the cloudlet exec list.
         *
         * @param <T> the generic type
         * @return the cloudlet exec list
         */
        @SuppressWarnings("unchecked")
        public <T extends ResCloudlet> List<T> getCloudletExecList() {
            return (List<T>) cloudletExecList;
        }

        /**
         * Sets the cloudlet exec list.
         *
         * @param <T> the generic type
         * @param cloudletExecList the new cloudlet exec list
         */
        protected <T extends ResCloudlet> void setCloudletExecList(List<T> cloudletExecList) {
            this.cloudletExecList = cloudletExecList;
        }

        /**
         * Gets the cloudlet paused list.
         *
         * @param <T> the generic type
         * @return the cloudlet paused list
         */
        @SuppressWarnings("unchecked")
        public <T extends ResCloudlet> List<T> getCloudletPausedList() {
            return (List<T>) cloudletPausedList;
        }

        /**
         * Sets the cloudlet paused list.
         *
         * @param <T> the generic type
         * @param cloudletPausedList the new cloudlet paused list
         */
        protected <T extends ResCloudlet> void setCloudletPausedList(List<T> cloudletPausedList) {
            this.cloudletPausedList = cloudletPausedList;
        }

        /**
         * Gets the cloudlet finished list.
         *
         * @param <T> the generic type
         * @return the cloudlet finished list
         */
        @SuppressWarnings("unchecked")
        public <T extends ResCloudlet> List<T> getCloudletFinishedList() {
            return (List<T>) cloudletFinishedList;
        }

        /**
         * Sets the cloudlet finished list.
         *
         * @param <T> the generic type
         * @param cloudletFinishedList the new cloudlet finished list
         */
        protected <T extends ResCloudlet> void setCloudletFinishedList(List<T> cloudletFinishedList) {
            this.cloudletFinishedList = cloudletFinishedList;
        }

        /**
         * Gets the cloudlet failed list.
         *
         * @param <T> the generic type
         * @return the cloudlet failed list.
         */
        @SuppressWarnings("unchecked")
        public <T extends ResCloudlet> List<T>  getCloudletFailedList() {
            return (List<T>) cloudletFailedList;
        }

        /**
         * Sets the cloudlet failed list.
         *
         * @param <T> the generic type
         * @param cloudletFailedList the new cloudlet failed list.
         */
        protected <T extends ResCloudlet> void setCloudletFailedList(List<T> cloudletFailedList) {
            this.cloudletFailedList = cloudletFailedList;
        }

    }


