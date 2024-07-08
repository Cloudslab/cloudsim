package org.cloudbus.cloudsim.selectionPolicies;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.container.utils.Correlation;
import org.cloudbus.cloudsim.core.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Remo Andreoli (June 2024)
 * Loosely based on HostSelectionPolicyMinimumCorrelation from the previous version of CloudSim
 *
 * @since CloudSim toolkit 7.0
 */
public class PowerSelectionPolicyMinimumCorrelation implements SelectionPolicy<PowerHostEntity> {

    private SelectionPolicy<HostEntity> fallbackPolicy;

    public PowerSelectionPolicyMinimumCorrelation(final SelectionPolicy<HostEntity> fallbackPolicy) {
        super();
        setFallbackPolicy(fallbackPolicy);
    }

    @Override
    public PowerHostEntity select(List<PowerHostEntity> candidates, Object obj, Set<PowerHostEntity> excludedCandidates) {
        double[] utilizationHistory;

        if (obj instanceof PowerGuestEntity guest) {
            utilizationHistory = guest.getUtilizationHistoryList();
        } else {
            throw new IllegalArgumentException("PowerSelectionPolicyMinimumCorrelation.selectHost() requires a power-aware guest entity");
        }

        Correlation correlation = new Correlation();
        double minCor = Double.MAX_VALUE;
        PowerHostEntity selectedHost = null;
        for (PowerHostEntity hostCandidate : candidates) {
            if (excludedCandidates.contains(hostCandidate)) {
                continue;
            }

            double[] hostUtilization = hostCandidate.getUtilizationHistory();
            if (hostUtilization.length > 5) {

                double cor = correlation.getCor(hostUtilization, utilizationHistory);
                if (cor < minCor) {
                    minCor = cor;
                    selectedHost = hostCandidate;
                }
            }
        }

        if (selectedHost == null) {
            Log.printlnConcat(CloudSim.clock()+": "+getClass().getSimpleName()+": fallback activated (No host found)");
            List<HostEntity> baseCandidates = candidates.stream().map(c -> (HostEntity) c).toList();
            Set<HostEntity> baseExcludedCandidates = excludedCandidates.stream().map(xc -> (HostEntity) xc).collect(Collectors.toSet());

            selectedHost = (PowerHostEntity) fallbackPolicy.select(baseCandidates, obj, baseExcludedCandidates);
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
