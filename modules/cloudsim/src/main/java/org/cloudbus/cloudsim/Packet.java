/*
 * Gokul Poduval & Chen-Khong Tham
 * Computer Communication Networks (CCN) Lab
 * Dept of Electrical & Computer Engineering
 * National University of Singapore
 * August 2004
 *
 * Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2004, The University of Melbourne, Australia and National
 * University of Singapore
 * Packet.java - Interface of a Network Packet.
 *
 */

package org.cloudbus.cloudsim;

/**
 * Defines the structure for a network packet.
 * 
 * @author Gokul Poduval
 * @author Chen-Khong Tham, National University of Singapore
 * @since CloudSim Toolkit 1.0
 */
public interface Packet {

	/**
	 * Returns a string describing this packet in detail.
	 * 
	 * @return description of this packet
	 * @pre $none
	 * @post $none
	 */
	@Override
	String toString();

	/**
	 * Returns the size of this packet
	 * 
	 * @return size of the packet
	 * @pre $none
	 * @post $none
	 */
	long getSize();

	/**
	 * Sets the size of this packet
	 * 
	 * @param size size of the packet
	 * @return <tt>true</tt> if it is successful, <tt>false</tt> otherwise
	 * @pre size >= 0
	 * @post $none
	 */
	boolean setSize(long size);

	/**
	 * Returns the destination id of this packet.
	 * 
	 * @return destination id
	 * @pre $none
	 * @post $none
	 */
	int getDestId();

	/**
	 * Returns the ID of this packet
	 * 
	 * @return packet ID
	 * @pre $none
	 * @post $none
	 */
	int getId();

	/**
	 * Returns the ID of the source of this packet.
	 * 
	 * @return source id
	 * @pre $none
	 * @post $none
	 */
	int getSrcId();

	/**
	 * Gets the network service type of this packet
	 * 
	 * @return the network service type
	 * @pre $none
	 * @post $none
         * 
         * @todo Is it the Type of Service (ToS) of IPv4, like in
         * the {@link Cloudlet#netToS}? If yes, so the names would
         * be standardized.
	 */
	int getNetServiceType();

	/**
	 * Sets the network service type of this packet.
	 * <p>
	 * By default, the service type is 0 (zero). It is depends on the packet scheduler to determine
	 * the priority of this service level.
	 * 
	 * @param serviceType this packet's service type
	 * @pre serviceType >= 0
	 * @post $none
	 */
	void setNetServiceType(int serviceType);

	/**
	 * Gets an entity ID from the last hop that this packet has traversed.
	 * 
	 * @return an entity ID
	 * @pre $none
	 * @post $none
	 */
	int getLast();

	/**
	 * Sets an entity ID from the last hop that this packet has traversed.
	 * 
	 * @param last an entity ID from the last hop
	 * @pre last > 0
	 * @post $none
	 */
	void setLast(int last);

	/**
	 * Gets this packet tag
	 * 
	 * @return this packet tag
	 * @pre $none
	 * @post $none
	 */
	int getTag();

}
