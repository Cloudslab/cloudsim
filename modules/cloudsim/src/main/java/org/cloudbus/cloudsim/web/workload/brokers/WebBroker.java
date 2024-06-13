package org.cloudbus.cloudsim.web.workload.brokers;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudActionTags;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.EX.MonitoringBrokerEX;
import org.cloudbus.cloudsim.EX.util.CustomLog;
import org.cloudbus.cloudsim.web.*;
import org.cloudbus.cloudsim.web.workload.IWorkloadGenerator;

import java.util.*;
import java.util.logging.Level;

/**
 * A broker that takes care of the submission of web sessions to the data center
 * it handles. The broker submits the cloudlets of the provided web sessions
 * continuously over a specified period. Consequently clients must specify the
 * endpoint (in terms of time) of the simulation.
 * 
 * @author nikolay.grozev
 * 
 */
public class WebBroker extends MonitoringBrokerEX {
    private boolean isTimerRunning = false;
    private final double stepPeriod;
    private final Map<Long, ILoadBalancer> appsToLoadBalancers = new HashMap<>();
    private final Map<Long, List<IWorkloadGenerator>> appsToGenerators = new HashMap<>();

    private final LinkedHashMap<Integer, WebSession> activeSessions = new LinkedHashMap<>();
    private final List<WebSession> completedSessions = new ArrayList<>();
    private final List<WebSession> canceledSessions = new ArrayList<>();

    /** Mapping of application Ids to entry points. */
    private final Map<Long, IEntryPoint> entryPoins = new HashMap<>();

    private final String[] metadata;

    /**
     * By default CloudSim's brokers use all available datacenters. So we need
     * to enforce only the data center we want.
     */
    private final int dataCenterId;

    /**
     * Creates a new web broker.
     * 
     * @param name
     *            - the name of the broker.
     * @param refreshPeriod
     *            - the period of polling web sessions for new cloudlets.
     * @param lifeLength
     *            - the length of the simulation.
     * @param dataCenterId
     *            - the ids of the datacenters this broker operates with. If
     *            null all present data centers are used.
     * 
     * @throws Exception
     *             - if something goes wrong. See the documentation of the super
     *             class.
     */
    public WebBroker(final String name, final double refreshPeriod, final double lifeLength,
            final double monitoringPeriod, final double autoscalePeriod, final int dataCenterId,
            final String... metadata) throws Exception {
        super(name, lifeLength, monitoringPeriod, autoscalePeriod);
        this.stepPeriod = refreshPeriod;
        this.dataCenterId = dataCenterId;
        this.metadata = metadata;
    }

    /**
     * Creates a new web broker.
     * 
     * @param name
     *            - the name of the broker.
     * @param refreshPeriod
     *            - the period of polling web sessions for new cloudlets.
     * @param lifeLength
     *            - the length of the simulation.
     * @param dataCenterId
     *            - the ids of the datacenters this broker operates with. If
     *            null all present data centers are used.
     * 
     * @throws Exception
     *             - if something goes wrong. See the documentation of the super
     *             class.
     */
    public WebBroker(final String name, final double refreshPeriod, final double lifeLength, final int dataCenterId)
            throws Exception {
        this(name, refreshPeriod, lifeLength, 0.1, -1, dataCenterId);
    }

    /**
     * Returns the id of the datacentre that this web broker handles.
     * 
     * @return the id of the datacentre that this web broker handles.
     */
    public int getDataCenterId() {
        return dataCenterId;
    }

    /**
     * Returns the sessions canceled due to lack of resources to serve them.
     * 
     * @return the session that were canceled due to lack of resources to serve
     *         them.
     */
    public List<WebSession> getCanceledSessions() {
        return canceledSessions;
    }

    public String[] getMetadata() {
        return metadata;
    }

    /**
     * Returns the sessions that were successfully served.
     * 
     * @return the sessions that were successfully served.
     */
    public List<WebSession> getServedSessions() {
        List<WebSession> result = new ArrayList<>(completedSessions);
        result.addAll(activeSessions.values());
        return result;
    }

