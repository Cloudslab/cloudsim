package org.cloudbus.cloudsim.auction.bid;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.auction.AuctionException;
import org.cloudbus.cloudsim.auction.vm.DatacenterAbstractVm;

/**
 * Represents a bid from a datacenter
 * 
 * @author Youness Teimoury
 * Blog: http://youness-teimoury.blogspot.com/
 * Email: youness126@gmail.com
 * Created on 2011/8/16 
 */
public class DatacenterBid extends Bid {

	public DatacenterBid(int datacenterID) {
		super(datacenterID);
	}
	
	public DatacenterBid(int datacenterID, long price) {
		super(datacenterID, price);
	}

	@Override
	public void addVM(Vm vm, int quantity) {
		if (vm instanceof DatacenterAbstractVm) {
			DatacenterAbstractVm dvm = ((DatacenterAbstractVm) vm);
			super.addVM(vm, quantity);
			this.addPrice(dvm.getTotalCostPerMI() * quantity);
		} else {
			throw new AuctionException("The offerd vm in datacenter bid must be of type DatacenterAbstractVm.");
		}
	}
	
	@Override
	public boolean isAscOrder() {
		return true;
	}

}