package org.cloudbus.cloudsim.power;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;

//THE ALGORITHM CLASS WILL HAVE TWO METHODS AND BOTH OF THESE WILL BE Q-LEARNING AND SARSA

public class Algorithm {
	
	private String algorithm;
	private Environment env;
	private Double reward;
	private int currentState;
	private int nextState;
	private PowerVm lastVmMoved;
	private int lastAction;
	private boolean migrateMoreThanOne;
	List<Integer> stateList = new ArrayList<Integer>();
	List<Integer> actionList = new ArrayList<Integer>();
	List<PowerHost> oldHostList = new ArrayList<PowerHost>();
	List<Double> oldPowerList = new ArrayList<Double>();
	List<Double> newPowerList = new ArrayList<Double>();
	List<Double> oldHostUtilizationList = new ArrayList<Double>();
	List<Double> oldVmUtilizationList = new ArrayList<Double>();
	public static int migrationCounter = 0;
	private int testCounter = 1;
	public boolean testVar = false;
	public static double randomActionValue = 0.0;
	public static String actionSelectionPolicy = "";
	public static String selectedAlgorithm = ""; 
	public static double alpha = 0.0;
	public static double gamma = 0.0;

	
	HashMap<PowerVm,Integer> VmActionPair = new HashMap<PowerVm,Integer>();
	HashMap<PowerVm,Integer> VmStatePair = new HashMap<PowerVm,Integer>();
	HashMap<PowerVm,Double> VmHostUtilizationPair = new HashMap<PowerVm,Double>();
	HashMap<PowerVm,Double> VmVmUtilizationPair = new HashMap<PowerVm,Double>();
	HashMap<PowerVm,PowerHost> VmOldHostPair = new HashMap<PowerVm,PowerHost>();
	HashMap<PowerVm,Double> OldPower = new HashMap<PowerVm,Double>();

	HashMap<Pair,Integer> stateActionCounter = new HashMap<Pair,Integer>();
	HashMap<Integer,Integer> stateCounter = new HashMap<Integer,Integer>();
	



	public Algorithm(String algorithm, Environment env) {
		this.setAlgorithm(algorithm);
		this.setEnv(env); 
		//initStateActionCounter();
	}
	
	public static void setMigrationCount(int d) {
		migrationCounter = d;
	}
	
	public static int getMigrationCount() {
		return migrationCounter;
	}
	
	public static void setActionSelectionPolicy(String policy) {
		actionSelectionPolicy = policy;
	}
	
	public static String getActionSelectionPolicy() {
		return actionSelectionPolicy;
	}
	
	public static void setAlpha(double newAlpha) {
		alpha = newAlpha;
	}
	
	public static double getAlpha() {
		return alpha;
	}

	public static void setGamma(double newGamma) {
		gamma = newGamma;
	}
	
	public static double getGamma() {
		return gamma;
	}
	
	public static void setRandomActionValue(double d) {
		randomActionValue = d;
	}
	
	public static double getRandomActionValue() {
		return randomActionValue;
	}
	
	public static void setSelectedAlgorithm(String alg) {
		selectedAlgorithm = alg;
	}
	
	public static String getSelectedAlgorithm() {
		return selectedAlgorithm;
	}
	
	public void initStateActionCounter() {
		
		int numStates = 200;
		int numActions = 100;
		
		for(int i=0; i<numStates; i++){
			
			for(int j=0; j<numActions; j++){
			
				
				Pair<Integer, Integer> entry = new Pair<Integer, Integer>(i, j);
	
				this.stateActionCounter.put(entry, 0);		
				
			}
		}
	}
	
	public void incrementStateCounter(Integer state) {
		
		if(stateCounter.containsKey(state)) {
			int value = stateCounter.get(state);
			int newValue = value + 1;
			stateCounter.replace(state, newValue);
		}
		else {
			stateCounter.put(state, 1);
		}
	
	}
	public void incrementStateActionCounter(Integer state, Integer action) {
		
		Pair<Integer,Integer> key = new Pair<Integer,Integer>(state, action);
		//int value = stateActionCounter.get(key);
		//int newValue = value + 1;
		
		//stateActionCounter.replace(key, value, newValue);
		
		//HERE WE WANT TO CHECK IF THE STATE ACTION PAIR IS IN IT AND IF NOT WE ADD IT 
		
		//IF IT CONTAINS IT WE INCREMENT
		if(stateActionCounter.containsKey(key)) {
			int value = stateActionCounter.get(key);
			int newValue = value + 1;
			stateActionCounter.replace(key, newValue);
			
		}
		//ELSE WE ADD IT TO IT 
		else {
			stateActionCounter.put(key, 1);
		}
		
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}
	
	public boolean getMultipleVmToMigrate() {
		return migrateMoreThanOne;
	}

	public void setMultipleVmToMigrate(boolean migrateMoreThanOne) {
		this.migrateMoreThanOne = migrateMoreThanOne;
	}
	
