package org.cloudbus.cloudsim.auction.auctioneer;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.auction.AuctionException;
import org.cloudbus.cloudsim.auction.bid.Bid;
import org.cloudbus.cloudsim.auction.bid.DatacenterBid;
import org.cloudbus.cloudsim.auction.bid.DatacenterBrokerBid;
import org.cloudbus.cloudsim.auction.vm.DatacenterAbstractVm;
import org.cloudbus.cloudsim.auction.vm.VmUtils;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;

import java.util.*;

/**
 * VM Allocation Based on Combinatorial Double Auction
 * Represents an auctioneer agent that is responsible to handle an auction
 * 
 * @author Youness Teimoury
 * Blog: http://youness-teimoury.blogspot.com/
 * Email: youness126@gmail.com
 * Created on 2011/7/17
 */
public class Auctioneer extends SimEntity {
	
	//Create an auctioneer object
    //TODO This attribute must be a member of CloudSim class
    /** Auctioneer*/
    private static Auctioneer auctioneer ;

    //TODO This method must be a member of CloudSim class
	public static int getAuctioneerEntityId() {		
		return auctioneer.getId();
	}
	
	public static void initAuctioneer() {
		if (auctioneer ==null) {
			auctioneer = new Auctioneer("Auctioneer");
		}
	}

	/**
	 * The required minimum number of registered bidders to start the auction
	 */
	private static final int MIN_REGISTERED_ENTITIES = 2;

	//TODO decide when to release the following reference at the end of auction
	/**
	 * The real bids are maintained in this list.
	 */
	private List<BidCalculation> bidCalcs;

	//TODO decide when to release the following reference little by little maybe when every broker gets its portion 
	/**
	 * Contains the assignment of offered VMs to requested VMs
	 */
	private AuctionAllocation allocations = new AuctionAllocation();

	/**
	 * The set contains the id of entities that has requested to get notification 
	 * of auction start.
	 * The set is cleared after auction end notification.
	 * Note that a bidder is not necessarily a registered-to-be-notified 
	 * bidder. This means every one can bid the auction away from being 
	 * registered to get notifications or not.
	 */
	private Set<Integer> registeredEntityList;

	/**
	 * Represents if auction is open or not.
	 */
	private boolean auctionOpen;

	public Auctioneer(String name) {
		super(name);
	}

	@Override
	public void processEvent(SimEvent ev) {
		switch (ev.getTag()) {

		case AuctionTags.REGISTER_BIDDER:
			this.registerEntity(ev.getSource());
			this.decideStartAuction();
			break;

		case AuctionTags.AUCTION_START:
			this.informAuctionStart();
			break;

		case AuctionTags.BID_AUCTION:
			this.registerBid((Bid) ev.getData());
			this.decideCloseAuction();
			break;

		case AuctionTags.AUCTION_CLOSE:
			this.informAuctionClose();
			break;
		}

	}

	private boolean auctionStartNotificationSent;

	private void decideStartAuction() {
		/**
		 * We can imagine if a fixed number of registers were accepted, start the auction
		 */
		 if (!this.auctionStartNotificationSent && 
				 this.registeredEntityList.size() >= Auctioneer.MIN_REGISTERED_ENTITIES) {
			 sendNow(getId(), AuctionTags.AUCTION_START);
			 this.auctionStartNotificationSent = true;
			 this.sentStartAuctionNotifications++;
		 }            
	}

	/**
	 * The amount of START_AUCTION notifications sent to registered entitiyes.
	 */
	private int sentStartAuctionNotifications;

	/**
	 * If all registered entities were sent START_AUCTION notification, close the auction
	 */
	private void decideCloseAuction() {
		//The first condition in if clause is to send the inner ACTION_CLOSE notifications once for all
		if (this.auctionStartNotificationSent && 
				this.sentStartAuctionNotifications <= this.registeredEntityList.size()) {
			sendNow(getId(), AuctionTags.AUCTION_CLOSE);
			this.auctionStartNotificationSent = false;
			this.sentStartAuctionNotifications = 0;
		}
	}


	@Override
	public void startEntity() {}

