package org.cloudbus.cloudsim.container.hostSelectionPolicies;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.power.PowerHostUtilizationHistory;

import java.util.List;
import java.util.Set;

/**
 * Created by sareh on 11/08/15.
 * Modified by Remo Andreoli (Feb 2024)
 */
public class HostSelectionPolicyLeastFull extends HostSelectionPolicy{

    @Override
    public Host getHost(List<Host> hostList, Object obj, Set<? extends Host> excludedHostList) {
        double minUsage = Double.MAX_VALUE;
        Host selectedHost = null;
        for (Host host : hostList) {
            if (excludedHostList.contains(host)) {
                continue;
            }
            if (host instanceof PowerHostUtilizationHistory) {
                double hostUtilization= ((PowerHostUtilizationHistory) host).getUtilizationOfCpu();
                if ( hostUtilization < minUsage ) {
                    minUsage = hostUtilization;
                    selectedHost = host;

                }


            }
        }

        return selectedHost;
    }
}
