package org.cloudbus.cloudsim.container.hostSelectionPolicies;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.core.HostEntity;

import java.util.List;
import java.util.Set;

/**
 * Created by sareh on 12/08/15.
 * Modified by Remo Andreoli (Feb 2024)
 */
public class HostSelectionPolicyFirstFit extends HostSelectionPolicy {
    @Override
    public HostEntity getHost(List<HostEntity> hostList, Object obj, Set<? extends HostEntity> excludedHostList) {
        HostEntity host = null;
        for (HostEntity host1 : hostList) {
            if (excludedHostList.contains(host1)) {
                continue;
            }
            host = host1;
            break;
        }
    return host;
    }
}