	@Override
	public void shutdownEntity() {
		// TODO Auto-generated method stub

	}

	/**
	 * Register the entity (datacenter, Broker, etc.) to notify it when the auction starts
	 * @param simEntityID int
	 */
	public void registerEntity(int simEntityID) {
		if (this.registeredEntityList == null) {
			this.registeredEntityList = new HashSet<Integer>(); 
		}
		this.registeredEntityList.add(simEntityID);
	}

	/**
	 * Inform the registered bidders (datacenters, brokers, etc.) that action 
	 * is going to start accepting bids.
	 */
	public void informAuctionStart() {
		this.openAuction();
		if (this.registeredEntityList != null) {
			for (int entityID : this.registeredEntityList) {
				sendNow(entityID, AuctionTags.AUCTION_START);
			}
		}
	}

	private void openAuction() {
		this.auctionOpen = true;
	}

	private void closeAuction() {
		this.auctionOpen = false;
	}

	private boolean isAuctionOpen() {
		return this.auctionOpen;
	}


	/**
	 * Registers a bid from bidders (datacenter, broker)
	 * @param bid Bid
	 */
	private void registerBid(Bid bid) {
		if (!this.isAuctionOpen()) {
			sendNow(bid.getBidderID(), AuctionTags.BID_ACKNOWLEDGE, false);
			return;
		}

		if (bid instanceof DatacenterBid || bid instanceof DatacenterBrokerBid) {

			if (this.bidCalcs == null) {
				this.bidCalcs = new ArrayList<BidCalculation>();
			}
			this.bidCalcs.add(new BidCalculation(bid));
			sendNow(bid.getBidderID(), AuctionTags.BID_ACKNOWLEDGE, true);

		} else {
			throw new AuctionException("Not handled Bid type.");
		}

	}

	/**
	 * - Inform the bidders (datacenters and brokers) that action is closed and
	 * no more request is processed.
	 * - Clear the list of registered bidders.
	 * - Compute auction
	 * - Return results
	 */
	public void informAuctionClose() {
		//Close auction
		this.closeAuction();
		if (this.registeredEntityList != null) {
			for (int entityID : this.registeredEntityList) {
				sendNow(entityID, AuctionTags.AUCTION_CLOSE);
			}
			this.registeredEntityList = null;
		}

		if (this.bidCalcs != null) {
			//Compute auction
			this.computeAuction();

			//Return result
			this.allocationPublication();
		}
	}

	/**
	 * Calculate the auction assignments (looser, winner and their shares)
	 */	
	private void computeAuction() {
		//Separate broker bids and datacenter bids into two bunches
		List<BidCalculation> brokerBidCalcs = getBidCalcs(this.bidCalcs, DatacenterBrokerBid.class);
		List<BidCalculation> datacenterBidCalcs = getBidCalcs(this.bidCalcs, DatacenterBid.class);

        /**
         * Order each bunch of bids
         */

		Comparator<BidCalculation> comparator = new NormComparator();//TODO decide from one of the following comparators: //AverageBidComparator//NormComparator
		
		//broker bids are ordered in descending order
		Collections.sort(brokerBidCalcs, comparator);
        printBidCalcs("brokers", brokerBidCalcs);
		//datacenter bids are ordered in ascending order
		Collections.sort(datacenterBidCalcs, comparator);
        printBidCalcs("datacenters", datacenterBidCalcs);

		for (BidCalculation brokerBidCalc : brokerBidCalcs) {
			List<Vm> brokerVMs = brokerBidCalc.getOrderedResources();
			if (brokerVMs != null) { // If broker has request
				for (BidCalculation datacenterBidCalc : datacenterBidCalcs) {
					/**
					 * The broker average bid must be equal or greater than the offered average 
					 * bid to let the broker bid accepted.
					 */
					if (brokerBidCalc.getAverageBid() >= datacenterBidCalc.getAverageBid()) {
						List<Vm> datacenterVMs = datacenterBidCalc.getOrderedResources();
						if (datacenterVMs != null) { // If datacenter has offer


							this.algorithmEngine(brokerBidCalc, brokerVMs, datacenterBidCalc, datacenterVMs);						

							
						} // end of if (datacenterVMs != null)
					} // end of if (brokerBidCalc.getAverageBid() >= datacenterBidCalc.getAverageBid())
				} // end of for (BidCalculation datacenterBidCalc : datacenterBidCalcs)
				
				/**
				 * Check if resources are not completely assigned to broker.
				 */				
				this.satisfactionHandling(brokerBidCalc);
				
			} // end of if (brokerVMs != null)
		} // end of for (BidCalculation brokerBidCalc : brokerBidCalcs)
	}

