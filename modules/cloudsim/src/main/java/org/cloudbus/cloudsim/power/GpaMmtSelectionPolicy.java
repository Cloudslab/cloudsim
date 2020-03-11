/*
* Growth Potential Aware Minimum Migration Time Selection Policy
* modified version of GPA selection policy
* @author: Sabir Mohammedi Taieb
* @since: Cloudsim 3.0 
*/

package org.cloudbus.cloudsim.power;

import org.cloudbus.cloudsim.Vm;
import java.util.List;
import org.cloudbus.cloudsim.core.CloudSim;

public class GpaMmtSelectionPolicy extends PowerVmSelectionPolicy{
    
    private double t; //current time

    @Override
    public Vm getVmToMigrate(PowerHost host) {
        List<PowerVm> migratableVms = getMigratableVms(host);
        if (migratableVms.isEmpty()) {
            return null;
        }
        Vm selectedVm = null;
        double minMetric = Double.MAX_VALUE;
        double max_util = Double.MIN_VALUE; //max mean usage
        double min_util = Double.MAX_VALUE; //min current usage
        for (PowerVm vm : migratableVms) {
            if (vm.isInMigration()) {
                continue;
            }
            //Mmt
            double metric = vm.getRam();
            if (metric < minMetric) {
                minMetric = metric;
                //GPA
                double timeCurrent = CloudSim.clock();
                double current_util = vm.getTotalUtilizationOfCpuMips(timeCurrent);
                double mean_util = vm.getUtilizationMean();
                if (mean_util > current_util) {
                    double vdiff = mean_util - current_util;
                    double utilization = mean_util + vdiff;
                    if (utilization > max_util && min_util != Double.MAX_VALUE) {
                        max_util = utilization;
                        if (max_util > min_util) {
                            selectedVm = vm;
                        }
                    }
                } else {
                    double utilization = current_util + (current_util - mean_util);
                    if (utilization < min_util) {
                        min_util = utilization;
                        if (max_util < min_util) {
                            selectedVm = vm;
                        }
                    }
                }
            }
        }
        return selectedVm;
    }
}