package org.cloudbus.cloudsim.EX.billing;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.EX.util.CustomLog;
import org.cloudbus.cloudsim.EX.vm.VmEX;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    public BigDecimal bill(final List<? extends GuestEntity> vms) {
        BigDecimal result = BigDecimal.ZERO;
        for (GuestEntity vm : vms) {
            if (vm instanceof VmEX) {
                VmEX vmEx = (VmEX) vm;
                if (shouldBillVm(vmEx)) {
                    result = result.add(billSingleVm(vmEx));
                }
            } else {
                CustomLog.printConcat("Unable to bill VM", vm.getId(), " as it is not of type ", VmEX.class.getName());
            }
        }
        return result;
    }

    @Override
    public BigDecimal bill(final List<? extends GuestEntity> vms, double before) {
        BigDecimal result = BigDecimal.ZERO;
        for (GuestEntity vm : vms) {
            if (vm instanceof VmEX vmEx) {
                if (shouldBillVm(vmEx)) {
                    result = result.add(billSingleVmUntil(vmEx, before));
                }
            } else {
                CustomLog.printConcat("Unable to bill VM", vm.getId(), " as it is not of type ", VmEX.class.getName());
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
    public boolean shouldBillVm(final VmEX vm) {
        return keyOf(vm) != null;
    }

    /**
     * Returns the bill for a single VM.
     * 
     * @param vm
     *            - the vm
     * @return the bill for a single VM.
     */
    public abstract BigDecimal billSingleVm(final VmEX vm);

    /**
     * Returns the accummulated bill for a single VM before a specified time.
     * 
     * @param vm
     *            - the vm
     * @param endTime
     * @return the bill for a single VM.
     */
    public abstract BigDecimal billSingleVmUntil(final VmEX vm, double endTime);

    /**
     * Returns the current simulation time. Can be overridden for test purposes.
     * 
     * @return the current simulation time.
     */
    protected double getCurrentTime() {
        return CloudSim.clock();
    }

    public static ImmutablePair<String, String> keyOf(final VmEX vm) {
        if (vm.getMetadata() != null) {
            return ImmutablePair.of(vm.getMetadata().getType(), vm.getMetadata().getOS());
        }
        return null;
    }

    @Override
    public BigDecimal normalisedCostPerMinute(final Vm vm) {
        BigDecimal result = BigDecimal.valueOf(-1);
        if (vm instanceof VmEX) {
            Pair<String, String> key = keyOf((VmEX) vm);
            try {
                result = prices.containsKey(key) ? prices.get(key).divide(BigDecimal.valueOf(60d), RoundingMode.HALF_UP) : result;
            } catch (ArithmeticException ex) {
                result = prices.containsKey(key) ? BigDecimal.valueOf(prices.get(key).doubleValue() / 60d) : result;
            }
        }
        return result;
    }
}