    private void algorithmEngine(BidCalculation brokerBidCalc, List<Vm> requestedVMs,
                                 BidCalculation datacenterBidCalc, List<Vm> offeredVMs) {

		for (Vm requestedVM : requestedVMs) {
			for (Vm offeredVM : offeredVMs) {
				
				/**
				 * See if any amount requested
				 */
				int requestAmount = brokerBidCalc.getRemainingQuantity(requestedVM);
				if (requestAmount == 0) { // If nothing is requested
					break; // Process next requested VM
				}

				/**
				 * See if enough amount of resource is offered
				 */
				int offerAmount = datacenterBidCalc.getRemainingQuantity(offeredVM);
				if (offerAmount == 0) { // If nothing is offered
					continue; // Process next offered VM
				}
				
				/**
				 * See if offered VM can satisfy the request
				 */
				if (VmUtils.canServe(offeredVM, requestedVM)) {

					int assignAmount = (requestAmount <= offerAmount ? requestAmount : offerAmount);

					/**
					 * Assign mapped-together VMs
					 */
					this.assignVMs(brokerBidCalc, requestedVM, datacenterBidCalc, offeredVM, assignAmount);
					break;
				} // end of if (requestedVM.getWeight() <= offeredVM.getWeight())
			}// end of for (Vm offeredVM : offeredVMs)
		}// end of for (Vm requestedVM : requestedVMs)
	}
	
	/**
	 * If a broker is not assigned all its requests, release its assigned resources.
	 * @param brokerBidCalc BidCalculation
	 */
	private void satisfactionHandling(BidCalculation brokerBidCalc) {
		if (!brokerBidCalc.isSatisfied()) {
			
			/**
			 * Get allocated broker VMs
			 */
			Set<Vm> brokerAllocatedVMs = brokerBidCalc.getAllocatedVMs();
			if (brokerAllocatedVMs != null) {
				for (Vm brokerVM : brokerAllocatedVMs) {
					
					/**
					 * Get allocated datacenter-bids for a broker-VM
					 */
					Set<BidCalculation> allocatedDatacenterBids =
						this.allocations.getAllocatedDatacenterBids(brokerVM);
					
					if (allocatedDatacenterBids != null) {
						for (BidCalculation allocatedDatacenterBid : allocatedDatacenterBids) {
							/**
							 * Get allocated datacenter-VMs in the 'datacenter-bid for a broker-VM'
							 */
							Vm allocatedDatacenterVM = this.allocations.getAllocatedVM(brokerVM, allocatedDatacenterBid);
							if (allocatedDatacenterVM != null) {
								int allocatedQuantity = this.allocations.getAllocatedQuantity(brokerVM, 
										allocatedDatacenterBid);

								/**
								 * Roll back the price and requested amount
								 */
								this.releaseVMsStep1(brokerBidCalc, brokerVM, 
										allocatedDatacenterBid, allocatedDatacenterVM, allocatedQuantity);

							} // if (allocatedDatacenterVMs != null)
						} // for (BidCalculation allocatedDatacenterBid : allocatedDatacenterBids)

						/**
						 * To avoid ConcurrentModificationException, we do this operation out 
						 * of the preceding for loop.
						 */
						this.releaseVMsStep2(brokerVM);
						
						
					} // if (allocatedDatacenterBids != null)
				} // end of for (Vm brokerVM : brokerAllocatedVMs)
			} // end of if (brokerAllocatedVMs != null)
		} // if satisfied
	}

