package org.cloudbus.cloudsim.tieredconfigurations;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.Datacenter;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RealTimeSimulationManager {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private Datacenter currentDatacenter;
    private final PowerData powerData;

    public RealTimeSimulationManager(PowerData initialPowerData) {
        this.powerData = initialPowerData;
    }

    // Check every 5 minutes
    public void startRealTimeUpdates() {
        scheduler.scheduleAtFixedRate(this::updatePowerDataAndReselectDatacenter, 0, 5, TimeUnit.MINUTES);
    }

    private void updatePowerDataAndReselectDatacenter() {
        try {
            // Fetch or simulate new power data
            //simulatePowerDataChange();
            double fossilFreePercentage = powerData.getFossilFreePercentage();

            // Reselect the datacenter based on updated power data
            currentDatacenter = PowerMain.selectDatacenterBasedOnPowerData(powerData);
            System.out.println("Updated Datacenter(" + getCurrentDatacenter().getName() + ") selection based on fossil-free percentage: " + fossilFreePercentage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private void simulatePowerDataChange() {
//        // Simulate fluctuation in fossil-free percentage for demonstration purposes
//        double newFossilFreePercentage = Math.random() * 100;
//        powerData.setFossilFreePercentage(newFossilFreePercentage);
//        System.out.println("Simulated Power Data Update: Fossil-Free Percentage = " + newFossilFreePercentage);
//    }

    public void stopRealTimeUpdates() {
        scheduler.shutdown();
    }

    public Datacenter getCurrentDatacenter() {
        return currentDatacenter;
    }
}
