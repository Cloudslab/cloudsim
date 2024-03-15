package org.cloudbus.cloudsim.container.vmSelectionPolicies;


import org.cloudbus.cloudsim.container.core.*;
import org.cloudbus.cloudsim.power.PowerHost;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sareh on 28/07/15.
 * Modified by Remo Andreoli (Feb 2024)
 */
public abstract class PowerContainerVmSelectionPolicy {

        /**
         * Gets the vms to migrate.
         *
         * @param host the host
         * @return the vms to migrate
         */
        public abstract ContainerVm getVmToMigrate(PowerHost host);

        /**
         * Gets the migratable vms.
         *
         * @param host the host
         * @return the migratable vms
         */
        protected List<PowerContainerVm> getMigratableVms(PowerHost host) {
            List<PowerContainerVm> migratableVms = new ArrayList<>();
            for (PowerContainerVm vm : host.<PowerContainerVm> getGuestList()) {
                if (!vm.isInMigration()) {
                    migratableVms.add(vm);
                }
            }
            return migratableVms;
        }

}

