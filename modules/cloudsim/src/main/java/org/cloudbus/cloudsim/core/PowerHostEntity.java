package org.cloudbus.cloudsim.core;

import org.cloudbus.cloudsim.power.models.PowerModel;

/**
 * Represents a Physical Machine (PM) that stores its CPU utilization percentage history.
 * The history is used by VM allocation and selection policies.
 *
 * @author Remo Andreoli
 * @since CloudSim Toolkit 7.0
 */
public interface PowerHostEntity extends HostEntity {
    int HISTORY_LENGTH = 30; // static and final by default

    /**
     * Gets the host CPU utilization percentage history.
     *
     * @return the host CPU utilization percentage history
     */
    double[] getUtilizationHistory();

    /**
     * Sets the power model.
     *
     * @param powerModel the new power model
     */
    void setPowerModel(PowerModel powerModel);

    /**
     * Gets the power model.
     *
     * @return the power model
     */
    PowerModel getPowerModel();

    /**
     * Gets the power. For this moment only consumed by all PEs.
     *
     * @return the power
     */
    double getPower();

    /**
     * Gets the current power consumption of the host. For this moment only consumed by all PEs.
     *
     * @param utilization the utilization percentage (between [0 and 1]) of a resource that
     * is critical for power consumption
     * @return the power consumption
     */
    double getPower(double utilization);

    /**
     * Gets the max power that can be consumed by the host.
     *
     * @return the max power
     */
    default double getMaxPower() {
        double power = 0;
        try {
            power = getPowerModel().getPower(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        return power;
    }

    /**
     * Gets the energy consumption using linear interpolation of the utilization change.
     *
     * @param fromUtilization the initial utilization percentage
     * @param toUtilization the final utilization percentage
     * @param time the time
     * @return the energy
     */
    default double getEnergyLinearInterpolation(double fromUtilization, double toUtilization, double time) {
        if (fromUtilization == 0) {
            return 0;
        }
        double fromPower = getPower(fromUtilization);
        double toPower = getPower(toUtilization);
        return (fromPower + (toPower - fromPower) / 2) * time;
    }

}
