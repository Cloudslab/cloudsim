package org.cloudbus.cloudsim.web;

import org.cloudbus.cloudsim.EX.disk.HddVm;

import java.util.List;

/**
 * The load balancer of an application. Assigns sessions to servers. Each load
 * balancer serves an application, which is identified by a application id.
 * 
 * @author nikolay.grozev
 * 
 */
public interface ILoadBalancer {

    /**
     * The id of the load balancer.
     * 
     * @return - the id of the load balancer.
     */
    long getId();

    /**
     * Assigns the specified sessions to an application and a DB servers.
     * 
     * @param sessions
     *            - the sessions to assign. If the session is already assigned
     *            to servers, this operation does nothing.
     */
    void assignToServers(final WebSession... sessions);

    /**
     * Registers a new application server with this load balancer.
     * 
     * @param vm
     *            - the new app server.
     */
    void registerAppServer(final HddVm vm);

    /**
     * Returns the list of all app servers managed by this load balancer.
     * 
     * @return the list of all app servers managed by this load balancer.
     */
    List<HddVm> getAppServers();

    /**
     * Returns the list of all currently running app servers managed by this
     * load balancer.
     * 
     * @return the list of all currently running app servers managed by this
     *         load balancer.
     */
    List<HddVm> getRunningAppServers();

    /**
     * Returns the balancer which manages cloudlets to DB server mapping.
     * 
     * @return the balancer which manages cloudlets to DB server mapping.
     */
    IDBBalancer getDbBalancer();

    /**
     * Returns the id of the application served by this load balancer.
     * 
     * @return the id of the application served by this load balancer.
     */
    long getAppId();

    /**
     * Returns the IP address of this load balancer.
     * 
     * @return the IP address of this load balancer.
     */
    String getIp();

}
