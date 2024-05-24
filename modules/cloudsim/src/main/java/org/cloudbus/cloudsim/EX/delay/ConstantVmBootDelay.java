package org.cloudbus.cloudsim.EX.delay;

import org.cloudbus.cloudsim.core.GuestEntity;

/**
 * Defines the boot delay of a VM as a constant.
 * 
 * @author nikolay.grozev
 * 
 */
public class ConstantVmBootDelay implements IVmBootDelayDistribution {

    private final double delay;

    /**
     * Constr.
     * 
     * @param delay
     *            - the constant delay. Must not be negative.
     */
    public ConstantVmBootDelay(final double delay) {
        this.delay = delay;
    }

    @Override
    public double getDelay(final GuestEntity guest) {
        return delay;
    }
}
