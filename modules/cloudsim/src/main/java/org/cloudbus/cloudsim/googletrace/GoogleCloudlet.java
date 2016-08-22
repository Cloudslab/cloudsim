package org.cloudbus.cloudsim.googletrace;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModelFull;

public class GoogleCloudlet extends Cloudlet {

	private double memReq, delay, cpuReq, runtime;

	public GoogleCloudlet(int cloudletId, double length, double submitTime, double runtime,
			double cpuReq, double memReq) {
		super(cloudletId, (long) length, 1, 0, 0, new UtilizationModelFull(), new UtilizationModelFull(),
				new UtilizationModelFull());
		setMemReq(memReq);
		setCpuReq(cpuReq);
		setDelay(submitTime);
		setRuntime(runtime);
	}

	public double getRuntime() {
		return runtime;
	}
	
	public void setRuntime(double runtime) {
		this.runtime = runtime;
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
