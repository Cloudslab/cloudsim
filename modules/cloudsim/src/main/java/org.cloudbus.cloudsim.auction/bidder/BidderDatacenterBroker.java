package org.cloudbus.cloudsim.auction.bidder;

import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.auction.auctioneer.AuctionTags;
import org.cloudbus.cloudsim.auction.auctioneer.Auctioneer;
import org.cloudbus.cloudsim.auction.auctioneer.BidResult;
import org.cloudbus.cloudsim.auction.auctioneer.VmAllocation;
import org.cloudbus.cloudsim.auction.bid.DatacenterBrokerBid;
import org.cloudbus.cloudsim.auction.vm.ClonedVm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a Broker which can bid into Auction
 * 
 * @author Youness Teimoury
 * Blog: http://youness-teimoury.blogspot.com/
 * Email: youness126@gmail.com
 * Created on 2011/8/20
 */
public class BidderDatacenterBroker extends DatacenterBroker implements Bidder{

	private AuctionAgent auctionAgent;
	public BidderDatacenterBroker(String name) throws Exception {
		super(name);
		this.auctionAgent = new AuctionAgent(this);
	}

	@Override
	protected void processOtherEvent(SimEvent ev) {
		if (!this.auctionAgent.processAuctionEvent(ev)) {
			super.processOtherEvent(ev);
		}
	}
	
	@Override
	public void startEntity() {
		Log.printLine(getName() + " is starting registering to bid...");
		sendNow(Auctioneer.getAuctioneerEntityId(), AuctionTags.REGISTER_BIDDER, getId());
	}

	@Override
	public void bidAuction() {
		if (this.auctionAgent.isReadyToBid()) {
			sendNow(Auctioneer.getAuctioneerEntityId(), AuctionTags.BID_AUCTION,
					this.auctionAgent.getBid());
		}
	}
	
	/**
	 * Null checks aren't handled in this method to help debugging the 
	 * probable logical problems.
	 * @param ev SimEvent
	 */
	@Override
	public void processAllocations(SimEvent ev) {
		BidResult bidResult = (BidResult) ev.getData();
		if (bidResult.isWinner()) {//this.getId()
			//get the list of VMs and the amounts to be created			
			Map<Vm, Map<Integer, VmAllocation>> vmAllocations = bidResult.getVMAllocations();
			
			if (vmAllocations != null) {
				Set<Vm> Vms = vmAllocations.keySet();
				for (Vm vm : Vms) {
					if (vm.getClass() == Vm.class) {
						Map<Integer, VmAllocation> datacenterRation = vmAllocations.get(vm);
						if (datacenterRation != null) {
							Set<Integer> datacenterIDs = datacenterRation.keySet();
							
							Vm clonedVm = vm;
							
							Iterator<Integer> datacenterIDsIterator = datacenterIDs.iterator();
							for (int i = 0; i < datacenterIDs.size(); i++) {
								/**
								 * A more than one datacenters have offered VMs therefore we 
								 * clone VM to create a new VM and request it 
								 */
								if (i > 0) {
									clonedVm = new ClonedVm(vm);
								}
								
								int datacenterID = datacenterIDsIterator.next();
								VmAllocation vmAllocation = datacenterRation.get(datacenterID);
								int allocatedAmount = vmAllocation.getAllocatedAmount();
								for (int j = 0; j < allocatedAmount; j++) {
									/**
									 * A datacenter has offered more than one VM therefore we 
									 * clone VM to create a new VM and request it 
									 */
									if (j > 0) {
										clonedVm = new ClonedVm(vm);
									}
									/**
									 * Add the VM to submit list. For more information look at information on
									 * this.submitVmList method. 
									 */
									getVmList().add(clonedVm);
									
									/**
									 * Create the VM on specified datacenter.
									 */
									this.createVmInDatacenter(datacenterID, clonedVm);
								}
							}
						}
					}
				}
			}
		}
	}

    /**
     * Creates the Vm in Datacenter
     *  
     * @param datacenterId Id of the chosen PowerDatacenter
     * @param vm Vm that is going to be created on Datacenter
     */
    protected void createVmInDatacenter(int datacenterId, Vm vm) {
		if (!getVmsToDatacentersMap().containsKey(vm.getId())) { //Youness: Find VMs that are not previously assigned to a Datacenter
			String datacenterName = CloudSim.getEntityName(datacenterId);
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + vm.getUid() + " in " + datacenterName);
			sendNow(datacenterId, CloudSimTags.VM_CREATE_ACK, vm);
			setVmsRequested(getVmsRequested()+1);
		}
		getDatacenterRequestedIdsList().add(datacenterId);
		
		/**
		 * TODO:VM acks are not handled in this method and this might 
		 * be a threat when calling this method directly 
		 */

	}


	public void submitBid(DatacenterBrokerBid bid) {
		this.auctionAgent.setBid(bid);
	}
	
	/**
	 * The submit action is done when the auctioneer acknowledges the broker to 
	 * create a VM on a specified datacenter. This action is done in processAllocations method.
	 * Specifying the submitted VMs is because when a Datacenter acknowledges to Broker that
	 * a VM is created, Broker can lookup it up in its submitted VMs to take proper action. This is 
	 * done in processVmCreate method of DatacenterBroker.
	 */
	@Deprecated
	public void submitVmList(List<? extends Vm> list) {
	}
	
}