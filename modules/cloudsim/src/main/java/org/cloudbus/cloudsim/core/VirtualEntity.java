/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2024, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.core;

/**
 * Represents a virtual entity that runs inside a Host and can run cloudlets as well as other Guest entities
 * (for nested virtualization).
 *
 * @author Remo Andreoli
 * @since CloudSim Toolkit 7.0
 */
public interface VirtualEntity extends HostEntity, GuestEntity {}
