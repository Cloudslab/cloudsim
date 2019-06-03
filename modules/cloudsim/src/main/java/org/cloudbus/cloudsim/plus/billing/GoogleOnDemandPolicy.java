package org.cloudbus.cloudsim.plus.billing;

import org.apache.commons.lang3.tuple.Pair;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.plus.vm.VMStatus;
import org.cloudbus.cloudsim.plus.vm.VMex;

import java.math.BigDecimal;
import java.util.Map;

import static org.cloudbus.cloudsim.Consts.MINUTE;

/**
 * Implements the default billing policy for the Google IaaS on demand
 * instances.
 * 
 * @author nikolay.grozev
 * 
 */
public class GoogleOnDemandPolicy extends BaseCustomerVmBillingPolicy {

    /**
     * Constr.
     * 
     * @param prices
     *            - the prices in a map with entries in the form [[vm-type, OS],
     *            price]. Prices are per hour.
     */
    public GoogleOnDemandPolicy(final Map<Pair<String, String>, BigDecimal> prices) {
        super(prices);
    }

    @Override
    public BigDecimal billSingleVm(final VMex vm) {
        double timeAfterBoot = vm.getTimeAfterBooting();
        return computeBill(vm, timeAfterBoot);
    }

    @Override
    public BigDecimal billSingleVmUntil(VMex vm, double endTime) {
        double time = vm.getEndTime() < 0 || vm.getEndTime() > endTime ? endTime - vm.getStartTime() : vm
                .getTimeAfterBooting();
        return computeBill(vm, time);
    }

    public BigDecimal computeBill(final VMex vm, double duration) {
        BigDecimal pricePerMin = null;
        try {
            pricePerMin = prices.get(keyOf(vm)).divide(BigDecimal.valueOf(60));
        } catch (ArithmeticException ex) {
            pricePerMin = BigDecimal.valueOf(prices.get(keyOf(vm)).doubleValue() / 60);
        }

        int chargeCount = (int) duration / MINUTE + 1;
        if (duration == (int) duration && (int) duration % MINUTE == 0) {
            chargeCount = (int) duration / MINUTE;
        }

        chargeCount = Math.max(10, chargeCount);
        return pricePerMin.multiply(BigDecimal.valueOf(chargeCount));
    }

    @Override
    public double nexChargeTime(final Vm vm) {
        double result = -1;
        if (vm instanceof VMex && ((VMex) vm).getStatus() == VMStatus.RUNNING) {
            VMex vmex = (VMex) vm;
            double elapsedTime = getCurrentTime() - vmex.getStartTime();
            result = vmex.getStartTime() + MINUTE * Math.max(10, (int) (elapsedTime / MINUTE) + 1);
        }
        return result;
    }
}
