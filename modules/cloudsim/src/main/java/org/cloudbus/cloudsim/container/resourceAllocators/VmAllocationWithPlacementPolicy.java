package org.cloudbus.cloudsim.container.resourceAllocators;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.container.placementPolicies.PlacementPolicy;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.core.HostEntity;

import java.util.*;

/**
 * Created by sareh on 16/12/15.
 * Modified by Remo Andreoli (March 2024)
 */
public class VmAllocationWithPlacementPolicy extends VmAllocationPolicySimple {
    /** The vm table. */

    @Getter @Setter
    private PlacementPolicy placementPolicy;


    public VmAllocationWithPlacementPolicy(List<? extends HostEntity> list, PlacementPolicy placementPolicy) {
        super(list);
        setPlacementPolicy(placementPolicy);
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
            HostEntity selectedHost = getPlacementPolicy().selectHost(getHostList(), guest, excludedHostList);
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
