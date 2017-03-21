package org.cloudbus.cloudsim.auction.bid;

/**
 * Represents a bid from a broker
 * 
 * @author Youness Teimoury
 * Blog: http://youness-teimoury.blogspot.com/
 * Email: youness126@gmail.com
 * Created on 2011/8/16
 * Updated on 2013/2/14
 */
public class DatacenterBrokerBid extends Bid {

    /**
     * The duration for which the resource is needed
     */
    private long duration;

    public DatacenterBrokerBid(int brokerID, double price, long duration) {
		super(brokerID, price);
        this.duration = duration;
	}

	@Override
	public boolean isAscOrder() {
		return false;
	}

    @Override
    public long getPriority() {
        return this.duration;
    }
}
