package org.cloudbus.cloudsim.container.hostSelectionPolicies;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.HostEntity;
import org.cloudbus.cloudsim.power.PowerHostUtilizationHistory;

import java.util.List;
import java.util.Set;

/**
 * Created by sareh on 11/08/15.
 * Modified by Remo Andreoli (Feb 2024)
 */
public class HostSelectionPolicyMostFull extends HostSelectionPolicy {

    @Override
    public HostEntity getHost(List<HostEntity> hostList, Object obj, Set<? extends HostEntity> excludedHostList) {
        HostEntity selectedHost = null;
        if(CloudSim.clock() >1.0){
        double maxUsage = Double.MIN_VALUE;
        for (HostEntity host : hostList) {
            if (excludedHostList.contains(host)) {
                continue;
            }

            if (host instanceof PowerHostUtilizationHistory) {
                double hostUtilization= ((PowerHostUtilizationHistory) host).getUtilizationOfCpu();
                if (hostUtilization > maxUsage) {
                    maxUsage = hostUtilization;
                    selectedHost = host;

                }
            }
        }

        }else {

//            At the simulation start all the VMs by leastFull algorithms.

            selectedHost = new HostSelectionPolicyFirstFit().getHost(hostList,obj ,excludedHostList);

        }
        return selectedHost;


    }


}
