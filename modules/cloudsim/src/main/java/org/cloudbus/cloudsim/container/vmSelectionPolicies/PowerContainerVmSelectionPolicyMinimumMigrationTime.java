package org.cloudbus.cloudsim.container.vmSelectionPolicies;

import org.cloudbus.cloudsim.container.core.*;

import java.util.List;

/**
 * Created by sareh on 30/07/15.
 */
public class PowerContainerVmSelectionPolicyMinimumMigrationTime extends  PowerContainerVmSelectionPolicy {



    @Override
    public ContainerVm getVmToMigrate(PowerContainerHost host) {
        List<PowerContainerVm> migratableVms = getMigratableVms(host);
        if (migratableVms.isEmpty()) {
            return null;
        }
        ContainerVm vmToMigrate = null;
        double minMetric = Double.MAX_VALUE;
        for (ContainerVm vm : migratableVms) {
            if (vm.isInMigration()) {
                continue;
            }
            double metric = vm.getRam();
            if (metric < minMetric) {
                minMetric = metric;
                vmToMigrate = vm;
            }
        }
        return vmToMigrate;
    }



}
