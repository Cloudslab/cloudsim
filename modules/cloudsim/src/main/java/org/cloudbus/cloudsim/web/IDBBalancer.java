package org.cloudbus.cloudsim.web;

import org.cloudbus.cloudsim.EX.disk.HddCloudlet;
import org.cloudbus.cloudsim.EX.disk.HddVm;

import java.util.List;

/**
 * Represents a matcher between DB cloudlets and DB virtual machines. It is
 * responsible for matching DB cloudlets to virtual DB servers that have the
 * corresponding data.
 * 
 * <br>
 * <br>
 * 
 * Also it allows some cloudlets to be discarded, because of caching etc.
 * 
 * 
 * @author nikolay.grozev
 * 
 */
public interface IDBBalancer {

    /**
     * Sets the appropriate vm id of cloudlet. In case there is caching, this
     * medhod can set the clloudlet as finished, if its data is in the cache. If
     * there is no DB VM with the appropriate data, then the method sets the
     * cloudlet as failed.
     * 
     * @param cloudlet
     *            - the cloudlet to assign to a VM.
     */
    void allocateToServer(final HddCloudlet cloudlet);

    /**
     * Returns the list of VMs among which the cloudlets are distributed.
     * 
     * @return the list of VMs among which the cloudlets are distributed.
     */
    List<HddVm> getVMs();

    /**
     * Sets the list of VMs among which the cloudlets are distributed.
     * 
     * @param vms
     *            - the list of VMs among which the cloudlets are distributed.
     */
    void setVms(final List<HddVm> vms);

}
