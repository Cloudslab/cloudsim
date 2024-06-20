package org.cloudbus.cloudsim.container.vmSelectionPolicies;


import org.cloudbus.cloudsim.container.core.PowerContainerVm;
import org.cloudbus.cloudsim.container.utils.Correlation;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.selectionPolicies.SelectionPolicy;

import java.util.List;
import java.util.Set;

/**
 * Created by sareh on 16/11/15.
 * Modified by Remo Andreoli (Feb 2024)
 */
public class PowerContainerVmSelectionPolicyCor implements SelectionPolicy<GuestEntity> {


    /**
     * The fallback policy.
     */
    private SelectionPolicy<GuestEntity> fallbackPolicy;

    /**
     * Instantiates a new power vm selection policy maximum correlation.
     *
     * @param fallbackPolicy the fallback policy
     */
    public PowerContainerVmSelectionPolicyCor(final SelectionPolicy<GuestEntity> fallbackPolicy) {
        super();
        setFallbackPolicy(fallbackPolicy);
    }

    @Override
    public GuestEntity select(List<GuestEntity> candidates, Object host, Set<GuestEntity> excludedCandidates) {
        if (candidates.isEmpty()) {
            return null;
        }
        GuestEntity selectedGuest = getContainerVM(candidates, (PowerHost) host);
        candidates.clear();
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

    public GuestEntity getContainerVM(List<GuestEntity> migratableContainerVMs, PowerHost host) {

        double[] corResult = new double[migratableContainerVMs.size()];
        Correlation correlation = new Correlation();
        int i = 0;
        double maxValue = -2;
        int id = -1;

        double[] hostUtilization = host.getUtilizationHistory();
        for (GuestEntity vm : migratableContainerVMs) {
            double[] containerUtilization = ((PowerContainerVm) vm).getUtilizationHistoryList();

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

        return migratableContainerVMs.get(id);

    }


}













