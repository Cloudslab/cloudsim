package TieredConfigurations;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;

public class PowerData {
    private double fossilFreePercentage;
    private double renewablePercentage;

    // Constructor
    public PowerData() throws IOException {
        EnergyAPI.ElectricityMapsAPI electricityMapsAPI = new EnergyAPI.ElectricityMapsAPI("JtPlg2xeVfyLW");
        // Parse the JSON data
        JsonObject jsonObject = JsonParser.parseString(electricityMapsAPI.getPowerBreakdown("FR")).getAsJsonObject();

        // Extract the fossilFreePercentage and renewablePercentage values
        this.fossilFreePercentage = jsonObject.get("fossilFreePercentage").getAsDouble();
        this.renewablePercentage = jsonObject.get("renewablePercentage").getAsDouble();
        System.out.println("Fossil Free Percentage: " + this.fossilFreePercentage);
        System.out.println("Renewable Percentage: " + this.renewablePercentage);
    }

    public double getFossilFreePercentage() {
        return fossilFreePercentage;
    }

    public double getRenewablePercentage() {
        return renewablePercentage;
    }
}
