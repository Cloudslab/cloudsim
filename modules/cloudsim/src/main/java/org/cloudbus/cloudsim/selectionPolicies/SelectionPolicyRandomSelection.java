package org.cloudbus.cloudsim.selectionPolicies;

import org.cloudbus.cloudsim.container.utils.RandomGen;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.HostEntity;

import java.util.List;
import java.util.Set;

/**
 * Created by Remo Andreoli (June 2024).
 * For Random policy.
 *
 * @since CloudSim toolkit 7.0
 */
public class SelectionPolicyRandomSelection implements SelectionPolicy<HostEntity> {
    @Override
    public HostEntity select(List<HostEntity> candidates, Object obj, Set<HostEntity> excludedCandidates) {
        HostEntity selectedHost = null;
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
}
