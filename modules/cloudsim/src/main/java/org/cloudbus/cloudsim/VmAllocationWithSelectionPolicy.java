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
public class VmAllocationWithSelectionPolicy extends VmAllocationPolicySimple {
    /** The vm table. */

    @Getter @Setter
    private SelectionPolicy selectionPolicy;


    public VmAllocationWithSelectionPolicy(List<? extends HostEntity> list, SelectionPolicy selectionPolicy) {
        super(list);
        setSelectionPolicy(selectionPolicy);
    }

    @Override
    public boolean allocateHostForGuest(GuestEntity guest) {
        return allocateHostForGuest(guest, findHostForGuest(guest));
    }

    @Override
    public HostEntity findHostForGuest(GuestEntity guest) {
        Set<HostEntity> excludedHostList = new HashSet<>();
        int tries = 0;

        do{
            HostEntity selectedHost = getSelectionPolicy().selectHost(getHostList(), guest, excludedHostList);
            if(selectedHost == null){
                return null;
            }

            if (selectedHost.isSuitableForGuest(guest)) {
                return selectedHost;
            } else {
                excludedHostList.add(selectedHost);
                tries ++;
            }
            } while (tries < getHostList().size());
        return null;
    }
}
