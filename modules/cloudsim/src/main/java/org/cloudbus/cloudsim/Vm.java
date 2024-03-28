/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.core.HostEntity;
import org.cloudbus.cloudsim.core.VmAbstract;
import org.cloudbus.cloudsim.lists.PeList;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a Virtual Machine (VM) that runs inside a Host, sharing a hostList with other VMs. It processes
 * cloudlets. This processing happens according to a policy, defined by the CloudletScheduler. Each
 * VM has a owner, which can submit cloudlets to the VM to execute them.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @author Remo Andreoli
 * @since CloudSim Toolkit 1.0
 */
public class Vm implements VmAbstract {

	/** The VM unique id. */
	@Getter @Setter
	private int id;

	/** The user id. */
	@Getter @Setter
	private int userId;

	/** A Unique Identifier (UID) for the VM, that is compounded by the user id and VM id. */
	@Setter @Getter
	private String uid;

	/** The size the VM image size (the amount of storage it will use, at least initially). */
	@Getter @Setter
	private long size;

	/** The MIPS capacity of each VM's PE. */
	@Getter @Setter
	private double mips;

	/** The number of PEs required by the VM. */
	@Getter @Setter
	private int numberOfPes;

	/** The required ram. */
	@Getter @Setter
	private int ram;

	/** The required bw. */
	@Getter @Setter
	private long bw;

	/** The Virtual Machine Monitor (VMM) that manages the VM. */
	@Getter @Setter(AccessLevel.PROTECTED)
	private String vmm;

	/** The Cloudlet scheduler the VM uses to schedule cloudlets execution. */
	@Getter @Setter(AccessLevel.PROTECTED)
	private CloudletScheduler cloudletScheduler;

	/** The Guest scheduler the Vm uses to schedule nested guest entities, such as containers. */
	@Setter @Getter
	private VmScheduler guestScheduler;

	/** The PM that hosts the VM. */
	@Setter @Getter
	private HostEntity host;


	/** Indicates if the VM is in migration process. */
	@Getter @Setter
	private boolean inMigration;
	
	/** Indicates if the VM is in Pause State (e.g. Stop & copy phase) */
	@Setter @Getter
	private boolean inPause;

	/** Indicates if the VM is waiting for nested guest entities to come. */
	@Setter @Getter
	private boolean inWaiting;

	/** The current allocated storage size. */
	@Getter @Setter(AccessLevel.PROTECTED)
	private long currentAllocatedSize;

	/** The current allocated ram. */
	@Getter @Setter
	private int currentAllocatedRam;

	/** The current allocated bw. */
	@Getter @Setter
	private long currentAllocatedBw;

	/** The current allocated mips for each VM's PE.
	 * TODO: Maybe replace with a call to getCloudletScheduler().getCurrentMipsShare()
	 */
	@Getter @Setter
	private List<Double> currentAllocatedMips;

	/** Indicates if the VM is being instantiated. */
	@Setter @Getter
	private boolean beingInstantiated;

	/** The ram provisioner for nested guest entities. */
	@Setter @Getter
	private RamProvisioner containerRamProvisioner;

	/** The bw provisioner for nested guest entities. */
	@Setter @Getter
	private BwProvisioner containerBwProvisioner;

	/** The pe list for nested guest entities. */
	@Setter @Getter
	private List<? extends Pe> peList;

	/** The nested guest list. */
	@Getter
	private final List<GuestEntity> guestList = new ArrayList<>();

	/** The nested guests migrating in. */
	@Getter
	private final List<GuestEntity> guestsMigratingIn = new ArrayList<>();

	/** Tells whether this VM is working properly (as a host for nested guests) or has failed. */
	private boolean failed;



	/** The mips allocation history. 
	 * TODO Instead of using a list, this attribute would be
	 * a map, where the key can be the history time
	 * and the value the history itself.
	 * By this way, if one wants to get the history for a given
	 * time, he/she doesn't have to iterate over the entire list
	 * to find the desired entry.
	 */
	@Getter
	private final List<VmStateHistoryEntry> stateHistory = new LinkedList<>();

