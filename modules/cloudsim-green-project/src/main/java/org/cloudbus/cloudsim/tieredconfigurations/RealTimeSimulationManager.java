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
            System.out.println("Changed Datacenter(" + getCurrentDatacenter().getName() + ") selection based on fossil-free percentage: " + fossilFreePercentage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /*
    Example Scenario to possibly create here?
    8:00 AM - High Resource Datacenter selected based on 80% renewable energy. All cloudlets are allocated to high-capacity VMs, with quick execution.
    11:00 AM - Renewable energy drops to 60%. The Medium Resource Datacenter is selected. New cloudlets are scheduled on medium-capacity VMs. Maybe migrate old cloudlets to new VMs?
    3:00 PM - Renewables fall below 30%. The Low Resource Datacenter is chosen. Same as before? Maybe limit cloudlets to only run important ones?
    7:00 PM - Renewables increase back to 50%. The Medium Resource Datacenter is selected again, migrate to this again and allow more cloudlets run?
     */
//    private void simulatePowerDataChange() {
//        // Simulate fossil-free percentage change for testing
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
