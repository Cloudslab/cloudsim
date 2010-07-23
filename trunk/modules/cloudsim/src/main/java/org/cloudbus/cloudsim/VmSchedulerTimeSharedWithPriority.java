/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2010, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * VmSchedulerTimeSharedWithPriority is a VMM allocation policy that
 * allows sharing of PEs among virtual machines. CPU Share of each VM can be
 * set through the priority field. The smaller value accepted for priority is
 * 1. Values smaller than that are set to 1.  Priority means how many times
 * one machine runs faster than the other. E.g.: if a VM A has priority
 * 1 and a VM B has a priority 2, B will run twice as faster as A.
 *
 * @author		Rodrigo N. Calheiros
 * @since		CloudSim Toolkit 1.0
 */
public class VmSchedulerTimeSharedWithPriority extends VmScheduler {

	protected Map<String,PEShare> peAllocationMap;
	protected int pesInUse;
	protected int shares;

	public VmSchedulerTimeSharedWithPriority(List<? extends Pe> pelist) {
		super(pelist);
		this.pesInUse=0;
		this.shares=0;
		this.peAllocationMap = new HashMap<String,PEShare>();
	}

	@Override
	public boolean allocatePesForVm(Vm vm, List<Double> mipsShare){
		int priority = vm.getPriority();
		if(priority<1) {
			priority=1;
		}
		peAllocationMap.put(vm.getUid(),new PEShare(vm.getPesNumber(),priority));
		pesInUse+=vm.getPesNumber();
		shares+=priority;
		return true;
	}

	@Override
	public void deallocatePesForVm(Vm vm){
		PEShare peShare = peAllocationMap.remove(vm.getUid());
		pesInUse-=peShare.getPes();
		shares-=peShare.getShare();
	}

	/**
	 * Returns the MIPS share of each Pe that is available to a given VM
	 * @param id ID of the VM
	 * @param userId ID of VM's owner
	 * @return an array containing the amount of MIPS of each pe that is available to the VM
	 * @pre $none
	 * @post $none
	 */
	@Override
	public List<Double> getAllocatedMipsForVm(Vm vm) {

		//divides available MIPS among all VMs
		List<Double> myShare = new LinkedList<Double>();
		for(int i=0;i<getPeList().size();i++) {
			myShare.add(0.0);
		}

		double capacity=0.0;
		for (int i=0;i<getPeList().size();i++){
			capacity+=getPeList().get(i).getMips();
			if(i+1==pesInUse) {
				break;
			}
		}

		capacity/=shares;

		PEShare peShare = this.peAllocationMap.get(vm.getUid());
		int pes = peShare.getPes();
		int share = peShare.getShare();

		for(int i=0;i<pes;i++){
			myShare.remove(i);
			myShare.add(i, capacity*share/pes);
		}

		return myShare;
	}

	/**
	 * Gets the sum of available MIPS in all PEs.
	 *
	 * @return total available mips
	 *
	 * @pre $none
	 * @post $none
	 */
	@Override
	public double getAvailableMips() {
		if (getPeList() == null) {
			Log.printLine("Pe list is empty");
			return 0;
		}

		double mips = 0.0;
		for (Pe pe : getPeList()) {
			mips += pe.getPeProvisioner().getAvailableMips();
		}

		return mips;
	}

	/**
	 * Internal class to store the PEs and the share of each VM.
	 *
	 */
	static class PEShare{
		private final int pes;
		private final int share;

		PEShare(int pes, int share){
			this.pes = pes;
			this.share = share;
		}

		public int getPes() {
			return pes;
		}

		public int getShare() {
			return share;
		}
	}

}
