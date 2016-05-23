package org.cloudbus.cloudsim.container.core;

import org.cloudbus.cloudsim.container.schedulers.ContainerVmScheduler;
import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmBwProvisioner;
import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmPe;
import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmRamProvisioner;
import org.cloudbus.cloudsim.power.models.PowerModel;

import java.util.List;

/**
 * Created by sareh on 28/07/15.
 */
public class PowerContainerHost extends ContainerHostDynamicWorkload {

    /**
     * The power model.
     */
    private PowerModel powerModel;

    /**
     * Instantiates a new host.
     *
     * @param id             the id
     * @param ramProvisioner the ram provisioner
     * @param bwProvisioner  the bw provisioner
     * @param storage        the storage
     * @param peList         the pe list
     * @param vmScheduler    the VM scheduler
     */
    public PowerContainerHost(
            int id,
            ContainerVmRamProvisioner ramProvisioner,
            ContainerVmBwProvisioner bwProvisioner,
            long storage,
            List<? extends ContainerVmPe> peList,
            ContainerVmScheduler vmScheduler,
            PowerModel powerModel) {
        super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
        setPowerModel(powerModel);
    }

    /**
     * Gets the power. For this moment only consumed by all PEs.
     *
     * @return the power
     */
    public double getPower() {
        return getPower(getUtilizationOfCpu());
    }

    /**
     * Gets the power. For this moment only consumed by all PEs.
     *
     * @param utilization the utilization
     * @return the power
     */
    protected double getPower(double utilization) {
        double power = 0;
        try {
            power = getPowerModel().getPower(utilization);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        return power;
    }

    /**
     * Gets the max power that can be consumed by the host.
     *
     * @return the max power
     */
    public double getMaxPower() {
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
     * @param fromUtilization the from utilization
     * @param toUtilization   the to utilization
     * @param time            the time
     * @return the energy
     */
    public double getEnergyLinearInterpolation(double fromUtilization, double toUtilization, double time) {
        if (fromUtilization == 0) {
            return 0;
        }
        double fromPower = getPower(fromUtilization);
        double toPower = getPower(toUtilization);
        return (fromPower + (toPower - fromPower) / 2) * time;
    }

    /**
     * Sets the power model.
     *
     * @param powerModel the new power model
     */
    protected void setPowerModel(PowerModel powerModel) {
        this.powerModel = powerModel;
    }

    /**
     * Gets the power model.
     *
     * @return the power model
     */
    public PowerModel getPowerModel() {
        return powerModel;
    }

}

