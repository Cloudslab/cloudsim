package org.cloudbus.cloudsim.selectionPolicies;

import org.cloudbus.cloudsim.core.GuestEntity;

import java.util.List;
import java.util.Set;

/**
 * Created by sareh on 16/11/15.
 * Modified by Remo Andreoli (Feb 2024)
 */
public class SelectionPolicyMaximumUsage<T extends GuestEntity> implements SelectionPolicy<T> {
    @Override
    public T select(List<T> candidates, Object obj, Set<T> excludedCandidates) {
        if (candidates.isEmpty()) {
            return null;
        }

        T selectedGuest = null;
        double maxMetric = Double.MIN_VALUE;

        for (T guest : candidates) {
            if (guest.isInMigration()) {
                continue;
            }
            double metric = guest.getCurrentRequestedTotalMips();
            if (maxMetric < metric) {
                maxMetric = metric;
                selectedGuest = guest;
            }
        }
        return selectedGuest;
    }
}
