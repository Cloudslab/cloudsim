package org.cloudbus.cloudsim.core;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.lists.PeList;

import javax.xml.crypto.Data;
import java.util.List;

/**
 * An interface for implementing Host entities. A Host entity is used for hosting guest entities
 * (e.g., Vms and/or containers) within a Datacenter. Therefore, a Host entity executes actions related to
 * management of guest entities (e.g., creation and destruction of Vms or containers).
 *
 * @author Remo Andreoli
 * @since CloudSim Toolkit 6.0
 */
public interface HostEntity extends CoreAttributes {
    /**
     * Requests updating of cloudlets' processing within the (possibly nested) guests running in this host.
     *
     * @param currentTime the current time
     * @return expected time of completion of the next cloudlet in all guests in this host or
     *         {@link Double#MAX_VALUE} if there is no future events expected in this host
     * @pre currentTime >= 0.0
     * @post $none
     * //TODO there is an inconsistency between the return value of this method
     * and the individual call of {@link GuestEntity#updateCloudletsProcessing(double, List) ,
     * and consequently the {@link CloudletScheduler#updateCloudletsProcessing(double, List) }.
     * The current method returns {@link Double#MAX_VALUE}  while the other ones
     * return 0. It has to be checked if there is a reason for this
     * difference.}
     */
    double updateGuestsProcessing(double currentTime);

    /**
     * Allocates PEs for a guest entity.
     *
     * @param guest     the guest
     * @param mipsShare the list of MIPS share to be allocated to the guest
     * @return $true if this policy allows a new guest in the host, $false otherwise
     * @pre $none
     * @post $none
     */
    boolean allocatePesForGuest(GuestEntity guest, List<Double> mipsShare);

    /**
     * Try to allocate resources to a new Guest in the Host.
     *
     * @param guest Guest entity being started
     * @return $true if the guest could be started in the host; $false otherwise
     * @pre $none
     * @post $none
     */
    boolean guestCreate(GuestEntity guest);

    /**
     * Destroys a guest running in the host.
     *
     * @param guest the Guest entity
     * @pre $none
     * @post $none
     */
    void guestDestroy(GuestEntity guest);

    /**
     * Destroys all VMs running in the host.
     *
     * @pre $none
     * @post $none
     */
    void guestDestroyAll();

    /**
     * Reallocate migrating in vms. Gets the VM in the migrating in queue
     * and allocate them on the host.
     */
    void reallocateMigratingInGuests();

    /**
     * Removes a migrating in guest entity.
     *
     * @param guest the guest
     */
    void removeMigratingInGuest(GuestEntity guest);

    /**
     * Adds a VM migrating into the current (dstHost) host.
     *
     * @param guest the vm
     */
    void addMigratingInGuest(GuestEntity guest);

    /**
     * Gets a VM by its id and user.
     *
     * @param vmId   the vm id
     * @param userId ID of VM's owner
     * @return the virtual machine object, $null if not found
     * @pre $none
     * @post $none
     */
    GuestEntity getGuest(int vmId, int userId);

    /**
     * Gets the guest list.
     *
     * @param <T> the generic type
     * @return the guest list
     */
    <T extends GuestEntity> List<T> getGuestList();

    /**
     * Gets the number of guest entities hosted by this host entity.
     *
     * @return the number of guest entities
     */
    int getNumberOfGuests();

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
     * Gets the host storage.
     *
     * @return the host storage
     * @pre $none
     * @post $result >= 0
     */
    long getStorage();

    /**
     * Gets the host id.
     *
     * @return the host id
     */
    int getId();

    /**
     * Gets the free pes number.
     *
     * @return the free pes number
     */
    int getNumberOfFreePes();

    /**
     * Gets the pe list.
     *
     * @param <T> the generic type
     * @return the pe list
     */
    <T extends Pe> List<T> getPeList();

    /**
     * Gets the MIPS share of each Pe that is allocated to a given guest entity.
     *
     * @param guest the guest
     * @return an array containing the amount of MIPS of each pe that is available to the guest
     * @pre $none
     * @post $none
     */
    List<Double> getAllocatedMipsForGuest(GuestEntity guest);

    /**
     * Gets the total allocated MIPS for a container over all the PEs.
     *
     * @param guest the container
     * @return the allocated mips for container
     */
    double getTotalAllocatedMipsForGuest(GuestEntity guest);

    /**
     * Gets the list of guest entities (container, vms, etc) migrating in.
     *
     * @return the guests migrating in
     */
    List<GuestEntity> getGuestsMigratingIn();

    /**
     * Gets the data center of the host.
     *
     * @return the data center where the host runs
     */
    Datacenter getDatacenter();

    /**
     * Sets the data center of the host.
     */
    void setDatacenter(Datacenter datacenter);

    void setInWaiting(boolean inWaiting);

    /**
     * Gets the guest entity scheduler.
     *
     * @return the guest entity scheduler
     */
    VmScheduler getGuestScheduler();

    /**
     * Checks if the host is suitable for a guest entity, i.e, it has enough resources
     * to attend the guest.
     *
     * @param guest the guest
     * @return true, if is suitable for guest
     */
    boolean isSuitableForGuest(GuestEntity guest);

    /**
     * Checks if the host PEs have failed.
     *
     * @return true, if the host PEs have failed; false otherwise
     */
    boolean isFailed();

    /**
     * In waiting flag. THe host entity is waiting for guest to come.
     */
    boolean isInWaiting();
}