	public PowerVm SARSA(PowerHost host, List<PowerVm> MigratableVmList) {
		
		
		
		//JUST TO MAKE IT WORK
		PowerVm VmToMigrate = MigratableVmList.get(0);
		return VmToMigrate;
		
	}
	

	
	//SO HERE WE ARE GOING TO IMPLEMENT THE NEW VERSION OF THE Q LEARNING 
	public PowerVm Migrate(PowerHost host, List<PowerVm> MigratableVmList, boolean migrateMoreThanOne) {
		
		//Log.printLine("Entering first Q learning");
		migrationCounter = migrationCounter + 1;
		
		
		double currentTime = CloudSim.clock();

		if(currentTime > 86300) {
			//Log.printLine("Here is the number of over-utilized mgrations::::::::::::::::::::::::::::::::::::::");
			//Log.printLine(migrationCounter);
			
			//Log.printLine("Here are the Q values: ");
			Hashtable<Pair,Double> qvls = this.env.getqValues();
			
	        Enumeration<Pair> e = qvls.keys();
	        
	        //while (e.hasMoreElements()) {
	        	//Pair key = e.nextElement();
				//Log.printLine(key);
				//Log.printLine(qvls.get(key));

	        //}
	        
			//Log.printLine("Number of times the state action pairs are visited::::::::::::::::::::::::::::::::: ");
			
			HashMap<Pair, Integer> q = stateActionCounter;

			
	        Set<Pair> val = q.keySet();
	        
	        //for (Pair key : val) {
	        	//Log.printLine(key);
	        	//Log.printLine(stateActionCounter.get(key));
	        //}
	        
	        HashMap<Integer,Integer> p = stateCounter;
	        
	        Set<Integer> v = p.keySet();
	        
	        //for (Integer key : v) {
	        	//Log.printLine(key);
	        	//Log.printLine(stateCounter.get(key));
	        //}
		}
		
		if(migrateMoreThanOne==false) {
			//WE NEED TO EMPTY BOTH LISTS
			
			//Log.printLine("Are we emptying these lists?");
			stateList.clear();
			actionList.clear();
			oldHostList.clear();
			oldPowerList.clear();
			newPowerList.clear();
			oldHostUtilizationList.clear();
			oldVmUtilizationList.clear();
			//Log.printLine("Start of Test Number::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
			//Log.printLine(testCounter);
			testCounter = testCounter + 1;


		}
		
		//ONLT INCLUDE FOR TESTING 
		//if(testCounter > 100) {
		    //System.exit(0);	
		//}
		
		
		//NOW WE WANT TO ADD THESE TWO
		
		
		double totalEnergy = 0.0;
		List<PowerHost> hostList = host.getDatacenter().getHostList();
		
		for(PowerHost h: hostList) {
			totalEnergy = totalEnergy + h.getPower();
		}
		
		//Log.printLine("Next is the power in first");
		//Log.printLine(totalEnergy);
		
		//oldPowerList.add(totalEnergy);
		oldHostList.add(host);
		
		setMultipleVmToMigrate(migrateMoreThanOne);
		
		
		PowerVm VmToMigrate = null;
		
		//Log.printLine("Migratable VM list");
		
		//for(PowerVm vm: MigratableVmList) {
			//Log.printLine(vm.getId());
		//}

		
		//SO WE NEED TO LOOP THROUGH ALL THE MIGRATABLE VMS
		//FOR EACH ONE WE NEED TO GET THE ACTION VALUE (VM UTILIZATION/ HOST UTILIZATION)
		//THEN FOR EACH WE LOOKUP THE CORRESPONDING Q VALUE FOR THAT ACTION VALUE 
		//THEN WE CHOOSE THE HIGHEST Q VALUE AS THE VM TO MOVE 
		
		List<PowerVm> hostVmList = host.getVmList();

		double migratableVmUtilization = 0.0;
		double totalHostUtilization = 0.0;
		Hashtable<Integer,Integer> actionStates = new Hashtable<Integer,Integer>();
		List<Integer> actions = new ArrayList<Integer>();
		
		//Log.printLine("These are the VM's on the host");
		for(PowerVm vm: hostVmList) {
			totalHostUtilization += vm.getTotalUtilizationOfCpuMips(CloudSim.clock()) / host.getTotalMips();
			//Log.printLine(vm.getId());
		}
				
		
		//Log.printLine("Actions available to us");
		for(PowerVm vm: MigratableVmList) {
			migratableVmUtilization = vm.getTotalUtilizationOfCpuMips(CloudSim.clock()) / host.getTotalMips();
			double actionValue = (migratableVmUtilization/totalHostUtilization) * 100.00;
	        int actionValueInt = (int) Math.round(actionValue);

			actionStates.put(vm.getId(), actionValueInt);
			actions.add(actionValueInt);
			//Log.printLine(actionValueInt);
			
		}
		
		//NOW WE NEED TO GET THE STATE AND THEN LOOKUP THE Q VALUE FOR EACH OF THESE NEW ACTIONS 
		Hashtable<Pair,Double> currentQTable = this.getEnv().getqValues();
		int currentState = this.getEnv().getStateLuke(host);
		//Log.printLine("Current State incoming");
		//Log.printLine(currentState);
		//int currentState = this.getEnv().getStateRachel(host);
		//int currentState = this.getEnv().getStateKieran(host, MigratableVmList);
		


		//setCurrentState(currentState);
		List<Double> qValus = new ArrayList<Double>();
		List<Integer> stateActionPairState = new ArrayList<Integer>();
		List<Integer> stateActionPairAction = new ArrayList<Integer>();
		List<Integer> vmIDs = new ArrayList<Integer>();

		Hashtable<Pair, Double> QValueList = new Hashtable<Pair, Double>(); 

		
		//LETS LOOP OVER ALL THE ACTIONS WE HAVE AVAILABLE TO US
		
		//Log.printLine("Q values for each action incoming");
		int index = 0;
		
		for (Integer d: actions) {
			
			
			Pair<Integer, Integer> pair = new Pair<Integer, Integer>(currentState, d);
			stateActionPairState.add(currentState);
			stateActionPairAction.add(d);
			vmIDs.add(MigratableVmList.get(index).getId());
			
			//NOW LOOKUP THE Q VALUE FOR EACH AND APPEND IT TO A LIST 
			double qValue = currentQTable.get(pair);
			//Log.printLine(qValue);
			qValus.add(qValue);
			QValueList.put(pair, qValue);
			index = index + 1;

		}

		//NOW WE JUST SELECT THE HIGHEST Q VALUE AND THEN TAKE THE VM AS THE HIGHEST Q VALUE AND MIGRATE THAT
		
		
		//NOW WE HAVE A LIST OF ALL THE VM IDS AND TS CORRESPONDING Q VALUES 
		//NOW WE NEED TO CHOOSE THE MAXIMUM AND THE LOOP BACK OVER THEM ALL AND ADD THEM IF THEY ARE EQUAL TO THE MAXIMUM
		
		List<Pair> maxRewardKeys = new ArrayList<Pair>();
		List<Integer> matchingKeyState = new ArrayList<Integer>();
		List<Integer> matchingKeyAction = new ArrayList<Integer>();
		List<Integer> VmIdThatMatch = new ArrayList<Integer>();
		int actionValueToReturn = 0;

		
        Double maxQVal = (Collections.max(QValueList.values())); 
        
        
		//Log.printLine("Current State Incoming:");
		//Log.printLine(currentState);
        //Log.printLine("Max Q value incoming");
        //Log.printLine(maxQVal);
        
        
        for(int i=0; i<stateActionPairState.size(); i++) {
        	//Log.printLine(qValues.get(i));
        	//Log.printLine(maxQVal);
        	if(qValus.get(i).equals(maxQVal)) {
        		//Log.printLine("Matches");
        		VmIdThatMatch.add(vmIDs.get(i));
        		matchingKeyState.add(stateActionPairState.get(i));
        		matchingKeyAction.add(stateActionPairAction.get(i));

        	}
        }
        
        //NOW WE CAN JUST RANDOMLY SELECT ONE 
        
        
        /*
        
        Set<Entry<Pair, Double>> entrySet = QValueList.entrySet();

        int listSize = 0; 
        for (Entry<Pair, Double> entry : entrySet) { 
        	
        	Log.printLine(entry.getValue());
        	Log.printLine(entry.getKey());

            if (entry.getValue() == maxQVal) {
            	
                //ADD THE ENTRY TO THE LIST 
            	//Log.printLine("Essential");
            	//Log.printLine(entry.getKey().getFirst());
            	//Log.printLine(entry.getKey().getSecond());
            	Log.printLine("Other values actually match the maimum Q value");
                maxRewardKeys.add(entry.getKey());
                listSize+=1;
            }
            
        }
        */

        //Log.printLine("IDS");
        //for(int i: VmIdThatMatch) {
        	//Log.printLine(i);
        //}

        
        //NOW WE HAVE A LIST RIGHT AND FIRSTLY WE SHOULD CHECK IF THAT LIST IS BIGGER THAN 1 IN SIZE
        
        //IF THERE ARE MORE THAN ONE VMS WITH THE SAME Q VALUE WE WILL RANDOMLY SELECT ONE
        int actionValueToMigrate;
        
        if(VmIdThatMatch.size() > 1) {

        	//Log.printLine("We select a random Q value as many are the same");
            Random rand = new Random();
            actionValueToMigrate = (int) VmIdThatMatch.get(rand.nextInt(VmIdThatMatch.size()));
            
            for(int i=0; i<matchingKeyAction.size(); i++) {
            	if(VmIdThatMatch.get(i).equals(actionValueToMigrate)) {
            		actionValueToReturn = matchingKeyAction.get(i);
            	}
            }
            
        
        }
        
        //OTHERWISE WE SET THE VM WE ARE RETURNING TO MIGRATE AS THE ONLY ONE IN THE LIST 
        else {
        	
        	//Log.printLine("Select the only q value in the list");
        	actionValueToMigrate = (int) VmIdThatMatch.get(0);
        	actionValueToReturn = matchingKeyAction.get(0);
	
        }

		
        //NOW THAT WE HAVE THE ID OF THE VM WE ARE GOING TO MIGRATE WE NEED TO LOOP THROUGH THE VM LIST AND TAKE THE VM OBJECT THAT MACTHES THE ID 
		List<PowerVm> vmList = new ArrayList<PowerVm>();
		vmList = host.getDatacenter().getVmList();
				
		for(PowerVm Vm : vmList) {
			if(Vm.getId()==actionValueToMigrate) {
				//Log.printLine("Valid VM ID");
				VmToMigrate = Vm;
			}
		}
        
		
		//lastVmMoved = VmToMigrate;
		setLastVmMoved(VmToMigrate);
		this.getEnv().setMovedVm(host.getId(), VmToMigrate.getId());
		this.getEnv().setCurrentStateValue(host.getId(), VmToMigrate.getId(), currentState);
		this.getEnv().setOldHost(VmToMigrate.getId(), host.getId());
		
		//LETS SET THE ACTION AND THE STATE 
		
		//actionList.add(actionValueToReturn);
		//stateList.add(currentState);
		//setLastAction(actionValueToMigrate);
		
		if(migrateMoreThanOne==true) {
			
			//ADD THE ACTION, CURRENT STATE AND 
		}
		
		Random r = new Random();
		int low = 0;
		int high = 100;
		int result = r.nextInt(high-low) + low;
		
		//IF THIS IS TRUE WE TAKE A RANDOM ACTION
		//what do we need to modify?
		//ACTION VALUE TO RETURN 
		//VMTOMIGRATE 
		if(result <= randomActionValue) {
		
			//Log.printLine("About to take a random action");
			//Log.printLine("Stats before we update with random");
			//Log.printLine(actionValueToReturn);
			//Log.printLine(VmToMigrate.getId());
			
			if(actionSelectionPolicy.contentEquals("egreedy")) {
					
					//Log.printLine("Random Action Section::::::::::::::::::::::::::::::::::::::::::");
					//Log.printLine(randomActionValue);
					//Log.printLine(result);
					
					int lowArray = 0;
					int highArray = MigratableVmList.size() - 1;
					int randomVmIndex = r.nextInt(highArray-lowArray) + lowArray;
					VmToMigrate = MigratableVmList.get(randomVmIndex);
					actionValueToReturn = actionStates.get(VmToMigrate.getId());
					//Log.printLine(VmToMigrate.getId());
					//Log.printLine(actionValueToReturn);
				
				}

		
			//SO IF ITS SOFTMAX WE NEED TO SOMEHOW SAY WHICH ONE IS MORE LIKELY 
			//SO WE SCALE THE PROBABILITY BASED ON THE Q VALUE IT HAS 
			else if(actionSelectionPolicy.contentEquals("softmax")) {
				
				//I THINK WE SHOULD LOOP OVER ALL THE ACTIONS AND ADD UP THEIR Q VALUES 
				//THEN THEIR PROBABILITY OF SELECTION IS THEIR Q VALUE OVER THE TOTAL
				
				double qValueTotal = 0.0;
				index = 0;
				
				for (Integer d: actions) {
					
					Pair<Integer, Integer> pair = new Pair<Integer, Integer>(currentState, d);
					qValueTotal = qValueTotal + currentQTable.get(pair);
					index = index + 1;
				}
				
				
				//NOW WE SHOULD GENERATE A RANDOM NUMBER BETWEEN 0 AND THE TOTAL 
				
				double newLow = 0.0;
				double newHigh = qValueTotal;
				double highPositive = Math.abs(newHigh);
				double randomValue = newLow + (highPositive - newLow) * r.nextDouble();
				
				//Log.printLine("Random value being generated");
				//Log.printLine(newHigh);
				//Log.printLine(randomValue);
				
				double valueToCheck = 0.0;
				boolean exit = false;
				int newIndex = 0;
				
				for (Integer d: actions) {
					
					Pair<Integer, Integer> pair = new Pair<Integer, Integer>(currentState, d);
					
					double qValuePostive = Math.abs(currentQTable.get(pair));
					valueToCheck = valueToCheck + qValuePostive;
					//Log.printLine("Value we check for");
					//Log.printLine(valueToCheck);
					
					if(valueToCheck > randomValue && exit==false) {
						
						//NOW WE NEED TO SET THIS ONE AS THE VM 
						VmToMigrate = MigratableVmList.get(newIndex);
						actionValueToReturn = actionStates.get(VmToMigrate.getId());
						exit = true;
					}
					newIndex = newIndex + 1;
				}
				
			}
			
			//Log.printLine("Stats after we update with random");
			//Log.printLine(actionValueToReturn);
			//Log.printLine(VmToMigrate.getId());
			
		}
		
		
		//oldVmUtilizationList.add(VmToMigrate.getTotalUtilizationOfCpuMips(CloudSim.clock()) / host.getTotalMips());
		//oldHostUtilizationList.add(totalHostUtilization);
		VmActionPair.put(VmToMigrate, actionValueToReturn);
		VmStatePair.put(VmToMigrate, currentState);
		VmHostUtilizationPair.put(VmToMigrate, totalHostUtilization); 
		VmVmUtilizationPair.put(VmToMigrate, VmToMigrate.getTotalUtilizationOfCpuMips(CloudSim.clock()) / host.getTotalMips());
		VmOldHostPair.put(VmToMigrate, host);
		OldPower.put(VmToMigrate, totalEnergy);
		
		
		
		incrementStateActionCounter(currentState, actionValueToReturn);
		incrementStateCounter(currentState);
		
		//("Current State");
		//Log.printLine(currentState);
		//Log.printLine("Selected Action");
		//Log.printLine(actionValueToReturn);
		//Log.printLine("VM ID we are moving");
		//Log.printLine(VmToMigrate.getId());
		return VmToMigrate;

	}
	
