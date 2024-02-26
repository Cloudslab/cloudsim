package org.cloudbus.cloudsim.container.containerSelectionPolicies;

import org.cloudbus.cloudsim.container.core.Container;
import org.cloudbus.cloudsim.container.core.PowerContainer;
import org.cloudbus.cloudsim.power.PowerHost;

import java.util.List;

/**
 * Created by sareh on 3/08/15.
 * Modified by Remo Andreoli (Feb 2024)
 */
public class PowerContainerSelectionPolicyMinimumMigrationTime extends PowerContainerSelectionPolicy {


    /*
     * (non-Javadoc)
     * @see
     * PowerContainerSelectionPolicy#getContainerToMigrate
     */
    @Override
    public Container getContainerToMigrate(PowerHost host) {
        List<PowerContainer> migratableContainers = getMigratableContainers(host);
        if (migratableContainers.isEmpty()) {
            return null;
        }
        Container containerToMigrate = null;
        double minMetric = Double.MAX_VALUE;
        for (Container container : migratableContainers) {
            if (container.isInMigration()) {
                continue;
            }
            double metric = container.getRam();
            if (metric < minMetric) {
                minMetric = metric;
                containerToMigrate = container;
            }
        }
        return containerToMigrate;
    }

}
