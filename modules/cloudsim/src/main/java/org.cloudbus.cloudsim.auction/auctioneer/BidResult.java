package org.cloudbus.cloudsim.auction.auctioneer;

import org.cloudbus.cloudsim.Vm;

import java.util.Map;

/**
 * VM Allocation Based on Combinatorial Double Auction
 * Represents the result of a bid from bidder.
 * This result is sent to bidders after auction runs the algorithm.
 * 
 * @author Youness Teimoury
 * Blog: http://youness-teimoury.blogspot.com/
 * Email: youness126@gmail.com
 * Created on 2011/8/22
 */
public class BidResult {

	private double calculatedPrice;
	/**
	 * Key in first map represents the requested VM by broker
	 * Key in second map is the datacenter's ID which is going to serve the request
	 * Value in second map is the amount that the datacenter can offer for requested VM
	 */
	private Map<Vm, Map<Integer, VmAllocation>> allocations;

	public double getCalculatedPrice() {
		return calculatedPrice;
	}

	public BidResult(double calculatedPrice, Map<Vm, Map<Integer, VmAllocation>> allocations) {
		this.calculatedPrice = calculatedPrice;
		this.allocations = allocations;
	}

	public boolean isWinner() {
		return this.allocations != null;
	}

	/**
	 * Key in first map represents the requested VM by broker
	 * Key in second map is the datacenter's ID which is going to serve the request
	 * Value in second map is the amount and price that the datacenter can offer for requested VM
	 * 
	 * @return Map<AbstractVm, Map<Integer, VmAllocation>>
	 */
	public Map<Vm, Map<Integer, VmAllocation>> getVMAllocations() {
		return allocations;
	}

}