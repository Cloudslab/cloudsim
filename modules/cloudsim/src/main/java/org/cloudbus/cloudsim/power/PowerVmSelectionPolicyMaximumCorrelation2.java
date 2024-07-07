package org.cloudbus.cloudsim.power;


import org.cloudbus.cloudsim.container.utils.Correlation;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.core.PowerGuestEntity;
import org.cloudbus.cloudsim.selectionPolicies.SelectionPolicy;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by sareh on 16/11/15.
 * Modified by Remo Andreoli (Feb 2024)
 */
public class PowerVmSelectionPolicyMaximumCorrelation2 implements SelectionPolicy<PowerGuestEntity> {


    /**
     * The fallback policy.
     */
    private SelectionPolicy<GuestEntity> fallbackPolicy;

    /**
     * Instantiates a new power vm selection policy maximum correlation.
     *
     * @param fallbackPolicy the fallback policy
     */
    public PowerVmSelectionPolicyMaximumCorrelation2(final SelectionPolicy<GuestEntity> fallbackPolicy) {
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
            List<GuestEntity> baseCandidates = candidates.stream().map(c -> (GuestEntity) c).toList();
            Set<GuestEntity> baseExcludedCandidates = excludedCandidates.stream().map(xc -> (GuestEntity) xc).collect(Collectors.toSet());

            return (PowerGuestEntity) getFallbackPolicy().select(baseCandidates, host, baseExcludedCandidates);
        }
    }

    /**
     * Gets the fallback policy.
     *
     * @return the fallback policy
     */
    public SelectionPolicy<GuestEntity> getFallbackPolicy() {
        return fallbackPolicy;
    }


    /**
     * Sets the fallback policy.
     *
     * @param fallbackPolicy the new fallback policy
     */
    public void setFallbackPolicy(final SelectionPolicy<GuestEntity> fallbackPolicy) {
        this.fallbackPolicy = fallbackPolicy;
    }

    public PowerGuestEntity getContainerVM(List<PowerGuestEntity> migrableContainerVMs, PowerHost host) {

        double[] corResult = new double[migrableContainerVMs.size()];
        Correlation correlation = new Correlation();
        int i = 0;
        double maxValue = -2;
        int id = -1;

        double[] hostUtilization = host.getUtilizationHistory();
        for (PowerGuestEntity vm : migrableContainerVMs) {
            double[] containerUtilization = vm.getUtilizationHistoryList();

            double cor = correlation.getCor(hostUtilization, containerUtilization);
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