/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.List;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.lists.HostList;
import org.cloudbus.cloudsim.lists.PeList;

/**
 * DatacenterCharacteristics represents static properties of a resource such as resource
 * architecture, Operating System (OS), management policy (time- or space-shared), cost and time
 * zone at which the resource is located along resource configuration.
 * 
 * @author Manzur Murshed
 * @author Rajkumar Buyya
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class DatacenterCharacteristics {

	/** The resource id -- setup when Resource is created. */
	private int id;

	/** The architecture. */
	private String architecture;

	/** The os. */
	private String os;

	/** The host list. */
	private List<? extends Host> hostList;

	/** The time zone -- difference from GMT. */
	private double timeZone;

	/** Price/CPU-unit if unit = sec., then G$/CPU-sec. */
	private double costPerSecond;

	/** Resource Types -- allocation policy. */
	private int allocationPolicy;

	/** Time-shared system using Round-Robin algorithm. */
	public static final int TIME_SHARED = 0;

	/** Spaced-shared system using First Come First Serve (FCFS) algorithm. */
	public static final int SPACE_SHARED = 1;

	/** Assuming all PEs in all Machines have the same rating. */
	public static final int OTHER_POLICY_SAME_RATING = 2;

	/**
	 * Assuming all PEs in a Machine have the same rating. However, each Machine has different
	 * rating to each other.
	 */
	public static final int OTHER_POLICY_DIFFERENT_RATING = 3;

	/** A resource that supports Advanced Reservation mechanisms. */
	public static final int ADVANCE_RESERVATION = 4;

	/** The vmm. */
	private String vmm;

	/** The cost per mem. */
	private double costPerMem;

	/** The cost per storage. */
	private double costPerStorage;

	/** The cost per bw. */
	private double costPerBw;

	/**
	 * Allocates a new DatacenterCharacteristics object. If the time zone is invalid, then by
	 * default, it will be GMT+0.
	 * 
	 * @param architecture the architecture of a resource
	 * @param os the operating system used
	 * @param vmm the virtual machine monitor used
	 * @param hostList list of machines in a resource
	 * @param timeZone local time zone of a user that owns this reservation. Time zone should be of
	 *            range [GMT-12 ... GMT+13]
	 * @param costPerSec the cost per sec to use this resource
	 * @param costPerMem the cost to use memory in this resource
	 * @param costPerStorage the cost to use storage in this resource
	 * @param costPerBw the cost per bw
	 * @pre architecture != null
	 * @pre OS != null
	 * @pre VMM != null
	 * @pre machineList != null
	 * @pre timeZone >= -12 && timeZone <= 13
	 * @pre costPerSec >= 0.0
	 * @pre costPerMem >= 0
	 * @pre costPerStorage >= 0
	 * @post $none
	 */
	public DatacenterCharacteristics(
			String architecture,
			String os,
			String vmm,
			List<? extends Host> hostList,
			double timeZone,
			double costPerSec,
			double costPerMem,
			double costPerStorage,
			double costPerBw) {
		setId(-1);
		setArchitecture(architecture);
		setOs(os);
		setHostList(hostList);
		setAllocationPolicy(allocationPolicy);
		setCostPerSecond(costPerSec);

		setTimeZone(0.0);

		setVmm(vmm);
		setCostPerMem(costPerMem);
		setCostPerStorage(costPerStorage);
		setCostPerBw(costPerBw);
	}

	/**
	 * Gets the name of a resource.
	 * 
	 * @return the resource name
	 * @pre $none
	 * @post $result != null
	 */
	public String getResourceName() {
		return CloudSim.getEntityName(getId());
	}

	/**
	 * Gets a Machine with at least one empty Pe.
	 * 
	 * @return a Machine object or if not found
	 * @pre $none
	 * @post $none
	 */
	public Host getHostWithFreePe() {
		return HostList.getHostWithFreePe(getHostList());
	}

	/**
	 * Gets a Machine with at least a given number of free Pe.
	 * 
	 * @param peNumber the pe number
	 * @return a Machine object or if not found
	 * @pre $none
	 * @post $none
	 */
	public Host getHostWithFreePe(int peNumber) {
		return HostList.getHostWithFreePe(getHostList(), peNumber);
	}

	/**
	 * Gets Millions Instructions Per Second (MIPS) Rating of a Processing Element (Pe). It is
	 * assumed all PEs' rating is same in a given machine.
	 * 
	 * @return the MIPS Rating or if no PEs are exists.
	 * @pre $none
	 * @post $result >= -1
	 */
	public int getMipsOfOnePe() {
		if (getHostList().size() == 0) {
			return -1;
		}

		return PeList.getMips(getHostList().get(0).getPeList(), 0);
	}

	/**
	 * Gets Millions Instructions Per Second (MIPS) Rating of a Processing Element (Pe). It is
	 * essential to use this method when a resource is made up of heterogenous PEs/machines.
	 * 
	 * @param id the machine ID
	 * @param peId the Pe ID
	 * @return the MIPS Rating or if no PEs are exists.
	 * @pre id >= 0
	 * @pre peID >= 0
	 * @post $result >= -1
	 */
	public int getMipsOfOnePe(int id, int peId) {
		if (getHostList().size() == 0) {
			return -1;
		}

		return PeList.getMips(HostList.getById(getHostList(), id).getPeList(), peId);
	}

	/**
	 * Gets the total MIPS rating, which is the sum of MIPS rating of all machines in a resource.
	 * <p>
	 * Total MIPS rating for:
	 * <ul>
	 * <li>TimeShared = 1 Rating of a Pe * Total number of PEs
	 * <li>Other policy same rating = same as TimeShared
	 * <li>SpaceShared = Sum of all PEs in all Machines
	 * <li>Other policy different rating = same as SpaceShared
	 * <li>Advance Reservation = 0 or unknown. You need to calculate this manually.
	 * </ul>
	 * 
	 * @return the sum of MIPS ratings
	 * @pre $none
	 * @post $result >= 0
	 */
	public int getMips() {
		int mips = 0;
		switch (getAllocationPolicy()) {
		// Assuming all PEs in all Machine have same rating.
			case DatacenterCharacteristics.TIME_SHARED:
			case DatacenterCharacteristics.OTHER_POLICY_SAME_RATING:
				mips = getMipsOfOnePe() * HostList.getNumberOfPes(getHostList());
				break;

			// Assuming all PEs in a given Machine have the same rating.
			// But different machines in a Cluster can have different rating
			case DatacenterCharacteristics.SPACE_SHARED:
			case DatacenterCharacteristics.OTHER_POLICY_DIFFERENT_RATING:
				for (Host host : getHostList()) {
					mips += host.getTotalMips();
				}
				break;

			default:
				break;
		}

		return mips;
	}

	/**
	 * Gets the CPU time given the specified parameters (only for TIME_SHARED). <tt>NOTE:</tt> The
	 * CPU time for SPACE_SHARED and ADVANCE_RESERVATION are not yet implemented.
	 * 
	 * @param cloudletLength the length of a Cloudlet
	 * @param load the load of a Cloudlet
	 * @return the CPU time
	 * @pre cloudletLength >= 0.0
	 * @pre load >= 0.0
	 * @post $result >= 0.0
	 */
	public double getCpuTime(double cloudletLength, double load) {
		double cpuTime = 0.0;

		switch (getAllocationPolicy()) {
			case DatacenterCharacteristics.TIME_SHARED:
				cpuTime = cloudletLength / (getMipsOfOnePe() * (1.0 - load));
				break;

			default:
				break;
		}

		return cpuTime;
	}

	/**
	 * Gets the total number of PEs for all Machines.
	 * 
	 * @return number of PEs
	 * @pre $none
	 * @post $result >= 0
	 */
	public int getNumberOfPes() {
		return HostList.getNumberOfPes(getHostList());
	}

	/**
	 * Gets the total number of <tt>FREE</tt> or non-busy PEs for all Machines.
	 * 
	 * @return number of PEs
	 * @pre $none
	 * @post $result >= 0
	 */
	public int getNumberOfFreePes() {
		return HostList.getNumberOfFreePes(getHostList());
	}

	/**
	 * Gets the total number of <tt>BUSY</tt> PEs for all Machines.
	 * 
	 * @return number of PEs
	 * @pre $none
	 * @post $result >= 0
	 */
	public int getNumberOfBusyPes() {
		return HostList.getNumberOfBusyPes(getHostList());
	}

	/**
	 * Sets the particular Pe status on a Machine.
	 * 
	 * @param status Pe status, either <tt>Pe.FREE</tt> or <tt>Pe.BUSY</tt>
	 * @param hostId Machine ID
	 * @param peId Pe id
	 * @return otherwise (Machine id or Pe id might not be exist)
	 * @pre machineID >= 0
	 * @pre peID >= 0
	 * @post $none
	 */
	public boolean setPeStatus(int status, int hostId, int peId) {
		return HostList.setPeStatus(getHostList(), status, hostId, peId);
	}

	/**
	 * Gets the cost per Millions Instruction (MI) associated with a resource.
	 * 
	 * @return the cost using a resource
	 * @pre $none
	 * @post $result >= 0.0
	 */
	public double getCostPerMi() {
		return getCostPerSecond() / getMipsOfOnePe();
	}

	/**
	 * Gets the total number of machines.
	 * 
	 * @return total number of machines this resource has.
	 */
	public int getNumberOfHosts() {
		return getHostList().size();
	}

	/**
	 * Gets the current number of failed machines.
	 * 
	 * @return current number of failed machines this resource has.
	 */
	public int getNumberOfFailedHosts() {
		int numberOfFailedHosts = 0;
		for (Host host : getHostList()) {
			if (host.isFailed()) {
				numberOfFailedHosts++;
			}
		}
		return numberOfFailedHosts;
	}

	/**
	 * Checks whether all machines of this resource are working properly or not.
	 * 
	 * @return if all machines are working, otherwise
	 */
	public boolean isWorking() {
		boolean result = false;
		if (getNumberOfFailedHosts() == 0) {
			result = true;
		}

		return result;
	}

	/**
	 * Get the cost to use memory in this resource.
	 * 
	 * @return the cost to use memory
	 */
	public double getCostPerMem() {
		return costPerMem;
	}

	/**
	 * Sets cost to use memory.
	 * 
	 * @param costPerMem cost to use memory
	 * @pre costPerMem >= 0
	 * @post $none
	 */
	public void setCostPerMem(double costPerMem) {
		this.costPerMem = costPerMem;
	}

	/**
	 * Get the cost to use storage in this resource.
	 * 
	 * @return the cost to use storage
	 */
	public double getCostPerStorage() {
		return costPerStorage;
	}

	/**
	 * Sets cost to use storage.
	 * 
	 * @param costPerStorage cost to use storage
	 * @pre costPerStorage >= 0
	 * @post $none
	 */
	public void setCostPerStorage(double costPerStorage) {
		this.costPerStorage = costPerStorage;
	}

	/**
	 * Get the cost to use bandwidth in this resource.
	 * 
	 * @return the cost to use bw
	 */
	public double getCostPerBw() {
		return costPerBw;
	}

	/**
	 * Sets cost to use bw cost to use bw.
	 * 
	 * @param costPerBw the cost per bw
	 * @pre costPerBw >= 0
	 * @post $none
	 */
	public void setCostPerBw(double costPerBw) {
		this.costPerBw = costPerBw;
	}

	/**
	 * Gets the VMM in use in the datacenter.
	 * 
	 * @return the VMM name
	 */
	public String getVmm() {
		return vmm;
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the id.
	 * 
	 * @param id the new id
	 */
	protected void setId(int id) {
		this.id = id;
	}

	/**
	 * Gets the architecture.
	 * 
	 * @return the architecture
	 */
	protected String getArchitecture() {
		return architecture;
	}

	/**
	 * Sets the architecture.
	 * 
	 * @param architecture the new architecture
	 */
	protected void setArchitecture(String architecture) {
		this.architecture = architecture;
	}

	/**
	 * Gets the os.
	 * 
	 * @return the os
	 */
	protected String getOs() {
		return os;
	}

	/**
	 * Sets the os.
	 * 
	 * @param os the new os
	 */
	protected void setOs(String os) {
		this.os = os;
	}

	/**
	 * Gets the host list.
	 * 
	 * @param <T> the generic type
	 * @return the host list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Host> List<T> getHostList() {
		return (List<T>) hostList;
	}

	/**
	 * Sets the host list.
	 * 
	 * @param <T> the generic type
	 * @param hostList the new host list
	 */
	protected <T extends Host> void setHostList(List<T> hostList) {
		this.hostList = hostList;
	}

	/**
	 * Gets the time zone.
	 * 
	 * @return the time zone
	 */
	protected double getTimeZone() {
		return timeZone;
	}

	/**
	 * Sets the time zone.
	 * 
	 * @param timeZone the new time zone
	 */
	protected void setTimeZone(double timeZone) {
		this.timeZone = timeZone;
	}

	/**
	 * Gets the cost per second.
	 * 
	 * @return the cost per second
	 */
	public double getCostPerSecond() {
		return costPerSecond;
	}

	/**
	 * Sets the cost per second.
	 * 
	 * @param costPerSecond the new cost per second
	 */
	protected void setCostPerSecond(double costPerSecond) {
		this.costPerSecond = costPerSecond;
	}

	/**
	 * Gets the allocation policy.
	 * 
	 * @return the allocation policy
	 */
	protected int getAllocationPolicy() {
		return allocationPolicy;
	}

	/**
	 * Sets the allocation policy.
	 * 
	 * @param allocationPolicy the new allocation policy
	 */
	protected void setAllocationPolicy(int allocationPolicy) {
		this.allocationPolicy = allocationPolicy;
	}

	/**
	 * Sets the vmm.
	 * 
	 * @param vmm the new vmm
	 */
	protected void setVmm(String vmm) {
		this.vmm = vmm;
	}
}
