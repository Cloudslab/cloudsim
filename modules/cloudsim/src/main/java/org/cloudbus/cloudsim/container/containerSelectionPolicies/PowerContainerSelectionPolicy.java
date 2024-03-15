package org.cloudbus.cloudsim.container.containerSelectionPolicies;


import org.cloudbus.cloudsim.container.core.Container;
import org.cloudbus.cloudsim.container.core.PowerContainer;
import org.cloudbus.cloudsim.container.core.PowerContainerVm;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.power.PowerHost;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sareh on 31/07/15.
 * Modified by Remo Andreoli (Feb 2024)
 */
public abstract class PowerContainerSelectionPolicy {

    /**
     * Gets the containers to migrate.
     *
     * @param host the host
     * @return the container to migrate
     */
    public abstract Container getContainerToMigrate(PowerHost host);

    /**
     * Gets the migratable containers.
     *
     * @param host the host
     * @return the migratable containers
     */
    protected List<PowerContainer> getMigratableContainers(PowerHost host) {
        List<PowerContainer> migratableContainers= new ArrayList<>();
        for (PowerContainerVm vm : host.<PowerContainerVm>getGuestList()) {
            if (!vm.isInMigration()) {
                for (GuestEntity container: vm.getGuestList()){

                    if(!container.isInMigration() && !vm.getGuestsMigratingIn().contains(container)){
                        migratableContainers.add((PowerContainer) container);}

                }



            }
        }
        return migratableContainers;
    }

}
