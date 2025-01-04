/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2024, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.core;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.lists.PeList;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

import java.util.*;

/**
 * An interface for implementing Host entities. A Host entity is used for hosting guest entities
 * (e.g., Vms and/or containers) within a Datacenter. Therefore, a Host entity executes actions related to
 * management of guest entities (e.g., creation and destruction of Vms or containers).
 *
 * @author Remo Andreoli
 * @since CloudSim Toolkit 7.0
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
     * //@TODO there is an inconsistency between the return value of this method
     * and the individual call of {@link GuestEntity#updateCloudletsProcessing(double, List) ,
     * and consequently the {@link CloudletScheduler#updateCloudletsProcessing(double, List) }.
     * The current method returns {@link Double#MAX_VALUE}  while the other ones
     * return 0. It has to be checked if there is a reason for this
     * difference.}
     */
    double updateCloudletsProcessing(double currentTime);

    /**
     * Try to allocate resources to a new Guest in the Host.
     *
     * @param guest Guest entity being started
     * @return $true if the guest could be started in the host; $false otherwise
     * @pre $none
     * @post $none
     */
    default boolean guestCreate(GuestEntity guest) {
        if (getStorage() < guest.getSize()) {
            Log.printlnConcat(CloudSim.clock(), "[GuestScheduler.guestCreate] Allocation of ", guest.getClassName(), " #",guest.getId(),
                    " failed by storage");
            return false;
        }

        if (!getGuestRamProvisioner().allocateRamForGuest(guest, guest.getCurrentRequestedRam())) {
            Log.printlnConcat(CloudSim.clock(), "[GuestScheduler.guestCreate] Allocation of ", guest.getClassName(), " #",guest.getId(),
                    " failed by RAM ");
            return false;
        }

        if (!getGuestBwProvisioner().allocateBwForGuest(guest, guest.getCurrentRequestedBw())) {
            Log.printlnConcat(CloudSim.clock(), "[GuestScheduler.guestCreate] Allocation of ", guest.getClassName(), " #",guest.getId(),
                    " failed by BW");
            getGuestRamProvisioner().deallocateRamForGuest(guest);
            return false;
        }

        // NOTE: this calls peProvisioner.allocateMipsForGuest
        if (!getGuestScheduler().allocatePesForGuest(guest, guest.getCurrentRequestedMips())) {
            Log.printlnConcat(CloudSim.clock(), "[GuestScheduler.guestCreate] Allocation of ", guest.getClassName(), " #",guest.getId(),
                    " failed by Number of PEs or MIPS");
            getGuestRamProvisioner().deallocateRamForGuest(guest);
            getGuestBwProvisioner().deallocateBwForGuest(guest);
            return false;
        }

        setStorage(getStorage() - guest.getSize());
        getGuestList().add(guest);
        guest.setHost(this);
        return true;
    }

    /**
     * Destroys a guest running in the host.
     *
     * @param guest the Guest entity
     * @pre $none
     * @post $none
     */
    default void guestDestroy(GuestEntity guest) {
        if (guest != null) {
            guestDeallocate(guest);
            getGuestList().remove(guest);
            //Log.printlnConcat(getClassName()," # ",getId()," guestDestroy: ", guest.getClassName()," #", guest.getId(), " is deleted from the list");

            /*while(getGuestList().contains(guest)){ // @TODO: Do I really need this?
                Log.printlnConcat(guest.getClassName(), " #", guest.getId(), " is still not deallocated yet; Polling...");
            }*/
            guest.setHost(null);
        }
    }

    /**
     * Deallocate all resources of a guest entity from the host.
     *
     * @param guest the guest
     */
    default void guestDeallocate(GuestEntity guest) {
        getGuestRamProvisioner().deallocateRamForGuest(guest);
        getGuestBwProvisioner().deallocateBwForGuest(guest);
        getGuestScheduler().deallocatePesForGuest(guest);
        guest.setHost(null);
        setStorage(getStorage() + guest.getSize());
    }

    /**
     * Destroys all guest entities running in the host.
     *
     * @pre $none
     * @post $none
     */
    default void guestDestroyAll() {
        getGuestRamProvisioner().deallocateRamForAllGuests();
        getGuestBwProvisioner().deallocateBwForAllGuests();
        getGuestScheduler().deallocatePesForAllGuests();

        for (GuestEntity guest : getGuestList()) {
            guest.setHost(null);
            setStorage(getStorage() + guest.getSize());
        }

        getGuestList().clear();
    }

    /**
     * Reallocate migrating in vms. Gets the VM in the migrating in queue
     * and allocate them on the host.
     */
    default void reallocateMigratingInGuests() {
        for (GuestEntity guest : getGuestsMigratingIn()) {
            if (!getGuestList().contains(guest)) {
                getGuestList().add(guest);
            }
            if (!getGuestScheduler().getGuestsMigratingIn().contains(guest.getUid())) {
                getGuestScheduler().getGuestsMigratingIn().add(guest.getUid());
            }
            getGuestRamProvisioner().allocateRamForGuest(guest, guest.getCurrentRequestedRam());
            getGuestBwProvisioner().allocateBwForGuest(guest, guest.getCurrentRequestedBw());
            getGuestScheduler().allocatePesForGuest(guest, guest.getCurrentRequestedMips());
            setStorage(getStorage() - guest.getSize());
        }
    }

    /**
     * Removes a migrating in guest entity.
     *
     * @param guest the guest
     */
    default void removeMigratingInGuest(GuestEntity guest) {
        guestDeallocate(guest);
        getGuestsMigratingIn().remove(guest);
        getGuestList().remove(guest);
        //Log.printlnConcat(getClassName()," # ",getId()," removeMigratingInGuest: ",guest.getClassName()," #",guest.getId()," is deleted from the list");
        getGuestScheduler().getGuestsMigratingIn().remove(guest.getUid());
        guest.setInMigration(false);
    }

    /**
     * Adds a VM migrating into the current (dstHost) host.
     *
     * @param guest the vm
     */
    default void addMigratingInGuest(GuestEntity guest) {
        guest.setInMigration(true);

        if (!getGuestsMigratingIn().contains(guest)) {
            if (getStorage() < guest.getSize()) {
                Log.printlnConcat("[host.addMigratingInGuest] Allocation of ", guest.getClassName(), " #", guest.getId(), " to ", getClassName(), " #",
								  getId(), " failed by storage");
                System.exit(0);
            }

            if (!getGuestRamProvisioner().allocateRamForGuest(guest, guest.getCurrentRequestedRam())) {
                Log.printlnConcat("[host.addMigratingInGuest] Allocation of ", guest.getClassName(), " #", guest.getId(), " to ", getClassName(), " #",
								  getId(), " failed by RAM");
                System.exit(0);
            }

            if (!getGuestBwProvisioner().allocateBwForGuest(guest, guest.getCurrentRequestedBw())) {
                Log.printlnConcat("[host.addMigratingInGuest] Allocation of ", guest.getClassName(), " #", guest.getId(), " to ", getClassName(), " #",
								  getId(), " failed by BW");
                System.exit(0);
            }

            getGuestScheduler().getGuestsMigratingIn().add(guest.getUid());
            if (!getGuestScheduler().allocatePesForGuest(guest, guest.getCurrentRequestedMips())) {
                Log.printlnConcat("[host.addMigratingInGuest] Allocation of ", guest.getClassName(), " #", guest.getId(), " to ", getClassName(), " #",
								  getId(), " failed by MIPS");
                System.exit(0);
            }

            setStorage(getStorage() - guest.getSize());

            getGuestsMigratingIn().add(guest);
            getGuestList().add(guest);
            updateCloudletsProcessing(CloudSim.clock());
            guest.getHost().updateCloudletsProcessing(CloudSim.clock());
        }
    }

    /**
     * Gets a guest entity allocated in the host by its ID and user.
     *
     * @param guestId the guest ID
     * @param userId ID of guest's owner
     * @return the guest entity object, $null if not found
     * @pre $none
     * @post $none
     */
    default GuestEntity getGuest(int guestId, int userId) {
        for (GuestEntity guest : getGuestList()) {
            if (guest.getId() == guestId && guest.getUserId() == userId) {
                return guest;
            }
        }
        return null;
    }

    /**
     * Gets the guest list.
     *
     * @param <T> the generic type
     * @return the guest list
     */
    <T extends GuestEntity> List<T> getGuestList();

    /**
     * Gets the number of guest entities hosted by this host entity.
     * @TODO: Maybe "return getGuestList().size()" is a better default
     * @return the number of guest entities
     */
    default int getNumberOfGuests() {
        int numberOfGuests = 0;

        for (GuestEntity guest : getGuestList()) {
            if (!guest.isInMigration()) {
                numberOfGuests++;
            }
        }
        return numberOfGuests;
    }
    /**
     * Gets the host bw (in bits/s).
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
    <T extends GuestEntity> List<T> getGuestsMigratingIn();

    /**
     * Gets the maximum utilization among the PEs of a given guest entity.
     * @param guest The guest to get its PEs maximum utilization
     * @return The maximum utilization among the PEs of the guest.
     */
    default double getMaxUtilizationAmongGuestsPes(GuestEntity guest) {
        return PeList.getMaxUtilizationAmongGuestsPes(getPeList(), guest);
    }

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

    @Deprecated
    default VmScheduler getVmScheduler() { return getGuestScheduler(); }

    /**
     * Gets the bw provisioner.
     *
     * @return the bw provisioner
     */
    BwProvisioner getGuestBwProvisioner();

    /**
     * Gets the ram provisioner.
     *
     * @return the ram provisioner
     */
    RamProvisioner getGuestRamProvisioner();

    /**
     * Sets the particular Pe status on the host entity.
     *
     * @param peId the pe id
     * @param status Pe status, either <tt>Pe.FREE</tt> or <tt>Pe.BUSY</tt>
     * @return <tt>true</tt> if the Pe status has changed, <tt>false</tt> otherwise (Pe id might not
     *         be exist)
     * @pre peID >= 0
     * @post $none
     */
    default boolean setPeStatus(int peId, int status) {
        return PeList.setPeStatus(getPeList(), peId, status);
    }

    /**
     * Sets the storage.
     *
     * @param storage the new storage
     */
    void setStorage(long storage);

    /**
     * Checks if the host is suitable for a guest entity, i.e, it has enough resources
     * to attend the guest.
     *
     * @param guest the guest
     * @return true, if is suitable for guest
     */
    default boolean isSuitableForGuest(GuestEntity guest) {
        return (getGuestScheduler().getPeCapacity() >= guest.getCurrentRequestedMaxMips() &&
                getGuestScheduler().getAvailableMips() >= guest.getCurrentRequestedTotalMips() &&
                getGuestRamProvisioner().isSuitableForGuest(guest, guest.getCurrentRequestedRam()) &&
                getGuestBwProvisioner().isSuitableForGuest(guest, guest.getCurrentRequestedBw()));
    }

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

    /**
     * Gets the list of migratable VMs from a given host.
     *
     * @return the list of migratable VMs
     */
    default List<GuestEntity> getMigrableVms() {
        List<GuestEntity> migratableVms = new ArrayList<>();
        for (GuestEntity vm : getGuestList()) {
            if (!vm.isInMigration()) {
                migratableVms.add(vm);
            }
        }
        return migratableVms;
    }
}
