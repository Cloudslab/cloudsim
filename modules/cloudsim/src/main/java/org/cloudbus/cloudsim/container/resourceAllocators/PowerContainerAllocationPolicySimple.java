package org.cloudbus.cloudsim.container.resourceAllocators;

import org.cloudbus.cloudsim.container.core.Container;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.core.HostEntity;

import java.util.List;
import java.util.Map;

/**
 * Created by sareh on 16/07/15.
 * Modified by Remo Andreoli (March 2024)
 */
public class PowerContainerAllocationPolicySimple extends PowerContainerAllocationPolicy {


    public PowerContainerAllocationPolicySimple(List<? extends HostEntity> list) {
        super(list);
    }

    @Override
    public List<Map<String, Object>> optimizeAllocation(List<? extends GuestEntity> containerList) {
        return null;
    }
}
