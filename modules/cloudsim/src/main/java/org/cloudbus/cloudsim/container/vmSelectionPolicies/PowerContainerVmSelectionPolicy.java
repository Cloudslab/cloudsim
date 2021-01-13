package org.cloudbus.cloudsim.container.vmSelectionPolicies;


import org.cloudbus.cloudsim.container.core.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sareh on 28/07/15.
 * The VM Selection Policy is used for selecting VMs to migrate when a host status is determined as
 * "Overloaded"
 */
public abstract class PowerContainerVmSelectionPolicy {

        /**
         * Gets the vms to migrate.
         *
         * @param host the host
         * @return the vms to migrate
         */
        public abstract ContainerVm getVmToMigrate(PowerContainerHost host);

        /**
         * Gets the migratable vms.
         *
         * @param host the host
         * @return the migratable vms
         */
        protected List<PowerContainerVm> getMigratableVms(PowerContainerHost host) {
            List<PowerContainerVm> migratableVms = new ArrayList<>();
            for (PowerContainerVm vm : host.<PowerContainerVm> getVmList()) {
                if (!vm.isInMigration()) {
                    migratableVms.add(vm);
                }
            }
            return migratableVms;
        }

}