	/**
	 * Creates a new Vm object.
	 * 
	 * @param id unique ID of the VM
	 * @param userId ID of the VM's owner
	 * @param mips the mips
	 * @param numberOfPes amount of CPUs
	 * @param ram amount of ram
	 * @param bw amount of bandwidth
	 * @param size The size the VM image size (the amount of storage it will use, at least initially).
	 * @param vmm virtual machine monitor
	 * @param cloudletScheduler cloudletScheduler policy for cloudlets scheduling
	 *
	 * @pre id >= 0
	 * @pre userId >= 0
	 * @pre size > 0
	 * @pre ram > 0
	 * @pre bw > 0
	 * @pre cpus > 0
	 * @pre priority >= 0
	 * @pre cloudletScheduler != null
	 * @post $none
	 */
	public Vm(
			int id,
			int userId,
			double mips,
			int numberOfPes,
			int ram,
			long bw,
			long size,
			String vmm,
			CloudletScheduler cloudletScheduler) {
		setId(id);
		setUserId(userId);
		setUid(getUid(userId, id));
		setMips(mips);
		setNumberOfPes(numberOfPes);
		setRam(ram);
		setBw(bw);
		setSize(size);
		setVmm(vmm);
		setCloudletScheduler(cloudletScheduler);

		setInMigration(false);
		setInPause(false);
		setBeingInstantiated(true);

		setCurrentAllocatedBw(0);
		setCurrentAllocatedMips(null);
		setCurrentAllocatedRam(0);
		setCurrentAllocatedSize(0);
	}

	/**
	 * Create a new VM object that is capable to host nested guest entities
	 * @param id
	 * @param userId
	 * @param mips
	 * @param numberOfPes
	 * @param ram
	 * @param bw
	 * @param size
	 * @param vmm
	 * @param cloudletScheduler
	 * @param guestScheduler
	 * @param containerRamProvisioner
	 * @param containerBwProvisioner
	 * @param peList
	 */
	public Vm(
			int id,
			int userId,
			double mips,
			int numberOfPes,
			int ram,
			long bw,
			long size,
			String vmm,
			CloudletScheduler cloudletScheduler,
			VmScheduler guestScheduler,
			RamProvisioner containerRamProvisioner,
			BwProvisioner containerBwProvisioner,
			List<? extends Pe> peList
	) {
		this(id, userId, mips, peList.size(), ram, bw, size, vmm, cloudletScheduler);
		if (numberOfPes != peList.size()) {
			throw new RuntimeException("numberOfPes != peList.size");
		}

		if (containerRamProvisioner.getRam() > getRam()) {
			throw new RuntimeException("ramProvisioner.ram > ram");
		}

		if (containerBwProvisioner.getBw() > getBw()) {
			throw new RuntimeException("bwProvisioner.bw > bw");
		}
		
		setGuestScheduler(guestScheduler);

		setPeList(peList);
		setContainerRamProvisioner(containerRamProvisioner);
		setContainerBwProvisioner(containerBwProvisioner);
	}

	/**
	 * Updates the processing of cloudlets running on this VM.
	 * 
	 * @param currentTime current simulation time
	 * @param mipsShare list with MIPS share of each Pe available to the scheduler
	 * @return time predicted completion time of the earliest finishing cloudlet, or 0 if there is no
	 *         next events
	 * @pre currentTime >= 0
	 * @post $none
	 */
	public double updateCloudletsProcessing(double currentTime, List<Double> mipsShare) {
		double smallerTime = Double.MAX_VALUE;

		if (mipsShare != null) {
			// Cloudlets hosted here
			double time1 = getCloudletScheduler().updateCloudletsProcessing(currentTime, mipsShare);
			if (time1 > 0.0 && time1 < smallerTime) {
				smallerTime = time1;
			}

			// Cloudlets hosted in nested guests (if any)
			for (GuestEntity container : getGuestList()) {
				double time2 = container.updateCloudletsProcessing(currentTime, getGuestScheduler().getAllocatedMipsForGuest(container));
				if (time2 > 0.0 && time2 < smallerTime) {
					smallerTime = time2;
				}
			}

			return smallerTime;
		}
		return 0.0;
	}

