package org.cloudbus.cloudsim.core;

/**
 * Represents a Virtual Machine (VM) that runs inside a Host and can run cloudlets as well as other Guest entities.
 *
 * @author Remo Andreoli
 * @since CloudSim Toolkit 6.0
 */
public interface VmAbstract extends HostEntity, GuestEntity {}
