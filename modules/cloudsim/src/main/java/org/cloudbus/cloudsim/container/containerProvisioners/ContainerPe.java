package org.cloudbus.cloudsim.container.containerProvisioners;


/**
 *
 *  @author Sareh Fotuhi Piraghaj
 */


public class ContainerPe {

    /**
     * Denotes Pe is FREE for allocation.
     */
    public static final int FREE = 1;

    /**
     * Denotes Pe is allocated and hence busy in processing Cloudlet.
     */
    public static final int BUSY = 2;

    /**
     * Denotes Pe is failed and hence it can't process any Cloudlet at this moment. This Pe is
     * failed because it belongs to a machine which is also failed.
     */
    public static final int FAILED = 3;

    /**
     * The id.
     */
    private int id;

    // FOR SPACE SHARED RESOURCE: Jan 21
    /**
     * The status of Pe: FREE, BUSY, FAILED: .
     */
    private int status;

    /**
     * The Processing Element (PE) a.k.a. CPU provisioner.
     */
    private ContainerPeProvisioner containerPeProvisioner;

    /**
     * Allocates a new Pe object.
     *
     * @param id            the Pe ID
     * @param containerPeProvisioner the pe provisioner
     * @pre id >= 0
     * @pre peProvisioner != null
     * @post $none
     */
    public ContainerPe(int id, ContainerPeProvisioner containerPeProvisioner) {
        setId(id);
        setContainerPeProvisioner(containerPeProvisioner);

        // when created it should be set to FREE, i.e. available for use.
        status = FREE;
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
     * Gets the id.
     *
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the MIPS Rating of this Pe.
     *
     * @param d the mips
     * @pre mips >= 0
     * @post $none
     */
    public void setMips(double d) {
        getContainerPeProvisioner().setMips(d);
    }

    /**
     * Gets the MIPS Rating of this Pe.
     *
     * @return the MIPS Rating
     * @pre $none
     * @post $result >= 0
     */
    public int getMips() {
        return (int) getContainerPeProvisioner().getMips();
    }

    /**
     * Gets the status of this Pe.
     *
     * @return the status of this Pe
     * @pre $none
     * @post $none
     */
    public int getStatus() {
        return status;
    }

    /**
     * Sets Pe status to free, meaning it is available for processing. This should be used by SPACE
     * shared hostList only.
     *
     * @pre $none
     * @post $none
     */
    public void setStatusFree() {
        setStatus(FREE);
    }

    /**
     * Sets Pe status to busy, meaning it is already executing Cloudlets. This should be used by
     * SPACE shared hostList only.
     *
     * @pre $none
     * @post $none
     */
    public void setStatusBusy() {
        setStatus(BUSY);
    }

    /**
     * Sets this Pe to FAILED.
     *
     * @pre $none
     * @post $none
     */
    public void setStatusFailed() {
        setStatus(FAILED);
    }

    /**
     * Sets Pe status to either <tt>Pe.FREE</tt> or <tt>Pe.BUSY</tt>
     *
     * @param status Pe status, <tt>true</tt> if it is FREE, <tt>false</tt> if BUSY.
     * @pre $none
     * @post $none
     */
    public void setStatus(int status) {
        this.status = status;
    }



    public ContainerPeProvisioner getContainerPeProvisioner() {
        return containerPeProvisioner;
    }
    /**
     * Sets the pe provisioner.
     *
     * @param containerPeProvisioner the new pe provisioner
     */
    public void setContainerPeProvisioner(ContainerPeProvisioner containerPeProvisioner) {
        this.containerPeProvisioner = containerPeProvisioner;
    }
}


