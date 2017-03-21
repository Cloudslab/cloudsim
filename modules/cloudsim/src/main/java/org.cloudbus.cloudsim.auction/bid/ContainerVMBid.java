package org.cloudbus.cloudsim.auction.bid;

/**
 * Represents a bid from a Container VM
 * @author henri.vandenbulk
 */
public class ContainerVMBid extends Bid {

    /**
     * The duration for which the resource is needed
     */
    private long duration;

    public ContainerVMBid(int containerVMID, double price, long duration) {
		super(containerVMID, price);
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
