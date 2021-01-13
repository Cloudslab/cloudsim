package org.cloudbus.cloudsim.auction.bidder;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.auction.auctioneer.AuctionTags;
import org.cloudbus.cloudsim.auction.auctioneer.Auctioneer;
import org.cloudbus.cloudsim.auction.auctioneer.VmAllocation;
import org.cloudbus.cloudsim.auction.bid.DatacenterBid;
import org.cloudbus.cloudsim.auction.vm.ClonedVm;
import org.cloudbus.cloudsim.auction.vm.DatacenterAbstractVm;
import org.cloudbus.cloudsim.auction.vm.VmCharacteristics;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;

import java.util.List;
import java.util.Map;

/**
 * VM Allocation Based on Combinatorial Double Auction
 * Represents a datacenter that can bid some offers in an auction
 * 
 * @author Youness Teimoury
 * Blog: http://youness-teimoury.blogspot.com/
 * Email: youness126@gmail.com
 * Created on 2011/8/28
 */
public class BidderDatacenter extends Datacenter implements Bidder{
	private AuctionAgent auctionAgent;
	
	public BidderDatacenter(String name,
                            DatacenterCharacteristics characteristics,
                            VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList,
                            double schedulingInterval) throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList,
				schedulingInterval);
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
		super.startEntity();
		sendNow(Auctioneer.getAuctioneerEntityId(), AuctionTags.REGISTER_BIDDER, getId());
	}

	@Override
	public void bidAuction() {
		if (this.auctionAgent.isReadyToBid()) {
			sendNow(Auctioneer.getAuctioneerEntityId(), AuctionTags.BID_AUCTION,
					this.auctionAgent.getBid());
		}
	}

	public void submitBid(DatacenterBid bid) {
		this.auctionAgent.setBid(bid);
	}

	/**
	 * The method is overrided to calculate the MIPs debt based on auction algorithm price
	 */
	@Override
	protected void checkCloudletCompletion() {
		List<? extends Host> list = getVmAllocationPolicy().getHostList();
		for (int i = 0; i < list.size(); i++) {
			Host host = list.get(i);
			for (Vm vm : host.getVmList()) {
				while (vm.getCloudletScheduler().isFinishedCloudlets()){
					Cloudlet cl = vm.getCloudletScheduler().getNextFinishedCloudlet();
					// TODO: Not sure how this is used yet.
//					calcMIPSDebt(vm, cl);
					if (cl != null) {
						Log.printLine("cloudlet_" + cl.getCloudletId() + " finished and will be returned.");
						sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_RETURN, cl);
					}
				}
			}
		}
	}

	/**
	 * The OfferedVMs and list of their assigned requested VMs (bundled with 
	 * requested VMs' relative requested amount and auctioned price).
	 */
	private Map<DatacenterAbstractVm, List<VmAllocation>> assignments = null;
	
	@SuppressWarnings("unchecked")
	@Override
    public void processAllocations(SimEvent ev) {
		this.assignments = (Map<DatacenterAbstractVm, List<VmAllocation>>) ev.getData();
	}

	private void calcMIPSDebt(Vm vm, Cloudlet cl) {
		double amount = 0.0;
//		if (getDebts().containsKey(cl.getUserId())) {
//			amount = getDebts().get(cl.getUserId());
//		}
		
		for (DatacenterAbstractVm datacenterAbstractVm : this.assignments.keySet()) {
			for (VmAllocation vmAllocation : this.assignments.get(datacenterAbstractVm)) {
				if (vmAllocation.getVm() == vm) {
					amount +=  vmAllocation.getAllocationPrice() * cl.getCloudletLength();
//					getDebts().put(vm.getUserId(), amount);
					break;
				}
			}
		}
	}

	/**
	 * Service the VM if it is allocated by Auction
	 */
	@Override
	protected void processVmCreate(SimEvent ev, boolean ack) {
		Vm vm = (Vm) ev.getData();
		
		DatacenterAbstractVm datacenterAbstractVm = getAllocatedDatacenterVM(vm);
		if (datacenterAbstractVm != null) {
			//A trick to calculate costs based on VM characteristics not the Datacenter characteristics
			VmCharacteristics vmCharacteristics = datacenterAbstractVm.getVmCharacteristics();
			getCharacteristics().setCostPerMem(vmCharacteristics.getCostPerMem());
			getCharacteristics().setCostPerStorage(vmCharacteristics.getCostPerStorage());
			super.processVmCreate(ev, ack);
		}
    }
	
	@Override
    protected void processCloudletSubmit(SimEvent ev, boolean ack) {
		Cloudlet cl = (Cloudlet) ev.getData();
        int userId = cl.getUserId();
        int vmId = cl.getVmId();
        Host host = getVmAllocationPolicy().getHost(vmId, userId);
        Vm vm = host.getVm(vmId, userId);

		DatacenterAbstractVm datacenterAbstractVm = getAllocatedDatacenterVM(vm);
		if (datacenterAbstractVm != null) {
			//A trick to calculate costs based on VM characteristics not the Datacenter characteristics
			VmCharacteristics vmCharacteristics = datacenterAbstractVm.getVmCharacteristics();
			getCharacteristics().setCostPerBw(vmCharacteristics.getCostPerBw());
			super.processCloudletSubmit(ev, ack);
		}
    }

	/**
	 * Returns the datacenter abstract VM that is allocated for parameter VM
	 * 
	 * @param vm Vm
	 * @return DatacenterAbstractVm
	 */
	private DatacenterAbstractVm getAllocatedDatacenterVM(Vm vm) {
		Vm parameterVM;
		if (vm instanceof ClonedVm) { //If more than one VM of the same type requested
			parameterVM = ((ClonedVm)vm).getBaseVM();
		} else {
			parameterVM = vm;
		}

		for (DatacenterAbstractVm datacenterAbstractVm : this.assignments.keySet()) {
			for (VmAllocation vmAllocation : this.assignments.get(datacenterAbstractVm)) {
				if (vmAllocation.getVm() == parameterVM) {
			 	    return datacenterAbstractVm;
				}
			}
		}
		return null;
	}
	
	public void printDebts() {
		Log.printLine("*****BidderDatacenter: "+this.getName()+"*****");
		Log.printLine("Broker ID\t\tDebt");

//		Set<Integer> keys = getDebts().keySet();
//		Iterator<Integer> iter = keys.iterator();
//		DecimalFormat df = new DecimalFormat("#.##");
//		while (iter.hasNext()) {
//			int key = iter.next();
//			double value = getDebts().get(key);
//			Log.printLine(key+"\t\t"+df.format(value));
//		}
		Log.printLine("**********************************");
	}
	
}