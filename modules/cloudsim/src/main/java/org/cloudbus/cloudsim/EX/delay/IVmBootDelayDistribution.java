package org.cloudbus.cloudsim.EX.delay;

import org.cloudbus.cloudsim.core.GuestEntity;

/**
 * Defines how much boot delay should be there for a given VM.
 * 
 * @author nikolay.grozev
 * @author Remo Andreoli
 */
public interface IVmBootDelayDistribution {

    /**
     * Returns how much is the booting time of the parameter VM.
     * 
     * @param guest
     *            - the VM to check for. Must not be null.
     * @return the time
     */
    double getDelay(final GuestEntity guest);

}
