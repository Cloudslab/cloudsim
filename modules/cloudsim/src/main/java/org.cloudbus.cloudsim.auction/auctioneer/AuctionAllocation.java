package org.cloudbus.cloudsim.auction.auctioneer;

import org.cloudbus.cloudsim.Vm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * VM Allocation Based on Combinatorial Double Auction
 * Represents the assignment of offered VMs to requested VMs
 * 
 * @author Youness Teimoury
 * Blog: http://youness-teimoury.blogspot.com/
 * Email: youness126@gmail.com
 * Created on 2011/8/22
 */
class AuctionAllocation {

	/**
	 * Used to maintain which VM from datacenter's bid is assigned to the requested VM.
	 * 1. Key in first Map<VM, ...> is the requested VM from broker
	 * 2. Key in second Map<BidCalculation, AbstractVmAllocation> is the datacenter's bid, from which vm(s) is/are assigned
	 * 3. Value in second Map<BidCalculation, AbstractVmAllocation> is the VM and its amount assigned to 1 from a datacenter's bid
	 */
	private HashMap<Vm, HashMap<BidCalculation, VmAllocation>> allocations;
	
	public AuctionAllocation() {		
		this.allocations = new HashMap<Vm, HashMap<BidCalculation, VmAllocation>>();
	}
	
	void addAllocation(Vm requestedVM, BidCalculation datacenterBid,
                       Vm offeredVM, int quantity, double price) {
		
		if (!this.allocations.containsKey(requestedVM)) {
			this.addNewRequestedVM(requestedVM, datacenterBid, offeredVM, quantity, price);
		} else {
			HashMap<BidCalculation, VmAllocation> datacenterBids =
				this.allocations.get(requestedVM);
			this.addNewDatacenterBid(datacenterBids, datacenterBid, offeredVM, quantity, price);
		}
	}
	
	void removeAllocation(Vm brokerVm) {
		this.allocations.remove(brokerVm);
	}

	void removeAllocation(Vm brokerVM, BidCalculation datacenterBid) {
		HashMap<BidCalculation, VmAllocation> datacenterBids =
			this.allocations.get(brokerVM);
		datacenterBids.remove(datacenterBid);
	}

	private void addNewRequestedVM(Vm requestedVM, BidCalculation datacenterBid,
                                   Vm offeredVM, int quantity, double price) {
		
		HashMap<BidCalculation, VmAllocation> bids =
			new HashMap<BidCalculation, VmAllocation> ();
		this.addNewDatacenterBid(bids, datacenterBid, offeredVM, quantity, price);
		
		this.allocations.put(requestedVM, bids);	
	}
	
	private void addNewDatacenterBid(HashMap<BidCalculation, VmAllocation> datacenterBids,
                                     BidCalculation datacenterBid, Vm offeredVM, int quantity, double price) {
		 datacenterBids.put(datacenterBid, new VmAllocation(offeredVM, quantity, price));
	}
	
	/**
	 * Returns the allocated datacenter bids for the broker vm
	 * @param vm Vm
	 * @return Set<BidCalculation>
	 */
	public Set<BidCalculation> getAllocatedDatacenterBids(Vm vm) {
		HashMap<BidCalculation, VmAllocation> datacenterBids =
			this.allocations.get(vm);
		if (datacenterBids != null) {
			return datacenterBids.keySet();
		}
		return null;
	}

	/**
	 * Returns the allocated datacenter VMs for the broke's VM
	 * @param brokerVM AbstractVm broker's VM
	 * @param allocatedDatacenterBid BidCalculation datacenter's bid
	 * @return Vm The allocated datacenter VM
	 */
	public Vm getAllocatedVM(Vm brokerVM,
                             BidCalculation allocatedDatacenterBid) {
		HashMap<BidCalculation, VmAllocation> datacenterBids =
			this.allocations.get(brokerVM);
		if (datacenterBids != null && datacenterBids.containsKey(allocatedDatacenterBid)) {
			VmAllocation allocatedVM = datacenterBids.get(allocatedDatacenterBid);
			
			if (allocatedVM != null) {
				return allocatedVM.getVm();
			}
		}
		return null;
	}

	/**
	 * 
	 * @param brokerVM AbstractVm broker's VM
	 * @param allocatedDatacenterBid BidCalculation datacenter's bid
	 * @return allocated quantity
	 */
	public int getAllocatedQuantity(Vm brokerVM,
			BidCalculation allocatedDatacenterBid) {
		HashMap<BidCalculation, VmAllocation> datacenterBids =
			this.allocations.get(brokerVM);
		if (datacenterBids != null && datacenterBids.containsKey(allocatedDatacenterBid)) {
			VmAllocation allocatedVM =	datacenterBids.get(allocatedDatacenterBid);
			if (allocatedVM != null) {
				return allocatedVM.getAllocatedAmount();
			}
		}
		return 0;
	}

	/**
	 * 
	 * @param brokerVM Vm broker's VM
	 * @param allocatedDatacenterBid BidCalculation datacenter's bid
	 * @return VmAllocation allocated quantity, price, ...
	 */
	public VmAllocation getVmAllocation(Vm brokerVM,
                                        BidCalculation allocatedDatacenterBid) {
		HashMap<BidCalculation, VmAllocation> datacenterBids =
			this.allocations.get(brokerVM);
		if (datacenterBids != null && datacenterBids.containsKey(allocatedDatacenterBid)) {
			VmAllocation allocatedVM =	datacenterBids.get(allocatedDatacenterBid);
			return allocatedVM;
		}
		return null;
	}

	/**
	 * Returns the list of requested VMs (in VMAllocation format) for the offeredVM bundled in
	 * datacenter (wrapped in BidCalculation)
	 * @param bidCalc BidCalculation The datacenter's bid calculation
	 * @param offeredVM Vm The offered VM in bidCalc
	 * @return List<VmAllocation> list of requested VMs assigned to offeredVM
	 */
	public List<VmAllocation> getRequestAllocations(BidCalculation bidCalc, Vm offeredVM) {
		List<VmAllocation> requestAllocations = null;
		
		Set<Vm> requestedVMs = this.allocations.keySet();
		if (requestedVMs != null) {
			for (Vm requestedVM : requestedVMs) {
				HashMap<BidCalculation, VmAllocation> datacenterBidMap = this.allocations.get(requestedVM);
				if (datacenterBidMap != null) {
					Set<BidCalculation> datacenterBids = datacenterBidMap.keySet();
					if (datacenterBids != null) {
						for (BidCalculation datacenterBid : datacenterBids) {
							if (datacenterBid == bidCalc) {
								VmAllocation offeredVMAllocation = datacenterBidMap.get(datacenterBid);
								if (offeredVMAllocation.getVm() == offeredVM) {
									
									VmAllocation requestedVMAllocation = new VmAllocation(requestedVM,
											offeredVMAllocation.getAllocatedAmount(), 
											offeredVMAllocation.getAllocationPrice());
									
									if (requestAllocations == null) {
										requestAllocations = new ArrayList<VmAllocation>();
									}
									requestAllocations.add(requestedVMAllocation);
									
								}
							}
						}
					}
				}
			}
		}
		return requestAllocations;
	}
	
}