package org.cloudbus.cloudsim.googletrace;

public class GoogleTaskState {

	private int taskId;
	private int resourceId;
	private double cpuReq;
	private double startTime;
	private double runtime;
	private double submitTime;
	private int status;
	private double finishTime;

	public GoogleTaskState(int taskId, int resourceId, double cpuReq,
			double submitTime, double startTime, double finishTime, double runtime,
			int status) {
		this.taskId = taskId;
		this.resourceId = resourceId;
		this.cpuReq = cpuReq;
		this.submitTime = submitTime;
		this.startTime = startTime;
		this.finishTime = finishTime;
		this.runtime = runtime;
		this.status = status;
	}

	public int getTaskId() {
		return taskId;
	}

	public int getResourceId() {
		return resourceId;
	}

	public double getCpuReq() {
		return cpuReq;
	}

	public double getStartTime() {
		return startTime;
	}

	public double getRuntime() {
		return runtime;
	}

	public double getSubmitTime() {
		return submitTime;
	}

	public int getStatus() {
		return status;
	}

	public double getFinishTime() {
		return finishTime;
	}
	
}
