/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2024, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.selectionPolicies;

import org.cloudbus.cloudsim.core.HostEntity;

import java.util.*;

/**
 * Created by Remo Andreoli (June 2024).
 * For Worst-Fit policy.
 *
 * @TODO: This allocation policy is currently not working properly, because the status of host Ps is not changed anywhere
 *        therefore every Pe is always free. SelectionPolicyMostFull is an alternative.
 * @since CloudSim toolkit 7.0
 */

public class SelectionPolicyWorstFit<T extends HostEntity> implements SelectionPolicy<T> {
    /** The map between each VM and the number of Pes used.
     * The map key is a VM UID and the value is the number of used Pes for that VM. */
    private Map<String, Integer> usedPes;

    /** The number of free Pes for each host from hostCandidates. */
    private List<Integer> freePes;

    @Override
    public T select(List<T> candidates, Object obj, Set<T> excludedCandidates) {
        int moreFree = Integer.MIN_VALUE;
        T selectedHost = null;

        for (T hostCandidate : candidates) {
            if (excludedCandidates.contains(hostCandidate)) {
                continue;
            }

            if (hostCandidate.getNumberOfFreePes() > moreFree) {
                selectedHost = hostCandidate;
                moreFree = hostCandidate.getNumberOfFreePes();
            }
        }
        return selectedHost;
    }

    protected List<Integer> getFreePes() { return freePes; }
    protected void setFreePes(List<Integer> freePes) { this.freePes = freePes; }

    protected Map<String, Integer> getUsedPes() { return usedPes; }
    protected void setUsedPes(Map<String, Integer> usedPes) { this.usedPes = usedPes; }
}
