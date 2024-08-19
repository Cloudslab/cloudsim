/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2024, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.selectionPolicies;

import org.cloudbus.cloudsim.container.utils.RandomGen;
import org.cloudbus.cloudsim.Log;

import java.util.List;
import java.util.Set;

/**
 * Created by Remo Andreoli (June 2024).
 * For Random policy.
 *
 * @since CloudSim toolkit 7.0
 */
public class SelectionPolicyRandomSelection<CandidateEntity> implements SelectionPolicy<CandidateEntity> {
    @Override
    public CandidateEntity select(List<CandidateEntity> candidates, Object obj, Set<CandidateEntity> excludedCandidates) {
        CandidateEntity selectedHost = null;
        while (true) {
            if (!candidates.isEmpty()) {
                int randomNum = new RandomGen().getNum(candidates.size());
                selectedHost = candidates.get(randomNum);
                if (excludedCandidates.contains(selectedHost)) {
                    continue;
                }
            } else {
                Log.println("Error: The host entity list is empty");
            }

            return selectedHost;
        }
    }

    /* Alternative without our RandomGen wrapper

    @Override
	public GuestEntity select(List<GuestEntity> candidates, Object obj, Set<GuestEntity> excludedCandidates) {
		if (candidates.isEmpty()) {
			return null;
		}

		int index = rand.nextInt(candidates.size());
		return candidates.get(index);
	}
     */
}
