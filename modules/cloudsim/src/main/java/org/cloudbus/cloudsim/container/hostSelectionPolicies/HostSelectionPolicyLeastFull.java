package org.cloudbus.cloudsim.container.hostSelectionPolicies;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.core.HostEntity;
import org.cloudbus.cloudsim.power.PowerHostUtilizationHistory;

import java.util.List;
import java.util.Set;

/**
 * Created by sareh on 11/08/15.
 * Modified by Remo Andreoli (Feb 2024)
 */
public class HostSelectionPolicyLeastFull extends HostSelectionPolicy{

    @Override
    public HostEntity getHost(List<HostEntity> hostList, Object obj, Set<? extends HostEntity> excludedHostList) {
        double minUsage = Double.MAX_VALUE;
        HostEntity selectedHost = null;
        for (HostEntity host : hostList) {
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