	public void updateQValue(PowerHost host) {
		//I THINK TO DO SO HERE WE HAVE TO BE IN THE NEXT STATE AND SELECT THE MAX Q VALUE FROM THE NEXT STATE 
		//SO WE NEED TO GET THE NEXT STATE RIGHT AND THEN LOOK AT ALL THE Q VALUES FOR THE VM'S WE COULD MOVE FROM THAT STATE 
		//WE MUST MAKE SURE THE VM'S WE LOOK AT THOUGH ARE STILL ON THE HOST 
		
		//LIST TO STORE THE Q VALUES FOR THE NEXT STATE
		List<Double> newStateQVals = new ArrayList<Double>();
		
		//NOW WE NEED TO LOOP OVER THESE AND THEN WE SELECT THE HIGHEST 
		
		//GET THE LIST OF VM's THAT ARE ON THE HOST - THESE ARE THE ONLY ACTIONS WE CAN TAKE
		List<Vm> hostVmList = host.getVmList();
		
		
		
		
	}

	//ISSUE HERE IS THAT WE ARE PASSING THE NEW HOST INSTEAD OF THE NEW HOST 
	public void SARSAUpdateQValues(PowerHost host, List<PowerVm> migratableVmList, PowerVm vm) {
		
		
		//THIS IS GOING TO BE VERY SIMILAR TO THE Q LEARNING ALGORITHM EXCEPT HERE WE ARE GOING TO AGAIN SELECT A NEW ACTION FROM THE POLICY AS WE DID IN THE FIRST PART 
		//WE WILL LOOK UP THAT Q VALUE FOR THE NEW STATE AND SELECTED ACTION FROM THAT STATE TO UPDATE THE OLD Q VALUE 
		
		//WHAT DO WE NEED HERE?
		//1) OLD STATE 
		//2) OLD ACTION
		//3) REWARD FOR THE ACTION WE JUST TOOK 
		//4) NEW VM WE WOULD MIGRATE BUT WE JUST TAKE ITS ACTION VALUE WE DO NOT MIGRATE IT 
		
		
		//Host here is the old host so what about the new host
		
		//Log.printLine("Entering second Q learning");
		//Log.printLine("VM ID we moved");
		//Log.printLine(vm.getId());
		
		int previousAction = VmActionPair.get(vm);
		int previousState = VmStatePair.get(vm);
		//double previousHostUtilization = VmHostUtilizationPair.get(vm);
		//double previousVmUtilization = VmVmUtilizationPair.get(vm);
		PowerHost oldHost = VmOldHostPair.get(vm);
		//double oldPower = OldPower.get(vm);
		
		migratableVmList = PowerVmSelectionPolicy.getMigratableVmsNew(oldHost);

		
		//Log.printLine(previousAction);
		//Log.printLine(previousState);
		//Log.printLine(previousHostUtilization);
		//Log.printLine(previousVmUtilization);

		
		VmActionPair.remove(vm);
		VmStatePair.remove(vm);
		VmHostUtilizationPair.remove(vm);
		VmVmUtilizationPair.remove(vm);
		VmOldHostPair.remove(vm);
		OldPower.remove(vm);
		
		//NOW WE NEED TO LOOKUP ALL THE THINGS WE JUST ADDED AND THEN REMOVE THEM FROM THE LIST

		
		if(migratableVmList == null) {
			//Log.printLine("Should be in here to return - update method");
			return;
		}
		

		double totalEnergy = 0.0;
		List<PowerHost> hostList = host.getDatacenter().getHostList();
		
		for(PowerHost h: hostList) {
			totalEnergy = totalEnergy + h.getPower();
		}
		
		//Log.printLine("Next is the power in second");
		//Log.printLine(totalEnergy);
		
		newPowerList.add(totalEnergy);
		
		//I THINK TO DO SO HERE WE HAVE TO BE IN THE NEXT STATE AND SELECT THE MAX Q VALUE FROM THE NEXT STATE 
		//SO WE NEED TO GET THE NEXT STATE RIGHT AND THEN LOOK AT ALL THE Q VALUES FOR THE VM'S WE COULD MOVE FROM THAT STATE 
		//WE MUST MAKE SURE THE VM'S WE LOOK AT THOUGH ARE STILL ON THE HOST 
		
		//LIST TO STORE THE Q VALUES FOR THE NEXT STATE
		//List<Double> newStateQVals = new ArrayList<Double>();
		
		//NOW WE NEED TO LOOP OVER THESE AND THEN WE SELECT THE HIGHEST 
		
		//GET THE LIST OF VM's THAT ARE ON THE HOST - THESE ARE THE ONLY ACTIONS WE CAN TAKE
		List<PowerVm> hostVmList = oldHost.getVmList();
		
		
		
		double reward = this.getEnv().getRewardLuke(previousAction);


		int nextState = this.getEnv().getStateLukeNew(previousState, previousAction);
		
		//Log.printLine("Next state incoming");
		//Log.printLine(nextState);
		//Log.printLine("Previous state incoming");
		//Log.printLine(previousState);

		setNextState(nextState);
		
		//NOW THAT WE HAVE THE NEXT STATE WE NEED TO CHOOSE THE HIGHEST Q VALUE FROM THAT
		//NEXT STEPS ARE 
		//WE NEED TO LOOP THROUGH ALL THE MIGRATABLE VMS AND GET THEIR ACTION VALUES 
		//THEN WE NEED TO GET THE CORRESPONDING Q VALUES FOR EACH (NEXT STATE, ACTION VALUE) PAIR
		//THEN FROM THIS WE SELECT THE BEST 
		
		
		
		//SO THIS WILL LOOP THROUGH ALL OF THE POSSIBLE VMS WE CAN MOVE AND SELECT THE 

		double migratableVmUtilization = 0.0;
		double totalHostUtilization = 0.0;
		
		Hashtable<Integer,Integer> actionStates = new Hashtable<Integer,Integer>();
		List<Integer> actions = new ArrayList<Integer>();
		
		//Log.printLine("These are the VM's on the host");
		for(PowerVm v: hostVmList) {
			totalHostUtilization += v.getTotalUtilizationOfCpuMips(CloudSim.clock()) / host.getTotalMips();
			//Log.printLine(vm.getId());
		}
				
		
		//Log.printLine("Actions available to us");
		for(PowerVm v: migratableVmList) {
			migratableVmUtilization = v.getTotalUtilizationOfCpuMips(CloudSim.clock()) / host.getTotalMips();
			double actionValue = (migratableVmUtilization/totalHostUtilization) * 100.00;
	        int actionValueInt = (int) Math.round(actionValue);

			actionStates.put(v.getId(), actionValueInt);
			actions.add(actionValueInt);
			//Log.printLine(actionValueInt);
			
		}	
		
		//NOW WE WANT TO SELECT THE MAXIMUM Q VALUE 
		//TO DO THAT WE WILL LOOP THROUGH THE NEXTSTATE ACTIONS PAIRS 
		
		Hashtable<Pair, Double> QValueList = new Hashtable<Pair, Double>(); 
		Hashtable<Pair,Double> currentQTable = this.getEnv().getqValues();
		List<Integer> stateActionPairState = new ArrayList<Integer>();
		List<Integer> stateActionPairAction = new ArrayList<Integer>();
		List<Integer> vmIDs = new ArrayList<Integer>();
		List<Double> qValus = new ArrayList<Double>();

		int index = 0;

		//Log.printLine("Actions");
		
		//LETS LOOP THROUGH THE STATE ACTIONS PAIRINGS AND LOOKUP THE Q VALUES FOR THESE
		for (Integer d: actions) {

			//Log.printLine(d);
			//Log.printLine(nextState);
			Pair<Integer, Integer> pair = new Pair<Integer, Integer>(nextState, d);
			stateActionPairState.add(nextState);
			stateActionPairAction.add(d);
			
			vmIDs.add(migratableVmList.get(index).getId());
			
			//NOW LOOKUP THE Q VALUE FOR EACH AND APPEND IT TO A LIST 
			double qValue = currentQTable.get(pair);
			//Log.printLine(actions);
			qValus.add(qValue);
			QValueList.put(pair, qValue);
			index = index + 1;	
		}
		
        Double maxQVal = (Collections.max(QValueList.values())); 

		
		List<Integer> matchingKeyState = new ArrayList<Integer>();
		List<Integer> matchingKeyAction = new ArrayList<Integer>();
		List<Integer> VmIdThatMatch = new ArrayList<Integer>();
		int actionValueToReturn = 0;

		        
        for(int i=0; i<stateActionPairState.size(); i++) {
        	//Log.printLine(qValues.get(i));
        	//Log.printLine(maxQVal);
        	if(qValus.get(i).equals(maxQVal)) {
        		//Log.printLine("Matches");
        		VmIdThatMatch.add(vmIDs.get(i));
        		matchingKeyState.add(stateActionPairState.get(i));
        		matchingKeyAction.add(stateActionPairAction.get(i));

        	}
        }
        
        
        //IF THERE ARE MORE THAN ONE VMS WITH THE SAME Q VALUE WE WILL RANDOMLY SELECT ONE
        int actionValueToMigrate;
        
        if(VmIdThatMatch.size() > 1) {

        	//Log.printLine("We select a random Q value as many are the same");
            Random rand = new Random();
            actionValueToMigrate = (int) VmIdThatMatch.get(rand.nextInt(VmIdThatMatch.size()));
            
            for(int i=0; i<matchingKeyAction.size(); i++) {
            	if(VmIdThatMatch.get(i).equals(actionValueToMigrate)) {
            		actionValueToReturn = matchingKeyAction.get(i);
            	}
            }
            
        
        }
        
        //OTHERWISE WE SET THE VM WE ARE RETURNING TO MIGRATE AS THE ONLY ONE IN THE LIST 
        else {
        	
        	//Log.printLine("Select the only q value in the list");
        	actionValueToMigrate = (int) VmIdThatMatch.get(0);
        	actionValueToReturn = matchingKeyAction.get(0);
	
        }
        
        PowerVm VmToMigrate = null;

		
        //NOW THAT WE HAVE THE ID OF THE VM WE ARE GOING TO MIGRATE WE NEED TO LOOP THROUGH THE VM LIST AND TAKE THE VM OBJECT THAT MACTHES THE ID 
		List<PowerVm> vmList = new ArrayList<PowerVm>();
		vmList = host.getDatacenter().getVmList();
				
		for(PowerVm Vm : vmList) {
			if(Vm.getId()==actionValueToMigrate) {
				//Log.printLine("Valid VM ID");
				VmToMigrate = Vm;
			}
		}
        
		
		//lastVmMoved = VmToMigrate;
		setLastVmMoved(VmToMigrate);
		this.getEnv().setMovedVm(host.getId(), VmToMigrate.getId());
		this.getEnv().setCurrentStateValue(host.getId(), VmToMigrate.getId(), currentState);
		this.getEnv().setOldHost(VmToMigrate.getId(), host.getId());
		
		//LETS SET THE ACTION AND THE STATE 
		
		//.add(actionValueToReturn);
		//stateList.add(currentState);
		//setLastAction(actionValueToMigrate);
		
		if(migrateMoreThanOne==true) {
			
			//ADD THE ACTION, CURRENT STATE AND 
		}
		
        
		double oldQValue = env.lookupQValue(previousState, previousAction);

        //NOW WE HAVE OUR CHOSEN ACTION WE NEED THE FOLLOWING 
		// Q VALUE FOR NEXT STATE, NEW ACTION
		// OLD Q VALUE 
		//
		//
		
		//double alpha = 0.5;
		//double gamma = 0.4;
		
        double newQVal = oldQValue + (alpha * (reward + (gamma * (maxQVal)) - oldQValue));

        env.updateqValue(previousState, previousAction, newQVal);

	}
	
