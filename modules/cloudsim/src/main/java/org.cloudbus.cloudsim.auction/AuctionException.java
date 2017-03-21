package org.cloudbus.cloudsim.auction;

/**
 * VM Allocation Based on Combinatorial Double Auction
 * Represents a bid from datacenter or a broker
 * 
 * @author Youness Teimoury
 * Blog: http://youness-teimoury.blogspot.com/
 * Email: youness126@gmail.com
 * Created on 2011/7/17
 */
public class AuctionException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AuctionException() {
		super();
	}

	public AuctionException(String message) {
		super(message);
	}

}
