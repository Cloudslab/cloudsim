package org.cloudbus.cloudsim.auction.vm;

import org.cloudbus.cloudsim.Vm;

/**
 * Represents the abstract VM that is offered by a Datacenter
 * 
 * @author Youness Teimoury
 * Blog: http://youness-teimoury.blogspot.com/
 * Email: youness126@gmail.com
 * Created on 2011/8/25
 * */
public class DatacenterAbstractVm extends Vm {

	private VmCharacteristics vmCharacteristics;

	public DatacenterAbstractVm(double mips, int pesNumber, int ram, long bw,
			long size, VmCharacteristics vmCharacteristics) {
		super(-1, -1, mips, pesNumber, ram, bw, size, vmCharacteristics.getVmm(), null);
		this.setVmCharacteristics(vmCharacteristics);
	}

	private void setVmCharacteristics(VmCharacteristics vmCharacteristics) {
		this.vmCharacteristics = vmCharacteristics;
	}

	public VmCharacteristics getVmCharacteristics() {
		return vmCharacteristics;
	}

	/*TODO I doubt if I should multiply pes number as following or not. The mechanism 
	 * may be changed by cloudlet scheduling policy.
	 * */
	public double getTotalCostPerMI() {
		return this.getVmCharacteristics().getCostPerSecond() / this.getMips() * this.getNumberOfPes();
	}

}