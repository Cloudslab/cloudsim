package org.cloudbus.cloudsim.tieredconfigurations;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;

import org.cloudbus.cloudsim.energyapi.ElectricityMapsAPI;

public class PowerData {
    private double fossilFreePercentage;
    private double renewablePercentage;

    public PowerData(double fossil, double renewable ) throws IOException {
        //ElectricityMapsAPI electricityMapsAPI = new ElectricityMapsAPI("JtPlg2xeVfyLW");
        // Parse the JSON data
        //JsonObject jsonObject = JsonParser.parseString(electricityMapsAPI.getPowerBreakdown("FR")).getAsJsonObject();

        // Extract the fossilFreePercentage and renewablePercentage values
        this.fossilFreePercentage = fossil;
        this.renewablePercentage = renewable;
//        this.fossilFreePercentage = jsonObject.get("fossilFreePercentage").getAsDouble();
//        this.renewablePercentage = jsonObject.get("renewablePercentage").getAsDouble();
//        System.out.println("Fossil Free Percentage: " + this.fossilFreePercentage);
//        System.out.println("Renewable Percentage: " + this.renewablePercentage);
    }

    public double getFossilFreePercentage() {
        return fossilFreePercentage;
    }

    public double getRenewablePercentage() {
        return renewablePercentage;
    }

    public void setFossilFreePercentage(double percentage) {
        this.fossilFreePercentage = percentage;
    }

    public void setRenewablePercentage(double percentage) {
        this.renewablePercentage = percentage;
    }
}
