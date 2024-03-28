package org.cloudbus.cloudsim.core;

/**
 * An interface for implementing attributes that are shared between Host and Guest entities.
 *
 * @author Remo Andreoli
 * @since CloudSim Toolkit 6.0
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
}
