package org.cloudbus.cloudsim.EX.billing;

import org.apache.commons.lang3.tuple.Pair;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.EX.vm.VmStatus;
import org.cloudbus.cloudsim.EX.vm.VmEX;

import java.math.BigDecimal;
import java.util.Map;

import static org.cloudbus.cloudsim.Consts.HOUR;

/**
 * Implements the default billing policy for Amazon EC2 on demand instances.
 * 
 * 
 * @author nikolay.grozev
 * 
 */
public class EC2OnDemandPolicy extends BaseCustomerVmBillingPolicy {

    /**
     * Constr.
     * 
     * @param prices
     *            - the prices in a map with entries in the form [[vm-type, OS],
     *            price]. Prices are hourly.
     */
    public EC2OnDemandPolicy(final Map<Pair<String, String>, BigDecimal> prices) {
        super(prices);
    }

    @Override
    public BigDecimal billSingleVm(final VmEX vm) {
        double timeAfterBoot = vm.getTimeAfterBooting();
        return computeBill(vm, timeAfterBoot);
    }

    @Override
    public BigDecimal billSingleVmUntil(VmEX vm, double endTime) {
        double time = vm.getEndTime() < 0 || vm.getEndTime() > endTime ? endTime - vm.getStartTime() : vm
                .getTimeAfterBooting();
        return computeBill(vm, time);
    }

    private BigDecimal computeBill(final VmEX vm, double duration) {
        int chargeCount = (int) duration / HOUR + 1;
        if (duration == (int) duration && (int) duration % HOUR == 0) {
            chargeCount = (int) duration / HOUR;
        }

        return prices.get(keyOf(vm)).multiply(BigDecimal.valueOf(chargeCount));
    }

    @Override
    public double nexChargeTime(final Vm vm) {
        double result = -1;
        if (vm instanceof VmEX vmex && ((VmEX) vm).getStatus() == VmStatus.RUNNING) {
            double elapsedTime = getCurrentTime() - vmex.getStartTime();
            result = vmex.getStartTime() + HOUR * ((int) (elapsedTime / HOUR) + 1);
        }
        return result;
    }

}