    /**
     * Returns the load balancers of this broker.
     * 
     * @return the load balancers of this broker.
     */
    public Map<Long, ILoadBalancer> getLoadBalancers() {
        return appsToLoadBalancers;
    }

    public double getStepPeriod() {
        return stepPeriod;
    }

    @Override
    public void processEvent(final SimEvent ev) {
        if (!isTimerRunning) {
            isTimerRunning = true;
            sendNow(getId(), WebTags.TIMER_TAG);
        }

        super.processEvent(ev);
    }

    public void submitSessions(final List<WebSession> webSessions, final long appId) {
        if (entryPoins.containsKey(appId)) {
            IEntryPoint entryPoint = entryPoins.get(appId);

            for (WebSession sess : webSessions) {
                CustomLog.printf("[Broker](%s) Session %d has arrived in the Entry Point of %s", toString(),
                        sess.getSessionId(), getName());
            }
            entryPoint.dispatchSessions(webSessions);
        } else {
            submitSessionsDirectly(webSessions, appId);
        }
    }

    /**
     * Submits new web sessions to this broker.
     * 
     * @param webSessions
     *            - the new web sessions.
     */
    /* pack access */void submitSessionsDirectly(final List<WebSession> webSessions, final long appId) {
        if (!CloudSim.running()) {
            submitSessionsAtTime(webSessions, appId, 0);
        } else {
            for (WebSession session : webSessions) {
                appsToLoadBalancers.get(appId).assignToServers(session);

                // If the load balancer could not assign it...
                if (session.getAppVmId() == null || session.getDbBalancer() == null) {
                    canceledSessions.add(session);
                    CustomLog.printf(Level.SEVERE,
                            "%s: Session %d could not be assigned to an AS server and is canceled.", toString(),
                            session.getSessionId());
                } else {
                    session.setUserId(getId());
                    // Let the session prepare the first cloudlets
                    if (session.areVirtualMachinesReady()) {
                        session.notifyOfTime(CloudSim.clock());
                    } else {
                        // If the VMs are not yet ready - start the session
                        // later and extend its ideal end
                        session.setIdealEnd(session.getIdealEnd() + stepPeriod);
                        session.notifyOfTime(CloudSim.clock() + stepPeriod);
                    }

                    activeSessions.put(session.getSessionId(), session);

                    // Start the session or schedule it if its VMs are not
                    // initiated.
                    if (session.areVirtualMachinesReady()) {
                        updateSessions(session.getSessionId());
                    } else {
                        send(getId(), stepPeriod, WebTags.UPDATE_SESSION_TAG, session.getSessionId());
                    }
                }
            }
        }
    }

    /**
     * Submits new sessions after the specified delay.
     * 
     * @param webSessions
     *            - the list of sessions to submit.
     * @param loadBalancerId
     *            - the id of the load balancer to submit to.
     * @param delay
     *            - the delay to submit after.
     */
    public void submitSessionsAtTime(final List<WebSession> webSessions, final long loadBalancerId, final double delay) {
        Object data = new Object[] { webSessions, loadBalancerId };
        if (isTimerRunning) {
            send(getId(), delay, WebTags.SUBMIT_SESSION_TAG, data);
        } else {
            presetEvent(getId(), WebTags.SUBMIT_SESSION_TAG, data, delay);
        }
    }

    /**
     * Adds a new load balancer for handling incoming sessions.
     * 
     * @param balancer
     *            - the balancer to add. Must not be null.
     */
    public void addLoadBalancer(final ILoadBalancer balancer) {
        appsToLoadBalancers.put(balancer.getAppId(), balancer);
        appsToGenerators.put(balancer.getAppId(), new ArrayList<>());
    }

    /**
     * Associates this broker with an entry point, which can distribute the
     * incoming workload to multiple borkers/clouds.
     * 
     * @param entryPoint
     *            - the entry point. Must not be null.
     */
    public void addEntryPoint(final IEntryPoint entryPoint) {
        IEntryPoint currEP = entryPoins.get(entryPoint.getAppId());
        if (entryPoint != entryPoins.get(entryPoint.getAppId())) {
            entryPoins.put(entryPoint.getAppId(), entryPoint);
            entryPoint.registerBroker(this);
            if (currEP != null) {
                currEP.deregisterBroker(this);
            }
        }
    }

