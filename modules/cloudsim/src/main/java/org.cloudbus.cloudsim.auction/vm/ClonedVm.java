package org.cloudbus.cloudsim.auction.vm;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.Vm;

/**
 * VM Allocation Based on Combinatorial Double Auction
 * Represents a VM that is cloned from a base VM because the requested quantity 
 * for base VM was more than one
 * 
 * @author Youness Teimoury
 * Blog: http://youness-teimoury.blogspot.com/
 * Email: youness126@gmail.com
 * Created on 2011/9/16
 */
public class ClonedVm extends Vm {
	private static int ID_COUNTER = -1;
	/**
	 * The parent VM for this fake VM
	 */
	private Vm baseVM;

	public static int getID() {
		ID_COUNTER++;
		return ID_COUNTER;
	}
	
	public Vm getBaseVM() {		
		return this.baseVM;
	}

	@SuppressWarnings("unchecked")
	public ClonedVm(Vm vm) {
		super(ClonedVm.getID(), vm.getUserId(), vm.getMips(), vm.getNumberOfPes(),
				vm.getRam(), vm.getBw(), vm.getSize(), vm.getVmm(), vm.getCloudletScheduler());
		this.baseVM = vm;
		
		/**
		 * Create a new cloudlet scheduler based on baseVM type
		 */
		Class<CloudletScheduler> clazz = (Class<CloudletScheduler>) vm.getCloudletScheduler().getClass();
		CloudletScheduler cloudletScheduler = null;
		try {
			cloudletScheduler = clazz.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		this.setCloudletScheduler(cloudletScheduler);
	}

}