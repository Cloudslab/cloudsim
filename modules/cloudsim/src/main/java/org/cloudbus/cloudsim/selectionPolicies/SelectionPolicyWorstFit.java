package org.cloudbus.cloudsim.selectionPolicies;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.core.HostEntity;

import java.util.*;

/**
 * Created by Remo Andreoli June (2024).
 * For Worst-Fit policy.
 */

public class SelectionPolicyWorstFit extends SelectionPolicy {
    /** The map between each VM and the number of Pes used.
     * The map key is a VM UID and the value is the number of used Pes for that VM. */
    @Getter(AccessLevel.PROTECTED) @Setter(AccessLevel.PROTECTED)
    private Map<String, Integer> usedPes;

    /** The number of free Pes for each host from hostCandidates. */
    @Getter(AccessLevel.PROTECTED) @Setter(AccessLevel.PROTECTED)
    private List<Integer> freePes;

    @Override
    public HostEntity selectHost(List<HostEntity> hostCandidates, Object obj, Set<HostEntity> excludedHostCandidates) {
        int moreFree = Integer.MIN_VALUE;
        HostEntity selectedHost = null;

        for (HostEntity hostCandidate : hostCandidates) {
            if (excludedHostCandidates.contains(hostCandidate)) {
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
