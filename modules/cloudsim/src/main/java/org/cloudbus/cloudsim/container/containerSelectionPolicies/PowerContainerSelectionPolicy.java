package org.cloudbus.cloudsim.container.containerSelectionPolicies;


import org.cloudbus.cloudsim.container.core.Container;
import org.cloudbus.cloudsim.container.core.PowerContainer;
import org.cloudbus.cloudsim.container.core.PowerContainerHost;
import org.cloudbus.cloudsim.container.core.PowerContainerVm;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sareh on 31/07/15.
 */
public abstract class PowerContainerSelectionPolicy {

    /**
     * Gets the containers to migrate.
     *
     * @param host the host
     * @return the container to migrate
     */
    public abstract Container getContainerToMigrate(PowerContainerHost host);

    /**
     * Gets the migratable containers.
     *
     * @param host the host
     * @return the migratable containers
     */
    protected List<PowerContainer> getMigratableContainers(PowerContainerHost host) {
        List<PowerContainer> migratableContainers= new ArrayList<>();
        for (PowerContainerVm vm : host.<PowerContainerVm> getVmList()) {
            if (!vm.isInMigration()) {
                for (Container container: vm.getContainerList()){

                    if(!container.isInMigration() && !vm.getContainersMigratingIn().contains(container)){
                        migratableContainers.add((PowerContainer) container);}

                }



            }
        }
        return migratableContainers;
    }

}
