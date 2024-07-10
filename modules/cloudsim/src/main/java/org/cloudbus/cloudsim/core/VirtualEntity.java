package org.cloudbus.cloudsim.core;

/**
 * Represents a virtual entity that runs inside a Host and can run cloudlets as well as other Guest entities
 * (for nested virtualization).
 *
 * @author Remo Andreoli
 * @since CloudSim Toolkit 7.0
 */
public interface VirtualEntity extends HostEntity, GuestEntity {}
