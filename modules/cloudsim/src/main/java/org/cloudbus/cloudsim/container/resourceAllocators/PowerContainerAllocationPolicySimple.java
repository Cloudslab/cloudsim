package org.cloudbus.cloudsim.container.resourceAllocators;

import org.cloudbus.cloudsim.container.core.Container;

import java.util.List;
import java.util.Map;

/**
 * Created by sareh on 16/07/15.
 */
public class PowerContainerAllocationPolicySimple extends PowerContainerAllocationPolicy {


    public PowerContainerAllocationPolicySimple() {
        super();
    }

    @Override
    public List<Map<String, Object>> optimizeAllocation(List<? extends Container> containerList) {
        return null;
    }
}
