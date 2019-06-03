package org.cloudbus.cloudsim.plus.vm;

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
public enum VMStatus {

    /** The VM is booting. */
    INITIALISING(),
    /** The VM is functional and is running. */
    RUNNING(INITIALISING),
    /** The VM has been terminated. */
    TERMINATED(RUNNING, INITIALISING);

    private final Set<VMStatus> validPrevStates = new HashSet<>();

    private VMStatus(final VMStatus... statuses) {
        validPrevStates.addAll(Arrays.asList(statuses));
    }

    /**
     * Returns if the param status can be a successor of this status.
     * 
     * @param stat
     *            - the new status to check. Must not be null.
     * @return if the param status can be a successor of this status.
     */
    public boolean isValidNextState(final VMStatus stat) {
        return stat.validPrevStates.contains(this);
    }

}
