package org.cloudbus.cloudsim.network.datacenter;

import java.util.*;

/**
 * This class represents a communication interface between host entities and networked cloudlets.
 * @TODO: Remo Andreoli: this needs to include the guest entities too, eventually
 *
 * @author Remo Andreoli
 * @since CloudSim Toolkit 7.0
 */
public class NetworkInterfaceCard {
    private final List<HostPacket> pktsToSend;

    private final List<HostPacket> receivedPkts;

    public NetworkInterfaceCard() {
        pktsToSend = new LinkedList<>();
        receivedPkts = new LinkedList<>();
    }

    public List<HostPacket> getPktsToSend() { return pktsToSend; }
    public List<HostPacket> getReceivedPkts() { return receivedPkts; }
}
