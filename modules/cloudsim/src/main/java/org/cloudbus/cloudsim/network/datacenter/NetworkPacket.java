/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2024, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

/**
 * NetworkPacket represents the packet which travel from one physical host to another.
 * 
 * <br/>Please refer to following publication for more details:<br/>
 * <ul>
 * <li><a href="http://dx.doi.org/10.1109/UCC.2011.24">Saurabh Kumar Garg and Rajkumar Buyya, NetworkCloudSim: Modelling Parallel Applications in Cloud
 * Simulations, Proceedings of the 4th IEEE/ACM International Conference on Utility and Cloud
 * Computing (UCC 2011, IEEE CS Press, USA), Melbourne, Australia, December 5-7, 2011.</a>
 * </ul>
 * 
 * @author Saurabh Kumar Garg
 * @author Remo Andreoli
 * @since CloudSim Toolkit 1.0
 */
public class NetworkPacket {
    /** Information about the ''virtual'' sender and receiver of the packet. */
    HostPacket pkt;

    /** Id of the sender host. */
    int senderHostId;

    /** Id of the receiver host. */
    int receiverHostId;

    public NetworkPacket(int hostId, HostPacket pkt) {
        this.pkt = pkt;

        senderHostId = hostId;
        receiverHostId = -1; // still unknown
    }

    public int getSenderGuestId() { return pkt.senderGuestId; }
    public int getReceiverGuestId() { return pkt.receiverGuestId; }
}
