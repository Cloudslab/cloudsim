package org.cloudbus.cloudsim.googletrace;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModelFull;

public class GoogleCloudlet extends Cloudlet {

	private double memReq, delay, cpuReq;

	public GoogleCloudlet(int cloudletId, double length, double submitTime,
			double cpuReq, double memReq) {
		super(cloudletId, (long) length, 1, 0, 0, new UtilizationModelFull(), new UtilizationModelFull(),
				new UtilizationModelFull());
		setMemReq(memReq);
		setCpuReq(cpuReq);
		setDelay(submitTime);
	}

	public double getMemReq() {
		return memReq;
	}

	public void setMemReq(double memReq) {
		this.memReq = memReq;
	}

	public double getDelay() {
		return delay;
	}

	public void setDelay(double delay) {
		this.delay = delay;
	}

	public double getCpuReq() {
		return cpuReq;
	}

	public void setCpuReq(double cpuReq) {
		this.cpuReq = cpuReq;
	}
	
}
