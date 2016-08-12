package org.cloudbus.cloudsim.googletrace;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;

public class GoogleCloudlet extends Cloudlet {

	private double memReq, delay, cpuReq;
	
	public GoogleCloudlet(int cloudletId, long cloudletLength, int pesNumber,
			long cloudletFileSize, long cloudletOutputSize,
			UtilizationModel utilizationModelCpu,
			UtilizationModel utilizationModelRam,
			UtilizationModel utilizationModelBw, double cpuReq, double memReq, double delay) {
		super(cloudletId, cloudletLength, pesNumber, cloudletFileSize,
				cloudletOutputSize, utilizationModelCpu, utilizationModelRam,
				utilizationModelBw);
		setMemReq(memReq);
		setCpuReq(cpuReq);
		setDelay(delay);
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
