package org.cloudbus.cloudsim.container.vmSelectionPolicies;

import org.cloudbus.cloudsim.container.core.ContainerVm;
import org.cloudbus.cloudsim.container.core.PowerContainerHost;
import org.cloudbus.cloudsim.container.core.PowerContainerVm;

import java.util.List;

/**
 * Created by sareh on 16/11/15.
 */
public class PowerContainerVmSelectionPolicyMaximumUsage extends PowerContainerVmSelectionPolicy {
    /*
     * (non-Javadoc)
     * @see
     * org.cloudbus.cloudsim.experiments.power.PowerVmSelectionPolicy#getVmsToMigrate(org.cloudbus
     * .cloudsim.power.PowerHost)
     */
    @Override
    public ContainerVm getVmToMigrate(PowerContainerHost host) {
        List<PowerContainerVm> migratableContainers = getMigratableVms(host);
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
