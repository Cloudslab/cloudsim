package org.cloudbus.cloudsim.container.hostSelectionPolicies;

import org.cloudbus.cloudsim.Host;

import java.util.List;
import java.util.Set;

/**
 * Created by sareh on 11/08/15.
 * Modified by Remo Andreoli (Feb 2024)
 */
public abstract class HostSelectionPolicy {

    /**
     * Gets the host
     *
     * @param hostList the host
     * @return the destination host to migrate
     */
    public abstract Host getHost(List<Host> hostList, Object obj, Set<? extends Host> excludedHostList);

}
