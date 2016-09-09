package org.cloudbus.cloudsim.googletrace;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class GoogleHost extends Host implements Comparable<Host> {
	
	private Map<Integer, Double> priorityToInUseMips;
	private Map<Integer, SortedSet<Vm>> priorityToVms;
	
	public GoogleHost(int id, List<? extends Pe> peList, VmScheduler vmScheduler, int numberOfPriorities) {
		super(id, new RamProvisionerSimple(Integer.MAX_VALUE),
				new BwProvisionerSimple(Integer.MAX_VALUE), Integer.MAX_VALUE,
				peList, vmScheduler);
		
		if (numberOfPriorities < 1) {
			throw new IllegalArgumentException("Number of priorities must be bigger than zero.");
		}
		
		setPriorityToVms(new HashMap<Integer, SortedSet<Vm>>());
		setPriorityToInUseMips(new HashMap<Integer, Double>());
		
		// initializing maps
		for (int priority = 0; priority < numberOfPriorities; priority++) {
			getPriorityToVms().put(priority, new TreeSet<Vm>());
			getPriorityToInUseMips().put(priority, new Double(0));
		}
	}
    @Override
    public int compareTo(Host other) {
        /*
		 * If this object has bigger amount of available mips it should be
		 * considered before the other one.
		 */
        int result = (-1) * (new Double(getAvailableMips()).compareTo(new Double(other
                .getAvailableMips())));

        if (result == 0)
            return new Integer(getId()).compareTo(new Integer(other.getId()));

        return result;
    }

    @Override
    public int hashCode() {
        return getId();
    }
    
	public Vm nextVmForPreempting() {
		for (int i = getPriorityToVms().size() - 1; i >= 0; i--) {
			if (!getPriorityToVms().get(i).isEmpty()) {
				return getPriorityToVms().get(i).last();
			}
		}
		return null;
	}
	
	@Override
	public boolean isSuitableForVm(Vm vm) {
		if (getVmScheduler().getAvailableMips() >= vm.getMips()) {
			return true;
		} else {
			GoogleVm gVm = (GoogleVm) vm;
			double mipsFromLessPriorityVms = getMipsInUseByLessPriorityVms(gVm.getPriority());
			return ((getVmScheduler().getAvailableMips() + mipsFromLessPriorityVms ) >= vm.getMips());
		}		
	}
	
	@Override
	public boolean vmCreate(Vm vm) {
		/*
		 * TODO The Host class add the VM into a List. We don't need that list.
		 * We may optimize the code.
		 */
		boolean result = super.vmCreate(vm);
		
		if (result) {
			// updating maps
			GoogleVm gVm = (GoogleVm) vm;
			
			getPriorityToVms().get(gVm.getPriority()).add(gVm);
			double priorityCurrentUse = getPriorityToInUseMips().get(gVm.getPriority()); 
			getPriorityToInUseMips().put(gVm.getPriority(), priorityCurrentUse + gVm.getMips());
		}
		return result;
	}
	
	@Override
	public void vmDestroy(Vm vm) {
		super.vmDestroy(vm);

		// updating maps
		GoogleVm gVm = (GoogleVm) vm;
		
		getPriorityToVms().get(gVm.getPriority()).remove(vm);
		double priorityCurrentUse = getPriorityToInUseMips().get(gVm.getPriority()); 
		getPriorityToInUseMips().put(gVm.getPriority(), priorityCurrentUse - gVm.getMips());
	}

	protected double getMipsInUseByLessPriorityVms(int priority) {
		double currentUse = 0;
		for (int i = priority + 1; i < getPriorityToInUseMips().size(); i++) {
			currentUse += getPriorityToInUseMips().get(i);
		}
		return currentUse;
	}

	public Map<Integer, Double> getPriorityToInUseMips() {
		return priorityToInUseMips;
	}

	protected void setPriorityToInUseMips(
			Map<Integer, Double> priorityToAvailableMips) {
		this.priorityToInUseMips = priorityToAvailableMips;
	}

	public Map<Integer, SortedSet<Vm>> getPriorityToVms() {
		return priorityToVms;
	}

	protected void setPriorityToVms(Map<Integer, SortedSet<Vm>> priorityToVms) {
		this.priorityToVms = priorityToVms;
	}
}
