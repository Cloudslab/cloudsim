package org.cloudbus.cloudsim.container.placementPolicies;

import org.cloudbus.cloudsim.core.HostEntity;
import org.cloudbus.cloudsim.power.PowerHost;

import java.util.List;
import java.util.Set;

/**
 * Created by sareh fotuhi Piraghaj on 16/12/15.
 * Modified by Remo Andreoli June (2024).
 * For Least-Full policy.
 */
public class PlacementPolicyLeastFull extends PlacementPolicy {
    @Override
    public HostEntity selectHost(List<HostEntity> hostCandidates, Object obj, Set<HostEntity> excludedHostCandidates) {
        double maxUsage = Double.MIN_VALUE;
        HostEntity selectedHost = null;

        for (HostEntity hostCandidate : hostCandidates) {
            if (excludedHostCandidates.contains(hostCandidate)) {
                continue;
            }

            double hostUsage;
            if (hostCandidate instanceof PowerHost powerHost) {
                hostUsage = powerHost.getUtilizationOfCpu();
            } else {
                hostUsage = hostCandidate.getGuestScheduler().getAvailableMips();
            }

            if (hostUsage > maxUsage) {
                maxUsage = hostUsage;
                selectedHost = hostCandidate;
            }
        }
        return selectedHost;
    }
}
