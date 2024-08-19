/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2024, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.core;

/**
 * An interface for implementing attributes that are shared between Host and Guest entities.
 *
 * @author Remo Andreoli
 * @since CloudSim Toolkit 7.0
 */
public interface CoreAttributes {
    /**
     * Gets the number of allocated pes to the guest entity.
     *
     * @return the number of pes
     */
    int getNumberOfPes();

    /**
     * Gets the total mips.
     *
     * @return the total mips
     */
    double getTotalMips();

    /**
     * Gets the simplified class name of the object. Mostly used for debug / log purposes.
     * @return the simplified classname
     */
    default String getClassName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Checks if entity is being instantiated. Some entities (such as a VirtualEntity object) need to be instantiated before
     * they can act as host entities.
     *
     * @return true, if guest is being instantiated
     */
    boolean isBeingInstantiated();
}
