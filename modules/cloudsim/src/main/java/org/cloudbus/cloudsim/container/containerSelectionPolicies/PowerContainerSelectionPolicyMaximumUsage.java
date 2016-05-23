package org.cloudbus.cloudsim.container.containerSelectionPolicies;

import org.cloudbus.cloudsim.container.core.PowerContainerHost;
import org.cloudbus.cloudsim.container.core.Container;
import org.cloudbus.cloudsim.container.core.PowerContainer;

import java.util.List;

/**
 * Created by sareh on 4/08/15.
 */
public class PowerContainerSelectionPolicyMaximumUsage extends PowerContainerSelectionPolicy {
    /*
    * (non-Javadoc)
    * @see
    * PowerContainerSelectionPolicy#getContainerToMigrate
    */
    @Override
    public Container getContainerToMigrate(PowerContainerHost host) {
        List<PowerContainer> migratableContainers = getMigratableContainers(host);
        if (migratableContainers.isEmpty()) {
            return null;
        }
        Container containerToMigrate = null;
        double maxMetric = Double.MIN_VALUE;
        for (Container container : migratableContainers) {
            if (container.isInMigration()) {
                continue;
            }
            double metric = container.getCurrentRequestedTotalMips();
            if (maxMetric < metric) {
                maxMetric = metric;
                containerToMigrate = container;
            }
        }
        return containerToMigrate;
    }
}
