/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */


package org.cloudbus.cloudsim;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * VmSchedulerSpaceShared is a VMM allocation policy that
 * allocates one or more Pe to a VM, and doesn't allow sharing
 * of PEs. If there is no free PEs to the VM, allocation fails.
 * Free PEs are not allocated to VMs
 *
 * @author Rodrigo N. Calheiros
 * @since CloudSim Toolkit 1.0 Beta
 * @invariant $none
 */
public class VmSchedulerSpaceShared extends VmScheduler {

	//Map containing VM ID and a vector of PEs allocated to this VM
	protected Map<String,Vector<Integer>> peAllocationMap;
	protected Vector<Integer> freePesVector;

	public VmSchedulerSpaceShared(List<? extends Pe> pelist) {
		super(pelist);

		this.peAllocationMap = new HashMap<String,Vector<Integer>>();

		freePesVector = new Vector<Integer>();
		for (int i=0;i<pelist.size();i++){
			freePesVector.add(i);
		}
	}

	@Override
	public boolean allocatePesForVm(Vm vm, List<Double> mipsShare) {

		//if there is no enough free PEs, fails
		if (freePesVector.size() < mipsShare.size()) {
			return false;
		}

		double currentFreeMips = getAvailableMips();
		Vector<Integer> chosenPEs = new Vector<Integer>();
		List<Double> newMipsList = new LinkedList<Double>();
		for(int i=0;i<mipsShare.size();i++){
			int allocatedPe = freePesVector.remove(0);
			chosenPEs.add(allocatedPe);

			//add the smaller between requested MIPS and available MIPS: if PE supplied more capacity than requested,
			//we reduce use of processor (and power consumption). Otherwise, we go PE's full power.
			if(getPeList().get(allocatedPe).getMips() < mipsShare.get(i)){
				newMipsList.add((double)getPeList().get(allocatedPe).getMips());
				currentFreeMips-=getPeList().get(allocatedPe).getMips();
			} else {
				newMipsList.add(mipsShare.get(i));
				currentFreeMips-=mipsShare.get(i);
			}
		}

		peAllocationMap.put(vm.getUid(),chosenPEs);
		getMipsMap().put(vm.getUid(),newMipsList);
		setAvailableMips(currentFreeMips);

		return true;
	}


	@Override
	public void deallocatePesForVm(Vm vm) {

		//free Pes
		Vector<Integer> peVector = peAllocationMap.remove(vm.getUid());

		if(peVector==null){
			Log.printLine(this.getClass()+":[Error]: no Pes allocated for this VM.");
			return;
		}

		while(!peVector.isEmpty()){
			Integer element = peVector.remove(0);
			freePesVector.add(element);
		}

		//update use of mips
		double currentMips=getAvailableMips();
		for (double mips: getMipsMap().get(vm.getUid())) {
			currentMips+=mips;
		}
		setAvailableMips(currentMips);
	}

}
