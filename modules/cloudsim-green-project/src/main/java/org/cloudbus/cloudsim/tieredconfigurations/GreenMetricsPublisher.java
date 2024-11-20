package org.cloudbus.cloudsim.tieredconfigurations;

import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.PushGateway;
import java.util.*;

public class GreenMetricsPublisher {
    private static final String PUSHGATEWAY_ADDRESS = "localhost:9091";
    private static final String JOB_NAME = "cloud_green_metrics";
    
    private static final PushGateway pushGateway = PushGateway.builder()
        .address(PUSHGATEWAY_ADDRESS)
        .job(JOB_NAME)
        .build();

    private static final Gauge greenScore = Gauge.builder()
        .name("datacenter_green_score")
        .help("Green score of the datacenter (0-1)")
        .labelNames("datacenter_id")
        .register();

    private static final Gauge powerConsumption = Gauge.builder()
        .name("datacenter_power_consumption")
        .help("Power consumption of the datacenter")
        .unit(Unit.NONE)
        .labelNames("datacenter_id")
        .register();

    private static final Gauge fossilFreePercentage = Gauge.builder()
        .name("datacenter_fossil_free_percentage")
        .help("Percentage of fossil-free energy")
        .unit(Unit.NONE)
        .labelNames("datacenter_id")
        .register();

    private static final Gauge activeVmsCount = Gauge.builder()
        .name("datacenter_active_vms")
        .help("Number of active VMs in the datacenter")
        .labelNames("datacenter_id")
        .register();

    private static final Gauge totalMips = Gauge.builder()
        .name("datacenter_total_mips")
        .help("Total MIPS capacity of the datacenter")
        .labelNames("datacenter_id")
        .register();

    private static final Gauge usedMips = Gauge.builder()
        .name("datacenter_used_mips")
        .help("Used MIPS in the datacenter")
        .labelNames("datacenter_id")
        .register();

    public static void publishMetrics(List<Datacenter> datacenters, PowerData powerData) {
        try {
            for (Datacenter dc : datacenters) {
                String datacenterId = dc.getName();
                MipsCalculation mipsCalc = calculateDatacenterMips(dc);
                
                double powerConsumptionValue = mipsCalc.usedMips / mipsCalc.totalMips;
                double greenScoreValue = calculateGreenScore(powerConsumptionValue, powerData);
                
                greenScore.labels(datacenterId).set(greenScoreValue);
                powerConsumption.labels(datacenterId).set(powerConsumptionValue);
                fossilFreePercentage.labels(datacenterId).set(powerData.getFossilFreePercentage());
                activeVmsCount.labels(datacenterId).set(dc.getVmList().size());
                totalMips.labels(datacenterId).set(mipsCalc.totalMips);
                usedMips.labels(datacenterId).set(mipsCalc.usedMips);
            }

            // Push all metrics to Pushgateway
            pushGateway.push();
            
        } catch (Exception e) {
            System.err.println("Error publishing metrics to Prometheus: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static class MipsCalculation {
        public final double totalMips;
        public final double usedMips;

        public MipsCalculation(double totalMips, double usedMips) {
            this.totalMips = totalMips;
            this.usedMips = usedMips;
        }
    }

    private static MipsCalculation calculateDatacenterMips(Datacenter datacenter) {
        double totalMips = 0;
        double usedMips = 0;
        
        for (Host host : datacenter.getHostList()) {
            totalMips += host.getTotalMips();
            usedMips += host.getUtilizationOfCpu() * host.getTotalMips();
        }
        
        return new MipsCalculation(totalMips, usedMips);
    }

    private static double calculateGreenScore(double powerConsumption, PowerData powerData) {
        double powerEfficiency = 1 - powerConsumption;
        double fossilFreeScore = powerData.getFossilFreePercentage() / 100.0;
        
        // Weight the scores (70% fossil-free, 30% power efficiency)
        return (fossilFreeScore * 0.7) + (powerEfficiency * 0.3);
    }
}