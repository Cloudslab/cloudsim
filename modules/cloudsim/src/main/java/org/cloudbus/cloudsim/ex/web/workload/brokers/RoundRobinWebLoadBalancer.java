package org.cloudbus.cloudsim.ex.web.workload.brokers;

import org.cloudbus.cloudsim.ex.core.disk.HddVm;
import org.cloudbus.cloudsim.ex.web.IDBBalancer;
import org.cloudbus.cloudsim.ex.web.SimpleWebLoadBalancer;

import java.util.List;

/**
 * 
 * @author nikolay.grozev
 * 
 */
public class RoundRobinWebLoadBalancer extends SimpleWebLoadBalancer {

    public RoundRobinWebLoadBalancer(long appId, String ip, List<HddVm> appServers, IDBBalancer dbBalancer,
            WebBroker broker) {
        super(appId, ip, appServers, dbBalancer, broker);
    }

    protected static double evaluateSuitability(final HddVm vm) {
        return 1;
    }
}
