package org.cloudbus.cloudsim.container.hostSelectionPolicies;

import org.cloudbus.cloudsim.container.core.ContainerHost;

import java.util.List;
import java.util.Set;

/**
 * Created by sareh on 11/08/15.
 */
public abstract class HostSelectionPolicy {

    /**
     * Gets the host
     *
     * @param hostList the host
     * @return the destination host to migrate
     */
    public abstract ContainerHost getHost(List<ContainerHost> hostList, Object obj, Set<? extends ContainerHost> excludedHostList);

}