    /**
     * Dis-associates the entry point and this broker/data centre.
     * 
     * @param entryPoint
     *            - the entry point. Must not be null.
     */
    public void removeEntryPoint(final IEntryPoint entryPoint) {
        if (entryPoint == entryPoins.get(entryPoint.getAppId())) {
            entryPoins.remove(entryPoint.getAppId());
            entryPoint.deregisterBroker(this);
        }
    }

    /**
     * Adds a new workload generator for the specified load balancer.
     * 
     * @param workloads
     *            - the workload generators.
     * @param loadBalancerId
     *            - the id of the load balancer. There must such a load balancer
     *            registered before this method is called.
     */
    public void addWorkloadGenerators(final List<? extends IWorkloadGenerator> workloads, final long loadBalancerId) {
        appsToGenerators.get(loadBalancerId).addAll(workloads);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cloudbus.cloudsim.DatacenterBroker#processOtherEvent(org.cloudbus
     * .cloudsim.plus.SimEvent)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void processOtherEvent(final SimEvent ev) {
        CloudSimTags tag = ev.getTag();

        if (tag == WebTags.TIMER_TAG) {
            if (CloudSim.clock() < getLifeLength()) {
                send(getId(), stepPeriod, tag);
                generateWorkload();
            }
        } else if (tag == WebTags.SUBMIT_SESSION_TAG) {
            Object[] data = (Object[]) ev.getData();
            submitSessions((List<WebSession>) data[0], (Long) data[1]);
        } else if (tag == WebTags.UPDATE_SESSION_TAG) {
            Integer sessId = (Integer) ev.getData();
            updateSessions(sessId);
        } else {
            super.processOtherEvent(ev);
        }
    }

    private void generateWorkload() {
        double currTime = CloudSim.clock();
        for (Map.Entry<Long, List<IWorkloadGenerator>> balancersToWorkloadGens : appsToGenerators.entrySet()) {
            long balancerId = balancersToWorkloadGens.getKey();
            for (IWorkloadGenerator gen : balancersToWorkloadGens.getValue()) {
                Map<Double, List<WebSession>> timeToSessions = gen.generateSessions(currTime, stepPeriod);
                for (Map.Entry<Double, List<WebSession>> sessEntry : timeToSessions.entrySet()) {
                    if (currTime == sessEntry.getKey()) {
                        submitSessions(sessEntry.getValue(), balancerId);
                    } else {
                        submitSessionsAtTime(sessEntry.getValue(), balancerId, sessEntry.getKey() - currTime);
                    }
                }
            }
        }
    }

    private void updateSessions(final Integer... sessionIds) {
        List<Integer> completedIds = new ArrayList<>();
        for (Integer id : sessionIds.length == 0 ? activeSessions.keySet() : Arrays.asList(sessionIds)) {
            WebSession sess = activeSessions.get(id);

            // If the session is complete - there is no need to update it.
            if (sess == null || sess.isComplete() || sess.isFailed()) {
                if (sess != null && sess.isFailed()) {
                    logSessionFailure(sess);
                }
                completedIds.add(id);
                continue;
            }

            // Check if all VMs for the sessions are set. In the simulation
            // start, this may not be so, as the refreshing action of the broker
            // may happen before the mapping of VMs to hosts.
            if (sess.areVirtualMachinesReady()) {
                double currTime = CloudSim.clock();

                // sess.notifyOfTime(currTime);
                try {
                    WebSession.StepCloudlets webCloudlets = sess.pollCloudlets(currTime);

                    if (webCloudlets != null) {

                        if (webCloudlets.asCloudlet.getUserId() != sess.getUserId() || sess.getUserId() != getId()) {
                            throw new IllegalStateException();
                        }

                        getCloudletList().add(webCloudlets.asCloudlet);
                        getCloudletList().addAll(webCloudlets.dbCloudlets);
                        submitCloudlets();

                        double nextIdealTime = currTime + stepPeriod;
                        sess.notifyOfTime(nextIdealTime);

                        send(getId(), stepPeriod, WebTags.UPDATE_SESSION_TAG, sess.getSessionId());
                    }
                } catch (SessionFailedException e) {
                    CustomLog.printf("Broker(%s): Session %d with metadata %s has failed. Details: %s", this,
                            sess.getSessionId(), Arrays.toString(sess.getMetadata()), e.getMessage());
                    completedIds.add(sess.getSessionId());
                }
            }
        }

        // Remote completed sessions...
        for (Integer id : completedIds) {
            WebSession sess = activeSessions.remove(id);
            if (sess != null) {
                completedSessions.add(sess);
            }
        }
    }