	public double updateGuestsProcessing(double currentTime) {
		double smallerTime = Double.MAX_VALUE;

		for (GuestEntity container : getGuestList()) {
			double time = container.updateCloudletsProcessing(currentTime, getGuestScheduler().getAllocatedMipsForGuest(container));
			if (time > 0.0 && time < smallerTime) {
				smallerTime = time;
			}
			double totalRequestedMips = container.getCurrentRequestedTotalMips();
			double totalAllocatedMips = getGuestScheduler().getTotalAllocatedMipsForGuest(container);
			container.addStateHistoryEntry(
					currentTime,
					totalAllocatedMips,
					totalRequestedMips,
					(container.isInMigration() && !getGuestsMigratingIn().contains(container)));
		}

		return smallerTime;
	}

	/**
	 * Gets the current requested mips.
	 *
	 * TODO: Remo Andreoli: Don't know yet how to reconcile with containerVm.getCurrentRequestedMips()
	 * @return the current requested mips
	 */
	public List<Double> getCurrentRequestedMips() {
		List<Double> currentRequestedMips = getCloudletScheduler().getCurrentRequestedMips();
		if (isBeingInstantiated()) {
			currentRequestedMips = new ArrayList<>();
			for (int i = 0; i < getNumberOfPes(); i++) {
				currentRequestedMips.add(getMips());
			}
		}
		return currentRequestedMips;
	}

	/**
	 * Gets the current requested total mips.
	 * 
	 * @return the current requested total mips
	 */
	public double getCurrentRequestedTotalMips() {
		double totalRequestedMips = 0;
		for (double mips : getCurrentRequestedMips()) {
			totalRequestedMips += mips;
		}
		return totalRequestedMips;
	}

	/**
	 * Gets the current requested max mips among all virtual PEs.
	 * 
	 * @return the current requested max mips
	 */
	public double getCurrentRequestedMaxMips() {
		double maxMips = 0;
		for (double mips : getCurrentRequestedMips()) {
			if (mips > maxMips) {
				maxMips = mips;
			}
		}
		return maxMips;
	}

	/**
	 * Gets the current requested bw.
	 * 
	 * @return the current requested bw
	 */
	public long getCurrentRequestedBw() {
		if (isBeingInstantiated()) {
			return getBw();
		}

		long requestedBwTemp = (long) (getCloudletScheduler().getCurrentRequestedUtilizationOfBw() * getBw());
		for (GuestEntity container : getGuestList()) {
			requestedBwTemp += container.getCurrentRequestedBw();
		}

		return requestedBwTemp;
	}

	/**
	 * Gets the current requested ram.
	 * 
	 * @return the current requested ram
	 */
	public int getCurrentRequestedRam() {
		if (isBeingInstantiated()) {
			return getRam();
		}

		int requestedRamTemp = (int) (getCloudletScheduler().getCurrentRequestedUtilizationOfRam() * getRam());
		for (GuestEntity container : getGuestList()) {
			requestedRamTemp += container.getCurrentRequestedRam();
		}

		return requestedRamTemp;
	}

	/**
	 * Gets total CPU utilization percentage of all clouddlets running on this VM at the given time
	 * 
	 * @param time the time
	 * @return total utilization percentage
	 */
	public double getTotalUtilizationOfCpu(double time) {
		double TotalUtilizationOfCpuTemp = getCloudletScheduler().getTotalUtilizationOfCpu(time);
		for (GuestEntity container : getGuestList()) {
			TotalUtilizationOfCpuTemp += container.getTotalUtilizationOfCpu(time);
		}

		return TotalUtilizationOfCpuTemp;
	}

	/**
	 * Get total CPU utilization of all cloudlets running on this VM at the given time (in MIPS).
	 * 
	 * @param time the time
	 * @return total cpu utilization in MIPS
         * @see #getTotalUtilizationOfCpu(double) 
	 */
	public double getTotalUtilizationOfCpuMips(double time) {
		return getTotalUtilizationOfCpu(time) * getMips();
	}

	/**
	 * Returns the MIPS share of each Pe that is allocated to a given container.
	 *
	 * @param guest the guest entity
	 * @return an array containing the amount of MIPS of each pe that is available to the guest
	 * @pre $none
	 * @post $none
	 */
	public List<Double> getAllocatedMipsForGuest(GuestEntity guest) {
		return getGuestScheduler().getAllocatedMipsForGuest(guest);
	}

