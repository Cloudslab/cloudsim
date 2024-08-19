/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2024, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

/**
 * NewtorkPacket represents the packet which travel from one server to another. Each packet contains
 * IDs of the sender and receiver guest which are communicating, time at which it is sent and received,
 * type and virtual IDs of tasks.
 * 
 * <br/>Please refer to following publication for more details:<br/>
 * <ul>
 * <li><a href="http://dx.doi.org/10.1109/UCC.2011.24">Saurabh Kumar Garg and Rajkumar Buyya, NetworkCloudSim: Modelling Parallel Applications in Cloud
 * Simulations, Proceedings of the 4th IEEE/ACM International Conference on Utility and Cloud
 * Computing (UCC 2011, IEEE CS Press, USA), Melbourne, Australia, December 5-7, 2011.</a>
 * </ul>
 * 
 * @author Saurabh Kumar Garg
 * @since CloudSim Toolkit 1.0
 * //TODO Attributes should be private
 */
public class NetworkPacket {
    /**
     * Information about the virtual send and receiver entities of the packet.
     */
    HostPacket pkt;

    /**
     * Id of the sender host.
     */
    int senderHostId;

    /**
     * Id of the receiver host.
     */
    int receiverHostId;

    /**
     * Id of the sender guest.
     */
    int senderGuestId;

    /**
     * Id of the receiver guest.
     */
    int receiverGuestId;

    /** Time when the packet was sent. */
    double sendTime;

    /** Time when the packet was received. */
    double recvTime;

    public NetworkPacket(int hostId, HostPacket pkt) {
        this.pkt = pkt;

        senderHostId = hostId;
        receiverHostId = -1; // still unknown

        senderGuestId = pkt.senderGuestId;
        receiverGuestId = pkt.receiverGuestId;

        sendTime = pkt.sendTime;
        recvTime = -1;
    }
}
