package org.cloudbus.cloudsim.googletrace;

import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Vm;

public class GoogleVm extends Vm implements Comparable<GoogleVm> {

	private int priority;
	private double submitTime;

	public GoogleVm(int id, int userId, double cpuReq, double memReq, double submitTime, int priority) {
		super(id, userId, cpuReq, 1, (int) memReq, 0, 0, "default", new CloudletSchedulerTimeShared());

		setSubmitTime(submitTime);
		setPriority(priority);
	}

	@Override
	public int compareTo(GoogleVm otherVm) {
		if (getPriority() < otherVm.getPriority()) {
			return -1;
		} else if (getPriority() > otherVm.getPriority()) {
			return 1;
		} else if (getSubmitTime() < otherVm.getSubmitTime()) {
			return -1;
		} else if (getSubmitTime() == otherVm.getSubmitTime()) {
			return 0;
		}
		return 1;
	}

	public double getSubmitTime() {
		return submitTime;
	}

	public void setSubmitTime(double submitTime) {
		this.submitTime = submitTime;
	}
	
	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}
}
