/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2024, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.core.HostEntity;
import org.cloudbus.cloudsim.core.NetworkedEntity;
import org.cloudbus.cloudsim.core.VirtualEntity;
import org.cloudbus.cloudsim.lists.CloudletList;
import org.cloudbus.cloudsim.lists.VmList;
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
    public NetworkVm(int id, int userId, double mips, int numberOfPes, int ram, long bw, long size, String vmm, CloudletScheduler cloudletScheduler, VmScheduler guestScheduler, RamProvisioner containerRamProvisioner, BwProvisioner containerBwProvisioner, List<? extends Pe> peList) {
        super(id, userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler, guestScheduler, containerRamProvisioner, containerBwProvisioner, peList);
    }

    public NetworkVm(int id, int userId, double mips, int numberOfPes, int ram, long bw, long size, String vmm, CloudletScheduler cloudletScheduler) {
        super(id, userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler);
    }

    @Override
    public double updateCloudletsProcessing(double currentTime, List<Double> mipsShare) {
        double smallerTime = super.updateCloudletsProcessing(currentTime, mipsShare);

        // send the packets to physical host
        sendPackets();

        return smallerTime;
    }

    @Override
    public void sendPackets() {
        // Sort of NATing: Overwrite the sender id, which may be that of a nested guest, for correct packet routing
        // and introduce an (optional) virtualization overhead to simulate the pass-through the virtual network
        // (nested) guest -> host
        for (NetworkInterfaceCard nic : getNics().values()) {
            for (HostPacket hpkt : nic.getPktsToSend()) {
                GuestEntity sender = VmList.getById(getGuestList(), hpkt.senderGuestId);
                if (sender != null && hpkt.senderGuestId != getId()) {
                    hpkt.senderGuestId = getId();
                }

                // Nested virtualization edge-case, but locally routed packet
                if (VmList.getById(this.getGuestList(), hpkt.receiverGuestId) != null) {
                    getNics().get(hpkt.receiverCloudletId).getReceivedPkts().add(hpkt);
                    nic.getPktsToSend().remove(hpkt);
                }

                hpkt.accumulatedVirtualizationOverhead += getVirtualizationOverhead();

                // Nested virtualization edge-case, but locally routed packet
                if (VmList.getById(this.getGuestList(), hpkt.receiverGuestId) != null) {
                    getNics().get(hpkt.receiverCloudletId).getReceivedPkts().add(hpkt);
                    nic.getPktsToSend().remove(hpkt);
                }
            }
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
