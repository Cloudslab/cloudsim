package TieredConfigurations;

public class PowerData {
    private double fossilFreePercentage;
    private double renewablePercentage;

    // Constructor
    public PowerData(double fossilFreePercentage, double renewablePercentage) {
        this.fossilFreePercentage = fossilFreePercentage;
        this.renewablePercentage = renewablePercentage;
    }

    public double getFossilFreePercentage() {
        return fossilFreePercentage;
    }

    public double getRenewablePercentage() {
        return renewablePercentage;
    }
}