	/**
	 * Assigns matched VMs together, specifying the amount and price of 
	 * assignment for both sides (broker and datacenter)    
	 */
	private void assignVMs(BidCalculation brokerBidCalc, Vm requestedVM,
                           BidCalculation datacenterBidCalc, Vm offeredVM, int assignAmount) {

		/**
		 * Compute price
		 */
		double payPrice = computePrice(brokerBidCalc, datacenterBidCalc, assignAmount);
		
		/**
		 * A number of requests were accepted therefore update the 
		 * amount of requests for broker side
		 * Update the price which broker must pay
		 */
		brokerBidCalc.assignAllocation(requestedVM, assignAmount, payPrice);
		/**
		 * A number of offers were offered therefore update the 
		 * amount of offers for datacenter side
		 * Update the price which datacenter must get
		 */
		datacenterBidCalc.assignAllocation(offeredVM, assignAmount, payPrice);
		
		this.allocations.addAllocation(requestedVM, datacenterBidCalc, offeredVM, assignAmount, payPrice);
 	}
	
	/**
	 * Releases assigned together VMs, subtracting the amount and price of 
	 * assignment for both sides (broker and datacenter)    
	 */
	private void releaseVMsStep1(BidCalculation brokerBidCalc, Vm brokerVM,
                                 BidCalculation datacenterBidCalc, Vm offeredVM, int assignAmount) {

		/**
		 * Compute price
		 */
		double subtractPrice = computePrice(brokerBidCalc, datacenterBidCalc, assignAmount);
		
		/**
		 * A number of requests were released therefore update the 
		 * amount of requests for broker side
		 * Update the price which broker must pay
		 */
		brokerBidCalc.releaseAllocation(brokerVM, assignAmount, subtractPrice);
		/**
		 * A number of offers were released therefore update the 
		 * amount of offers for datacenter side
		 * Update the price which datacenter must get
		 */
		datacenterBidCalc.releaseAllocation(offeredVM, assignAmount, subtractPrice);
	}
	
	/**
	 * Releases assigned datacenter bids to requested broker VMs from main allocations
	 * @param brokerVM Vm
	 */
	private void releaseVMsStep2(Vm brokerVM) {
		this.allocations.removeAllocation(brokerVM);
	}


	private static double computePrice(BidCalculation brokerBidCalc,
                                       BidCalculation datacenterBidCalc, int assignAmount) {
		return ((brokerBidCalc.getAverageBid() + datacenterBidCalc.getAverageBid()) / 2);		
	}
	 
	private static List<BidCalculation> getBidCalcs(List<BidCalculation> bidCalculations, Class<? extends Bid> class1) {
		List<BidCalculation> bidCalcs = null;
		for (BidCalculation bidCalc : bidCalculations) {
			if (bidCalc.getBidderType() == class1) {
				if (bidCalcs == null) {
					bidCalcs = new ArrayList<BidCalculation>();
				}
				bidCalcs.add(bidCalc);
			}
		}
		return bidCalcs;
	}

	/**
	 * Inform the bidders of the allocated bids  
	 */
	private void allocationPublication() {
		this.printAllocations();
		for (BidCalculation bidCalc : this.bidCalcs) {
			int entityID = bidCalc.getBidderID();
			Object data = null;
			if (bidCalc.getBidderType() == DatacenterBrokerBid.class) {
				data = createBidResult(bidCalc);
			} else if (bidCalc.getBidderType() == DatacenterBid.class) {
				data = createDatacenterBidResult(bidCalc);
			}
			sendNow(entityID, AuctionTags.ALLOCATION_PUBLICATION, data);
		}
		//TODO release this.bidCalcs in appropriate place
	}

