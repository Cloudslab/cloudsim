package org.cloudbus.cloudsim.auction.auctioneer;

/**
 * This class must be on comply with the attributes in CloudSimTags class
 * They integer values must not be interference with each other
 * 
 * @author Youness Teimoury
 * Blog: http://youness-teimoury.blogspot.com/
 * Email: youness126@gmail.com
 * Created on 2011/8/26
 */
public class AuctionTags {
    // starting constant value for cloud-related tags
    private static final int BASE = 0;

    /**
     * Denotes an event to register the broker into auction notification service
     */
	public static final int REGISTER_BIDDER = BASE + 43;

   /**
     * Denotes an event to inform the registered entities (in Auctioneer) 
     * that auction is started.
     */
	public static final int AUCTION_START = BASE + 44;
  
	/**
     * Denotes an event from bidder (datacenter/broker) to Auctioneer to
     *  bid in auction
     */
	public static final int BID_AUCTION = BASE + 45;

	/**
     * Denotes an event from Auctioneer to inform the bidder (datacenter/broker)
     *  that the bidder's bid is accepted or not. 
     */
	public static final int BID_ACKNOWLEDGE = BASE + 46;

   /**
     * Denotes an event to inform the registered entities (in Auctioneer) 
     * that auction is closed and no more bids are accepted.
     */
	public static final int AUCTION_CLOSE = BASE + 47;

	/**
     * Denotes an event from Auctioneer to inform the bidders of the  
     * allocation results.	
     */
	public static final int ALLOCATION_PUBLICATION = BASE + 48;

}