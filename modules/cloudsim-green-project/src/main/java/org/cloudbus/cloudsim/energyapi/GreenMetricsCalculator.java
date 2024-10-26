package org.cloudbus.cloudsim.energyapi;

public class GreenMetricsCalculator {
    /**
     * Conventional Carbon Intensity: Electricity Maps API the number already
     * Renewable Carbon Intensity: Electricity Maps API provides renewable percentages, calculate intensity manually. Formula: RenewableIntensity = (CarbonIntensity * RenewablePercentage) / 100
     * Workload Duration: Total time the workload runs
     * Workload Output: Custom Metric based on System's scheduler
     * Energy Consumed: AWS CloudWatch to  track the energy consumption of workloads
     * Cost Savings:
     * Carbon Savings:
     */
    // Placeholder values - these should be replaced by real API data in the future
    private static final double PLACEHOLDER_CONVENTIONAL_INTENSITY = 500.0; // gCO2/kWh
    private static final double PLACEHOLDER_RENEWABLE_INTENSITY = 100.0; // gCO2/kWh
    private static final double PLACEHOLDER_WORKLOAD_DURATION = 5.0; // hours
    private static final double PLACEHOLDER_WORKLOAD_OUTPUT = 1000.0;
    private static final double PLACEHOLDER_ENERGY_CONSUMED = 200.0; // kWh
    private static final double PLACEHOLDER_COST_SAVINGS = 300.0; // dollars
    private static final double PLACEHOLDER_CARBON_SAVINGS = 50.0; // gCO2

    /**
     * Carbon Emission Reduction (CER).
     * Formula: CER = (CarbonIntensityConventional - CarbonIntensityRenewable) * WorkloadDuration
     */
    public double calculateCER(double conventionalIntensity, double renewableIntensity, double workloadDuration) {
        return (conventionalIntensity - renewableIntensity) * workloadDuration;
    }

    /**
     * Energy Efficiency Score (EES).
     * Formula: EES = WorkloadOutput / TotalEnergyConsumed
     */
    public double calculateEES(double workloadOutput, double totalEnergyConsumed) {
        return workloadOutput / totalEnergyConsumed;
    }

    /**
     * Renewable Energy Utilization (REU).
     * Formula: REU = (WorkloadsOnRenewableEnergy / TotalWorkloads) * 100
     */
    public double calculateREU(double workloadsOnRenewable, double totalWorkloads) {
        return (workloadsOnRenewable / totalWorkloads) * 100;
    }

    /**
     * Cost-Carbon Tradeoff Score (CCTS).
     * Formula: CCTS = CostSavings + CarbonSavings
     */
    public double calculateCCTS(double costSavings, double carbonSavings) {
        return costSavings + carbonSavings;
    }

    // Print metrics based on placeholder data
    public void printSampleMetrics() {
        System.out.println("=== Green Metrics Calculation ===");

        double cer = calculateCER(
                PLACEHOLDER_CONVENTIONAL_INTENSITY,
                PLACEHOLDER_RENEWABLE_INTENSITY,
                PLACEHOLDER_WORKLOAD_DURATION
        );
        System.out.println("Carbon Emission Reduction (CER): " + cer + " gCO2");

        double ees = calculateEES(
                PLACEHOLDER_WORKLOAD_OUTPUT,
                PLACEHOLDER_ENERGY_CONSUMED
        );
        System.out.println("Energy Efficiency Score (EES): " + ees);

        double reu = calculateREU(80, 100);
        System.out.println("Renewable Energy Utilization (REU): " + reu + " %");

        double ccts = calculateCCTS(
                PLACEHOLDER_COST_SAVINGS,
                PLACEHOLDER_CARBON_SAVINGS
        );
        System.out.println("Cost-Carbon Tradeoff Score (CCTS): " + ccts);
    }

    public static void main(String[] args) {
        GreenMetricsCalculator calculator = new GreenMetricsCalculator();
        calculator.printSampleMetrics();
    }
}
