package org.cloudbus.cloudsim.vmplus.delay;

import org.cloudbus.cloudsim.Vm;

/**
 * Defines how much boot delay should be there for a given VM.
 * 
 * @author nikolay.grozev
 * 
 */
public interface IVMBootDelayDistribution {

    /**
     * Returns how much is the booting time of the parameter VM.
     * 
     * @param vm
     *            - the VM to check for. Must not be null.
     * @return the time
     */
    double getDelay(final Vm vm);

}