	/**
	 * Gets the total allocated MIPS for a guest entity over all the PEs.
	 *
	 * @param guest the guest entity
	 * @return the allocated mips for guest
	 */
	public double getTotalAllocatedMipsForGuest(GuestEntity guest) {
		return getGuestScheduler().getTotalAllocatedMipsForGuest(guest);
	}

	/**
	 * Adds the migrating in guest.
	 *
	 * @param guest the guest entity
	 */
	public void addMigratingInGuest(GuestEntity guest) {
		guest.setInMigration(true);

		if (!getGuestsMigratingIn().contains(guest)) {
			if (getSize() < guest.getSize()) {
				Log.printConcatLine("[GuestScheduler.addMigratingInContainer] Allocation of VM #", guest.getId(), " to Host #",
						getId(), " failed by storage");
				System.exit(0);
			}

			if (!getContainerRamProvisioner().allocateRamForGuest(guest, guest.getCurrentRequestedRam())) {
				Log.printConcatLine("[GuestScheduler.addMigratingInContainer] Allocation of VM #", guest.getId(), " to Host #",
						getId(), " failed by RAM");
				System.exit(0);
			}

			if (!getContainerBwProvisioner().allocateBwForGuest(guest, guest.getCurrentRequestedBw())) {
				Log.printLine("[GuestScheduler.addMigratingInContainer] Allocation of VM #" + guest.getId() + " to Host #"
						+ getId() + " failed by BW");
				System.exit(0);
			}

			getGuestScheduler().getGuestsMigratingIn().add(guest.getUid());
			if (!getGuestScheduler().allocatePesForGuest(guest, guest.getCurrentRequestedMips())) {
				Log.printLine(String.format("[GuestScheduler.addMigratingInContainer] Allocation of VM #%d to Host #%d failed by MIPS", guest.getId(), getId()));
				System.exit(0);
			}

			setSize(getSize() - guest.getSize());

			getGuestsMigratingIn().add(guest);
			getGuestList().add(guest);
			updateGuestsProcessing(CloudSim.clock());
			guest.getHost().updateGuestsProcessing(CloudSim.clock());
		}
	}

	/**
	 * Removes the migrating in vm.
	 *
	 * @param guest the container
	 */
	public void removeMigratingInGuest(GuestEntity guest) {
		guestDeallocate(guest);
		getGuestsMigratingIn().remove(guest);
		getGuestList().remove(guest);
		Log.printLine("Vm# "+getId()+"removeMigratingInGuest:......" + guest.getId() + "   Is deleted from the list");
		getGuestScheduler().getGuestsMigratingIn().remove(guest.getUid());
		guest.setInMigration(false);
	}

	/**
	 * Reallocate migrating in containers.
	 */
	public void reallocateMigratingInGuests() {
		for (GuestEntity container : getGuestsMigratingIn()) {
			if (!getGuestList().contains(container)) {
				getGuestList().add(container);
			}
			if (!getGuestScheduler().getGuestsMigratingIn().contains(container.getUid())) {
				getGuestScheduler().getGuestsMigratingIn().add(container.getUid());
			}
			getContainerRamProvisioner().allocateRamForGuest(container, container.getCurrentRequestedRam());
			getContainerBwProvisioner().allocateBwForGuest(container, container.getCurrentRequestedBw());
			getGuestScheduler().allocatePesForGuest(container, container.getCurrentRequestedMips());
			setSize(getSize() - container.getSize());
		}
	}

	/**
	 * Checks if is suitable for container.
	 *
	 * @param guest the container
	 * @return true, if is suitable for container
	 */
	public boolean isSuitableForGuest(GuestEntity guest) {

		return (getGuestScheduler().getPeCapacity() >= guest.getCurrentRequestedMaxMips()&& getGuestScheduler().getAvailableMips() >= guest.getTotalMips()
				&& getContainerRamProvisioner().isSuitableForGuest(guest, guest.getCurrentRequestedRam()) && getContainerBwProvisioner()
				.isSuitableForGuest(guest, guest.getCurrentRequestedBw()));
	}

