package org.cloudbus.cloudsim;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.selectionPolicies.SelectionPolicy;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.core.HostEntity;

import java.util.*;

/**
 * Created by sareh on 16/12/15.
 * Modified by Remo Andreoli (March 2024)
 */
public class VmAllocationWithSelectionPolicy extends VmAllocationPolicy {
    /** The vm table. */

    @Getter @Setter
    private SelectionPolicy selectionPolicy;

    @Getter @Setter
    private Set<HostEntity> excludedHostCandidates;

    public VmAllocationWithSelectionPolicy(List<? extends HostEntity> list, SelectionPolicy selectionPolicy) {
        super(list);
        setExcludedHostCandidates(new HashSet<>());
        setSelectionPolicy(selectionPolicy);
    }

    @Override
    public HostEntity findHostForGuest(GuestEntity guest) {
        clearExcludedHostCandidates();
        int tries = 0;

        do{
            HostEntity selectedHost = getSelectionPolicy().selectHost(getHostList(), guest, excludedHostCandidates);
            if(selectedHost == null){
                return null;
            }

            if (selectedHost.isSuitableForGuest(guest)) {
                return selectedHost;
            } else {
                excludedHostCandidates.add(selectedHost);
                tries ++;
            }
            } while (tries < getHostList().size());
        return null;
    }

    public void clearExcludedHostCandidates() {
        excludedHostCandidates.clear();
    }
}
