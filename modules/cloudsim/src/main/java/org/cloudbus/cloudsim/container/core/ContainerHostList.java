package org.cloudbus.cloudsim.container.core;


import org.cloudbus.cloudsim.container.lists.ContainerVmPeList;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by sareh on 10/07/15.
 */
public class ContainerHostList {

    /**
     * Gets the Machine object for a particular ID.
     *
     * @param <T>      the generic type
     * @param hostList the host list
     * @param id       the host ID
     * @return the Machine object or <tt>null</tt> if no machine exists
     * @pre id >= 0
     * @post $none
     * @see
     */
    public static <T extends ContainerHost> T getById(List<T> hostList, int id) {
        for (T host : hostList) {
            if (host.getId() == id) {
                return host;
            }
        }
        return null;
    }

    /**
     * Gets the total number of PEs for all Machines.
     *
     * @param <T>      the generic type
     * @param hostList the host list
     * @return number of PEs
     * @pre $none
     * @post $result >= 0
     */
    public static <T extends ContainerHost> int getNumberOfPes(List<T> hostList) {
        int numberOfPes = 0;
        for (T host : hostList) {
            numberOfPes += host.getPeList().size();
        }
        return numberOfPes;
    }

    /**
     * Gets the total number of <tt>FREE</tt> or non-busy PEs for all Machines.
     *
     * @param <T>      the generic type
     * @param hostList the host list
     * @return number of PEs
     * @pre $none
     * @post $result >= 0
     */
    public static <T extends ContainerHost> int getNumberOfFreePes(List<T> hostList) {
        int numberOfFreePes = 0;
        for (T host : hostList) {
            numberOfFreePes += ContainerVmPeList.getNumberOfFreePes(host.getPeList());
        }
        return numberOfFreePes;
    }

    /**
     * Gets the total number of <tt>BUSY</tt> PEs for all Machines.
     *
     * @param <T>      the generic type
     * @param hostList the host list
     * @return number of PEs
     * @pre $none
     * @post $result >= 0
     */
    public static <T extends ContainerHost> int getNumberOfBusyPes(List<T> hostList) {
        int numberOfBusyPes = 0;
        for (T host : hostList) {
            numberOfBusyPes += ContainerVmPeList.getNumberOfBusyPes(host.getPeList());
        }
        return numberOfBusyPes;
    }

    /**
     * Gets a Machine with free Pe.
     *
     * @param <T>      the generic type
     * @param hostList the host list
     * @return a machine object or <tt>null</tt> if not found
     * @pre $none
     * @post $none
     */
    public static <T extends ContainerHost> T getHostWithFreePe(List<T> hostList) {
        return getHostWithFreePe(hostList, 1);
    }

    /**
     * Gets a Machine with a specified number of free Pe.
     *
     * @param <T>       the generic type
     * @param hostList  the host list
     * @param pesNumber the pes number
     * @return a machine object or <tt>null</tt> if not found
     * @pre $none
     * @post $none
     */
    public static <T extends ContainerHost> T getHostWithFreePe(List<T> hostList, int pesNumber) {
        for (T host : hostList) {
            if (ContainerVmPeList.getNumberOfFreePes(host.getPeList()) >= pesNumber) {
                return host;
            }
        }
        return null;
    }

    /**
     * Sets the particular Pe status on a Machine.
     *
     * @param <T>      the generic type
     * @param hostList the host list
     * @param status   Pe status, either <tt>Pe.FREE</tt> or <tt>Pe.BUSY</tt>
     * @param hostId   the host id
     * @param peId     the pe id
     * @return <tt>true</tt> if the Pe status has changed, <tt>false</tt> otherwise (Machine id or
     * Pe id might not be exist)
     * @pre machineID >= 0
     * @pre peID >= 0
     * @post $none
     */
    public static <T extends ContainerHost> boolean setPeStatus(List<T> hostList, int status, int hostId, int peId) {
        T host = getById(hostList, hostId);
        if (host == null) {
            return false;
        }
        return host.setPeStatus(peId, status);
    }

    /**
     * Sort by cpu utilization.
     *
     * @param hostList the vm list
     */
    public static <T extends ContainerHost> void sortByCpuUtilization(List<T> hostList) {
        Collections.sort(hostList, new Comparator<T>() {

            @Override
            public int compare(T a, T b) throws ClassCastException {
                Double aUtilization = ((PowerContainerHost) a).getUtilizationOfCpu();
                Double bUtilization = ((PowerContainerHost) b).getUtilizationOfCpu();
                return bUtilization.compareTo(aUtilization);
            }
        });
    }

    public static <T extends ContainerHost> void sortByCpuUtilizationDescending(List<T> hostList) {

        Collections.sort(hostList, Collections.reverseOrder(new Comparator<T>() {

            @Override
            public int compare(T a, T b) throws ClassCastException {
                Double aUtilization = ((PowerContainerHost) a).getUtilizationOfCpu();
                Double bUtilization = ((PowerContainerHost) b).getUtilizationOfCpu();
                return bUtilization.compareTo(aUtilization);
            }
        }));


    }

}
