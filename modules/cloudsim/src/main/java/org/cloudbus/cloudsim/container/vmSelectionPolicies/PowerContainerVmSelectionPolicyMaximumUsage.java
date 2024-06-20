package org.cloudbus.cloudsim.container.vmSelectionPolicies;

import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.selectionPolicies.SelectionPolicy;

import java.util.List;
import java.util.Set;

/**
 * Created by sareh on 16/11/15.
 * Modified by Remo Andreoli (Feb 2024)
 */
public class PowerContainerVmSelectionPolicyMaximumUsage implements SelectionPolicy<GuestEntity> {
    @Override
    public GuestEntity select(List<GuestEntity> candidates, Object obj, Set<GuestEntity> excludedCandidates) {
        if (candidates.isEmpty()) {
            return null;
        }

        GuestEntity selectedGuest = null;
        double maxMetric = Double.MIN_VALUE;

        for (GuestEntity vm : candidates) {
            if (vm.isInMigration()) {
                continue;
            }
            double metric = vm.getCurrentRequestedTotalMips();
            if (maxMetric < metric) {
                maxMetric = metric;
                selectedGuest = vm;
            }
        }
        return selectedGuest;
    }
}
