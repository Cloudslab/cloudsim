package org.cloudbus.cloudsim.power;

import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;

//THE AIM HERE IS TO UTILIZE THE ALGORITHM TO COME SELECT A VM TO MIGRATE 
//WE CAN USE SARSA OR Q-LEARNING
//THIS CLASS WILL THEREFORE ALSO STORE THE MATRIX THAT WILL STORE THE Q VALUES 

public class Agent {

	private Algorithm algorithm; 
	private List<PowerVm> MigratableVmList;
	private Environment env;
	private Host host;
	
	
	public Agent(Algorithm algorithm, Environment env) {
		this.algorithm = algorithm;
		this.setEnv(env);
		//this.getEnv().initQValues();
	}
	
	public Algorithm getAlgorithm() {
		return this.algorithm;
	}

	public Algorithm setAlgorithm(Algorithm alg) {
		return this.algorithm = alg;
	}
	
	public Environment getEnv() {
		return env;
	}

	public void setEnv(Environment env) {
		this.env = env;
	}
	
	public PowerVm getAction(PowerHost host, List<PowerVm> MigratableVmList, boolean migrateMoreThanOne) {
		setHost(host);

		//System.out.println("here");
		//THIS IS GOING TO BE CALLED EACH TIME THAT A HOST IS OVERRUN 
		
		//THIS IS JUST A PLACEHOLDER TO ALLOW THE FUNCTION TO WORK 
		PowerVm VmToMigrate = null;
		
		
		//SO HERE WE HAVE A LIST OF ALL THE POSSIBLE VM'S WHICH ARE ESSENTIALLY THE POSSIBLE ACTIONS AVAILABLE 
		//SO I THINK NOW WE NEED TO 
		
		if (Algorithm.getSelectedAlgorithm().contentEquals("SARSA")) {
			
			//WE WILL SOMEHOW CALL THE SARSA ALGORITHM AND USE THE SARSA FUNCTIONALITY IN THE ALGORITHM CLASS
			VmToMigrate = algorithm.Migrate(host, MigratableVmList, migrateMoreThanOne);
		}
		else if(Algorithm.getSelectedAlgorithm().contentEquals("Q-Learning")) {
			
			//WE WILL SOMEHOW CALL THE Q-Learning ALGORITHM AND USE THE Q-Learning FUNCTIONALITY IN THE ALGORITHM CLASS
			//Log.printLine(MigratableVmList);
			VmToMigrate = algorithm.Migrate(host, MigratableVmList, migrateMoreThanOne);
		}
		
		//Log.printLine("Are we leaving here with a vm to migrate?");
		//Log.printLine(VmToMigrate.getId());
		return VmToMigrate;
	}

	public void updateQValues(PowerHost host, List<PowerVm> MigratableVmList, PowerVm vm) {
		//WHAT DO WE NEED TO DO HERE?
		//WE NEED TO 
		
		Host oldHost = getHost();
		
		//Log.printLine("VMS:");
		//for(PowerVm p: MigratableVmList) {
			//Log.printLine(p.getId());
		//}
		//Log.printLine("DO WE GET HERE");
		
		if (Algorithm.getSelectedAlgorithm().contentEquals("SARSA")) {
			algorithm.SARSAUpdateQValues(host, MigratableVmList, vm);
		}
		
		else if(Algorithm.getSelectedAlgorithm().contentEquals("Q-Learning")) {
			algorithm.QLearningUpdateQValues(host, MigratableVmList, vm);

		}
		

	}
	
	public void reset() {
		algorithm.reset();
	}

	public Host getHost() {
		return host;
	}

	public void setHost(Host host) {
		this.host = host;
	}
	

	

	
}


//public String getAlgorithm()