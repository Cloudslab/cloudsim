/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.core.HostEntity;
import org.cloudbus.cloudsim.core.VirtualEntity;
import org.cloudbus.cloudsim.lists.PeList;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a Virtual Machine (VM) that runs inside a Host, sharing a hostList with other VMs. It can have nested
 * guest entities running inside. It processes cloudlets. This processing happens according to a policy,
 * defined by the CloudletScheduler. Each VM has a owner, which can submit cloudlets to the VM to execute them.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @author Remo Andreoli
 * @since CloudSim Toolkit 1.0
 */
public class Vm implements VirtualEntity {

	/** The VM unique id. */
	private final int id;

	/** The user id. */
	private int userId;

	/** A Unique Identifier (UID) for the VM, that is compounded by the user id and VM id. */
	private String uid;

	/** The size the VM image size (the amount of storage it will use, at least initially). */
	private long size;

	/** The MIPS capacity of each VM's PE. */
	private double mips;

	/** The number of PEs required by the VM. */
	private int numberOfPes;

	/** The required ram. */
	private int ram;

	/** The required bw. */
	private long bw;

	/** The Virtual Machine Monitor (VMM) that manages the VM. */
	private String vmm;

	/** The Cloudlet scheduler the VM uses to schedule cloudlets execution. */
	private CloudletScheduler cloudletScheduler;

	/** The Guest scheduler the Vm uses to schedule nested guest entities, such as containers. */
	private VmScheduler guestScheduler;

	/** The PM that hosts the VM. */
	private HostEntity host;

	/** Indicates if the VM is in migration process. */
	private boolean inMigration;
	
	/** Indicates if the VM is in Pause State (e.g. Stop & copy phase) */
	private boolean inPause;

	/** Indicates if the VM is waiting for nested guest entities to come. */
	private boolean inWaiting;

	/** The current allocated storage size. */
	private long currentAllocatedSize;

	/** The current allocated ram. */
	private int currentAllocatedRam;

	/** The current allocated bw. */
	private long currentAllocatedBw;

	/** The current allocated mips for each VM's PE.
	 * @TODO: Maybe replace with a call to getCloudletScheduler().getCurrentMipsShare()
	 */
	private List<Double> currentAllocatedMips;

	/** Indicates if the VM is being instantiated. */
	private boolean beingInstantiated;

	/** The ram provisioner for (nested) guest entities. */
	private RamProvisioner guestRamProvisioner;

	/** The bw provisioner for (nested) guest entities. */
	private BwProvisioner guestBwProvisioner;

	/** The pe list for nested guest entities. */
	private List<? extends Pe> peList;

	/** The nested guest list. */
	private final List<? extends GuestEntity> guestList = new ArrayList<>();

	/** The nested guests migrating in. */
	private final List<? extends GuestEntity> guestsMigratingIn = new ArrayList<>();

	/** Tells whether this VM is working properly (as a host for nested guests) or has failed. */
	private boolean failed;

	/** The mips allocation history.
	 * @TODO Instead of using a list, this attribute would be
	 * a map, where the key can be the history time
	 * and the value the history itself.
	 * By this way, if one wants to get the history for a given
	 * time, he/she doesn't have to iterate over the entire list
	 * to find the desired entry.
	 */
	private final List<VmStateHistoryEntry> stateHistory = new LinkedList<>();

	private int virtualizationOverhead;

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
		this.id = id;
		setUserId(userId);
		setUid(GuestEntity.getUid(userId, id));
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

		if (PeList.getTotalMips(peList) > mips) {
			throw new RuntimeException("PeList.getTotalMips(peList) > mips");
		}

		if (containerRamProvisioner.getRam() > getRam()) {
			throw new RuntimeException("ramProvisioner.ram > ram");
		}

		if (containerBwProvisioner.getBw() > getBw()) {
			throw new RuntimeException("bwProvisioner.bw > bw");
		}
		
		setGuestScheduler(guestScheduler);

