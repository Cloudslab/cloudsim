package org.cloudbus.cloudsim.auction.bidder;

import org.cloudbus.cloudsim.auction.auctioneer.AuctionTags;
import org.cloudbus.cloudsim.auction.bid.Bid;
import org.cloudbus.cloudsim.core.SimEvent;

/**
 * VM Allocation Based on Combinatorial Double Auction
 * Each bidder has an AuctionAgent to defer the responsibilities of handling 
 * auction events to it.
 * 
 * @author Youness Teimoury
 * Blog: http://youness-teimoury.blogspot.com/
 * Email: youness126@gmail.com
 * Created on 2011/8/20
 */
public class AuctionAgent {
	/**
	 * The owner of this auction-agent
	 */
	private Bidder bidder;

	/**
	 * The auction bidder's bid
	 */
	private Bid bid;

	/**
	 * The activity (open/close) of auctioneer entity
	 */
	public boolean auctionOpen;

	/**
	 * Denotes if the bid is accepted by Auctioneer or not
	 */
	public boolean bidAccepted;

	
	/**
	 * Getters & Setters  method
	 */
	public Bid getBid() {
		return this.bid;
	}
	
	public void setBid(Bid bid) {
		this.bid = bid;
	}

	private boolean isAuctionOpen() {
		return auctionOpen;
	}

	/**
	 * Constructor
	 * @param bidder Bidder
	 */
	public AuctionAgent(Bidder bidder) {
		this.bidder = bidder;
	}

	/**
	 * Process the Auction event
	 * 
	 * @param ev SimEvent
	 * @return true if the event is an auction event (this means it is processed)
	 */
	protected boolean processAuctionEvent(SimEvent ev) {
		switch (ev.getTag()){
		/**
		 * The following cases are related to Auction notion
		 */
		case AuctionTags.AUCTION_START:
			this.processAuctionOpened();
			break;
		case AuctionTags.BID_ACKNOWLEDGE:
			this.processBidAcknowledgment((Boolean) ev.getData());
			break;
		case AuctionTags.AUCTION_CLOSE:
			this.processAuctionClosed();
			break;
		case AuctionTags.ALLOCATION_PUBLICATION:
			this.bidder.processAllocations(ev);
			break;
		default:
			return false;	
		}
		return true;
	}
	
	private void processAuctionOpened() {
		this.auctionOpen = true;
		this.bidder.bidAuction();
	}
	
	private void processBidAcknowledgment(Boolean flag) {
		this.bidAccepted = flag;
	}
	
	private void processAuctionClosed() {
		this.auctionOpen = false;
	}
	
	public boolean isReadyToBid() {
		return this.isAuctionOpen() && this.bid != null;
	}

}