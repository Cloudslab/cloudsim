package org.cloudbus.cloudsim.EX;

import org.cloudbus.cloudsim.core.CloudSimTags;

public enum CloudSimEXTags implements CloudSimTags {
    BROKER_DESTROY_ITSELF_NOW,
    BROKER_DESTROY_VMS_NOW,
    BROKER_SUBMIT_VMS_NOW,
    BROKER_CLOUDLETS_NOW,

    BROKER_MEASURE_UTIL_NOW,
    BROKER_RECORD_UTIL_NOW,
    BROKER_AUTOSCALE_NOW,

    DATACENTER_BOOT_VM_TAG
}