		setPeList(peList);
		setGuestRamProvisioner(containerRamProvisioner);
		setGuestBwProvisioner(containerBwProvisioner);
		setVirtualizationOverhead(0);
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
			if (time1 < smallerTime) {
				smallerTime = time1;
			}

			// Cloudlets hosted in nested guests (if any)
			for (GuestEntity guest : getGuestList()) {
				double time2 = guest.updateCloudletsProcessing(currentTime, getGuestScheduler().getAllocatedMipsForGuest(guest));
				if (time2 > 0 && time2 < smallerTime) {
					smallerTime = time2;
				}
			}

			return smallerTime;
		}
		return 0.0;
	}

	/**
	 * Requests updating of cloudlets' processing within the (possibly nested) guests running in this host.
	 * @param currentTime the current time
	 * @return
	 */
	public double updateCloudletsProcessing(double currentTime) {
		double smallerTime = Double.MAX_VALUE;

		for (GuestEntity guest : getGuestList()) {
			double time = guest.updateCloudletsProcessing(currentTime, getGuestScheduler().getAllocatedMipsForGuest(guest));
			if (time > 0.0 && time < smallerTime) {
				smallerTime = time;
			}
			double totalRequestedMips = guest.getCurrentRequestedTotalMips();
			double totalAllocatedMips = getGuestScheduler().getTotalAllocatedMipsForGuest(guest);
			guest.addStateHistoryEntry(
					currentTime,
					totalAllocatedMips,
					totalRequestedMips,
					(guest.isInMigration() && !getGuestsMigratingIn().contains(guest)));
		}

		return smallerTime;
	}

	@Deprecated
	public double updateVmProcessing(double currentTime, List<Double> mipsShare) {
		return updateCloudletsProcessing(currentTime, mipsShare);
	}

	/**
	 * Gets the current requested mips .
	 *
	 * @return the current requested mips
	 */
	public List<Double> getCurrentRequestedMips() {
		List<Double> currentRequestedMips;

		if (isBeingInstantiated()) {
			currentRequestedMips = new ArrayList<>(getNumberOfPes());
			for (int i = 0; i < getNumberOfPes(); i++) {
				currentRequestedMips.add(getMips());
			}
		} else {
			currentRequestedMips = getCloudletScheduler().getCurrentRequestedMips();
			for (GuestEntity guest : getGuestList()) {
				currentRequestedMips.addAll(guest.getCurrentRequestedMips());
			}
		}

		return currentRequestedMips;
	}

	@Override
	public double getCurrentRequestedTotalMips() {
		double currentRequestedMips = 0.0;

		if (isBeingInstantiated()) {
			currentRequestedMips += getMips() * getNumberOfPes();
		} else {
			currentRequestedMips += getCloudletScheduler().getCurrentRequestedTotalMips();
			for (GuestEntity guest : getGuestList()) {
				currentRequestedMips += guest.getCurrentRequestedTotalMips();
			}
		}

		return currentRequestedMips;
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
		for (GuestEntity guest : getGuestList()) {
			requestedBwTemp += guest.getCurrentRequestedBw();
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
		for (GuestEntity guest : getGuestList()) {
			requestedRamTemp += guest.getCurrentRequestedRam();
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
		for (GuestEntity guest : getGuestList()) {
			TotalUtilizationOfCpuTemp += guest.getTotalUtilizationOfCpu(time);
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
	 * Returns the MIPS share of each Pe that is allocated to a given guest entity.
	 *
	 * @param guest the guest entity
	 * @return a list containing the amount of MIPS of each pe that is available to the guest
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

	public CloudletScheduler getCloudletScheduler() { return cloudletScheduler; }

	@Override
	public int getVirtualizationOverhead() { return virtualizationOverhead; }

	@Override
	public void setVirtualizationOverhead(int overhead) { virtualizationOverhead = overhead; }

	public void setCloudletScheduler(CloudletScheduler cloudletScheduler) { this.cloudletScheduler = cloudletScheduler; }

	public VmScheduler getGuestScheduler() { return guestScheduler; }
	public void setGuestScheduler(VmScheduler guestScheduler) { this.guestScheduler = guestScheduler; }

	public int getId() { return id; }

	public long getBw() { return bw; }
	public void setBw(long bw) { this.bw = bw; }

	public int getRam() { return ram; }
	public void setRam(int ram) { this.ram = ram; }

	public int getNumberOfPes() { return numberOfPes; }
	public void setNumberOfPes(int numberOfPes) { this.numberOfPes = numberOfPes; }

	public double getMips() { return mips; }
	public void setMips(double mips) { this.mips = mips; }

	public long getSize() { return size; }
	public void setSize(long size) { this.size = size; }

	public String getUid() { return uid; }
	public void setUid(String uid) { this.uid = uid; }

	public int getUserId() { return userId; }
	public void setUserId(int userId) { this.userId = userId; }

	public String getVmm() { return vmm; }
	public void setVmm(String vmm) { this.vmm = vmm; }

	public void setHost(HostEntity host) { this.host = host; }

	public List<? extends GuestEntity> getGuestList() { return guestList; }
	public List<? extends GuestEntity> getGuestsMigratingIn() { return guestsMigratingIn; }

	public boolean isInPause() { return inPause; }
	public void setInPause(boolean inPause) { this.inPause = inPause; }

	public boolean isInMigration() { return inMigration; }
	public void setInMigration(boolean inMigration) { this.inMigration = inMigration; }

	public boolean isInWaiting() { return inWaiting; }
	public void setInWaiting(boolean inWaiting) { this.inWaiting = inWaiting; }

	public long getCurrentAllocatedSize() { return currentAllocatedSize; }
	public void setCurrentAllocatedSize(long currentAllocatedSize) { this.currentAllocatedSize = currentAllocatedSize; }

	public long getCurrentAllocatedBw() { return currentAllocatedBw; }
	public void setCurrentAllocatedBw(long currentAllocatedBw) { this.currentAllocatedBw = currentAllocatedBw; }

	public int getCurrentAllocatedRam() { return currentAllocatedRam; }
	public void setCurrentAllocatedRam(int currentAllocatedRam) { this.currentAllocatedRam = currentAllocatedRam; }

	public List<Double> getCurrentAllocatedMips() { return currentAllocatedMips; }
	public void setCurrentAllocatedMips(List<Double> currentAllocatedMips) {
		this.currentAllocatedMips = currentAllocatedMips;
	}

	public boolean isBeingInstantiated() { return beingInstantiated; }
	public void setBeingInstantiated(boolean beingInstantiated) { this.beingInstantiated = beingInstantiated; }

	public RamProvisioner getGuestRamProvisioner() { return guestRamProvisioner; }
	public void setGuestRamProvisioner(RamProvisioner guestRamProvisioner) {
		this.guestRamProvisioner = guestRamProvisioner;
	}

	public BwProvisioner getGuestBwProvisioner() { return guestBwProvisioner; }
	public void setGuestBwProvisioner(BwProvisioner guestBwProvisioner) { this.guestBwProvisioner = guestBwProvisioner; }

	public List<? extends Pe> getPeList() { return peList; }
	public void setPeList(List<? extends Pe> peList) { this.peList = peList; }
	/**
	 * @TODO: I'm not sure if this is alright, but I need it for compatibility reasons with default guestCreate()
	 */
	public long getStorage() {
		return getSize();
	}
	public void setStorage(long storage) { setSize(storage); }

	public Datacenter getDatacenter() {
		return getHost().getDatacenter();
	}

	public void setDatacenter(Datacenter datacenter) {
		if (!isBeingInstantiated()) { // wait for the Vm to be bound to a host
			getHost().setDatacenter(datacenter);
		}
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

	public HostEntity getHost() { return host; }

	public List<VmStateHistoryEntry> getStateHistory() { return stateHistory; }

	/**
	 * DEPRECATED: USE GuestEntity.getUid(userId, vmId) instead!
	 */
	@Deprecated
	public static String getUid(int userId, int vmId) {
		return userId + "-" + vmId;
	}
}
