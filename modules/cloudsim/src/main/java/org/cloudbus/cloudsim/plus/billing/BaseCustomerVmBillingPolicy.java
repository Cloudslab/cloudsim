package org.cloudbus.cloudsim.plus.billing;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.plus.util.CustomLog;
import org.cloudbus.cloudsim.plus.vm.VMex;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Implements a policy for billing a customer's vms. Simply sums the bills for
 * all VMs.
 * 
 * @author nikolay.grozev
 * 
 */
public abstract class BaseCustomerVmBillingPolicy implements IVmBillingPolicy {

    protected final Map<Pair<String, String>, BigDecimal> prices;

    /**
     * Constr.
     * 
     * @param prices
     *            - the prices in a map with entries in the form [[vm-type, OS],
     *            price].It us up to the subclasses to interpret if these prices
     *            are per hour/minute etc.
     */
    public BaseCustomerVmBillingPolicy(final Map<Pair<String, String>, BigDecimal> prices) {
        this.prices = prices;
    }

    @Override
    public BigDecimal bill(final List<? extends Vm> vms) {
        BigDecimal result = BigDecimal.ZERO;
        for (Vm vm : vms) {
            if (vm instanceof VMex) {
                VMex vmEx = (VMex) vm;
                if (shouldBillVm(vmEx)) {
                    result = result.add(billSingleVm(vmEx));
                }
            } else {
                CustomLog.printConcat("Unable to bill VM", vm.getId(), " as it is not of type ", VMex.class.getName());
            }
        }
        return result;
    }

    @Override
    public BigDecimal bill(final List<? extends Vm> vms, double before) {
        BigDecimal result = BigDecimal.ZERO;
        for (Vm vm : vms) {
            if (vm instanceof VMex) {
                VMex vmEx = (VMex) vm;
                if (shouldBillVm(vmEx)) {
                    result = result.add(billSingleVmUntil(vmEx, before));
                }
            } else {
                CustomLog.printConcat("Unable to bill VM", vm.getId(), " as it is not of type ", VMex.class.getName());
            }
        }
        return result;
    }

    /**
     * Returns if the VM should be billed. Override this method to implement
     * logic like billing the VMs of a given user.
     * 
     * @param vm
     *            - the vm to check for.
     * @return if the VM should be billed.
     */
    public boolean shouldBillVm(final VMex vm) {
        return keyOf(vm) != null;
    }

    /**
     * Returns the bill for a single VM.
     * 
     * @param vm
     *            - the vm
     * @return the bill for a single VM.
     */
    public abstract BigDecimal billSingleVm(final VMex vm);

    /**
     * Returns the accummulated bill for a single VM before a specified time.
     * 
     * @param vm
     *            - the vm
     * @param endTime
     * @return the bill for a single VM.
     */
    public abstract BigDecimal billSingleVmUntil(final VMex vm, double endTime);

    /**
     * Returns the current simulation time. Can be overridden for test purposes.
     * 
     * @return the current simulation time.
     */
    protected double getCurrentTime() {
        return CloudSim.clock();
    }

    public static ImmutablePair<String, String> keyOf(final VMex vm) {
        if (vm.getMetadata() != null) {
            return ImmutablePair.of(vm.getMetadata().getType(), vm.getMetadata().getOS());
        }
        return null;
    }

    @Override
    public BigDecimal normalisedCostPerMinute(final Vm vm) {
        BigDecimal result = BigDecimal.valueOf(-1);
        if (vm instanceof VMex) {
            Pair<String, String> key = keyOf((VMex) vm);
            try {
                result = prices.containsKey(key) ? prices.get(key).divide(BigDecimal.valueOf(60d)) : result;
            } catch (ArithmeticException ex) {
                result = prices.containsKey(key) ? BigDecimal.valueOf(prices.get(key).doubleValue() / 60d) : result;
            }
        }
        return result;
    }
}
