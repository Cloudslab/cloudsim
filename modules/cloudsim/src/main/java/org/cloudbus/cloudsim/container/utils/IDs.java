package org.cloudbus.cloudsim.container.utils;

/**
 * Created by sareh on 13/08/15.
 * Modified by Remo Andreoli (Feb 2024)
 */

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.container.core.*;

import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

/**
 * A factory for CloudSim entities' ids. CloudSim requires a lot of ids, that
 * are provided by the end user. This class is a utility for automatically
 * generating valid ids.
 * Modifies for containers
 *
 * @author nikolay.grozev
 * @author Remo Andreoli
 */

public final class IDs {


    private static final Map<Class<?>, Integer> COUNTERS = new LinkedHashMap<>();
    private static final Set<Class<?>> NO_COUNTERS = new HashSet<>();
    private static int globalCounter = 1;

    static {
        COUNTERS.put(Cloudlet.class, 1);
        COUNTERS.put(ContainerVm.class, 1);
        COUNTERS.put(Container.class, 1);
        COUNTERS.put(Host.class, 1);
        COUNTERS.put(ContainerDatacenterBroker.class, 1);
        COUNTERS.put(Pe.class, 1);
    }

    private IDs() {
    }

    /**
     * Returns a valid id for the specified class.
     *
     * @param clazz - the class of the object to get an id for. Must not be null.
     * @return a valid id for the specified class.
     */
    public static synchronized int pollId(final Class<?> clazz) {
        Class<?> matchClass = null;
        if (COUNTERS.containsKey(clazz)) {
            matchClass = clazz;
        } else if (!NO_COUNTERS.contains(clazz)) {
            for (Class<?> key : COUNTERS.keySet()) {
                if (key.isAssignableFrom(clazz)) {
                    matchClass = key;
                    break;
                }
            }
        }

        int result = -1;
        if (matchClass == null) {
            NO_COUNTERS.add(clazz);
            result = pollGlobalId();
        } else {
            result = COUNTERS.get(matchClass);
            COUNTERS.put(matchClass, result + 1);
        }

        if (result < 0) {
            throw new IllegalStateException("The generated id for class:" + clazz.getName()
                    + " is negative. Possible integer overflow.");
        }

        return result;
    }

    private static synchronized int pollGlobalId() {
        return globalCounter++;
    }

}


