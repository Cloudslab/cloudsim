package org.cloudbus.cloudsim.container.hostSelectionPolicies;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.container.core.*;
import org.cloudbus.cloudsim.container.utils.Correlation;
import org.cloudbus.cloudsim.power.PowerHostUtilizationHistory;

import java.util.List;
import java.util.Set;

/**
 * Created by sareh on 11/08/15.
 * Modified by Remo Andreoli (Feb 2024)
 */
public class HostSelectionPolicyMinimumCorrelation extends HostSelectionPolicy {

    private HostSelectionPolicy fallbackPolicy;

    /**
     * Instantiates a new power vm selection policy maximum correlation.
     *
     * @param fallbackPolicy the fallback policy
     */
    public HostSelectionPolicyMinimumCorrelation(final HostSelectionPolicy fallbackPolicy) {
        super();
        setFallbackPolicy(fallbackPolicy);
    }

    @Override
    public Host getHost(List<Host> hostList, Object obj, Set<? extends Host> excludedHostList) {

        double[] utilizationHistory;
        if (obj instanceof Container) {

            utilizationHistory = ((PowerContainer) obj).getUtilizationHistoryList();
        } else {

            utilizationHistory = ((PowerContainerVm) obj).getUtilizationHistoryList();
        }
        Correlation correlation = new Correlation();
        double minCor = Double.MAX_VALUE;
        Host selectedHost = null;
        for (Host host : hostList) {
            if (excludedHostList.contains(host)) {
                continue;
            }
            if (host instanceof PowerHostUtilizationHistory) {
                double[] hostUtilization = ((PowerHostUtilizationHistory) host).getUtilizationHistory();
                if (hostUtilization.length > 5) {

                    double cor = correlation.getCor(hostUtilization, utilizationHistory);
                    if (cor < minCor) {
                        minCor = cor;
                        selectedHost = host;

                    }
                }

            }
        }
        if (selectedHost == null) {

        }
        return selectedHost;
    }


    public HostSelectionPolicy getFallbackPolicy() {
        return fallbackPolicy;
    }

    public void setFallbackPolicy(HostSelectionPolicy fallbackPolicy) {
        this.fallbackPolicy = fallbackPolicy;
    }


}
