package org.cloudbus.cloudsim.core;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.Host;

import java.util.List;

/**
 * An interface for implementing Guest entities. A Guest entity runs inside a Host, which is shared among several guests.
 * It processes cloudlets according to a policy, defined by the CloudletScheduler. Each guest has a owner, which
 * can submit cloudlets to the guests to execute them.
 *
 * @author Remo Andreoli
 * @since CloudSim Toolkit 6.0
 */
public interface GuestEntity extends CoreAttributes {
    /**
     * Updates the processing of cloudlets running on this guest. This may start a chain of processing updates
     * in the guest entities which are nested within this guest (if any).
     *
     * @param currentTime current simulation time
     * @param mipsShare list with MIPS share of each Pe available to the scheduler
     * @return time predicted completion time of the earliest finishing cloudlet, or 0 if there is no
     *         next events
     * @pre currentTime >= 0
     * @post $none
     */
    double updateCloudletsProcessing(double currentTime, List<Double> mipsShare);

    /**
     * Gets the current requested mips.
     *
     * @return the current requested mips
     */
    List<Double> getCurrentRequestedMips();

    /**
     * Gets the current requested total mips.
     *
     * @return the current requested total mips
     */
    double getCurrentRequestedTotalMips();

    /**
     * Gets the current requested max mips among all virtual PEs.
     *
     * @return the current requested max mips
     */
    double getCurrentRequestedMaxMips();

    /**
     * Gets the current requested bw.
     *
     * @return the current requested bw
     */
    long getCurrentRequestedBw();

    /**
     * Gets the current requested ram.
     *
     * @return the current requested ram
     */
    int getCurrentRequestedRam();

    /**
     * Gets total CPU utilization percentage of all cloudlets running on this guest entity at the given time
     *
     * @param time the time
     * @return total utilization percentage
     */
    double getTotalUtilizationOfCpu(double time);

    /**
     * Adds a VM state history entry.
     *
     * @param time the time
     * @param allocatedMips the allocated mips
     * @param requestedMips the requested mips
     * @param isInMigration the is in migration
     */
    void addStateHistoryEntry(double time, double allocatedMips, double requestedMips, boolean isInMigration);

    /**
     * Gets the host that runs this guest.
     *
     * @return the host entity
     * @pre $none
     * @post $result != null
     */
    <T extends HostEntity> T getHost();

    /**
     * Gets the host bw.
     *
     * @return the host bw
     * @pre $none
     * @post $result > 0
     */
    long getBw();

    /**
     * Gets the host memory.
     *
     * @return the host memory
     * @pre $none
     * @post $result > 0
     */
    int getRam();

    /**
     * Gets the Million of Instructions Per Second (MIPS).
     *
     * @return the mips
     */
    double getMips();

    /**
     * Get utilization created by all cloudlets running on this container in MIPS.
     *
     * @param time the time
     * @return total utilization
     */
    double getTotalUtilizationOfCpuMips(double time);

    /**
     * Gets the image size required to store the guest entity on a host.
     *
     * @return the image size
     * @pre $none
     * @post $result >= 0
     */
    long getSize();

    /**
     * Gets the guest id.
     *
     * @return the guest id
     */
    int getId();

    /**
     * Gets the ID of the owner of the guest entity.
     *
     * @return Guest's owner ID
     * @pre $none
     * @post $none
     */
    int getUserId();

    /**
     * Gets unique string identifier of the VM.
     *
     * @return string uid
     */
    String getUid();

    /**
     * Gets the Cloudlet scheduler.
     */
    CloudletScheduler getCloudletScheduler();

    /**
     * Sets the host that runs this guest.
     *
     * @param host Host running the guest entity
     * @pre host != $null
     * @post $none
     */
    <T extends HostEntity> void setHost(T host);

    /**
     * Sets the current allocated ram.
     *
     * @param currentAllocatedRam the new current allocated ram
     */
    void setCurrentAllocatedRam(int currentAllocatedRam);

    /**
     * Sets the Cloudlet scheduler.
     */
    //CloudletScheduler setCloudletScheduler();

    /**
     * Sets the current allocated bw.
     *
     * @param currentAllocatedBw the new current allocated bw
     */
    void setCurrentAllocatedBw(long currentAllocatedBw);

    /**
     * The current allocated mips for each VM's PE.
     */
    void setCurrentAllocatedMips(List<Double> mips);

    /**
     * Sets the in migration.
     *
     * @param inMigration the new in migration
     */
    void setInMigration(boolean inMigration);

    /**
     * Sets the guest entity as being instantiated.
     *
     * @param beingInstantiated the new guest entity being instantiated
     */
    void setBeingInstantiated(boolean beingInstantiated);

    /**
     * Checks if guest entity is in migration.
     *
     * @return true, if is in migration
     */
    boolean isInMigration();
}
