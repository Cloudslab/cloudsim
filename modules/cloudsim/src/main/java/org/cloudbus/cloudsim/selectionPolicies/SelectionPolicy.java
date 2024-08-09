package org.cloudbus.cloudsim.selectionPolicies;

import org.cloudbus.cloudsim.core.CoreAttributes;

import java.util.List;
import java.util.Set;

/**
 *  Created by Remo Andreoli (June 2024).
 *
 *  Abstract class for writing selection policies.
 *  It is generally used for placement (select a host for a guest entity) for
 *  migration (select a guest to migrate from a host)
 *
 * @since CloudSim toolkit 7.0
 */

public interface SelectionPolicy<T> {
    /**
     * Select a host from the hostCandidates list, ignoring the hosts in the excluded list.
     *
     * @param candidates             the candidate list
     * @param obj                    For arbitrary data
     * @param excludedCandidates candidates to be ignored from list
     * @return the selected entity
     */
    T select(List<T> candidates, Object obj, Set<T> excludedCandidates);
}
