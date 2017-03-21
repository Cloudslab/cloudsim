package org.cloudbus.cloudsim.auction.auctioneer;

import org.cloudbus.cloudsim.auction.AuctionException;

import java.util.Comparator;

public class AverageBidComparator implements Comparator<BidCalculation> {

	@Override
	public int compare(BidCalculation o1, BidCalculation bc) {
		if (o1.getBidderType() != bc.getBidderType()) {
			throw new AuctionException("Incompatible types to be ordered.");
		}

		int returnValue = 0;
		if (o1.getAverageBid() < bc.getAverageBid()) {
			returnValue =  -1;
		} else if (o1.getAverageBid() > bc.getAverageBid()) {
			returnValue = 1;
		}
		return returnValue * (o1.isBidAscOrder()?1:-1);
	}

}