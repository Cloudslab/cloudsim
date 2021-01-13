package org.cloudbus.cloudsim.auction.bidder;

import org.cloudbus.cloudsim.core.SimEvent;

/**
 * VM Allocation Based on Combinatorial Double Auction
 * Represents an entity which wants to bid to an auction.
 * The entity must have an AuctonAgent by which the following methods are called.
 * 
 * @author Youness Teimoury
 * Blog: http://youness-teimoury.blogspot.com/
 * Email: youness126@gmail.com
 * Created on 2011/8/20
 */
public interface Bidder {
	
	public void bidAuction();
	
	public void processAllocations(SimEvent ev);
}