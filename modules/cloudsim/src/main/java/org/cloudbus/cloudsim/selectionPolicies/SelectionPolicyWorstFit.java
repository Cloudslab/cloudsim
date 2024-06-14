package org.cloudbus.cloudsim.selectionPolicies;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.core.HostEntity;

import java.util.*;

/**
 * Created by Remo Andreoli (June 2024).
 * For Worst-Fit policy.
 *
 * @since CloudSim toolkit 7.0
 */

public class SelectionPolicyWorstFit implements SelectionPolicy<HostEntity> {
    /** The map between each VM and the number of Pes used.
     * The map key is a VM UID and the value is the number of used Pes for that VM. */
    @Getter(AccessLevel.PROTECTED) @Setter(AccessLevel.PROTECTED)
    private Map<String, Integer> usedPes;

    /** The number of free Pes for each host from hostCandidates. */
    @Getter(AccessLevel.PROTECTED) @Setter(AccessLevel.PROTECTED)
    private List<Integer> freePes;

    @Override
    public HostEntity select(List<HostEntity> candidates, Object obj, Set<HostEntity> excludedCandidates) {
        int moreFree = Integer.MIN_VALUE;
        HostEntity selectedHost = null;

        for (HostEntity hostCandidate : candidates) {
            if (excludedCandidates.contains(hostCandidate)) {
                continue;
            }

            if (hostCandidate.getNumberOfFreePes() > moreFree) {
                selectedHost = hostCandidate;
                moreFree = hostCandidate.getNumberOfFreePes();
            }
        }
        return selectedHost;
    }
}
