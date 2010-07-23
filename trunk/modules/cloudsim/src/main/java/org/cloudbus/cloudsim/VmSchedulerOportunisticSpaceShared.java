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
 * VmmAllocationPolicyTimeSpaceShared is a VMM allocation policy that
 * allocates one or more Pe to a VM, and doesn't allow sharing
 * of PEs. If there is no free PEs to the VM, allocation fails
 * However, if there is free PEs, they are scheduled to the VMs
 * This policy ignores requested number of MIPS.
 *
 * @author		Rodrigo N. Calheiros
 * @author		Anton Beloglazov
 * @since		CloudSim Toolkit 1.0
 */
public class VmSchedulerOportunisticSpaceShared extends VmScheduler {

	protected Map<String,Integer> peAllocationMap;
	protected int pesInUse;

	public VmSchedulerOportunisticSpaceShared(List<? extends Pe> pelist) {
		super(pelist);
		this.pesInUse=0;
		this.peAllocationMap = new HashMap<String,Integer>();

	}

	@Override
	public boolean allocatePesForVm(Vm vm, List<Double> mipsShare){
		//if there is no freePes, fails
		if(vm.getPesNumber()>super.getPeList().size()) {
			return false;
		}
		peAllocationMap.put(vm.getUid(),vm.getPesNumber());
		pesInUse+=vm.getPesNumber();
		return true;
	}

	@Override
	public void deallocatePesForVm(Vm vm){
		int pes = peAllocationMap.remove(vm.getUid());
		pesInUse-=pes;
	}

	@Override
	public List<Double> getAllocatedMipsForVm(Vm vm) {
		double[] myShare = new double[super.getPeList().size()];
		for(int i=0;i<myShare.length;i++) {
			myShare[i]=0.0;
		}

		double capacity=0.0;
		for (int i=0;i<super.getPeList().size();i++){
			capacity+=super.getPeList().get(i).getMips();
		}

		//it receives the capacity of the allocated VMs and the capacity of the free PEs.
		if(pesInUse>super.getPeList().size()){
			capacity/=pesInUse;
		} else {
			capacity/=super.getPeList().size();
		}

		int pes = peAllocationMap.get(vm.getUid());

		for(int i=0;i<pes;i++){
			myShare[i]=capacity;
		}

		LinkedList<Double> outputVector = new LinkedList<Double>();
		for(double share: myShare) {
			outputVector.add(share);
		}

		return outputVector;
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

}
