package org.cloudbus.cloudsim.container.hostSelectionPolicies;

import org.cloudbus.cloudsim.container.core.ContainerHost;

import java.util.List;
import java.util.Set;

/**
 * Created by sareh on 12/08/15.
 */
public class HostSelectionPolicyFirstFit extends HostSelectionPolicy {
    @Override
    public ContainerHost getHost(List<ContainerHost> hostList, Object obj, Set<? extends ContainerHost> excludedHostList) {
        ContainerHost host = null;
        for (ContainerHost host1 : hostList) {
            if (excludedHostList.contains(host1)) {
                continue;
            }
            host= host1;
            break;
        }
    return host;
    }
}
