package org.cloudbus.cloudsim.container.hostSelectionPolicies;

import org.cloudbus.cloudsim.container.core.*;
import org.cloudbus.cloudsim.container.utils.Correlation;

import java.util.List;
import java.util.Set;

/**
 * Created by sareh on 11/08/15.
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
    public ContainerHost getHost(List<ContainerHost> hostList, Object obj, Set<? extends ContainerHost> excludedHostList) {

        double[] utilizationHistory;
        if (obj instanceof Container) {

            utilizationHistory = ((PowerContainer) obj).getUtilizationHistoryList();
        } else {

            utilizationHistory = ((PowerContainerVm) obj).getUtilizationHistoryList();
        }
        Correlation correlation = new Correlation();
        double minCor = Double.MAX_VALUE;
        ContainerHost selectedHost = null;
        for (ContainerHost host : hostList) {
            if (excludedHostList.contains(host)) {
                continue;
            }
            if (host instanceof PowerContainerHostUtilizationHistory) {
                double[] hostUtilization = ((PowerContainerHostUtilizationHistory) host).getUtilizationHistory();
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
