package org.cloudbus.cloudsim.auction.vm;

import org.cloudbus.cloudsim.auction.EntityCharacteristics;

/**
 * Represents the characteristics of a VM
 * 
 * @author Youness Teimoury
 * Blog: http://youness-teimoury.blogspot.com/
 * Email: youness126@gmail.com
 * Created on 2011/8/25
 */
public class VmCharacteristics extends EntityCharacteristics {

	public VmCharacteristics(String architecture, String os, String vmm,
			double timeZone, double costPerSec,
			double costPerMem, double costPerBw, double costPerStorage) {
		super(architecture, os, vmm, timeZone, costPerSec, costPerMem, costPerBw,
				costPerStorage);
	}

}