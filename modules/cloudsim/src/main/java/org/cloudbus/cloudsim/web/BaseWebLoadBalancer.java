package org.cloudbus.cloudsim.web;

import org.cloudbus.cloudsim.EX.disk.HddVm;
import org.cloudbus.cloudsim.EX.util.Id;
import org.cloudbus.cloudsim.EX.vm.VmStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Common functionality of load balancers.
 * 
 * @author nikolay.grozev
 * 
 */
public abstract class BaseWebLoadBalancer implements ILoadBalancer {

    protected final long id;
    protected final long appId;
    protected final String ip;
    protected final List<HddVm> appServers;
    protected final IDBBalancer dbBalancer;

    /**
     * Constructor.
     * 
     * @param appId
     *            - the id of the applications, which this load balancer is
     *            serving.
     * @param ip
     *            - the IP address represented in a standard Ipv4 or IPv6 dot
     *            notation.
     * @param appServers
     *            - the application servers. Must not be null.
     * @param dbBalancer
     *            - the balancer of the DB cloudlets among DB servers. Must not
     *            be null.
     */
    public BaseWebLoadBalancer(final long appId, final String ip, final List<HddVm> appServers,
            final IDBBalancer dbBalancer) {
        super();
        this.appId = appId;
        id = Id.pollId(SimpleWebLoadBalancer.class);
        this.appServers = appServers;
        this.dbBalancer = dbBalancer;
        this.ip = ip;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getAppId() {
        return appId;
    }

    @Override
    public String getIp() {
        return ip;
    }

    @Override
    public void registerAppServer(final HddVm vm) {
        appServers.add(vm);
    }

    @Override
    public List<HddVm> getAppServers() {
        return appServers;
    }

    @Override
    public List<HddVm> getRunningAppServers() {
        List<HddVm> result = new ArrayList<>();
        for (HddVm vm : getAppServers()) {
            if (vm.getStatus() == VmStatus.RUNNING) {
                result.add(vm);
            }
        }
        return result;
    }

    @Override
    public IDBBalancer getDbBalancer() {
        return dbBalancer;
    }
}
