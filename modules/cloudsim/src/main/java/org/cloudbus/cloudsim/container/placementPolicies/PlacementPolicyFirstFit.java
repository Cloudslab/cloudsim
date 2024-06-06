package org.cloudbus.cloudsim.container.placementPolicies;


import org.cloudbus.cloudsim.core.HostEntity;

import java.util.List;
import java.util.Set;

/**
 * Created by sareh fotuhi Piraghaj on 16/12/15.
 * Modified by Remo Andreoli June (2024).
 * For First Fit policy.
 */

public class PlacementPolicyFirstFit extends PlacementPolicy {
    @Override
    public HostEntity selectHost(List<HostEntity> hostCandidates, Object obj, Set<HostEntity> excludedHostCandidates) {
        HostEntity selectedHost = null;
        for (HostEntity hostCandidate : hostCandidates) {
            if (excludedHostCandidates.contains(hostCandidate)) {
                continue;
            }
            selectedHost = hostCandidate;
            break;
        }
        return selectedHost;
    }
}
