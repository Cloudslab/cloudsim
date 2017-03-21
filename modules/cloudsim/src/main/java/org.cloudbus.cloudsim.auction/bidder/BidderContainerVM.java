package org.cloudbus.cloudsim.auction.bidder;

import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.auction.auctioneer.AuctionTags;
import org.cloudbus.cloudsim.auction.auctioneer.Auctioneer;
import org.cloudbus.cloudsim.auction.auctioneer.BidResult;
import org.cloudbus.cloudsim.auction.auctioneer.VmAllocation;
import org.cloudbus.cloudsim.auction.bid.Bid;
import org.cloudbus.cloudsim.auction.bid.ContainerVMBid;
import org.cloudbus.cloudsim.auction.bid.DatacenterBrokerBid;
import org.cloudbus.cloudsim.auction.vm.ClonedVm;
import org.cloudbus.cloudsim.container.containerProvisioners.ContainerBwProvisioner;
import org.cloudbus.cloudsim.container.containerProvisioners.ContainerPe;
import org.cloudbus.cloudsim.container.containerProvisioners.ContainerRamProvisioner;
import org.cloudbus.cloudsim.container.core.ContainerVm;
import org.cloudbus.cloudsim.container.core.PowerContainerVm;
import org.cloudbus.cloudsim.container.schedulers.ContainerScheduler;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a Container VM that can bid on an auction
 *
 * @author henri.vandenbulk
 */
public class BidderContainerVM extends PowerContainerVm implements Bidder {

    private AuctionAgent auctionAgent;

    public BidderContainerVM(int id, int userId, double mips, float ram, long bw, long size, String vmm, ContainerScheduler containerScheduler, ContainerRamProvisioner containerRamProvisioner, ContainerBwProvisioner containerBwProvisioner, List<? extends ContainerPe> peList, double schedulingInterval) {
        super(id, userId, mips, ram, bw, size, vmm, containerScheduler, containerRamProvisioner, containerBwProvisioner, peList, schedulingInterval);
        this.auctionAgent = new AuctionAgent(this);
    }

//	public BidderContainerVM(String name) throws Exception {
//		super(name);
//		this.auctionAgent = new AuctionAgent(this);
//	}

//	@Override
//	protected void processOtherEvent(SimEvent ev) {
//		if (!this.auctionAgent.processAuctionEvent(ev)) {
//			super.processOtherEvent(ev);
//		}
//	}
//
//	@Override
//	public void startEntity() {
//		Log.printLine(this.getId() + " is starting and registering to bid...");
//		sendNow(Auctioneer.getAuctioneerEntityId(), AuctionTags.REGISTER_BIDDER, getId());
//	}

    @Override
    public void bidAuction() {
        if (this.auctionAgent.isReadyToBid()) {
//			sendNow(Auctioneer.getAuctioneerEntityId(), AuctionTags.BID_AUCTION,
//					this.auctionAgent.getBid());
        }
    }

    /*
        When asked to bid on an auction return the bid
     */
    public double bidOnAuction(double mips, double mem) {
//        if (this.auctionAgent.isReadyToBid()) {
//        return this.auctionAgent.getBid();
//        }
//        else return null;

        return computeScore(mips, mem);
    }

    /*
     * Compute the score which will be the bid.
     */
    public double computeScore(double mips, double mem) {
        double fractionUsedContainers = 0;
        double fractionUsedMemory = 1.0 - mem / this.getRam();
        double fractionUsedMips = 1.0 - mips / this.getTotalMips();
        int n = this.getNumberOfContainers();
        if (n != 0) {
            fractionUsedContainers = 1.0 - 1 / this.getNumberOfContainers();
        }
        return (fractionUsedMemory + fractionUsedMips + fractionUsedContainers) / 3.0;
    }

    /**
     * Null checks aren't handled in this method to help debugging the
     * probable logical problems.
     *
     * @param ev SimEvent
     */
    @Override
    public void processAllocations(SimEvent ev) {

    }
//	@Override
//	public void processAllocations(SimEvent ev) {
//		BidResult bidResult = (BidResult) ev.getData();
//		if (bidResult.isWinner()) {//this.getId()
//			//get the list of VMs and the amounts to be created
//			Map<Vm, Map<Integer, VmAllocation>> vmAllocations = bidResult.getVMAllocations();
//
//			if (vmAllocations != null) {
//				Set<Vm> Vms = vmAllocations.keySet();
//				for (Vm vm : Vms) {
//					if (vm.getClass() == Vm.class) {
//						Map<Integer, VmAllocation> datacenterRation = vmAllocations.get(vm);
//						if (datacenterRation != null) {
//							Set<Integer> datacenterIDs = datacenterRation.keySet();
//
//							Vm clonedVm = vm;
//
//							Iterator<Integer> datacenterIDsIterator = datacenterIDs.iterator();
//							for (int i = 0; i < datacenterIDs.size(); i++) {
//								/**
//								 * A more than one datacenters have offered VMs therefore we
//								 * clone VM to create a new VM and request it
//								 */
//								if (i > 0) {
//									clonedVm = new ClonedVm(vm);
//								}
//
//								int datacenterID = datacenterIDsIterator.next();
//								VmAllocation vmAllocation = datacenterRation.get(datacenterID);
//								int allocatedAmount = vmAllocation.getAllocatedAmount();
//								for (int j = 0; j < allocatedAmount; j++) {
//									/**
//									 * A datacenter has offered more than one VM therefore we
//									 * clone VM to create a new VM and request it
//									 */
//									if (j > 0) {
//										clonedVm = new ClonedVm(vm);
//									}
//									/**
//									 * Add the VM to submit list. For more information look at information on
//									 * this.submitVmList method.
//									 */
//									getVmList().add(clonedVm);
//
//									/**
//									 * Create the VM on specified datacenter.
//									 */
//									this.createVmInDatacenter(datacenterID, clonedVm);
//								}
//							}
//						}
//					}
//				}
//			}
//		}
//	}

    /**
     * Creates the Container in VM
     *
     * @param datacenterId Id of the chosen PowerDatacenter
     * @param vm           Vm that is going to be created on Datacenter
     */
//    protected void createContainerInVM(int datacenterId, Vm vm) {
//		if (!getVmsToDatacentersMap().containsKey(vm.getId())) { //Youness: Find VMs that are not previously assigned to a Datacenter
//			String datacenterName = CloudSim.getEntityName(datacenterId);
//			Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + vm.getUid() + " in " + datacenterName);
//			sendNow(datacenterId, CloudSimTags.VM_CREATE_ACK, vm);
//			setVmsRequested(getVmsRequested()+1);
//		}
//		getDatacenterRequestedIdsList().add(datacenterId);
//
//		/**
//		 * TODO:VM acks are not handled in this method and this might
//		 * be a threat when calling this method directly
//		 */
//
//	}
    public void submitBid(ContainerVMBid bid) {
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