package org.cloudbus.cloudsim.auction.bid;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.auction.vm.VmComparator;
import org.cloudbus.cloudsim.core.CloudSim;

import java.util.*;

/**
 * VM Allocation Based on Combinatorial Double Auction
 * Represents a bid from datacenter or a broker
 * 
 * @author Youness Teimoury
 * Blog: http://youness-teimoury.blogspot.com/
 * Email: youness126@gmail.com
 * Created on 2011/7/16
 */
public abstract class Bid {

	private final int bidderID;
	
	/**
     * Represents a bundle of resources and their relative offered/requested quantity that
     *  is bid by a Datacenter/Broker 
	 */
	private Map<Vm, Integer> resourceBundle;
	
	private double price;
	
	/**
	 * Getter & setters 
	 */
	
	public int getBidderID() {
		return bidderID;
	}

	public Map<Vm, Integer> getResourceBundle() {
		return resourceBundle;
	}
	
	public double getPrice() {
		return price;
	}
	
	public void setPrice(double price) {
		this.price = price;
	}
	
	public void addPrice(double price) {
		this.price += price;
	}

	/*
	 * Constructors 
	 */
	public Bid(int bidderID) {
		this(bidderID, 0);
	}
	
	public Bid(int bidderID, double price) {
		super();
		this.bidderID = bidderID;
		this.price = price;
	}

	/**
	 * Adds VM to resource bundle
	 * @param vm Vm
	 * @param quantity
	 */
	public void addVM(Vm vm, int quantity) {
		if (this.resourceBundle == null) {
			this.resourceBundle = new HashMap<Vm, Integer>();
		}
		this.updateVM(vm, quantity);
	}
	
	/**
	 *Updates the VM in resource bundle
	 * @param vm Vm
	 * @param quantity
	 */
	public void updateVM(Vm vm, int quantity) {
		this.resourceBundle.put(vm, quantity);
	}
	
	public boolean containsVM(Vm vm) {
		boolean vmExists = false;
		if (this.resourceBundle != null) {
			return this.resourceBundle.containsKey(vm);
		}
		return vmExists;
	}

	public Set<Vm> getResources() {
		return this.resourceBundle.keySet();
	}

	/**
	 * TODO: The overhead of this method is big. Since this method is 
	 * called many times, the result can be cached locally.
	 * @return List<AbstractVm> the ordered VMs in a bundle.
	 */
	public List<Vm> getOrderedResources() {
		List<Vm> orderedList = (new ArrayList<Vm>(this.getResources()));
		Collections.sort(orderedList, new VmComparator());
		return orderedList;
	}
	
	public Integer getQuantity(Vm vm)  {
		return this.resourceBundle.get(vm);
	}

	/**
	 *TODO: null checks aren't handled 
	 * @return The total amount of bids
	 */
	public int getTotalQuantity() {
		int totalAmount = 0;
		Collection<Integer> amounts = this.resourceBundle.values();
		for (int amount : amounts) {
			totalAmount += amount;
		}
		return totalAmount;
	}

	/**
	 * Decides if ordering in VM Allocation Based on 
	 * Combinatorial Double Auction is ascending/descending
	 * @return boolean true if ascending, false if descending
	 */
	public abstract boolean isAscOrder();
	
	@Override
	public String toString() {
		return  "Bidder" + CloudSim.getEntityName(this.bidderID) + " price: " + this.getPrice();
	}

    /**
     * @return The priority of the bid
     */
    public long getPriority() {
        return 1;
    }
}