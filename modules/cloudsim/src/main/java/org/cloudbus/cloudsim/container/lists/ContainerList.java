package org.cloudbus.cloudsim.container.lists;

import org.cloudbus.cloudsim.container.core.Container;

import java.util.List;

/**
 * Created by sareh on 17/07/15.
 */
public class ContainerList {
    public static <T extends Container> T getById(List<T> containerList, int id) {
        for (T container : containerList) {
            if (container.getId() == id) {
                return container;
            }
        }
        return null;
    }

    /**
     * Return a reference to a Vm object from its ID and user ID.
     *
     * @param id            ID of required VM
     * @param userId        the user ID
     * @param containerList the vm list
     * @return Vm with the given ID, $null if not found
     * @pre $none
     * @post $none
     */
    public static <T extends Container> T getByIdAndUserId(List<T> containerList, int id, int userId) {
        for (T container : containerList) {
            if (container.getId() == id && container.getUserId() == userId) {
                return container;
            }
        }
        return null;
    }


}
