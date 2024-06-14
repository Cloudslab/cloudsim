package org.cloudbus.cloudsim.selectionPolicies;

import org.cloudbus.cloudsim.core.HostEntity;

import java.util.List;
import java.util.Set;

/**
 *  Initially created by sareh fotuhi Piraghaj on 16/12/15.
 *  Completely overhauled by Remo Andreoli on June 2024.
 *
 *  Abstract class for writing placement policies for guest entities.
 */

public abstract class SelectionPolicy {
    /**
     * Select a host from the hostCandidates list, ignoring the hosts in the excluded list.
     *
     * @param hostCandidates the host list
     * @param obj For arbitrary data
     * @param excludedHostCandidates hosts to be ignored
     * @return the destination host to place guest
     */
    public abstract HostEntity selectHost(List<HostEntity> hostCandidates, Object obj, Set<HostEntity> excludedHostCandidates);
}
