package org.cloudbus.cloudsim.container.vmSelectionPolicies;

import org.cloudbus.cloudsim.container.core.ContainerVm;
import org.cloudbus.cloudsim.container.core.PowerContainerVm;
import org.cloudbus.cloudsim.power.PowerHost;

import java.util.List;

/**
 * Created by sareh on 16/11/15.
 * Modified by Remo Andreoli (Feb 2024)
 */
public class PowerContainerVmSelectionPolicyMaximumUsage extends PowerContainerVmSelectionPolicy {
    /*
     * (non-Javadoc)
     * @see
     * org.cloudbus.cloudsim.experiments.power.PowerVmSelectionPolicy#getVmsToMigrate(org.cloudbus
     * .cloudsim.power.PowerHost)
     */
    @Override
    public ContainerVm getVmToMigrate(PowerHost host) {
        List<PowerContainerVm> migratableContainers = getMigrableVms(host);
        if (migratableContainers.isEmpty()) {
            return null;
        }
        ContainerVm VmsToMigrate = null;
        double maxMetric = Double.MIN_VALUE;
        for (ContainerVm vm : migratableContainers) {
            if (vm.isInMigration()) {
                continue;
            }
            double metric = vm.getCurrentRequestedTotalMips();
            if (maxMetric < metric) {
                maxMetric = metric;
                VmsToMigrate = vm;
            }
        }
//        Log.formatLine("The Container To migrate is #%d from VmID %d from host %d", containerToMigrate.getId(),containerToMigrate.getVm().getId(), host.getId());
        return VmsToMigrate;
    }


}