	/**
	 * Destroys a container running in the VM.
	 *
	 * @param guest the container
	 * @pre $none
	 * @post $none
	 */
	public void guestDestroy(GuestEntity guest) {
		if (guest != null) {
			guestDeallocate(guest);
			getGuestList().remove(guest);
			Log.printLine("Vm# "+getId()+" guestDestroy:......" + guest.getId() + "Is deleted from the list");

			while(getGuestList().contains(guest)){
				Log.printConcatLine("The container", guest.getId(), " is still here");
			}
			guest.setHost(null);
		}
	}

	/**
	 * Destroys all containers running in the VM.
	 *
	 * @pre $none
	 * @post $none
	 */
	public void guestDestroyAll() {
		guestDeallocateAll();
		for (GuestEntity container : getGuestList()) {
			container.setHost(null);
			setSize(getSize() + container.getSize());
		}

		getGuestList().clear();
	}

	/**
	 * Deallocate all VMList for the container.
	 *
	 * @param guest the container
	 */
	protected void guestDeallocate(GuestEntity guest) {
		getContainerRamProvisioner().deallocateRamForGuest(guest);
		getContainerBwProvisioner().deallocateBwForGuest(guest);
		getGuestScheduler().deallocatePesForGuest(guest);
		setSize(getSize() + guest.getSize());
	}

	/**
	 * Deallocate all vmList for the container.
	 */
	protected void guestDeallocateAll() {
		getContainerRamProvisioner().deallocateRamForAllGuests();
		getContainerBwProvisioner().deallocateBwForAllGuests();
		getGuestScheduler().deallocatePesForAllGuests();
	}

	/**
	 * Allocates PEs for a VM.
	 *
	 * @param guest     the vm
	 * @param mipsShare the mips share
	 * @return $true if this policy allows a new VM in the host, $false otherwise
	 * @pre $none
	 * @post $none
	 */
	public boolean allocatePesForGuest(GuestEntity guest, List<Double> mipsShare) {
		return getGuestScheduler().allocatePesForGuest(guest, mipsShare);
	}

	/**
	 * Releases PEs allocated to a container.
	 *
	 * @param guest the container
	 * @pre $none
	 * @post $none
	 */
	public void deallocatePesForGuest(GuestEntity guest) {
		getGuestScheduler().deallocatePesForGuest(guest);
	}

	/**
	 * Allocates PEs and memory to a new container in the VM.
	 *
	 * @param guest container being started
	 * @return $true if the container could be started in the VM; $false otherwise
	 * @pre $none
	 * @post $none
	 */
	public boolean guestCreate(GuestEntity guest) {
//        Log.printLine("Host: Create VM???......" + container.getId());
		if (getSize() < guest.getSize()) {
			Log.printConcatLine("[GuestScheduler.ContainerCreate] Allocation of Container #", guest.getId(), " to VM #", getId(),
					" failed by storage");
			return false;
		}

		if (!getContainerRamProvisioner().allocateRamForGuest(guest, guest.getCurrentRequestedRam())) {
			Log.printConcatLine("[GuestScheduler.ContainerCreate] Allocation of Container #", guest.getId(), " to VM #", getId(),
					" failed by RAM");
			return false;
		}

		if (!getContainerBwProvisioner().allocateBwForGuest(guest, guest.getCurrentRequestedBw())) {
			Log.printConcatLine("[GuestScheduler.ContainerCreate] Allocation of Container #", guest.getId(), " to VM #", getId(),
					" failed by BW");
			getContainerRamProvisioner().deallocateRamForGuest(guest);
			return false;
		}

		if (!getGuestScheduler().allocatePesForGuest(guest, guest.getCurrentRequestedMips())) {
			Log.printConcatLine("[GuestScheduler.ContainerCreate] Allocation of Container #", guest.getId(), " to VM #", getId(),
					" failed by MIPS");
			getContainerRamProvisioner().deallocateRamForGuest(guest);
			getContainerBwProvisioner().deallocateBwForGuest(guest);
			return false;
		}

		setSize(getSize() - guest.getSize());
		getGuestList().add(guest);
		guest.setHost(this);
		return true;
	}

