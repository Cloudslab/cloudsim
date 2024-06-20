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

public class SelectionPolicyFirstFit<CandidateEntity> implements SelectionPolicy<CandidateEntity> {
    @Override
    public CandidateEntity select(List<CandidateEntity> candidates, Object obj, Set<CandidateEntity> excludedCandidates) {
        CandidateEntity selectedHost = null;
        for (CandidateEntity hostCandidate : candidates) {
            if (excludedCandidates.contains(hostCandidate)) {
                continue;
            }
            selectedHost = hostCandidate;
            break;
        }
        return selectedHost;
    }
}
