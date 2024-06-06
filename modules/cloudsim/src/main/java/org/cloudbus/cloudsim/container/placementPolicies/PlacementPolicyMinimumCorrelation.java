package org.cloudbus.cloudsim.container.placementPolicies;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.container.core.Container;
import org.cloudbus.cloudsim.container.core.ContainerVm;
import org.cloudbus.cloudsim.container.core.PowerContainer;
import org.cloudbus.cloudsim.container.core.PowerContainerVm;
import org.cloudbus.cloudsim.container.utils.Correlation;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.HostEntity;
import org.cloudbus.cloudsim.power.PowerHost;

import java.util.List;
import java.util.Set;

/**
 * Created by Remo Andreoli (June 2024)
 * Loosely based on HostSelectionPolicyMinimumCorrelation from the previous version of CloudSim
 *
 * @since CloudSim toolkit 7.0
 */
public class PlacementPolicyMinimumCorrelation extends PlacementPolicy {

    private PlacementPolicy fallbackPolicy;

    public PlacementPolicyMinimumCorrelation(final PlacementPolicy fallbackPolicy) {
        super();

        if (fallbackPolicy instanceof PlacementPolicyMinimumCorrelation) {
            throw new IllegalArgumentException("Cannot use same policy as the fallback policy");
        }
        setFallbackPolicy(fallbackPolicy);
    }

    @Override
    public HostEntity selectHost(List<HostEntity> hostCandidates, Object obj, Set<HostEntity> excludedHostCandidates) {
        double[] utilizationHistory;

        // @TODO: Remo Andreoli: ugly, further generalisation of the concept of power is needed
        if (obj instanceof Container) {
            utilizationHistory = ((PowerContainer) obj).getUtilizationHistoryList();
        } else if(obj instanceof ContainerVm) {
            utilizationHistory = ((PowerContainerVm) obj).getUtilizationHistoryList();
        } else {
            throw new IllegalArgumentException("PlacementPolicyMinimumCorrelation.selectHost() requires a power-aware guest entity");
        }

        Correlation correlation = new Correlation();
        double minCor = Double.MAX_VALUE;
        HostEntity selectedHost = null;
        for (HostEntity hostCandidate : hostCandidates) {
            if (excludedHostCandidates.contains(hostCandidate)) {
                continue;
            }

            if (hostCandidate instanceof PowerHost powerHost) {
                double[] hostUtilization = powerHost.getUtilizationHistory();
                if (hostUtilization.length > 5) {

                    double cor = correlation.getCor(hostUtilization, utilizationHistory);
                    if (cor < minCor) {
                        minCor = cor;
                        selectedHost = hostCandidate;
                    }
                }
            } else {
                Log.printlnConcat(CloudSim.clock()+": "+getClass().getSimpleName()+": fallback activated ("+hostCandidate.getClassName()+" # "+hostCandidate.getId()+" is not power-aware)");
                selectedHost = fallbackPolicy.selectHost(hostCandidates, obj, excludedHostCandidates);
            }
        }

        if (selectedHost == null) {
            Log.printlnConcat(CloudSim.clock()+": "+getClass().getSimpleName()+": fallback activated (No host found)");
            selectedHost = fallbackPolicy.selectHost(hostCandidates, obj, excludedHostCandidates);
        }
        return selectedHost;
    }


    public PlacementPolicy getFallbackPolicy() {
        return fallbackPolicy;
    }

    public void setFallbackPolicy(PlacementPolicy fallbackPolicy) {
        this.fallbackPolicy = fallbackPolicy;
    }


}
