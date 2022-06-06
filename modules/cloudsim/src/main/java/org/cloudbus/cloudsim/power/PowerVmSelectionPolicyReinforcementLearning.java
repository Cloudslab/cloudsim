
/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;

/**
 * The Reinforcement Learning VM selection policy.
 * 
 * If you are using any algorithms, policies or workload included in the power package, please cite
 * the following paper:
 * 
 * Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 3.0
 */
public class PowerVmSelectionPolicyReinforcementLearning extends PowerVmSelectionPolicy {

	/*
	 * (non-Javadoc)
	 * @see
	 * org.cloudbus.cloudsim.experiments.power.PowerVmSelectionPolicy#getVmsToMigrate(org.cloudbus
	 * .cloudsim.power.PowerHost)
	 */
	
	
	Environment env = new Environment();
	Algorithm alg = new Algorithm("Q-Learning", env);
	Agent agent = new Agent(alg, env);
	boolean runBefore = false; 
	
	//NEED TO SOMEHOW GIVE THE ENVIRONMENT THE HOST
	
	@Override
	public Vm getVmToMigrate(PowerHost host, boolean migrateMoreThanOne) {
		//Log.printLine("We have to get here right?");
		//Log.printLine("Surely we get in here:::::::::::::::::::::::::::::");
		
		if(runBefore==false) {
			env.setHost(host);
			this.runBefore = true;
		}
		
		List<PowerVm> migratableVms = getMigratableVms(host);
		
		//Log.printLine("Next should be null");
		//Log.printLine(migratableVms);
		if(migratableVms == null) {
			//Log.printLine("Should be in here to return null");
			return null;
		}
		//Log.printLine("Migratable VMS Key");
		//Log.printLine(migratableVms);
		PowerVm action = agent.getAction(host, migratableVms, migrateMoreThanOne);

		//Log.printLine("Do we get out of here");
		//Log.printLine(action);
		//HERE WE SHOULD 

		//Here we somehow have to get the choice from the agent 
		//First create the Agent class and put in the Q-Learning code 
		
		//So this will trigger something in the agent 
		
		//Create an agent object and call Agent.getAction()
		
		return action;
	}

	//Host is still the same object except now there should be one less host on it and thus we should be in a new state 
	@Override
	public void updateQValues(PowerHost host, PowerVm vm) {
		//NOW WE JUST NEED TO PUT THE FUNCTIONALITY IN HERE FOR THIS FUNCTION AND DO SOME TESTING WHICH NO DOUBT REVEAL NUMEROUS BUGS 
		//FIRST WE NEED A VARIABLE THAT IS GOING TO STORE THE CURRENT STATE, NEXT STATE AND THE REWARD
		
		//WHAT DO WE NEED TO PASS THIS 
		List<PowerVm> migratableVms = getMigratableVms(host);
		
		//Log.printLine("Host Name");
		//Log.printLine(host.getId());

		agent.updateQValues(host, migratableVms, vm);
		
		
		
	}
	
	@Override
	public String toString() {
		return "ReinforcementLearning";
		
	}
	
	@Override
	public void reset() {
		agent.reset();
	}
	
	//I THINK WE SHOULD CREATE A METHOD HERE THAT WE WILL CALL AFTER MIGRATION 
	//METHOD HERE TO START THE UPDATES
	
	

}
