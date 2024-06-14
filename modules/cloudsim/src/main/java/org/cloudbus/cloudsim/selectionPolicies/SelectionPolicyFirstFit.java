package org.cloudbus.cloudsim.selectionPolicies;


import org.cloudbus.cloudsim.core.HostEntity;

import java.util.List;
import java.util.Set;

/**
 * Created by Remo Andreoli (June 2024).
 * For First Fit policy.
 *
 * @since CloudSim toolkit 7.0
 */

public class SelectionPolicyFirstFit implements SelectionPolicy<HostEntity> {
    @Override
    public HostEntity select(List<HostEntity> candidates, Object obj, Set<HostEntity> excludedCandidates) {
        HostEntity selectedHost = null;
        for (HostEntity hostCandidate : candidates) {
            if (excludedCandidates.contains(hostCandidate)) {
                continue;
            }
            selectedHost = hostCandidate;
            break;
        }
        return selectedHost;
    }
}
