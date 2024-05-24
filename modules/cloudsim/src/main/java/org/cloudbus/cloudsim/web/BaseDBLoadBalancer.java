package org.cloudbus.cloudsim.web;

import org.cloudbus.cloudsim.EX.disk.HddVm;

import java.util.Arrays;
import java.util.List;

/**
 * Implements common functionalities for DB load balancers
 * 
 * @author nikolay.grozev
 * 
 */
public abstract class BaseDBLoadBalancer implements IDBBalancer {

    protected List<HddVm> dbVms;

    /**
     * Constr.
     * 
     * @param dbVms
     *            - The list of DB vms to distribute cloudlets among.
     */
    public BaseDBLoadBalancer(final List<HddVm> dbVms) {
        this.dbVms = dbVms;
    }

    /**
     * Constr.
     * 
     * @param dbVms
     *            - The list of DB vms to distribute cloudlets among.
     */
    public BaseDBLoadBalancer(final HddVm... dbVms) {
        this(Arrays.asList(dbVms));
    }

    @Override
    public List<HddVm> getVMs() {
        return dbVms;
    }

    @Override
    public void setVms(final List<HddVm> vms) {
        dbVms = vms;
    }

}