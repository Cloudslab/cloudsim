// PowerAwareVmMigration.java
package org.cloudbus.cloudsim.tieredconfigurations;

import org.cloudbus.cloudsim.*;
import java.util.*;

public class PowerAwareVmMigration {
    private static final double POWER_THRESHOLD = 0.8; // 80% power threshold
    private static final double GREEN_SCORE_WEIGHT = 0.7;
    private static final double PERFORMANCE_WEIGHT = 0.3;

    public static class MigrationDecision {
        public final Vm vm;
        public final Datacenter targetDatacenter;
        public final double greenScore;

        public MigrationDecision(Vm vm, Datacenter targetDatacenter, double greenScore) {
            this.vm = vm;
            this.targetDatacenter = targetDatacenter;
            this.greenScore = greenScore;
        }
    }

    public static List<MigrationDecision> evaluateAndPlanMigrations(
            List<Datacenter> datacenters, 
            Map<Integer, Vm> vmMap, 
            PowerData currentPowerData) {
        
        List<MigrationDecision> migrationDecisions = new ArrayList<>();
        
        Map<Datacenter, Double> datacenterPowerConsumption = new HashMap<>();
        for (Datacenter dc : datacenters) {
            double powerConsumption = calculateDatacenterPowerConsumption(dc);
            datacenterPowerConsumption.put(dc, powerConsumption);
        }

        for (Vm vm : vmMap.values()) {
            Datacenter currentDc = findCurrentDatacenter(datacenters, vm);
            if (currentDc == null) continue;

            // Calculate current datacenter metrics
            double currentPowerConsumption = datacenterPowerConsumption.get(currentDc);
            
            if (shouldMigrate(currentPowerConsumption, currentPowerData)) {
                MigrationDecision decision = findBestTargetDatacenter(
                    vm, 
                    datacenters, 
                    currentDc, 
                    currentPowerData,
                    datacenterPowerConsumption
                );
                
                if (decision != null) {
                    migrationDecisions.add(decision);
                }
            }
        }
        
        return migrationDecisions;
    }

    private static boolean shouldMigrate(double currentPowerConsumption, PowerData powerData) {
        return currentPowerConsumption > POWER_THRESHOLD && 
               powerData.getFossilFreePercentage() < 50.0;
    }

    private static double calculateDatacenterPowerConsumption(Datacenter datacenter) {
        double totalMips = 0;
        double usedMips = 0;
        
        for (Host host : datacenter.getHostList()) {
            totalMips += host.getTotalMips();
            usedMips += host.getUtilizationOfCpu() * host.getTotalMips();
        }
        
        return usedMips / totalMips;
    }

    private static Datacenter findCurrentDatacenter(List<Datacenter> datacenters, Vm vm) {
        for (Datacenter dc : datacenters) {
            if (dc.getVmList().contains(vm)) {
                return dc;
            }
        }
        return null;
    }

    private static MigrationDecision findBestTargetDatacenter(
            Vm vm,
            List<Datacenter> datacenters,
            Datacenter currentDc,
            PowerData powerData,
            Map<Datacenter, Double> datacenterPowerConsumption) {

        Datacenter bestTarget = null;
        double bestScore = -1;
        
        for (Datacenter targetDc : datacenters) {
            if (targetDc == currentDc) continue;
            
            double targetPowerConsumption = datacenterPowerConsumption.get(targetDc);
            if (canHostVm(targetDc, vm) && targetPowerConsumption < POWER_THRESHOLD) {
                double greenScore = calculateGreenScore(targetDc, powerData);
                double performanceScore = calculatePerformanceScore(targetDc, vm);
                double totalScore = (greenScore * GREEN_SCORE_WEIGHT) + 
                                  (performanceScore * PERFORMANCE_WEIGHT);
                
                if (totalScore > bestScore) {
                    bestScore = totalScore;
                    bestTarget = targetDc;
                }
            }
        }
        
        return bestTarget != null ? 
               new MigrationDecision(vm, bestTarget, bestScore) : null;
    }

    private static boolean canHostVm(Datacenter datacenter, Vm vm) {
        for (Host host : datacenter.getHostList()) {
            if (host.getVmScheduler().getAvailableMips() >= vm.getMips() &&
                host.getRamProvisioner().getAvailableRam() >= vm.getRam() &&
                host.getBwProvisioner().getAvailableBw() >= vm.getBw()) {
                return true;
            }
        }
        return false;
    }

    private static double calculateGreenScore(Datacenter datacenter, PowerData powerData) {
        // Combine fossil-free percentage with power efficiency
        return powerData.getFossilFreePercentage() / 100.0;
    }

    private static double calculatePerformanceScore(Datacenter datacenter, Vm vm) {
        // Calculate based on available resources vs VM requirements
        double availableMips = 0;
        double totalMips = 0;
        
        for (Host host : datacenter.getHostList()) {
            availableMips += host.getVmScheduler().getAvailableMips();
            totalMips += host.getTotalMips();
        }
        
        return availableMips / totalMips;
    }
}
