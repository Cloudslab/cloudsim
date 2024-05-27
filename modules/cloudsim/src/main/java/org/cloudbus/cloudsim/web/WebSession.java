package org.cloudbus.cloudsim.web;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.EX.disk.HddVm;
import org.cloudbus.cloudsim.EX.util.CustomLog;
import org.cloudbus.cloudsim.EX.util.Id;
import org.cloudbus.cloudsim.EX.util.TextUtil;
import org.cloudbus.cloudsim.EX.util.Textualize;

import java.util.*;
import java.util.logging.Level;

/**
 * A web session is a session established between a user and an application
 * server (deployed in a VM) and can use several database servers (deployed in
 * separate VMs). Throughout it's lifetime a session continuously generates
 * workload on the servers deployed in these virtual machines. The workload is
 * represented as cloudlets, which are sent to the assigned servers.
 * 
 * <br/>
 * <br/>
 * 
 * To achieve this a web session generates two consequent "streams" of cloudlets
 * and directs them to the servers. These streams are based on two instances of
 * {@link IGenerator} passed when constructing the session. The session takes
 * care to synchronize the two generators, so that consequent cloudlets from the
 * two generators are executed at the same pace. Thus if one of the servers is
 * performing better than the other, this will not be reflected in a quicker
 * exhaustion of its respective generator.
 * 
 * <br/>
 * <br/>
 * 
 * Since the session utilizes {@link IGenerator} instances, which are unaware of
 * the simulation time, it needs to notify them of how time changes. By design
 * (to improve testability) web sessions are also unaware of simulation time.
 * Thus they need to be notified by an external "clock entity" of the simulation
 * time at a predefined period of time. This is done by the
 * {@link WebSession.notifyOfTime} method.
 * 
 * 
 * @author nikolay.grozev
 * 
 */
@Textualize(properties = { "SessionId", "AppVmId", "ReadableStartTime", "StartTime", "FinishTime", "IdealEnd", "Delay",
        "Complete", "Failed", "SourceIP", "ServerIP" })
public class WebSession {

    private static final Set<Cloudlet.CloudletStatus> FAIL_CLOUDLET_STATES = new HashSet<>(Arrays.asList(Cloudlet.CloudletStatus.FAILED,
            Cloudlet.CloudletStatus.FAILED_RESOURCE_UNAVAILABLE, Cloudlet.CloudletStatus.CANCELED));

    private final IGenerator<? extends WebCloudlet> appServerCloudLets;
    private final IGenerator<? extends Collection<? extends WebCloudlet>> dbServerCloudLets;

    private WebCloudlet currentAppServerCloudLet = null;
    private List<? extends WebCloudlet> currentDBServerCloudLets = null;

    private Integer appVmId = null;
    private IDBBalancer dbBalancer;

    private int userId;
    private int cloudletsLeft;

    private double idealEnd;
    private double startTime = Double.NaN;

    private String sourceIP;
    private String serverIP;

    private final String[] metadata;
    private final int sessionId;

    /**
     * Creates a new instance with the specified cloudlet generators.
     * 
     * @param appServerCloudLets
     *            - a generator for cloudlets for the application server. Must
     *            not be null.
     * @param dbServerCloudLets
     *            - a generator for cloudlets for the db server. Must not be
     *            null.
     * @param userId
     *            - the use id. A valid user id must be set either through a
     *            constructor or the set method, before this instance is used.
     * @param metadata
     *            - various metadata of the session.
     */
    public WebSession(final IGenerator<? extends WebCloudlet> appServerCloudLets,
                      final IGenerator<? extends Collection<? extends WebCloudlet>> dbServerCloudLets, final int userId,
                      final int numberOfCloudlets, final double idealEnd, final String... metadata) {
        super();
        sessionId = Id.pollId(getClass());

        this.appServerCloudLets = appServerCloudLets;
        this.dbServerCloudLets = dbServerCloudLets;
        this.userId = userId;

        this.cloudletsLeft = numberOfCloudlets;
        this.idealEnd = idealEnd;
        this.metadata = metadata;
    }

