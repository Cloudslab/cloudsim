package org.cloudbus.cloudsim.container.hostSelectionPolicies;

import org.cloudbus.cloudsim.Host;

import java.util.List;
import java.util.Set;

/**
 * Created by sareh on 12/08/15.
 * Modified by Remo Andreoli (Feb 2024)
 */
public class HostSelectionPolicyFirstFit extends HostSelectionPolicy {
    @Override
    public Host getHost(List<Host> hostList, Object obj, Set<? extends Host> excludedHostList) {
        Host host = null;
        for (Host host1 : hostList) {
            if (excludedHostList.contains(host1)) {
                continue;
            }
            host= host1;
            break;
        }
    return host;
    }
}
