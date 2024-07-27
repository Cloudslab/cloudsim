package org.cloudbus.cloudsim.web;

import org.cloudbus.cloudsim.EX.disk.HddCloudlet;
import org.cloudbus.cloudsim.EX.disk.HddVm;
import org.cloudbus.cloudsim.EX.util.CustomLog;
import org.cloudbus.cloudsim.web.workload.brokers.WebBroker;

import java.util.*;
import java.util.logging.Level;

/**
 * Implements simple load balancing - sessions are assigned to the least busy
 * (in terms of CPU) application server VMs.
 * 
 * @author nikolay.grozev
 * @author Remo Andreoli
 */
public class SimpleWebLoadBalancer extends BaseWebLoadBalancer implements ILoadBalancer {

    private long startPositionWhenEqual = 0;
    private final StringBuffer debugSB = new StringBuffer();
    WebBroker broker;

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
    public SimpleWebLoadBalancer(final long appId, final String ip, final List<HddVm> appServers,
            final IDBBalancer dbBalancer) {
        super(appId, ip, appServers, dbBalancer);
    }

    public SimpleWebLoadBalancer(final long appId, final String ip, final List<HddVm> appServers,
            final IDBBalancer dbBalancer, WebBroker broker) {
        super(appId, ip, appServers, dbBalancer);
        this.broker = broker;
    }

    @Override
    public void assignToServers(final WebSession... sessions) {
        // Filter all sessions without an assigned application server
        List<WebSession> noAppServSessions = new ArrayList<>(Arrays.asList(sessions));
        noAppServSessions.removeIf(sess -> sess.getAppVmId() != null);

        List<HddVm> runingVMs = getRunningAppServers();
        // No running AS servers - log an error
        if (runingVMs.isEmpty()) {
            for (WebSession session : noAppServSessions) {
                if (getAppServers().isEmpty()) {
                    CustomLog.printf(Level.SEVERE,
                            "Simple Load Balancer(%s): session %d cannot be scheduled, as there are no AS servers",
                            broker == null ? "N/A" : broker, session.getSessionId());
                } else {
                    CustomLog
                            .printf(Level.SEVERE,
                                    "[Simple Load Balancer](%s): session %d cannot be scheduled, as all AS servers are either booting or terminated",
                                    broker == null ? "N/A" : broker, session.getSessionId());
                }
            }
        } else {
            @SuppressWarnings("unchecked")
            Map<Integer, Integer> usedASServers = broker != null ? this.broker.getASServersToNumSessions()
                    : Collections.EMPTY_MAP;

            // Get the VMs which are utilized the least
            debugSB.setLength(0);
            List<HddVm> bestVms = new ArrayList<>();
            double bestUtilization = Double.MAX_VALUE;
            for (HddVm vm : runingVMs) {
                double vmUtilization = evaluateSuitability(vm);
                if (!vm.isOutOfMemory()) {
                    if (vmUtilization < bestUtilization) {
                        bestVms.clear();
                        bestUtilization = vmUtilization;
                        bestVms.add(vm);
                    } else if (vmUtilization == bestUtilization) {
                        bestVms.add(vm);
                    }
                }

                debugSB.append(String.format("%s[%s] cpu(%.2f), ram(%.2f), cdlts(%d), sess(%d); ", vm, vm.getStatus(),
                        vm.getCPUUtil(), vm.getRAMUtil(), vm.getCloudletScheduler().getCloudletExecList().size(),
                        usedASServers.getOrDefault(vm.getId(), 0)));
            }

            // Distribute the sessions among the best VMs
            long i = startPositionWhenEqual++;
            if (!bestVms.isEmpty()) {
                for (WebSession session : noAppServSessions) {
                    long index = i++ % bestVms.size();
                    HddVm hostVM = bestVms.get((int) index);
                    session.setAppVmId(hostVM.getId());

                    CustomLog
                            .printf("[Simple Load Balancer(%s): Assigning sesssion %d to %s[%s] cpu(%.2f), ram(%.2f), cdlts(%d), sess(%d);",
                                    broker == null ? "N/A" : broker, session.getSessionId(), hostVM,
                                    hostVM.getStatus(), hostVM.getCPUUtil(), hostVM.getRAMUtil(), hostVM
                                            .getCloudletScheduler().getCloudletExecList().size(),
                                    usedASServers.getOrDefault(hostVM.getId(), 0));
                    CustomLog.printf("[Simple Load Balancer(%s), Candidate VMs: %s", broker == null ? "N/A" : broker,
                            debugSB);

                }
            }

        }

        // Set the DB VM
        for (WebSession session : sessions) {
            if (session.getDbBalancer() == null) {
                session.setDbBalancer(getDbBalancer());
            }
        }

        // Log the state of the DB servers
        debugSB.setLength(0);
        for (HddVm dbVm : getDbBalancer().getVMs()) {
            debugSB.append(String.format("%s cpu(%.2f), ram(%.2f), disk(%.2f), cdlts(%d);", dbVm, dbVm.getCPUUtil(),
                    dbVm.getRAMUtil(), dbVm.getDiskUtil(), dbVm.getCloudletScheduler().getCloudletExecList().size()));
        }
        CustomLog.printf("[Simple Load Balancer], DB VMs: %s", debugSB);

    }

    protected static double evaluateSuitability(final HddVm vm) {
        double sumExecCloudLets = 0;
        for (HddCloudlet cloudlet : vm.getCloudletScheduler().<HddCloudlet> getCloudletExecList()) {
            sumExecCloudLets += cloudlet.getCloudletLength();
        }
        double vmMips = vm.getMips() * vm.getNumberOfPes();
        return sumExecCloudLets / vmMips;
    }
}
