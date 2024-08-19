/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2024, The University of Melbourne, Australia
 */

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
