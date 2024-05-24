package org.cloudbus.cloudsim.EX;

/**
 * 
 * An autoscaling policy, which scales up/down the allocated cloud resources.
 * 
 * @author nikolay.grozev
 * 
 */
public interface IAutoscalingPolicy {

    /**
     * Invoked periodically or upon an event in order to allocate/deallocate
     * resources.
     * 
     * @param broker
     *            - the broker managing the resources.
     */
    void scale(final MonitoringBrokerEX broker);
}