    private void logSessionFailure(WebSession sess) {
        StringBuffer detailsBuffer = new StringBuffer();
        for (WebCloudlet wc : sess.getFailedCloudlets()) {
            detailsBuffer.append(String.format(
                    "Cloudlet %d on %s VM/Server %d has status %s ",
                    wc.getCloudletId(),
                    wc.getGuestId() == sess.getAppVmId() ? "AS" : "DB",
                    wc.getGuestId(),
                    wc.getCloudletStatusString()));
        }

        CustomLog.printf("Broker(%s): Session %d with metadata %s has failed. Details: %s", this, sess.getSessionId(),
                Arrays.toString(sess.getMetadata()), detailsBuffer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.cloudbus.cloudsim.DatacenterBroker#processCloudletReturn(org.cloudbus
     * .cloudsim.plus.SimEvent)
     */
    @Override
    protected void processCloudletReturn(final SimEvent ev) {
        super.processCloudletReturn(ev);
        Cloudlet cloudlet = (Cloudlet) ev.getData();
        if (CloudSim.clock() < getLifeLength()) {
            // kill the broker only if its life length is over/expired
            if (cloudlet instanceof WebCloudlet) {
                updateSessions(((WebCloudlet) cloudlet).getSessionId());
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see cloudsim.plus.SimEntity#startEntity()
     */
    @Override
    public void startEntity() {
        super.startEntity();
        schedule(getId(), 0, CloudActionTags.RESOURCE_CHARACTERISTICS_REQUEST, List.of(dataCenterId));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void processResourceCharacteristicsRequest(final SimEvent ev) {
        setDatacenterIdsList(ev.getData() == null ? CloudSim.getCloudResourceList() : (List<Integer>) ev.getData());
        setDatacenterCharacteristicsList(new HashMap<>());

        for (Integer datacenterId : getDatacenterIdsList()) {
            sendNow(datacenterId, CloudActionTags.RESOURCE_CHARACTERISTICS, getId());
        }
    }

    public Set<Integer> getSessionsInServer(int vmId) {
        Set<Integer> result = new LinkedHashSet<>();
        for (Map.Entry<Integer, WebSession> e : activeSessions.entrySet()) {
            WebSession session = e.getValue();
            if (!session.isComplete() && session.getAppVmId() == vmId) {
                result.add(session.getSessionId());
            }
        }
        return result;
    }

    public Set<Integer> getUsedASServers() {
        Set<Integer> result = new HashSet<>();
        for (Map.Entry<Integer, WebSession> e : activeSessions.entrySet()) {
            WebSession session = e.getValue();
            if (!session.isComplete()) {
                result.add(session.getAppVmId());
            }
        }
        return result;
    }

    public Map<Integer, Integer> getASServersToNumSessions() {
        Map<Integer, Integer> result = new HashMap<>();
        for (Map.Entry<Integer, WebSession> e : activeSessions.entrySet()) {
            WebSession session = e.getValue();
            if (!session.isComplete()) {
                result.put(session.getAppVmId(),
                        result.containsKey(session.getAppVmId()) ? result.get(session.getAppVmId()) + 1 : 1);
            }
        }
        return result;
    }

}
