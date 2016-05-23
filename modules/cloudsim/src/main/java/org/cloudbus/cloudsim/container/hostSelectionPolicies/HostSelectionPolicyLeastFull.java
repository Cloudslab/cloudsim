package org.cloudbus.cloudsim.container.hostSelectionPolicies;

import org.cloudbus.cloudsim.container.core.ContainerHost;
import org.cloudbus.cloudsim.container.core.PowerContainerHostUtilizationHistory;

import java.util.List;
import java.util.Set;

/**
 * Created by sareh on 11/08/15.
 */
public class HostSelectionPolicyLeastFull extends HostSelectionPolicy{

    @Override
    public ContainerHost getHost(List<ContainerHost> hostList, Object obj,Set<? extends ContainerHost> excludedHostList) {
        double minUsage = Double.MAX_VALUE;
        ContainerHost selectedHost = null;
        for (ContainerHost host : hostList) {
            if (excludedHostList.contains(host)) {
                continue;
            }
            if (host instanceof PowerContainerHostUtilizationHistory) {
                double hostUtilization= ((PowerContainerHostUtilizationHistory) host).getUtilizationOfCpu();
                if ( hostUtilization < minUsage ) {
                    minUsage = hostUtilization;
                    selectedHost = host;

                }


            }
        }

        return selectedHost;
    }
}
