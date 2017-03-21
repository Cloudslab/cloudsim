package org.cloudbus.cloudsim.auction.auctioneer;

import org.cloudbus.cloudsim.Vm;

/**
 * VM Allocation Based on Combinatorial Double Auction
 * Contains the amount of allocation for a VM
 * 
 * @author Youness Teimoury
 * Blog: http://youness-teimoury.blogspot.com/
 * Email: youness126@gmail.com
 * Created on 2011/8/24
 */
public class VmAllocation {
	private Vm vm;
	private int allocatedAmount;
	private double allocationPrice;
	
	public VmAllocation(Vm vm, int allocatedAmount, double allocationPrice) {
		this.vm = vm;
		this.allocatedAmount = allocatedAmount;
		this.allocationPrice = allocationPrice;
	}

	public Vm getVm() {
		return vm;
	}

	public int getAllocatedAmount() {
		return allocatedAmount;
	}

	public double getAllocationPrice() {
		return allocationPrice;
	}

}