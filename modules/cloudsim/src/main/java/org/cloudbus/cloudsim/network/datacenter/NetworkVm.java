/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2024, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.NetworkedEntity;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

import java.util.List;
import java.util.Map;

/**
 * A place-holder class to enable networking in a nested virtualization scenario. NetworkVm can be placed as a host entity
 * in a NetworkDatacenter, and its sole job is to relay function calls to the underlying NetworkHost.
 *
 * Eventually this class will implement a proper virtual network layer.
 *
 * @author Remo Andreoli
 * @since CloudSim Toolkit 7.0
 */
public class NetworkVm extends Vm implements NetworkedEntity {
    public NetworkVm(int id, int userId, double mips, int numberOfPes, int ram, long bw, long size, String vmm, CloudletScheduler cloudletScheduler) {
        super(id, userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler);
    }

    public NetworkVm(int id, int userId, double mips, int numberOfPes, int ram, long bw, long size, String vmm, CloudletScheduler cloudletScheduler, VmScheduler guestScheduler, RamProvisioner containerRamProvisioner, BwProvisioner containerBwProvisioner, List<? extends Pe> peList) {
        super(id, userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler, guestScheduler, containerRamProvisioner, containerBwProvisioner, peList);
    }

    public NetworkVm(int id, int userId, double mips, int numberOfPes, int ram, long bw, long size, String vmm, CloudletScheduler cloudletScheduler) {
        super(id, userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler);
    }

    @Override
    public double updateCloudletsProcessing(double currentTime, List<Double> mipsShare) {
        double smallerTime = super.updateCloudletsProcessing(currentTime, mipsShare);

        // Sort of bridging: Overwrite the sender id, which may be that of a nested guest, for correct packet routing
        for (NetworkInterfaceCard nic : getNics().values()) {
            for (HostPacket hpkt : nic.getPktsToSend()) {
                hpkt.senderGuestId = getId();
            }
        }

        return smallerTime;
    }

    @Override
    public void sendPackets() {
        if (getHost() == null) {
            throw new RuntimeException("Vm has not been placed");
        }
    }

    @Override
    public Map<Integer, NetworkInterfaceCard> getNics() {
        if (getHost() == null) {
            throw new RuntimeException("Vm has not been placed");
        }

        return ((NetworkedEntity) getHost()).getNics();
    }

    @Override
    public Switch getSwitch() {
        if (getHost() == null) {
            throw new RuntimeException("Vm has not been placed");
        }
        return ((NetworkedEntity) getHost()).getSwitch();
    }

    @Override
    public void setSwitch(Switch sw) {
        if (getHost() == null) {
            throw new RuntimeException("Vm has not been placed");
        }
        ((NetworkedEntity) getHost()).setSwitch(sw);
    }
}
