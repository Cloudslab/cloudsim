package org.cloudbus.cloudsim.selectionPolicies;

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
public class SelectionPolicyMinimumCorrelation implements SelectionPolicy<HostEntity> {

    private SelectionPolicy<HostEntity> fallbackPolicy;

    public SelectionPolicyMinimumCorrelation(final SelectionPolicy<HostEntity> fallbackPolicy) {
        super();

        if (fallbackPolicy instanceof SelectionPolicyMinimumCorrelation) {
            throw new IllegalArgumentException("Cannot use same policy as the fallback policy");
        }
        setFallbackPolicy(fallbackPolicy);
    }

    @Override
    public HostEntity select(List<HostEntity> candidates, Object obj, Set<HostEntity> excludedCandidates) {
        double[] utilizationHistory;

        // @TODO: Remo Andreoli: ugly, further generalisation of the concept of power is needed
        if (obj instanceof Container) {
            utilizationHistory = ((PowerContainer) obj).getUtilizationHistoryList();
        } else if(obj instanceof ContainerVm) {
            utilizationHistory = ((PowerContainerVm) obj).getUtilizationHistoryList();
        } else {
            throw new IllegalArgumentException("SelectionPolicyMinimumCorrelation.selectHost() requires a power-aware guest entity");
        }

        Correlation correlation = new Correlation();
        double minCor = Double.MAX_VALUE;
        HostEntity selectedHost = null;
        for (HostEntity hostCandidate : candidates) {
            if (excludedCandidates.contains(hostCandidate)) {
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
                selectedHost = fallbackPolicy.select(candidates, obj, excludedCandidates);
            }
        }

        if (selectedHost == null) {
            Log.printlnConcat(CloudSim.clock()+": "+getClass().getSimpleName()+": fallback activated (No host found)");
            selectedHost = fallbackPolicy.select(candidates, obj, excludedCandidates);
        }
        return selectedHost;
    }


    public SelectionPolicy getFallbackPolicy() {
        return fallbackPolicy;
    }

    public void setFallbackPolicy(SelectionPolicy fallbackPolicy) {
        this.fallbackPolicy = fallbackPolicy;
    }


}