	public int getNumberOfGuests() {
		int c =0;
		for(GuestEntity container: getGuestList()){
			if(!getGuestsMigratingIn().contains(container)){
				c++;
			}
		}
		return c;
	}
	/**
	 * Returns a container object.
	 *
	 * @param containerId the container id
	 * @param userId      ID of container's owner
	 * @return the container object, $null if not found
	 * @pre $none
	 * @post $none
	 */
	public GuestEntity getGuest(int containerId, int userId) {
		for (GuestEntity container : getGuestList()) {
			if (container.getId() == containerId && container.getUserId() == userId) {
				return container;
			}
		}
		return null;
	}

	/**
	 * Generate unique string identifier of the VM.
	 * 
	 * @param userId the user id
	 * @param vmId the vm id
	 * @return string uid
	 */
	public static String getUid(int userId, int vmId) {
		return userId + "-" + vmId;
	}

	/**
	 * TODO: Remo Andreoli: I'm not sure if this is alright
	 * @return
	 */
	public long getStorage() {
		return getSize();
	}

	public Datacenter getDatacenter() {
		return getHost().getDatacenter();
	}

	public void setDatacenter(Datacenter datacenter) {
		getHost().setDatacenter(datacenter);
	}

	/**
	 * Adds a VM state history entry.
	 * 
	 * @param time the time
	 * @param allocatedMips the allocated mips
	 * @param requestedMips the requested mips
	 * @param isInMigration the is in migration
	 */
	public void addStateHistoryEntry(
			double time,
			double allocatedMips,
			double requestedMips,
			boolean isInMigration) {
		VmStateHistoryEntry newState = new VmStateHistoryEntry(
				time,
				allocatedMips,
				requestedMips,
				isInMigration);
		if (!getStateHistory().isEmpty()) {
			VmStateHistoryEntry previousState = getStateHistory().get(getStateHistory().size() - 1);
			if (previousState.getTime() == time) {
				getStateHistory().set(getStateHistory().size() - 1, newState);
				return;
			}
		}
		getStateHistory().add(newState);
	}

	/**
	 * Sets the PEs of this machine to a FAILED status. NOTE: <tt>resName</tt> is used for debugging
	 * purposes, which is <b>ON</b> by default. Use {@link #setFailed(boolean)} if you do not want
	 * this information.
	 *
	 * @param resName the name of the resource
	 * @param failed  the failed
	 * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
	 */
	public boolean setFailed(String resName, boolean failed) {
		// all the PEs are failed (or recovered, depending on fail)
		this.failed = failed;
		PeList.setStatusFailed(getPeList(), resName, getId(), failed);
		return true;
	}

	/**
	 * Sets the PEs of this machine to a FAILED status.
	 *
	 * @param failed the failed
	 * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
	 */
	public boolean setFailed(boolean failed) {
		// all the PEs are failed (or recovered, depending on fail)
		this.failed = failed;
		PeList.setStatusFailed(getPeList(), failed);
		return true;
	}

	/**
	 * Sets the particular Pe status on this Machine.
	 *
	 * @param peId   the pe id
	 * @param status Pe status, either <tt>Pe.FREE</tt> or <tt>Pe.BUSY</tt>
	 * @return <tt>true</tt> if the Pe status has changed, <tt>false</tt> otherwise (Pe id might not
	 * be exist)
	 * @pre peID >= 0
	 * @post $none
	 */
	public boolean setPeStatus(int peId, int status) {
		return PeList.setPeStatus(getPeList(), peId, status);
	}

	/**
	 * Gets the total mips.
	 *
	 * @return the total mips
	 */
	public double getTotalMips() {
		if (getPeList() != null) {
			return PeList.getTotalMips(getPeList());
		}

		return getCurrentRequestedTotalMips();
	}

	/**
	 * Gets the free pes number.
	 *
	 * @return the free pes number
	 */
	public int getNumberOfFreePes() {
		if (getPeList() != null) {
			return PeList.getNumberOfFreePes(getPeList());
		}

		return getNumberOfPes();
	}

	public boolean isFailed() {
		return failed;
	}
}