	private BidResult createBidResult(BidCalculation bidCalc) {
		if (bidCalc.getBidderType() != DatacenterBrokerBid.class) {
			throw new AuctionException("Incompatible bid");
		}
		
		Map<Vm, Map<Integer, VmAllocation>> assignments = null;
		Set<Vm> vms = bidCalc.getAllocatedVMs();
		if (vms != null) {
			for (Vm vm : vms) {
				Map<Integer, VmAllocation> datacenters = null;
				Set<BidCalculation> datacenterBids = this.allocations.getAllocatedDatacenterBids(vm);
				if (datacenterBids != null) {
					for (BidCalculation datacenterBid : datacenterBids) {
						VmAllocation vmAllocation = this.allocations.getVmAllocation(vm, datacenterBid);
						if (datacenters == null) { //Initialization
							datacenters = new HashMap<Integer, VmAllocation>();
						}
						datacenters.put(datacenterBid.getBidderID(), vmAllocation);
					}
				}
				
				if (datacenters != null) {
					if (assignments == null) { //Initialization
						assignments = new HashMap<Vm, Map<Integer,VmAllocation>>();
					}
					assignments.put(vm, datacenters);
				}
			}
		}
		
		BidResult bidResult = new BidResult(bidCalc.getCalculatedPrice(), assignments);
		return bidResult;
	}
	
	private Map<DatacenterAbstractVm, List<VmAllocation>> createDatacenterBidResult(BidCalculation bidCalc) {
		if (bidCalc.getBidderType() != DatacenterBid.class) {
			throw new AuctionException("Incompatible bid");
		}
		
		Map<DatacenterAbstractVm, List<VmAllocation>> assignments = null;
		
		Set<Vm> offeredVMs = bidCalc.getAllocatedVMs();
		if (offeredVMs != null) {
			for (Vm offeredVM : offeredVMs) {
				List<VmAllocation> requestedVMs = this.allocations.getRequestAllocations(bidCalc, offeredVM);
				
				if (requestedVMs != null) {
					if (assignments == null) { //Initialization
						assignments = new HashMap<DatacenterAbstractVm, List<VmAllocation>> ();
					}
					if (!assignments.containsKey(offeredVM)) {
						assignments.put((DatacenterAbstractVm)offeredVM, null);
					}
					List<VmAllocation> vmAllocations = assignments.get(offeredVM);
					if (vmAllocations == null) {
						vmAllocations = new ArrayList<VmAllocation>();
						assignments.put((DatacenterAbstractVm)offeredVM, vmAllocations);
					}

					vmAllocations.addAll(requestedVMs);
				}
			}
		}
		return assignments;
	}
	
	private void printAllocations() {
		Log.printLine();
		Log.printLine("#######Auctioneer Allocation Publication#######");
		
		List<BidCalculation> brokerBidCalcs = getBidCalcs(this.bidCalcs, DatacenterBrokerBid.class);

		if (brokerBidCalcs != null) {
			Log.printLine("Broker(ID)\tVM\tDatacenter(ID)\tAmount\tCost(per unit)");
			
			for (BidCalculation bidCalc : brokerBidCalcs) {
				Log.printLine("--------------------------------------------------------------");
				BidResult bidResult = createBidResult(bidCalc);
				Map<Vm, Map<Integer, VmAllocation>> allocs = bidResult.getVMAllocations();

				Log.printLine(CloudSim.getEntity(bidCalc.getBidderID()).getName() + "(" + bidCalc.getBidderID() + ")");/*bidCalc.getBidderID())*/;
				if (allocs != null) {
					for (Vm vm : allocs.keySet()) {
						Map<Integer, VmAllocation> datacenterIDs = allocs.get(vm);
						if (datacenterIDs != null) {							
							for (Integer datacenterID : datacenterIDs.keySet()) {
								VmAllocation vmAllocation = datacenterIDs.get(datacenterID);
								Log.printLine("\t\t" + ((Vm)vm).getId() + "\t" +
										CloudSim.getEntityName(datacenterID)  + "(" + datacenterID  + ")" + "\t  " +
										vmAllocation.getAllocatedAmount() + "\t" + vmAllocation.getAllocationPrice());
							}
						}
					}
				} else {
					Log.printLine("\t\t\tNO ALLOCATION");
				}				
				Log.printLine();
			}
		}
		
		Log.printLine("#######Auctioneer Allocation Publication#######");
		Log.printLine();
	}

    private void printBidCalcs(String name, List<BidCalculation> brokerBidCalcs) {
        System.out.println("Printing the bid calculations for " + name);
        for (BidCalculation calc : brokerBidCalcs) {
            System.out.println(calc.toString() + ", norm=" + calc.getNorm());
        }
        System.out.println("Finished printing the bid calculations for " + name);
    }

}