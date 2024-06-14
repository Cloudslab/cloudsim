package org.cloudbus.cloudsim.selectionPolicies;

import org.cloudbus.cloudsim.core.HostEntity;
import org.cloudbus.cloudsim.power.PowerHost;

import java.util.List;
import java.util.Set;

/**
 * Created by Remo Andreoli (June 2024).
 * Least-Full policy.
 *
 * @since CloudSim toolkit 7.0
 */
public class SelectionPolicyLeastFull implements SelectionPolicy<HostEntity> {
    @Override
    public HostEntity select(List<HostEntity> candidates, Object obj, Set<HostEntity> excludedCandidates) {
        double maxAvailable = Double.MIN_VALUE;
        HostEntity selectedHost = null;

        for (HostEntity hostCandidate : candidates) {
            if (excludedCandidates.contains(hostCandidate)) {
                continue;
            }

            double hostAvailable;
            if (hostCandidate instanceof PowerHost powerHost) {
                hostAvailable = powerHost.getUtilizationOfCpu();
            } else {
                hostAvailable = hostCandidate.getGuestScheduler().getAvailableMips();
            }

            if (hostAvailable > maxAvailable) {
                maxAvailable = hostAvailable;
                selectedHost = hostCandidate;
            }
        }
        return selectedHost;
    }
}
