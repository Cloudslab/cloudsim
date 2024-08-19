/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2024, The University of Melbourne, Australia
 */

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
