package org.cloudbus.cloudsim.energyapi;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            // Initialize and test Electricity Maps API
            org.cloudbus.cloudsim.energyapi.ElectricityMapsAPI electricityMapsAPI = new org.cloudbus.cloudsim.energyapi.ElectricityMapsAPI("JtPlg2xeVfyLW");
            String carbonData = electricityMapsAPI.getCarbonIntensity("FR");
            System.out.println("Carbon Intensity Data: " + carbonData);

            String powerBreakdown = electricityMapsAPI.getPowerBreakdown("FR");
            System.out.println("Power Breakdown Data: " + powerBreakdown);

            // Initialize and test WattTime API
            org.cloudbus.cloudsim.energyapi.WattTimeAPI wattTimeAPI = new org.cloudbus.cloudsim.energyapi.WattTimeAPI();
            // wattTimeAPI.registerUser("KKRS123", "kkrs123!", "kevinle2@illinois.edu", "Testing123");
            wattTimeAPI.login("KKRS123", "kkrs123!");
            String moerData = wattTimeAPI.getData();
            System.out.println("Data: " + moerData);

        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }
}
