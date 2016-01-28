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
 * Represents static properties of a resource such as 
 * architecture, Operating System (OS), management policy (time- or space-shared), 
 * cost and time zone at which the resource is located along resource configuration.
 * 
 * @author Manzur Murshed
 * @author Rajkumar Buyya
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 * @todo the characteristics are used only for datacenter (as the class name indicates),
 * however, the class documentation uses the generic term "resource" instead of "datacenter",
 * giving the idea that the class can be used to describe characteristics of other resources.
 * However, the class was found being used only for datacenters.
 */
public class DatacenterCharacteristics {

	/** The datacenter id -- setup when datacenter is created. */
	private int id;

	/** The architecture of the resource. */
	private String architecture;

	/** The Operating System (OS) of the resource. */
	private String os;

	/** The hosts owned by the datacenter. */
	private List<? extends Host> hostList;

	/** The time zone, defined as the difference from GMT. */
	private double timeZone;

	/** Price/CPU-unit. If unit = sec., then the price is defined as G$/CPU-sec. */
	private double costPerSecond;

	/** The CPU allocation policy for all PMs of the datacenter, according to
         * constants such as {@link #TIME_SHARED}
         * and {@link #SPACE_SHARED}.
         * 
         * @todo The use of int constants difficult to know the valid values
         * for the property. It may be used a enum instead.
         */
	private int allocationPolicy;

	/** Time-shared CPU allocation policy using Round-Robin algorithm. */
	public static final int TIME_SHARED = 0;

	/** Spaced-shared CPU allocation policy using First Come First Serve (FCFS) algorithm. */
	public static final int SPACE_SHARED = 1;

	/** Assuming all PEs in all PMs have the same rating. */
	public static final int OTHER_POLICY_SAME_RATING = 2;

	/**
	 * Assuming all PEs in a PM have the same rating. However, each PM has different
	 * rating to each other.
	 */
	public static final int OTHER_POLICY_DIFFERENT_RATING = 3;

	/** A resource that supports Advanced Reservation mechanisms. */
	public static final int ADVANCE_RESERVATION = 4;

	/** The Virtual Machine Monitor (VMM), also called hypervisor, used
         * in the datacenter.. */
	private String vmm;

	/** The cost per each unity of RAM memory. */
	private double costPerMem;

	/** The cost per each unit of storage. */
	private double costPerStorage;

	/** The cost of each byte of bandwidth (bw) consumed. */
	private double costPerBw;

	/**
	 * Creates a new DatacenterCharacteristics object. If the time zone is invalid, then by
	 * default, it will be GMT+0.
	 * 
	 * @param architecture the architecture of the datacenter
	 * @param os the operating system used on the datacenter's PMs
	 * @param vmm the virtual machine monitor used
	 * @param hostList list of machines in the datacenter
	 * @param timeZone local time zone of a user that owns this reservation. Time zone should be of
	 *            range [GMT-12 ... GMT+13]
	 * @param costPerSec the cost per sec of CPU use in the datacenter
	 * @param costPerMem the cost to use memory in the datacenter
	 * @param costPerStorage the cost to use storage in the datacenter
	 * @param costPerBw the cost of each byte of bandwidth (bw) consumed
         * 
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
                /*@todo allocationPolicy is not a parameter. It is setting
                the attribute to itself, what has not effect. */
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
	 * Gets the first PM with at least one empty Pe.
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
	 * Gets the Million Instructions Per Second (MIPS) Rating of the first Processing Element (Pe)
         * of the first PM. 
         * <tt>NOTE:</tt>It is assumed all PEs' rating is same in a given machine.
         * 
	 * 
	 * @return the MIPS Rating or -1 if no PEs exists
         * 
	 * @pre $none
	 * @post $result >= -1
         * @todo It considers that all PEs of all PM have the same MIPS capacity,
         * what is not ensured because it is possible to add PMs of different configurations
         * to a datacenter. Even for the {@link Host} it is possible
         * to add Pe's of different capacities through the {@link Host#peList} attribute.
	 */
	public int getMipsOfOnePe() {
		if (getHostList().size() == 0) {
			return -1;
		}

                /*@todo Why is it always get the MIPS of the first host in the datacenter?
                The note in the method states that it is considered that all PEs into
                a PM have the same MIPS capacity, but different PM can have different
                PEs' MIPS.*/
		return PeList.getMips(getHostList().get(0).getPeList(), 0);
	}

