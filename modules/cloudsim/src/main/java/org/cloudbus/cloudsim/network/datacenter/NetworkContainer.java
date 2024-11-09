/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2024, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.container.core.Container;
import org.cloudbus.cloudsim.core.NetworkedEntity;

import java.util.List;
import java.util.Map;

/**
 * A place-holder class to compute virtualization overhead when using network with containers.
 * Compared to NetworkVm, there is no nesting here, since we assume that containers are the top-most virtualization layer.
 *
 * Eventually this class will implement a proper virtual network layer.
 *
 * @author Remo Andreoli
 * @since CloudSim Toolkit 7.0
 */

public class NetworkContainer extends Container implements NetworkedEntity {

    /**
     * Creates a new NetworkContainer object.
     *
     * @param id
     * @param userId
     * @param mips
     * @param numberOfPes
     * @param ram
     * @param bw
     * @param size
     * @param containerManager
     * @param containerCloudletScheduler
     */
    public NetworkContainer(int id, int userId, double mips, int numberOfPes, int ram, long bw, long size, String containerManager, CloudletScheduler containerCloudletScheduler) {
        super(id, userId, mips, numberOfPes, ram, bw, size, containerManager, containerCloudletScheduler);
    }

    @Override
    public double updateCloudletsProcessing(double currentTime, List<Double> mipsShare) {
        double smallerTime = super.updateCloudletsProcessing(currentTime, mipsShare);

        // Accumulate virtualization overhead for your own packets
        for (NetworkInterfaceCard nic : getNics().values()) {
            for (HostPacket hpkt : nic.getPktsToSend()) {
                if (hpkt.senderGuestId == getId()) {
                    hpkt.accumulatedVirtualizationOverhead += getVirtualizationOverhead();
                }
            }
        }

        return smallerTime;
    }

    @Override
    public void sendPackets() {
        if (getHost() == null) {
            throw new RuntimeException(getClassName()+" has not been placed");
        }
    }

    @Override
    public Map<Integer, NetworkInterfaceCard> getNics() {
        if (getHost() == null) {
            throw new RuntimeException(getClassName()+" has not been placed");
        }

        return ((NetworkedEntity) getHost()).getNics();
    }

    @Override
    public Switch getSwitch() {
        if (getHost() == null) {
            throw new RuntimeException(getClassName()+" has not been placed");
        }
        return ((NetworkedEntity) getHost()).getSwitch();
    }

    @Override
    public void setSwitch(Switch sw) {
        if (getHost() == null) {
            throw new RuntimeException(getClassName()+"has not been placed");
        }
        ((NetworkedEntity) getHost()).setSwitch(sw);
    }
}
