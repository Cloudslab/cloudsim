package org.cloudbus.cloudsim.selectionPolicies;

import org.cloudbus.cloudsim.container.utils.RandomGen;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.HostEntity;

import java.util.List;
import java.util.Set;

/**
 * Created by sareh fotuhi Piraghaj on 16/12/15.
 * Modified by Remo Andreoli June (2024).
 * For Random policy.
 */
public class SelectionPolicyRandomSelection extends SelectionPolicy {
    @Override
    public HostEntity selectHost(List<HostEntity> hostCandidates, Object obj, Set<HostEntity> excludedHostCandidates) {
        HostEntity selectedHost = null;
        while (true) {
            if (!hostCandidates.isEmpty()) {
                int randomNum = new RandomGen().getNum(hostCandidates.size());
                selectedHost = hostCandidates.get(randomNum);
                if (excludedHostCandidates.contains(selectedHost)) {
                    continue;
                }
            } else {
                Log.println("Error: The host entity list is empty");
            }

            return selectedHost;
        }
    }
}
