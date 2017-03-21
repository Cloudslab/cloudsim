package org.cloudbus.cloudsim.auction;

/**
 * Represents the characteristics in CloudSim environment.
 * TODO: DatacenterCharacteristics can extend this class in future.
 * 
 * @author Youness Teimoury
 * Blog: http://youness-teimoury.blogspot.com/
 * Email: youness126@gmail.com
 * Created on 2011/8/26
 */
public class EntityCharacteristics {

    /** The architecture. */
    private String architecture;

    /** The os. */
    private String os;

    /** The time zone -- difference from GMT. */
    private double timeZone;
    
    /** The vmm. */
    private String vmm;

    /** Price/CPU-unit if unit = sec., then G$/CPU-sec. */
    private double costPerSecond;

    /** The cost per mem. */
    private double costPerMem;

    /** The cost per storage. */
    private double costPerStorage;

    /** The cost per bw. */
    private double costPerBw;

	public EntityCharacteristics(
			String architecture,
			String os,
			String vmm,
			double timeZone,
			double costPerSec,
			double costPerMem,
			double costPerBw,
			double costPerStorage) {
		
        setArchitecture(architecture);
        setOs(os);
        setCostPerSecond(costPerSec);
        setTimeZone(0.0);
		setVmm(vmm);
		setCostPerMem(costPerMem);
		setCostPerBw(costPerBw);
		setCostPerStorage(costPerStorage);
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
	 * @param costPerMem  cost to use memory
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
	 * @param costPerStorage  cost to use storage
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
	 * Sets the vmm.
	 *
	 * @param vmm the new vmm
	 */
	protected void setVmm(String vmm) {
		this.vmm = vmm;
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

}
