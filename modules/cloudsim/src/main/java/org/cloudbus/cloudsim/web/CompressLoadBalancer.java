package org.cloudbus.cloudsim.web;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.EX.disk.HddVm;
import org.cloudbus.cloudsim.EX.util.CustomLog;
import org.cloudbus.cloudsim.EX.vm.MonitoredVmEX;
import org.cloudbus.cloudsim.web.workload.brokers.WebBroker;

import java.util.*;
import java.util.logging.Level;

/**
 * 
 * A load balancer, which compresses the load into the smallest number of VMs
 * whose utilisation is below certain thresholds.
 * 
 * @author nikolay.grozev
 * 
 */
public class CompressLoadBalancer extends BaseWebLoadBalancer implements ILoadBalancer {

    private final CPUUtilisationComparator cpuUtilReverseComparator;
    private final WebBroker broker;
    private final double cpuThreshold;
    private final double ramThreshold;

    private final StringBuilder debugSB = new StringBuilder();

    private final LinkedHashMap<Integer, Integer> secsToArrivals = new LinkedHashMap<>();

    /**
     * Const.
     * 
     * @param appId
     * @param ip
     * @param appServers
     * @param dbBalancer
     * @param cpuThreshold
     *            - the CPU threshold. Must be in the interval [0, 1].
     * @param ramThreshold
     *            - the RAM threshold. Must be in the interval [0, 1].
     */
    public CompressLoadBalancer(WebBroker broker, final long appId, final String ip, final List<HddVm> appServers,
            final IDBBalancer dbBalancer, double cpuThreshold, double ramThreshold) {
        super(appId, ip, appServers, dbBalancer);
        this.cpuThreshold = cpuThreshold;
        this.ramThreshold = ramThreshold;

        this.broker = broker;
        cpuUtilReverseComparator = new CPUUtilisationComparator();
    }

    @Override
    public void assignToServers(final WebSession... sessions) {
        // Filter all sessions without an assigned application server
        List<WebSession> noAppServSessions = new ArrayList<>(Arrays.asList(sessions));
        noAppServSessions.removeIf(sess -> sess.getAppVmId() != null);

        updateNumberOfSessions(noAppServSessions, (int) CloudSim.clock());

        List<HddVm> runingVMs = getRunningAppServers();
        // No running AS servers - log an error
        if (runingVMs.isEmpty()) {
            for (WebSession session : noAppServSessions) {
                if (getAppServers().isEmpty()) {
                    CustomLog.printf(Level.SEVERE,
                            "Load Balancer(%s): session %d cannot be scheduled, as there are no AS servers",
                            this.broker.toString(), session.getSessionId());
                } else {
                    CustomLog
                            .printf(Level.SEVERE,
                                    "[Load Balancer](%s): session %d cannot be scheduled, as all AS servers are either booting or terminated",
                                    this.broker.toString(), session.getSessionId());
                }
            }
        } else {// Assign to one of the running VMs
            for (WebSession session : noAppServSessions) {
                List<HddVm> vms = new ArrayList<>(runingVMs);
                Map<Integer, Integer> usedASServers = this.broker.getASServersToNumSessions();
                cpuUtilReverseComparator.setUsedASServers(usedASServers.keySet());
                vms.sort(cpuUtilReverseComparator);

                // For debug purposes:
                debugSB.setLength(0);
                for (HddVm vm : vms) {
                    debugSB.append(String.format("%s[%s] cpu(%.2f), ram(%.2f), cdlts(%d), sess(%d); ", vm,
                            (usedASServers.containsKey(vm.getId()) ? "" : "FREE, ") + vm.getStatus(), vm.getCPUUtil(),
                            vm.getRAMUtil(), vm.getCloudletScheduler().getCloudletExecList().size(),
                            usedASServers.getOrDefault(vm.getId(), 0)));
                }

                HddVm hostVM = vms.get(vms.size() - 1);
                for (HddVm vm : vms) {
                    if (vm.getCPUUtil() < cpuThreshold && vm.getRAMUtil() < ramThreshold && !vm.isOutOfMemory()) {
                        hostVM = vm;
                        break;
                    }
                }

                session.setAppVmId(hostVM.getId());
                CustomLog
                        .printf("[Load Balancer](%s): Assigning sesssion %d to %s[%s] cpu(%.2f), ram(%.2f), cdlts(%d), sess(%d);",
                                broker, session.getSessionId(), hostVM, hostVM.getStatus(), hostVM.getCPUUtil(),
                                hostVM.getRAMUtil(), hostVM.getCloudletScheduler().getCloudletExecList().size(),
                                usedASServers.getOrDefault(hostVM.getId(), 0));
                CustomLog.printf("[Load Balancer](%s), Candidate VMs: %s", broker, debugSB);

                // Log the state of the DB servers
                debugSB.setLength(0);
                for (HddVm dbVm : getDbBalancer().getVMs()) {
                    debugSB.append(String.format("%s cpu(%.2f), ram(%.2f), disk(%.2f), cdlts(%d);", dbVm,
                            dbVm.getCPUUtil(), dbVm.getRAMUtil(), dbVm.getDiskUtil(), dbVm.getCloudletScheduler()
                                    .getCloudletExecList().size()));
                }
                CustomLog.printf("[Load Balancer](%s), DB VMs: %s", broker, debugSB);
            }

            // Set the DB VM
            for (WebSession session : sessions) {
                if (session.getDbBalancer() == null) {
                    session.setDbBalancer(getDbBalancer());
                }
            }
        }
    }

    private void updateNumberOfSessions(List<WebSession> noAppServSessions, int time) {
        int secsToKeep = 60;
        if (noAppServSessions == null || !noAppServSessions.isEmpty()) {
            secsToArrivals.put(time,
                    secsToArrivals.getOrDefault(time, 0));
        }
        for (Iterator<Integer> iter = secsToArrivals.keySet().iterator(); iter.hasNext();) {
            int recorededTime = iter.next();
            if (recorededTime + secsToKeep < time) {
                iter.remove();
            } else {
                break;
            }
        }
    }

    public int getNumSessionsOverLastMinute() {
        updateNumberOfSessions(null, (int) CloudSim.clock());
        int result = 0;
        for (Map.Entry<Integer, Integer> e : secsToArrivals.entrySet()) {
            result += e.getValue();
        }
        return result;
    }

    private static class CPUUtilisationComparator implements Comparator<MonitoredVmEX> {

        private Set<Integer> usedASServers;

        public void setUsedASServers(Set<Integer> usedASServers) {
            this.usedASServers = usedASServers;
        }

        @Override
        public int compare(final MonitoredVmEX vm1, final MonitoredVmEX vm2) {
            if (!usedASServers.contains(vm1.getId()) && !usedASServers.contains(vm2.getId())) {
                return 0;
            } else if (!usedASServers.contains(vm1.getId())) {
                return 1;
            } else if (!usedASServers.contains(vm2.getId())) {
                return -1;
            } else {
                return -Double.compare(vm1.getCPUUtil(), vm2.getCPUUtil());
            }
        }
    }
}
