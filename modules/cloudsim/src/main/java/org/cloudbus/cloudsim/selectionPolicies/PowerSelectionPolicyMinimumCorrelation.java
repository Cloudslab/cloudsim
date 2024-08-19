/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2024, The University of Melbourne, Australia
 */

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

    private SelectionPolicy<PowerHostEntity> fallbackPolicy;

    public PowerSelectionPolicyMinimumCorrelation(final SelectionPolicy<PowerHostEntity> fallbackPolicy) {
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

        double minCor = Double.MAX_VALUE;
        PowerHostEntity selectedHost = null;
        for (PowerHostEntity hostCandidate : candidates) {
            if (excludedCandidates.contains(hostCandidate)) {
                continue;
            }

            double[] hostUtilization = hostCandidate.getUtilizationHistory();
            if (hostUtilization.length > 5) {

                double cor = Correlation.getCor(hostUtilization, utilizationHistory);
                if (cor < minCor) {
                    minCor = cor;
                    selectedHost = hostCandidate;
                }
            }
        }

        if (selectedHost == null) {
            Log.printlnConcat(CloudSim.clock(), ": ", getClass().getSimpleName(), ": fallback activated (No host found)");
            selectedHost = fallbackPolicy.select(candidates, obj, excludedCandidates);
        }
        return selectedHost;
    }


    public SelectionPolicy<PowerHostEntity> getFallbackPolicy() {
        return fallbackPolicy;
    }

    public void setFallbackPolicy(SelectionPolicy<PowerHostEntity> fallbackPolicy) { this.fallbackPolicy = fallbackPolicy; }


}
