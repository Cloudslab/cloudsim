package org.cloudbus.cloudsim.web.workload.brokers;

import org.cloudbus.cloudsim.web.WebSession;

import java.util.List;

/**
 * Represents an entry point for an application, which uses multiple clouds
 * (Multi-Cloud). Each entry point is associated with a list of web brokers,
 * representing the different cloud data centres. The entry point receives
 * workloads (web sessions) and forwards them accordingly to the registered web
 * brokers.
 * 
 * @author nikolay.grozev
 * 
 */
public interface IEntryPoint {

    /**
     * Registers a broker/cloud to this entry point. Subsequently the entry
     * point will consider it when distributing the workload among the
     * registered clouds.
     * 
     * @param broker
     *            - the broker to register. Must not be null.
     */
    void registerBroker(WebBroker broker);

    /**
     * Deregisters the broker/cloud from this entry point. Subsequent workload
     * coming to this entry point will not be distributed to this cloud.
     * 
     * @param webBroker
     *            - the broker to deregister/remove. Must not be null.
     */
    void deregisterBroker(WebBroker webBroker);

    /**
     * Returns the id of the application, which this entry point is a part of.
     * 
     * @return - the Id of the application, which this entry point is a part of.
     */
    long getAppId();

    /**
     * Dispatches the sessions to the appropriate brokers/clouds.
     * 
     * @param webSessions
     *            - the web sessions to dispatch. Must not be null.
     */
    void dispatchSessions(List<WebSession> webSessions);

    /**
     * Returns how many sessions have been dispatched by this entry point.
     * 
     * @return how many sessions have been dispatched by this entry point.
     */
    long getSessionsDispatched();

}