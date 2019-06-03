package org.cloudbus.cloudsim.plus.delay;

import org.cloudbus.cloudsim.Vm;

/**
 * Defines the boot delay of a VM as a constant.
 * 
 * @author nikolay.grozev
 * 
 * @param <V>
 */
public class ConstantVMBootDelay implements IVMBootDelayDistribution {

    private final double delay;

    /**
     * Constr.
     * 
     * @param delay
     *            - the constant delay. Must not be negative.
     */
    public ConstantVMBootDelay(final double delay) {
        this.delay = delay;
    }

    @Override
    public double getDelay(final Vm vm) {
        return delay;
    }
}
