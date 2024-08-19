/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2024, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.selectionPolicies;

import org.cloudbus.cloudsim.core.HostEntity;
import org.cloudbus.cloudsim.power.PowerHost;

import java.util.List;
import java.util.Set;

/**
 * Created by Remo Andreoli (June 2024).
 * Most-Full (aka Worst-Fit) policy.
 *
 * @since CloudSim toolkit 7.0
 */
public class SelectionPolicyMostFull implements SelectionPolicy<HostEntity> {
    @Override
    public HostEntity select(List<HostEntity> candidates, Object obj, Set<HostEntity> excludedCandidates) {
        double minAvailable = Double.MAX_VALUE;
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

            if ( hostAvailable < minAvailable ) {
                minAvailable = hostAvailable;
                selectedHost = hostCandidate;
            }
        }
        return selectedHost;
    }
}
