/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2024, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

import java.util.*;

/**
 * This class represents a communication interface between host entities and networked cloudlets.
 * @TODO: this needs to include the guest entities too, eventually
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