	/**
	 * Gets Millions Instructions Per Second (MIPS) Rating of a Processing Element (Pe). It is
	 * essential to use this method when a datacenter is made up of heterogenous PEs per PMs.
	 * 
	 * @param id the machine ID
	 * @param peId the Pe ID
	 * @return the MIPS Rating or -1 if no PEs are exists.
         * 
	 * @pre id >= 0
	 * @pre peID >= 0
	 * @post $result >= -1
         * 
         * @todo The id parameter would be renamed to pmId to be clear.
	 */
	public int getMipsOfOnePe(int id, int peId) {
		if (getHostList().size() == 0) {
			return -1;
		}

		return PeList.getMips(HostList.getById(getHostList(), id).getPeList(), peId);
	}

	/**
	 * Gets the total MIPS rating, which is the sum of MIPS rating of all PMs in a datacenter.
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
         * 
	 * @pre $none
	 * @post $result >= 0
	 */
	public int getMips() {
		int mips = 0;
                /*@todo It assumes that the heterogeinety of PE's capacity of PMs
                is dependent of the CPU allocation policy of the Datacenter.
                However, I don't see any relation between PMs heterogeinety and
                allocation policy.
                I can have a time shared policy in a datacenter of
                PMs with the same or different processing capacity.
                The same is true for a space shared or even any other policy. 
                */
                
                /*@todo the method doesn't use polymorphism to ensure that it will
                automatically behave according to the instance of the allocationPolicy used.
                The use of a switch here breaks the Open/Close Principle (OCP).
                Thus, it doesn't allow the class to be closed for changes
                and opened for extension.
                If a new scheduler is created, the class has to be changed
                to include the new scheduler in switches like that below.
                */
		switch (getAllocationPolicy()) {
                        // Assuming all PEs in all PMs have same rating.
                        /*@todo But it is possible to add PMs of different configurations
                            in a hostlist attached to a DatacenterCharacteristic attribute
                            of a Datacenter*/
			case DatacenterCharacteristics.TIME_SHARED:
			case DatacenterCharacteristics.OTHER_POLICY_SAME_RATING:
				mips = getMipsOfOnePe() * HostList.getNumberOfPes(getHostList());
			break;

			// Assuming all PEs in a given PM have the same rating.
			// But different PMs in a Cluster can have different rating
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
	 * Gets the amount of CPU time (in seconds) that the cloudlet will spend
         * to finish processing, considering the current CPU allocation policy 
         * (currently only for TIME_SHARED) and cloudlet load. 
         * @todo <tt>NOTE:</tt> The
	 * CPU time for SPACE_SHARED and ADVANCE_RESERVATION are not yet implemented.
	 * 
	 * @param cloudletLength the length of a Cloudlet
	 * @param load the current load of a Cloudlet (percentage of load from 0 to 1)
	 * @return the CPU time (in seconds)
         * 
	 * @pre cloudletLength >= 0.0
	 * @pre load >= 0.0
	 * @post $result >= 0.0
	 */
	public double getCpuTime(double cloudletLength, double load) {
		double cpuTime = 0.0;

		switch (getAllocationPolicy()) {
			case DatacenterCharacteristics.TIME_SHARED:
                                /*@todo It is not exacly clear what this method does.
                                I guess it computes how many time the cloudlet will
                                spend using the CPU to finish its job, considering 
                                the CPU allocation policy. By this way,
                                the load parameter may be cloudlet's the percentage of load (from 0 to 1).
                                Then, (getMipsOfOnePe() * (1.0 - load)) computes the amount
                                MIPS that is currently being used by the cloudlet.
                                Dividing the total cloudlet length in MI by that result
                                returns the number of seconds that the cloudlet will spend
                                to execute its total MI.
                                
                                This method has to be reviewed and documentation
                                checked.
                            
                                If load is equals to 1, this calculation will 
                                raise and division by zero exception, what makes invalid
                                the pre condition defined in the method documention*/
				cpuTime = cloudletLength / (getMipsOfOnePe() * (1.0 - load));
				break;

			default:
				break;
		}

		return cpuTime;
	}

	/**
	 * Gets the total number of PEs for all PMs.
	 * 
	 * @return number of PEs
	 * @pre $none
	 * @post $result >= 0
	 */
	public int getNumberOfPes() {
		return HostList.getNumberOfPes(getHostList());
	}

	/**
	 * Gets the total number of <tt>FREE</tt> or non-busy PEs for all PMs.
	 * 
	 * @return number of PEs
	 * @pre $none
	 * @post $result >= 0
	 */
	public int getNumberOfFreePes() {
		return HostList.getNumberOfFreePes(getHostList());
	}

	/**
	 * Gets the total number of <tt>BUSY</tt> PEs for all PMs.
	 * 
	 * @return number of PEs
	 * @pre $none
	 * @post $result >= 0
	 */
	public int getNumberOfBusyPes() {
		return HostList.getNumberOfBusyPes(getHostList());
	}

	/**
	 * Sets the particular Pe status on a PM.
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
	 * Gets the cost per Million Instruction (MI) associated with a Datacenter.
	 * 
	 * @return the cost using CPU of PM in the Datacenter
	 * @pre $none
	 * @post $result >= 0.0
         * @todo Again, it considers that all PEs of all PM have the same MIPS capacity,
         * what is not ensured because it is possible to add PMs of different configurations
         * to a datacenter
	 */
	public double getCostPerMi() {
		return getCostPerSecond() / getMipsOfOnePe();
	}

	/**
	 * Gets the total number of PMs.
	 * 
	 * @return total number of machines the Datacenter has.
	 */
	public int getNumberOfHosts() {
		return getHostList().size();
	}

	/**
	 * Gets the current number of failed PMs.
	 * 
	 * @return current number of failed PMs the Datacenter has.
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
	 * Checks whether all PMs of the datacenter are working properly or not.
	 * 
	 * @return if all PMs are working, otherwise
	 */
	public boolean isWorking() {
		boolean result = false;
		if (getNumberOfFailedHosts() == 0) {
			result = true;
		}

		return result;
	}

	/**
	 * Get the cost to use memory in the datacenter.
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
	 * Get the cost to use storage in the datacenter.
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
	 * Get the cost to use bandwidth in the datacenter.
	 * 
	 * @return the cost to use bw
	 */
	public double getCostPerBw() {
		return costPerBw;
	}

	/**
	 * Sets cost to use bw.
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
	 * Gets the datacenter id.
	 * 
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the datacenter id.
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
	 * Gets the Operating System (OS).
	 * 
	 * @return the Operating System (OS)
	 */
	protected String getOs() {
		return os;
	}

	/**
	 * Sets the Operating System (OS).
	 * 
	 * @param os the new Operating System (OS)
	 */
	protected void setOs(String os) {
		this.os = os;
	}

	/**
	 * Gets the host list.
	 * 
	 * @param <T> the generic type
	 * @return the host list
         * @todo check this warning below
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
	 * Gets the cost per second of CPU.
	 * 
	 * @return the cost per second
	 */
	public double getCostPerSecond() {
		return costPerSecond;
	}

	/**
	 * Sets the cost per second of CPU.
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
