/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

/**
 * HostPacket represents the packet that travels through the virtual network within a Host.
 * It contains information about cloudlets which are communicating.
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
 * @todo Attributes should be private
 */
public class HostPacket {
        /**
         * Id of the sender VM.
         */
	int sender;

        /**
         * Id of the receiver VM.
         */
	int reciever;
        
        /**
         * Id of the sender cloudlet.
         */
	int virtualsendid;

        /**
         * Id of the receiver cloudlet.
         */
        int virtualrecvid;

        /**
         * The length of the data being sent (in bytes).
        */        
	double data;

        /**
         * The time the packet was sent.
         */
	double sendtime;

        /**
         * The time the packet was received.
         */
	double recievetime;

	public HostPacket(
			int sender,
			int reciever,
			double data,
			double sendtime,
			double recievetime,
			int vsnd,
			int vrvd) {
		super();
		this.sender = sender;
		this.reciever = reciever;
		this.data = data;
		this.sendtime = sendtime;
		this.recievetime = recievetime;
		virtualrecvid = vrvd;
		virtualsendid = vsnd;
	}
}
