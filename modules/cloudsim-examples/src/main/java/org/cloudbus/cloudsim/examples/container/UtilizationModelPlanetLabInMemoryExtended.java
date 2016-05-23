package org.cloudbus.cloudsim.examples.container;

import org.cloudbus.cloudsim.UtilizationModelPlanetLabInMemory;
import org.cloudbus.cloudsim.examples.power.Constants;

import java.io.IOException;

/**
 * Created by sareh on 5/08/15.
 */
public class UtilizationModelPlanetLabInMemoryExtended extends UtilizationModelPlanetLabInMemory {

    public UtilizationModelPlanetLabInMemoryExtended(String inputPath, double schedulingInterval) throws NumberFormatException, IOException {
        super(inputPath, schedulingInterval);
    }

    public UtilizationModelPlanetLabInMemoryExtended(String inputPath, double schedulingInterval, int dataSamples) throws NumberFormatException, IOException {
        super(inputPath, schedulingInterval, dataSamples);
    }

    /*
     * (non-Javadoc)
     * @see cloudsim.power.UtilizationModel#getUtilization(double)
     */
    @Override
    public double getUtilization(double inputTime) {
        double utilization;
        if (inputTime > Constants.SIMULATION_LIMIT || inputTime == Constants.SIMULATION_LIMIT) {
            utilization = calUtilization(inputTime % Constants.SIMULATION_LIMIT);
        } else {
            utilization = calUtilization(inputTime);
        }

        return utilization;
    }

    public double calUtilization(double time) {
//        Log.print(time);
        double[] data = super.getData();
        if (time % getSchedulingInterval() == 0) {
            return data[(int) time / (int) getSchedulingInterval()];
        }
        int time1 = (int) Math.floor(time / getSchedulingInterval());
        int time2 = (int) Math.ceil(time / getSchedulingInterval());
        double utilization1 = data[time1];
        double utilization2 = data[time2];
        double delta = (utilization2 - utilization1) / ((time2 - time1) * getSchedulingInterval());
        double utilization = utilization1 + delta * (time - time1 * getSchedulingInterval());

        return utilization;

    }

}