	public void QLearningUpdateQValues(PowerHost host, List<PowerVm> migratableVmList, PowerVm vm) {
		
		
		//WE SHOULD RETURN ALL THE TIMES WHERE ITS NOT A OVER MIGRATION 
		if(!VmActionPair.containsKey(vm)) {
			return;
		}
		
		//Host here is the old host so what about the new host
		
		//Log.printLine("Entering second Q learning");
		//Log.printLine("VM ID we moved");
		//Log.printLine(vm.getId());
		
		int previousAction = VmActionPair.get(vm);
		int previousState = VmStatePair.get(vm);
		//double previousHostUtilization = VmHostUtilizationPair.get(vm);
		//double previousVmUtilization = VmVmUtilizationPair.get(vm);
		//PowerHost oldHost = VmOldHostPair.get(vm);
		//double oldPower = OldPower.get(vm);
		
		//Log.printLine(previousAction);
		//Log.printLine(previousState);
		//Log.printLine(previousHostUtilization);
		//Log.printLine(previousVmUtilization);

		
		VmActionPair.remove(vm);
		VmStatePair.remove(vm);
		VmHostUtilizationPair.remove(vm);
		VmVmUtilizationPair.remove(vm);
		VmOldHostPair.remove(vm);
		OldPower.remove(vm);
		
		//NOW WE NEED TO LOOKUP ALL THE THINGS WE JUST ADDED AND THEN REMOVE THEM FROM THE LIST

		
		if(migratableVmList == null) {
			//Log.printLine("Should be in here to return - update method");
			return;
		}
		

		double totalEnergy = 0.0;
		List<PowerHost> hostList = host.getDatacenter().getHostList();
		
		for(PowerHost h: hostList) {
			totalEnergy = totalEnergy + h.getPower();
		}
		
		//Log.printLine("Next is the power in second");
		//Log.printLine(totalEnergy);
		
		//newPowerList.add(totalEnergy);
		
		//I THINK TO DO SO HERE WE HAVE TO BE IN THE NEXT STATE AND SELECT THE MAX Q VALUE FROM THE NEXT STATE 
		//SO WE NEED TO GET THE NEXT STATE RIGHT AND THEN LOOK AT ALL THE Q VALUES FOR THE VM'S WE COULD MOVE FROM THAT STATE 
		//WE MUST MAKE SURE THE VM'S WE LOOK AT THOUGH ARE STILL ON THE HOST 
		
		//LIST TO STORE THE Q VALUES FOR THE NEXT STATE
		List<Double> newStateQVals = new ArrayList<Double>();
		
		//NOW WE NEED TO LOOP OVER THESE AND THEN WE SELECT THE HIGHEST 
		
		//GET THE LIST OF VM's THAT ARE ON THE HOST - THESE ARE THE ONLY ACTIONS WE CAN TAKE
		List<Vm> hostVmList = host.getVmList();
		
		
		
		/*
		
		
		int oldHostIndex = oldHostList.size() - 1;
		
		//double reward = this.getEnv().getReward(host, vm);
		int indexOldPowerList = oldPowerList.size() - 1;
		int indexNewPowerList = newPowerList.size() - 1;
		int indexOldHostUtilizationList = oldHostUtilizationList.size() - 1;
		int indexOldVmUtilizationList = oldVmUtilizationList.size() - 1;

		//double reward = this.getEnv().getReward(host, vm);
		//double reward = this.getEnv().getRewardPower(oldPower, totalEnergy);
		//Log.printLine(oldHost.getId());
		double reward = this.getEnv().getRewardLuke(previousAction);
		//double reward = this.getEnv().getRewardState(oldHost);

		
		oldHostUtilizationList.remove(indexOldHostUtilizationList);
		oldVmUtilizationList.remove(indexOldVmUtilizationList);
		oldPowerList.remove(indexOldPowerList);
		newPowerList.remove(indexNewPowerList);
		
		
		*/
		
		
		double reward = this.getEnv().getRewardLuke(previousAction);

		
		//Log.printLine("Host and VM after migration coming up");
		//Log.printLine(host.getId());
		//Log.printLine(vm.getId());
		
		//Log.printLine("Previous action we took");
		//Log.printLine(previousAction);
		
		//int nextState = this.getEnv().getStateLuke(oldHost);
		//int nextState = this.getEnv().getStateRachel(host);
		//int nextState = this.getEnv().getStateKieran(host, migratableVmList);
		int nextState = this.getEnv().getStateLukeNew(previousState, previousAction);

		//Log.printLine("Next state incoming");
		//Log.printLine(nextState);
		//Log.printLine("Previous state incoming");
		//Log.printLine(previousState);

		setNextState(nextState);
		
		//int index = stateList.size() - 1;
		//int currentState = stateList.get(index);
		//int action = actionList.get(index);
		
		//actionList.remove(index);
		//stateList.remove(index);
		
		
		//SO NOW WE HAVE THE CURRENT STATE, NEXT STATE AND THE REWARD OBSERVED
		//WE NEED TO LOOP THROUGH THE Q VALUE TABLE FOR THE NEXT STATE AND SELECT THE MAXIMUM Q VALUE 
		
		//NOW GET THE MAX Q VALUE FROM THE NEW STATE 
		
		List<Double> nextStateQvals = new ArrayList<Double>();

		
		//THIS SHOULD BE NEXT STATE AND ALL THE ACTONS NO?
		//SO WE SHOULD BE LOOPING THROUGH ALL THE POSSIBLE ACTIONS FOR THE NET STATE
		//WE NEED TO GET THE LIST OF VM IDS 
		
		List<Integer> uniqueIDS = env.getUniqueIDS();
		

		for(int i=0; i<100; i++) {
			//Log.printLine(i);
			nextStateQvals.add(env.lookupQValue(nextState, i));

		}
		
		
		//NOW WE NEED TO GET THE MAX
		
		double maxQValNextState = Collections.max(nextStateQvals);
		
		//Log.printLine("Next state maximum Q value incoming");
		//Log.printLine(maxQValNextState);
		
		//NOW WE HAVE ALL THE INFORMATION WE NEED EXCEPT ALPHA AND GAMMA AND THE OLD Q VALUE 
		//OLD Q VALUE IS THE OLD STATE AND THE ACTION WE TOOK 
		
		//double alpha = 0.5;
		//double gamma = 0.4;
		
		//LAST ACTION NEEDS TO BE CHANGED TOO 
		//int lastAction = getLastVmMoved().getId();
		
		//int oldHostofVm = this.getEnv().getOldHost(vm.getId());
	
		//int action = this.getEnv().getMovedVm(oldHostofVm);
		//int action = getLastAction();
		
		//WE NEED TO CHANGE THIS LAST ACTION TO BE THE ACTION AND NOT THE VM
		
		
		
		//WE NEED TO PUT SOMETHING IN HERE SO WE CAN GET THE VM WE MOVED 
		//WE NEED ACCESS TO THE OLD HOST HERE
		
		
		//currentState = this.getEnv().lookupCurrentStateValue(oldHostofVm, action);
		//currentState = getCurrentState();
		
		//Log.printLine(previousState);
	
		double oldQValue = env.lookupQValue(previousState, previousAction);
		//Log.printLine("Old Q value");


        double newQVal = oldQValue + (alpha * (reward + (gamma * (maxQValNextState)) - oldQValue));
        
        //Log.print("New Q value");
        //Log.printLine(newQVal);
        	
        env.updateqValue(previousState, previousAction, newQVal);
        
	
        //System.exit(0);

		
	}
	
	public void reset() {
		this.getEnv().reset();
	}
	
	public Environment getEnv() {
		return env;
	}

	public void setEnv(Environment env) {
		this.env = env;
	}

	public Double getReward() {
		return reward;
	}

	public void setReward(Double reward) {
		this.reward = reward;
	}

	public int getCurrentState() {
		return currentState;
	}

	public void setCurrentState(int currentState) {
		this.currentState = currentState;
	}

	public int getNextState() {
		return nextState;
	}

	public void setNextState(int nextState) {
		this.nextState = nextState;
	}
	
	public PowerVm getLastVmMoved() {
		return lastVmMoved;
	}

	public void setLastVmMoved(PowerVm lastVmMoved) {
		this.lastVmMoved = lastVmMoved;
	}

	public int getLastAction() {
		return lastAction;
	}

	public void setLastAction(int lastAction) {
		this.lastAction = lastAction;
	}


}


