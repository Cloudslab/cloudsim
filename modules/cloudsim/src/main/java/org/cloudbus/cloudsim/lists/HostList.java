/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2010, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.lists;

import java.util.List;

import org.cloudbus.cloudsim.Host;

// TODO: Auto-generated Javadoc
/**
 * HostList is a collection of operations on lists of hosts.
 *
 * @author		Anton Beloglazov
 * @since		CloudSim Toolkit 2.0
 */
public class HostList {

	/**
	 * Gets the Machine object for a particular ID.
	 *
	 * @param <T> the generic type
	 * @param hostList the host list
	 * @param id    the host ID
	 * @return the Machine object or <tt>null</tt> if no machine exists
	 * @see gridsim.Machine
	 * @pre id >= 0
	 * @post $none
	 */
    public static <T extends Host> T getById(List<T> hostList, int id) {
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
     * @param <T> the generic type
     * @param hostList the host list
     * @return number of PEs
     * @pre $none
     * @post $result >= 0
     */
    public static <T extends Host> int getPesNumber(List<T> hostList) {
        int pesNumber = 0;
        for (T host : hostList) {
    		pesNumber += host.getPeList().size();
		}
        return pesNumber;
    }

    /**
     * Gets the total number of <tt>FREE</tt> or non-busy PEs for all Machines.
     *
     * @param <T> the generic type
     * @param hostList the host list
     * @return number of PEs
     * @pre $none
     * @post $result >= 0
     */
    public static <T extends Host> int getFreePesNumber(List<T> hostList) {
        int freePesNumber = 0;
        for (T host : hostList) {
        	freePesNumber += PeList.getFreePesNumber(host.getPeList());
		}
        return freePesNumber;
    }

    /**
     * Gets the total number of <tt>BUSY</tt> PEs for all Machines.
     *
     * @param <T> the generic type
     * @param hostList the host list
     * @return number of PEs
     * @pre $none
     * @post $result >= 0
     */
    public static <T extends Host> int getBusyPesNumber(List<T> hostList) {
        int busyPesNumber = 0;
        for (T host : hostList) {
        	busyPesNumber += PeList.getBusyPesNumber(host.getPeList());
		}
        return busyPesNumber;
    }

    /**
     * Gets a Machine with free Pe.
     *
     * @param <T> the generic type
     * @param hostList the host list
     * @return a machine object or <tt>null</tt> if not found
     * @pre $none
     * @post $none
     */
    public static <T extends Host> T getHostWithFreePe(List<T> hostList) {
        return getHostWithFreePe(hostList, 1);
    }

    /**
     * Gets a Machine with a specified number of free Pe.
     *
     * @param <T> the generic type
     * @param hostList the host list
     * @param pesNumber the pes number
     * @return a machine object or <tt>null</tt> if not found
     * @pre $none
     * @post $none
     */
    public static <T extends Host> T getHostWithFreePe(List<T> hostList, int pesNumber) {
        for (T host : hostList) {
        	if (PeList.getFreePesNumber(host.getPeList()) >= pesNumber) {
        		return host;
        	}
		}
        return null;
    }

    /**
     * Sets the particular Pe status on a Machine.
     *
     * @param <T> the generic type
     * @param hostList the host list
     * @param status   Pe status, either <tt>Pe.FREE</tt> or <tt>Pe.BUSY</tt>
     * @param hostId the host id
     * @param peId the pe id
     * @return <tt>true</tt> if the Pe status has changed, <tt>false</tt>
     * otherwise (Machine id or Pe id might not be exist)
     * @pre machineID >= 0
     * @pre peID >= 0
     * @post $none
     */
    public static <T extends Host> boolean setPeStatus(List<T> hostList, int status, int hostId, int peId) {
        T host = getById(hostList, hostId);
        if (host == null) {
            return false;
        }
        return host.setPeStatus(peId, status);
    }

}

