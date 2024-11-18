// Kevin Le (kevinle2)

package org.cloudbus.cloudsim.tieredconfigurations.power;

import java.io.IOException;

public class PowerAwareData {
    private double powerConsumptionTotal;

    public PowerAwareData(double powerConsumptionTotal) throws IOException {
        this.powerConsumptionTotal = powerConsumptionTotal;
    }

    public double getPowerConsumptionTotal() {
        return powerConsumptionTotal;
    }

    public void setPowerConsumptionTotal(double powerConsumptionTotal) {
        this.powerConsumptionTotal = powerConsumptionTotal;
    }
}