    /**
     * Returns the source IP of the session. If null - the source is unknown.
     * 
     * @return the source IP of the session or null if unknown. The returned
     *         value is either null or a string in the standard dot form for
     *         IPv4 or IPv6 addresses.
     */
    public String getSourceIP() {
        return sourceIP;
    }

    /**
     * Sets the source IP of the session. If null - the source is considered
     * unknown.
     * 
     * @param sourceIP
     *            - the new source IP to set. Either null or a string in the
     *            standard dot form for IPv4 or IPv6 addresses.
     */
    public void setSourceIP(final String sourceIP) {
        this.sourceIP = sourceIP;
    }

    /**
     * Returns the server IP of the session. If null - the server IP is unknown.
     * 
     * @return the server IP of the session or null if unknown. The returned
     *         value is either null or a string in the standard dot form for
     *         IPv4 or IPv6 addresses.
     */
    public String getServerIP() {
        return serverIP;
    }

    /**
     * Sets the server IP of the session. If null - the server is considered
     * unknown.
     * 
     * @param serverIP
     *            - the new server IP to set. Either null or a string in the
     *            standard dot form for IPv4 or IPv6 addresses.
     */
    public void setServerIP(final String serverIP) {
        this.serverIP = serverIP;
    }

    /**
     * Returns the metadata of this session.
     * 
     * @return the metadata of this session.
     */
    public String[] getMetadata() {
        return metadata;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(final int userId) {
        this.userId = userId;
    }

    /**
     * Creates cloudlets to submit to the virtual machines. If at this time no
     * cloudlets should be sent to the servers null is returned.
     * 
     * @param currTime
     *            - the current time of the simulation.
     * @return the result as described above.
     */
    public StepCloudlets pollCloudlets(final double currTime) {

        StepCloudlets result = null;
        boolean appCloudletFinished = currentAppServerCloudLet == null || currentAppServerCloudLet.isFinished();
        boolean dbCloudletFinished = currentDBServerCloudLets == null
                || areAllCloudletsFinished(currentDBServerCloudLets);
        boolean appServerNextReady = !appServerCloudLets.isEmpty()
                && appServerCloudLets.peek().getIdealStartTime() <= currTime;
        boolean dbServerNextReady = !dbServerCloudLets.isEmpty()
                && getEarliestIdealStartTime(dbServerCloudLets.peek()) <= currTime;

        if (cloudletsLeft != 0 && appCloudletFinished && !dbCloudletFinished) {
            CustomLog.printf(Level.FINE, "Session %d in AS VM %d blocked in DB layer", getSessionId(), appVmId);
        }

        if (cloudletsLeft != 0 && appCloudletFinished && dbCloudletFinished && appServerNextReady && dbServerNextReady) {
            result = new StepCloudlets(appServerCloudLets.poll(), new ArrayList<>(dbServerCloudLets.poll()));
            currentAppServerCloudLet = result.asCloudlet;
            currentDBServerCloudLets = result.dbCloudlets;

            currentAppServerCloudLet.setGuestId(appVmId);
            if (assignToServer(dbBalancer, currentDBServerCloudLets)) {

                currentAppServerCloudLet.setSessionId(getSessionId());
                currentAppServerCloudLet.setUserId(userId);
                setSessionAndUserIds(getSessionId(), userId, currentDBServerCloudLets);

                cloudletsLeft--;

                if (Double.isNaN(startTime)) {
                    startTime = Math.min(currentAppServerCloudLet.getIdealStartTime(),
                            getEarliestIdealStartTime(currentDBServerCloudLets));
                }
            } else {
                throw new SessionFailedException(sessionId, "The data for web session " + sessionId
                        + " could not be found.");
            }
        }
        return result;
    }

    /**
     * NOTE!!! - used only for test purposes.
     * 
     * @return - the current app server cloudlet
     */
    /* package access */WebCloudlet getCurrentAppServerCloudLet() {
        return currentAppServerCloudLet;
    }

    /**
     * NOTE!!! - used only for test purposes.
     * 
     * @return - the current db server cloudlets.
     */
    /* package access */List<? extends WebCloudlet> getCurrentDBServerCloudLets() {
        return currentDBServerCloudLets;
    }

    /**
     * Gets notified of the current time.
     * 
     * @param time
     *            - the current CloudSim time.
     */
    public void notifyOfTime(final double time) {
        appServerCloudLets.notifyOfTime(time);
        dbServerCloudLets.notifyOfTime(time);
    }

    /**
     * Returns the id of the VM hosting the application server.
     * 
     * @return the id of the VM hosting the application server.
     */
    public Integer getAppVmId() {
        return appVmId;
    }

    /**
     * Sets the id of the VM hosting the app server. NOTE! you must set this id
     * before polling cloudlets.
     * 
     * @param appVmId
     *            - the id of the VM hosting the app server.
     */
    public void setAppVmId(final int appVmId) {
        this.appVmId = appVmId;
    }

    /**
     * Returns the balancer of DB cloudlets to the VMs hosting the db server.
     * 
     * @return the balancer of DB cloudlets to the VMs hosting the db server.
     */
    public IDBBalancer getDbBalancer() {
        return dbBalancer;
    }

    /**
     * Sets the balancer of DB cloudlets to the VMs hosting the db server. NOTE!
     * you must set this before polling cloudlets.
     * 
     * @param dbBalancer
     *            - the DB VM balancer.
     */
    public void setDbBalancer(final IDBBalancer dbBalancer) {
        this.dbBalancer = dbBalancer;
    }

    /**
     * Returns the id of the session.
     * 
     * @return the id of the session.
     */
    public int getSessionId() {
        return sessionId;
    }

    /**
     * Returns the accumulated delay of this session. If the session has a
     * cloudlet running -1 is returned.
     * 
     * @return the accumulated delay of this session. If the session has a
     *         cloudlet running -1 is returned.
     */
    public double getDelay() {
        return Math.max(0, getFinishTime() - idealEnd);
    }

    /**
     * Returns the finish of this session. If the session has a cloudlet running
     * -1 is returned.
     * 
     * @return the finish time of this session. If the session has a cloudlet
     *         running -1 is returned.
     */
    public double getFinishTime() {
        double finishAS = currentAppServerCloudLet == null || !currentAppServerCloudLet.isFinished() ? -1
                : currentAppServerCloudLet.getExecFinishTime();
        double finishDB = currentDBServerCloudLets == null || !areAllCloudletsFinished(currentDBServerCloudLets) ? -1
                : getLatestFinishTime(currentDBServerCloudLets);
        return Math.max(0, Math.max(finishAS, finishDB));
    }

    /**
     * Sets the ideal end of the session.
     * 
     * @param idealEnd
     */
    public void setIdealEnd(final double idealEnd) {
        this.idealEnd = idealEnd;
    }

    /**
     * Returns the ideal end time of this session.
     * 
     * @return the ideal end time of this session.
     */
    public double getIdealEnd() {
        return idealEnd;
    }

    /**
     * Returns the starting time of the session.
     * 
     * @return the starting time of the session.
     */
    public double getStartTime() {
        return startTime;
    }

    /**
     * Returns a readable representation of the start time.
     * 
     * @return a readable representation of the start time.
     */
    public String getReadableStartTime() {
        return TextUtil.getReadableTime(startTime);
    }

    /**
     * Returns if the session has completed.
     * 
     * @return if the session has completed.
     */
    public boolean isComplete() {
        return cloudletsLeft == 0 && currentAppServerCloudLet != null && currentAppServerCloudLet.isFinished()
                && currentDBServerCloudLets != null && areAllCloudletsFinished(currentDBServerCloudLets);
    }

    /**
     * Returns if the session has failed.
     * 
     * @return if the session has failed.
     */
    public boolean isFailed() {
        return (currentAppServerCloudLet != null && FAIL_CLOUDLET_STATES.contains(currentAppServerCloudLet.getStatus()))
                || (currentDBServerCloudLets != null && anyCloudletsFailed(currentDBServerCloudLets));
    }

    /**
     * If the session has failed, returns the cloudlets which caused the
     * failure.
     * 
     * @return If the session has failed, returns the cloudlets which caused the
     *         failure.
     */
    public List<WebCloudlet> getFailedCloudlets() {
        if (isFailed()) {
            List<WebCloudlet> res = new ArrayList<>();
            if (currentAppServerCloudLet != null
                    && FAIL_CLOUDLET_STATES.contains(currentAppServerCloudLet.getStatus())) {
                res.add(currentAppServerCloudLet);
                for (WebCloudlet wc : currentDBServerCloudLets) {
                    if (FAIL_CLOUDLET_STATES.contains(wc.getStatus())) {
                        res.add(wc);
                    }
                }
            }
            return res;
        } else {
            return Collections.emptyList();
        }
    }

    private boolean anyCloudletsFailed(List<? extends WebCloudlet> currentDBServerCloudLets2) {
        for (WebCloudlet wc : currentDBServerCloudLets2) {
            if (FAIL_CLOUDLET_STATES.contains(wc.getStatus())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns if the virtual machines serving this session have been set up, so
     * that it can execute.
     * 
     * @return if the virtual machines serving this session have been set up, so
     *         that it can execute.
     */
    public boolean areVirtualMachinesReady() {
        for (HddVm vm : getDbBalancer().getVMs()) {
            if (vm.getHost() == null) {
                return false;
            }
        }
        return appVmId != null;
    }

    private static boolean areAllCloudletsFinished(final List<? extends WebCloudlet> cloudlets) {
        boolean result = true;
        for (WebCloudlet cl : cloudlets) {
            if (!cl.isFinished()) {
                result = false;
                break;
            }
        }
        return result;
    }

    private static double getLatestFinishTime(final Collection<? extends WebCloudlet> cloudlets) {
        double result = -1;
        for (WebCloudlet cl : cloudlets) {
            if (cl.getExecFinishTime() > result) {
                result = cl.getExecFinishTime();
            }
        }
        return result;
    }

    private static double getEarliestIdealStartTime(final Collection<? extends WebCloudlet> cloudlets) {
        double result = -1;
        for (WebCloudlet cl : cloudlets) {
            if (result == -1 || cl.getIdealStartTime() < result) {
                result = cl.getIdealStartTime();
            }
        }
        return result;
    }

    private static boolean assignToServer(final IDBBalancer dbBalancer,
            final Collection<? extends WebCloudlet> cloudlets) {
        for (WebCloudlet cl : cloudlets) {
            dbBalancer.allocateToServer(cl);
            if (cl.getGuestId() < 0) {
                return false;
            }
        }
        return true;
    }

    private static void setSessionAndUserIds(final int sessId, final int userId,
            final Collection<? extends WebCloudlet> cloudlets) {
        for (WebCloudlet cl : cloudlets) {
            cl.setSessionId(sessId);
            cl.setUserId(userId);
        }
    }

    /**
     * 
     * A structure, containing the cloudlets that need to be executed for a step
     * in the simulation.
     * 
     * @author nikolay.grozev
     * 
     */
    public static class StepCloudlets {
        /**
         * The app server clouldet for the step.
         */
        public WebCloudlet asCloudlet;
        /**
         * The db cloudlets for the step.
         */
        public List<? extends WebCloudlet> dbCloudlets;

        public StepCloudlets(final WebCloudlet asCloudlet, final List<? extends WebCloudlet> dbCloudlets) {
            super();
            this.asCloudlet = asCloudlet;
            this.dbCloudlets = dbCloudlets;
        }
    }

}
