package org.cloudbus.cloudsim.network.datacenter;

public class TaskStage {
	public TaskStage(int type, double data, double time, double stageid,long memory,
			int peer,int vpeer) {
		super();
		this.type = type;
		this.data = data;
		this.time = time;
		this.stageid = stageid;
		this.memory = memory;
		this.peer = peer;
		this.vpeer=vpeer;
	}
	int vpeer;
	int type;//execution, recv, send,
	double data;//data generated or send or recv
	double time;//execution time for this stage
	double stageid;
	long memory;
	int peer;//from whom data needed to be recieved or send
	
}
