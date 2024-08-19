/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2024, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.EX.vm;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * Represents the status of VM.
 * 
 * @author nikolay.grozev
 * 
 */
public enum VmStatus {

    /** The VM is booting. */
    INITIALISING(),
    /** The VM is functional and is running. */
    RUNNING(INITIALISING),
    /** The VM has been terminated. */
    TERMINATED(RUNNING, INITIALISING);

    private final Set<VmStatus> validPrevStates = new HashSet<>();

    VmStatus(final VmStatus... statuses) {
        validPrevStates.addAll(Arrays.asList(statuses));
    }

    /**
     * Returns if the param status can be a successor of this status.
     * 
     * @param stat
     *            - the new status to check. Must not be null.
     * @return if the param status can be a successor of this status.
     */
    public boolean isValidNextState(final VmStatus stat) {
        return stat.validPrevStates.contains(this);
    }

}
