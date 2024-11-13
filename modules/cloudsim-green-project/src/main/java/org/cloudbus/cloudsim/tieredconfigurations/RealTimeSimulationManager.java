package org.cloudbus.cloudsim.tieredconfigurations;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class RealTimeSimulationManager extends SimEntity {
    protected enum RealTimeSimulationTags implements CloudSimTags {
        CREATE_SIMULATOR
    }

    private Datacenter currentDatacenter;
    private final PowerData powerData;
    private int currentHourIndex = 0; // Tracks the current hour in the CSV data
    private List<Double> fossilFreePercentages; // Holds fossil-free percentages from CSV

    public RealTimeSimulationManager(String name, PowerData initialPowerData) {
        super(name);
        this.powerData = initialPowerData;
        this.fossilFreePercentages = loadFossilFreePercentages("modules/cloudsim-green-project/src/main/java/org/cloudbus/cloudsim/energyapi/powerBreakdownData.csv");
        System.out.println(fossilFreePercentages);
    }

    // Load fossil-free percentages from the CSV file
    private List<Double> loadFossilFreePercentages(String filePath) {
        List<Double> percentages = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                String[] columns = line.split(",");
                double fossilFreePercentage = Double.parseDouble(columns[28].trim());
                percentages.add(fossilFreePercentage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return percentages;
    }

    @Override
    public void startEntity() {
        // Schedule the first update at time 0
        super.startEntity();
        schedule(getId(), 0, RealTimeSimulationTags.CREATE_SIMULATOR);
    }

    @Override
    public void processEvent(SimEvent ev) {
        if (currentHourIndex < fossilFreePercentages.size()) {
            simulatePowerDataChange();

            schedule(getId(), CloudSim.getMinTimeBetweenEvents() + 3600, RealTimeSimulationTags.CREATE_SIMULATOR); // 1-hour intervals in simulation time

            CloudSim.resumeSimulation();
        } else {
            System.out.println("End of simulation data.");
            CloudSim.stopSimulation();
        }
    }

//    // Check every 1 hour
//    public void startRealTimeUpdates() {
//        scheduler.scheduleAtFixedRate(this::updatePowerDataAndReselectDatacenter, 0, 1, TimeUnit.HOURS);
//    }

//    private void updatePowerDataAndReselectDatacenter() {
//        try {
//            // Fetch or simulate new power data
//            System.out.println("Updating power data");
//            simulatePowerDataChange();
//            // double fossilFreePercentage = powerData.getFossilFreePercentage();
//
//            // Reselect the datacenter based on updated power data
//            double fossilFreePercentage = powerData.getFossilFreePercentage();
//            currentDatacenter = PowerMain.selectDatacenterBasedOnPowerData(powerData);
//            System.out.println("Changed Datacenter(" + getCurrentDatacenter().getName() + ") selection based on fossil-free percentage: " + fossilFreePercentage);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private void simulatePowerDataChange() {
        // Simulate fossil-free percentage change for testing based on data gathered over 24h in California
        double newFossilFreePercentage = fossilFreePercentages.get(currentHourIndex);
        powerData.setFossilFreePercentage(newFossilFreePercentage);

        try {
            currentDatacenter = PowerMain.selectDatacenterBasedOnPowerData(powerData);
            System.out.println("Hour " + currentHourIndex + ": Changed Datacenter(" + currentDatacenter.getName()
                    + ") selection based on fossil-free percentage: " + newFossilFreePercentage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        currentHourIndex++;
//        if (currentHourIndex < fossilFreePercentages.size()) {
//            double newFossilFreePercentage = fossilFreePercentages.get(currentHourIndex);
//            powerData.setFossilFreePercentage(newFossilFreePercentage);
//
//            System.out.println("Simulated Power Data Update: Fossil-Free Percentage = " + newFossilFreePercentage);
//            currentHourIndex++;
//        } else {
//            System.out.println("End of 23-hour simulation data.");
//            stopRealTimeUpdates();
//        }
    }

    @Override
    public void shutdownEntity() {
        System.out.println("RealTimeSimulationManager is shutting down.");
    }

//    public void stopRealTimeUpdates() {
//        scheduler.shutdown();
//    }

    public Datacenter getCurrentDatacenter() {
        return currentDatacenter;
    }
}
