package org.cloudbus.cloudsim.auction.auctioneer;

import org.cloudbus.cloudsim.auction.AuctionException;

import java.util.Comparator;

/**
 * Default comparator for BidCalculation
 */
public class NormComparator implements Comparator<BidCalculation> {

	@Override
	public int compare(BidCalculation bc1, BidCalculation bc2) {
		if (bc1.getBidderType() != bc2.getBidderType()) {
			throw new AuctionException("Incompatible types to be ordered.");
		}

		int returnValue = 0;
		if (bc1.getNorm() < bc2.getNorm()) {
			returnValue =  -1;
		} else if (bc1.getNorm() > bc2.getNorm()) {
			returnValue = 1;
		}
		return returnValue * (bc1.isBidAscOrder()?1:-1);
	}

}