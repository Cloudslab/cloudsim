/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2024, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.selectionPolicies;


import org.cloudbus.cloudsim.container.utils.Correlation;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.core.PowerGuestEntity;
import org.cloudbus.cloudsim.power.PowerHost;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by sareh on 16/11/15.
 * Modified by Remo Andreoli (Feb 2024)
 */
public class PowerSelectionPolicyMaximumCorrelation2 implements SelectionPolicy<PowerGuestEntity> {


    /**
     * The fallback policy.
     */
    private SelectionPolicy<PowerGuestEntity> fallbackPolicy;

    /**
     * Instantiates a new power vm selection policy maximum correlation.
     *
     * @param fallbackPolicy the fallback policy
     */
    public PowerSelectionPolicyMaximumCorrelation2(final SelectionPolicy<PowerGuestEntity> fallbackPolicy) {
        super();
        setFallbackPolicy(fallbackPolicy);
    }

    @Override
    public PowerGuestEntity select(List<PowerGuestEntity> candidates, Object host, Set<PowerGuestEntity> excludedCandidates) {
        if (candidates.isEmpty()) {
            return null;
        }
        PowerGuestEntity selectedGuest = getContainerVM(candidates, (PowerHost) host);
        if (selectedGuest != null) {
//            Log.printConcatLine("We have to migrate the container with ID", container.getId());
            return selectedGuest;
        } else {
            return getFallbackPolicy().select(candidates, host, excludedCandidates);
        }
    }

    /**
     * Gets the fallback policy.
     *
     * @return the fallback policy
     */
    public SelectionPolicy<PowerGuestEntity> getFallbackPolicy() {
        return fallbackPolicy;
    }


    /**
     * Sets the fallback policy.
     *
     * @param fallbackPolicy the new fallback policy
     */
    public void setFallbackPolicy(final SelectionPolicy<PowerGuestEntity> fallbackPolicy) {
        this.fallbackPolicy = fallbackPolicy;
    }

    public PowerGuestEntity getContainerVM(List<PowerGuestEntity> migrableContainerVMs, PowerHost host) {

        double[] corResult = new double[migrableContainerVMs.size()];
        int i = 0;
        double maxValue = -2;
        int id = -1;

        double[] hostUtilization = host.getUtilizationHistory();
        for (PowerGuestEntity vm : migrableContainerVMs) {
            double[] containerUtilization = vm.getUtilizationHistoryList();

            double cor = Correlation.getCor(hostUtilization, containerUtilization);
            if (Double.isNaN(cor)) {
                cor = -3;
            }
            corResult[i] = cor;

            if(corResult[i] > maxValue) {
                maxValue = corResult[i];
                id = i;
            }

            i++;

        }

        if (id == -1) {
            Log.printlnConcat("Problem with correlation list.");
        }

        return migrableContainerVMs.get(id);
    }
}