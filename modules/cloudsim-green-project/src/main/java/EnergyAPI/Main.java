package EnergyAPI;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            // Initialize and test Electricity Maps API
            EnergyAPI.ElectricityMapsAPI electricityMapsAPI = new EnergyAPI.ElectricityMapsAPI("JtPlg2xeVfyLW");
            String carbonData = electricityMapsAPI.getCarbonIntensity("FR");
            System.out.println("Carbon Intensity Data: " + carbonData);

            // Initialize and test WattTime API
            EnergyAPI.WattTimeAPI wattTimeAPI = new EnergyAPI.WattTimeAPI();
            // wattTimeAPI.registerUser("KKRS123", "kkrs123!", "kevinle2@illinois.edu", "Testing123");
            wattTimeAPI.login("KKRS123", "kkrs123!");
            String moerData = wattTimeAPI.getData();
            System.out.println("Data: " + moerData);

        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }
}
