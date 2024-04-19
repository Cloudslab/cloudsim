package org.cloudbus.cloudsim.web.workload.brokers;

import org.cloudbus.cloudsim.geolocation.IGeolocationService;
import org.cloudbus.cloudsim.web.WebSession;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author nikolay.grozev
 * 
 */
public abstract class BaseEntryPoint implements IEntryPoint {

    private final List<WebBroker> brokers = new ArrayList<>();
    private final IGeolocationService geoService;
    private final List<WebSession> canceledSessions = new ArrayList<>();
    private final long appId;

    private long sessionsDispatched = 0;

    /**
     * Constr.
     * 
     * @param geoService
     *            - provides the IP utilities needed by the entry point. Must
     *            not be null.
     * @param appId
     *            - the id of the application this entry point services. Must
     *            not be null.
     */
    public BaseEntryPoint(final IGeolocationService geoService, final long appId) {
        this.geoService = geoService;
        this.appId = appId;

    }

    @Override
    public void registerBroker(final WebBroker broker) {
        if (!brokers.contains(broker)) {
            brokers.add(broker);
            broker.addEntryPoint(this);
        }
    }

    @Override
    public void deregisterBroker(final WebBroker webBroker) {
        brokers.remove(webBroker);
        webBroker.removeEntryPoint(this);
    }

    /**
     * Should be overriden. Only counts the number of sessions.
     */
    @Override
    public void dispatchSessions(List<WebSession> webSessions) {
        sessionsDispatched += webSessions.size();
    }

    @Override
    public long getAppId() {
        return appId;
    }

    public List<WebBroker> getBrokers() {
        return brokers;
    }

    public IGeolocationService getGeoService() {
        return geoService;
    }

    public List<WebSession> getCanceledSessions() {
        return canceledSessions;
    }

    /**
     * Returns how many sessions have been dispatched by this entry point.
     * 
     * @return how many sessions have been dispatched by this entry point.
     */
    @Override
    public long getSessionsDispatched() {
        return sessionsDispatched;
    }
}