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
import java.util.Map;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.core.predicates.PredicateType;

/**
 * PowerDatacenter is a class that enables simulation of power-aware data centers.
 * 
 * If you are using any algorithms, policies or workload included in the power package please cite
 * the following paper:
 * 
 * Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class PowerDatacenter extends Datacenter {

	/** The power. */
	private double power;

	/** The disable migrations. */
	private boolean disableMigrations;

	/** The cloudlet submited. */
	private double cloudletSubmitted;

	/** The migration count. */
	private int migrationCount;
	
	private boolean started = false;

	/**
	 * Instantiates a new datacenter.
	 * 
	 * @param name the name
	 * @param characteristics the res config
	 * @param schedulingInterval the scheduling interval
	 * @param utilizationBound the utilization bound
	 * @param vmAllocationPolicy the vm provisioner
	 * @param storageList the storage list
	 * @throws Exception the exception
	 */
	public PowerDatacenter(
			String name,
			DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy,
			List<Storage> storageList,
			double schedulingInterval) throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);

		setPower(0.0);
		setDisableMigrations(false);
		setCloudletSubmitted(-1);
		setMigrationCount(0);
	}
	
	/**
	 * Updates processing of each cloudlet running in this PowerDatacenter. It is necessary because
	 * Hosts and VirtualMachines are simple objects, not entities. So, they don't receive events and
	 * updating cloudlets inside them must be called from the outside.
	 * 
	 * @pre $none
	 * @post $none
	 */
	//IN THIS METHOD WE SIMPLY TAKE ONE OF THE OVER UTILIZED HOSTS AND WORK WITH THAT 
	//
	@Override
	protected void updateCloudletProcessingRL(boolean type) {
		//Log.printLine("This must be called - POWER");
		if (getCloudletSubmitted() == -1 || getCloudletSubmitted() == CloudSim.clock()) {
			CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
			schedule(getId(), 300, CloudSimTags.VM_DATACENTER_EVENT);
			schedule(getId(), 300, CloudSimTags.VM_DATACENTER_EVENT_UNDER);

			return;
		}
		double currentTime = CloudSim.clock();

		// if some time passed since last processing
		if (currentTime > getLastProcessTime()) {
			//System.out.print(currentTime + " ");
			//Log.printLine("Migration Count Incoming: ");
			//Log.printLine(this.getMigrationCount());
			
			double minTime = updateCloudetProcessingWithoutSchedulingFutureEventsForce();

			if (!isDisableMigrations()) {
				//Log.printLine("About to call Optimize Allocation");
				
				if(started==false) {
					//Log.printLine("We should never come back in here");
					List<PowerHostUtilizationHistory> overUtilizedHost = new ArrayList<PowerHostUtilizationHistory>();
					List<PowerHostUtilizationHistory> overUtilizedHostList = getVmAllocationPolicy().getOverUtilizedHosts();

					
					//Log.printLine("In the first important loop");
					List<Map<String, Object>> migrationMap = getVmAllocationPolicy().optimizeAllocationRL(overUtilizedHost, overUtilizedHostList, true);
					started = true;
				}
				
				List<PowerHostUtilizationHistory> overUtilizedHostList = getVmAllocationPolicy().getOverUtilizedHosts();
				
				//Log.printLine("Here is the number of over-utilized hosts");
				//Log.print(overUtilizedHostList.size());

				
				//THEN WE WILL WORK ON THE FIRST HOST
				//Log.printLine("type next");
				//Log.printLine(type);
				if(type==true) {
	
					List<PowerHostUtilizationHistory> overUtilizedHost = new ArrayList<PowerHostUtilizationHistory>();
					
					if(overUtilizedHostList.size() > 0) {
						overUtilizedHost.add(overUtilizedHostList.get(0));
					}

					List<Map<String, Object>> migrationMap = getVmAllocationPolicy().optimizeAllocationRL(overUtilizedHost, overUtilizedHostList, false);
					
					//Log.printLine(migrationMap);
	
					if (migrationMap != null) {
						for (Map<String, Object> migrate : migrationMap) {
							Vm vm = (Vm) migrate.get("vm");
							PowerHost targetHost = (PowerHost) migrate.get("host");
							PowerHost oldHost = (PowerHost) vm.getHost();
	
							if (oldHost == null) {
								//Log.formatLine(
										//"%.2f: Migration of VM #%d to Host #%d is started",
										//currentTime,
										//vm.getId(),
										//targetHost.getId());
							} else {
								//Log.formatLine(
										//"%.2f: Migration of VM #%d from Host #%d to Host #%d is started",
										//currentTime,
										//vm.getId(),
										//oldHost.getId(),
										//targetHost.getId());
							}
	
							targetHost.addMigratingInVm(vm);
							incrementMigrationCount();
	
							/** VM migration delay = RAM / bandwidth **/
							// we use BW / 2 to model BW available for migration purposes, the other
							// half of BW is for VM communication
							// around 16 seconds for 1024 MB using 1 Gbit/s network
							send(
									getId(),
									vm.getRam() / ((double) targetHost.getBw() / (2 * 8000)),
									CloudSimTags.VM_MIGRATE,
									migrate);
						}
					}
				}
	
			
				//WE HAVE NO OVERUTILIZED HOSTS SO WE DO THE UNDER UTILIZED
				else {
						//Log.printLine("Should only enter this every once in a while");
						List<PowerHostUtilizationHistory> overUtilizedHost = new ArrayList<PowerHostUtilizationHistory>();
	
						List<Map<String, Object>> migrationMap = getVmAllocationPolicy().optimizeAllocationRL(overUtilizedHost, overUtilizedHostList, true);
						
						//Log.printLine(migrationMap);
		
						if (migrationMap != null) {
							for (Map<String, Object> migrate : migrationMap) {
								Vm vm = (Vm) migrate.get("vm");
								PowerHost targetHost = (PowerHost) migrate.get("host");
								PowerHost oldHost = (PowerHost) vm.getHost();
		
								if (oldHost == null) {
									//Log.formatLine(
											//"%.2f: Migration of VM #%d to Host #%d is started",
											//currentTime,
											//vm.getId(),
											//targetHost.getId());
								} else {
									//Log.formatLine(
											//"%.2f: Migration of VM #%d from Host #%d to Host #%d is started",
											//currentTime,
											//vm.getId(),
											//oldHost.getId(),
											//targetHost.getId());
								}
		
								targetHost.addMigratingInVm(vm);
								incrementMigrationCount();
		
								/** VM migration delay = RAM / bandwidth **/
								// we use BW / 2 to model BW available for migration purposes, the other
								// half of BW is for VM communication
								// around 16 seconds for 1024 MB using 1 Gbit/s network
								send(
										getId(),
										vm.getRam() / ((double) targetHost.getBw() / (2 * 8000)),
										CloudSimTags.VM_MIGRATE_UNDER,
										migrate);
							}
						}
					}
	
				}
				
				// schedules an event to the next time
				if (minTime != Double.MAX_VALUE) {
					CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
					send(getId(), 30, CloudSimTags.VM_DATACENTER_EVENT);
					if(type==false) {
						send(getId(), 300, CloudSimTags.VM_DATACENTER_EVENT_UNDER);
					}

				}
	
				setLastProcessTime(currentTime);
			}
		}
	


	
	
	
	
	
	/**
	 * Updates processing of each cloudlet running in this PowerDatacenter. It is necessary because
	 * Hosts and VirtualMachines are simple objects, not entities. So, they don't receive events and
	 * updating cloudlets inside them must be called from the outside.
	 * 
	 * @pre $none
	 * @post $none
	 */
	//Over-utilized hosts:
	@Override
	protected void updateCloudletProcessing(boolean type) {
		//Log.printLine("This must be called - POWER");
		if (getCloudletSubmitted() == -1 || getCloudletSubmitted() == CloudSim.clock()) {
			CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
			schedule(getId(), 300, CloudSimTags.VM_DATACENTER_EVENT);
			//schedule(getId(), 300, CloudSimTags.VM_DATACENTER_EVENT_UNDER);			
			return;
		}
		double currentTime = CloudSim.clock();

		// if some time passed since last processing
		if (currentTime > getLastProcessTime()) {
			//System.out.print(currentTime + " ");
			//Log.printLine("Migration Count Incoming: ");
			//Log.printLine(this.getMigrationCount());
			
			double minTime = updateCloudetProcessingWithoutSchedulingFutureEventsForce();

			if (!isDisableMigrations()) {
				//Log.printLine("About to call Optimize Allocation");
				
				if(started==false) {
					//Log.printLine("We should never come back in here");
					List<PowerHostUtilizationHistory> overUtilizedHost = new ArrayList<PowerHostUtilizationHistory>();
					List<PowerHostUtilizationHistory> overUtilizedHostList = getVmAllocationPolicy().getOverUtilizedHosts();

					
					//Log.printLine("In the first important loop");
					List<Map<String, Object>> migrationMap = getVmAllocationPolicy().optimizeAllocation(overUtilizedHostList, false);
					started = true;
				}
				
				List<PowerHostUtilizationHistory> overUtilizedHostList = getVmAllocationPolicy().getOverUtilizedHosts();
				//Log.print(overUtilizedHostList);
				
				//List<Map<String, Object>> migrationMap = null;
				
				int tag = 0;
				
				//if(type==true) {
					//tag = CloudSimTags.VM_MIGRATE;
					//migrationMap = getVmAllocationPolicy().optimizeAllocation(overUtilizedHostList, true);
				//}
				//else {
				//tag = CloudSimTags.VM_MIGRATE;
				//Log.printLine("This is the tag");
				//Log.printLine(tag);
				//Log.printLine(CloudSimTags.VM_MIGRATE_UNDER);
				
				//List<PowerHostUtilizationHistory> overUtilizedHostListEmpty = new ArrayList<PowerHostUtilizationHistory>();
				List<Map<String, Object>> migrationMap = getVmAllocationPolicy().optimizeAllocation(overUtilizedHostList, false);

				//}
				
				//Log.printLine(migrationMap);

				if (migrationMap != null) {
					for (Map<String, Object> migrate : migrationMap) {
						Vm vm = (Vm) migrate.get("vm");
						PowerHost targetHost = (PowerHost) migrate.get("host");
						PowerHost oldHost = (PowerHost) vm.getHost();

						if (oldHost == null) {
							//Log.formatLine(
									//"%.2f: Migration of VM #%d to Host #%d is started",
									//currentTime,
									//vm.getId(),
									//targetHost.getId());
						} else {
							//Log.formatLine(
									//"%.2f: Migration of VM #%d from Host #%d to Host #%d is started",
									//currentTime,
									//vm.getId(),
									//oldHost.getId(),
									//targetHost.getId());
						}

						targetHost.addMigratingInVm(vm);
						incrementMigrationCount();

						/** VM migration delay = RAM / bandwidth **/
						// we use BW / 2 to model BW available for migration purposes, the other
						// half of BW is for VM communication
						// around 16 seconds for 1024 MB using 1 Gbit/s network
						send(
								getId(),
								vm.getRam() / ((double) targetHost.getBw() / (2 * 8000)),
								CloudSimTags.VM_MIGRATE,
								migrate);
					}
				}
			}

			// schedules an event to the next time
			if (minTime != Double.MAX_VALUE) {
				CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
				send(getId(), 300, CloudSimTags.VM_DATACENTER_EVENT);
				
				//if(type==false) {
					//send(getId(), 320, CloudSimTags.VM_DATACENTER_EVENT_UNDER);

				//}
			}

			setLastProcessTime(currentTime);
		}
	}
	
	
	/**
	 * Updates processing of each cloudlet running in this PowerDatacenter. It is necessary because
	 * Hosts and VirtualMachines are simple objects, not entities. So, they don't receive events and
	 * updating cloudlets inside them must be called from the outside.
	 * 
	 * @pre $none
	 * @post $none
	 */
	@Override
	protected void updateCloudletProcessingReinforcementLearning() {
		
		getVmAllocationPolicy().getVmSelectionPolicy().reset();
		
		//WE WANT THIS TO CALL OPTIMIZEALLOCATION BUT ONLY FOR ONE HOST
		
		//1 GET ALL THE HOSTS THAT ARE OVERUTILIZED 
		//2 LOOP OVER THEM 
		//3 PASS THEM TO OPTIMIZE ALLOCATION ONE BY ONE 
		//4 FINISH THE MIGRATION
		//5 GO TO STEP 2
		
		String selectionPolicy = getVmAllocationPolicy().getVmSelectionPolicy().toString();
		PowerVmSelectionPolicyReinforcementLearning pVmPolicy = new PowerVmSelectionPolicyReinforcementLearning();
		String st = "ReinforcementLearning";
	
		//Log.printLine(selectionPolicy.equalsIgnoreCase(st));
		//Log.printLine("Selection Policy Incoming: ");
		//Log.printLine(pVmPolicy);
		
		if(!selectionPolicy.equalsIgnoreCase(st)) {
			updateCloudletProcessing(false);
		}
		else {
		
			if (getCloudletSubmitted() == -1 || getCloudletSubmitted() == CloudSim.clock()) {
				CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
				//Log.printLine("Is this where we start scheduling events?");
				//Log.printLine(getSchedulingInterval());
				schedule(getId(), 300, CloudSimTags.VM_DATACENTER_EVENT);
				return;
			}
			double currentTime = CloudSim.clock();
	
			// if some time passed since last processing
			if (currentTime > getLastProcessTime()) {
				System.out.print(currentTime + " ");
				//Log.printLine("Migration Count Incoming: ");
				//Log.printLine(this.getMigrationCount());
	
				double minTime = updateCloudetProcessingWithoutSchedulingFutureEventsForce();
	
				if (!isDisableMigrations()) {
					
					if(started==false) {
						//Log.printLine("We should never come back in here");
						List<PowerHostUtilizationHistory> overUtilizedHost = new ArrayList<PowerHostUtilizationHistory>();
						List<PowerHostUtilizationHistory> overUtilizedHostList = getVmAllocationPolicy().getOverUtilizedHosts();

						
						//Log.printLine("In the first important loop");
						List<Map<String, Object>> migrationMap = getVmAllocationPolicy().optimizeAllocationRL(overUtilizedHost, overUtilizedHostList, true);
						started = true;
					}
			
			//THE PROBLEM HERE IS THAT THIS IS NOT RETURNING ANY OVERUTILIZED HOSTS 
			//WHY IS THAT?
					
					//Log.printLine("We are in updateCloudletProcessingReinforcementLearning");
		
					
					List<PowerHostUtilizationHistory> overUtilizedHostList = getVmAllocationPolicy().getOverUtilizedHosts();
					
					//Log.printLine("Overutilized host zise");
					//Log.print(overUtilizedHostList.size());
			
					//IF OVERUTILIZED HOSTS ARE EMPTY THEN WE STILL NEED TO CALL THE METHOD SO T WILL LOOK AFTER THE UNDERUTILIZED HOSTS 
					
					if(overUtilizedHostList.size() == 0) {
						//Log.printLine("In with no overutilized hosts");
						
						//List<PowerHostUtilizationHistory> underUtilizedHost = new ArrayList<PowerHostUtilizationHistory>();
						List<PowerHostUtilizationHistory> overUtilizedHost = new ArrayList<PowerHostUtilizationHistory>();

					
						//YEAH WE WILL DO THE UNDERUTILIZED PART WHEN THERE ARE NO OVERUTILIZED HOSTS 
						List<Map<String, Object>> migrationMap = getVmAllocationPolicy().optimizeAllocationRL(overUtilizedHost, overUtilizedHostList, true);
					
						if (migrationMap != null) {
							for (Map<String, Object> migrate : migrationMap) {
								Vm vm = (Vm) migrate.get("vm");
								PowerHost targetHost = (PowerHost) migrate.get("host");
								PowerHost oldHost = (PowerHost) vm.getHost();
		
								if (oldHost == null) {
									//Log.formatLine(
											//"%.2f: Migration of VM #%d to Host #%d is started",
											//currentTime,
											//vm.getId(),
											//targetHost.getId());
								} else {
									//Log.formatLine(
											//"%.2f: Migration of VM #%d from Host #%d to Host #%d is started",
											//currentTime,
											//vm.getId(),
											//oldHost.getId(),
											//targetHost.getId());
								}
		
								targetHost.addMigratingInVm(vm);
								incrementMigrationCount();
								//Number of VM migrations
		
								/** VM migration delay = RAM / bandwidth **/
								// we use BW / 2 to model BW available for migration purposes, the other
								// half of BW is for VM communication
								// around 16 seconds for 1024 MB using 1 Gbit/s network
								send(
										getId(),
										vm.getRam() / ((double) targetHost.getBw() / (2 * 8000)),
										CloudSimTags.VM_MIGRATE_UNDER,
										migrate);
								}	

							}
					}
					
					//Log.printLine("About to print the host that are overutilized that we should be looping over then");
					//Log.printLine(overUtilizedHostList);optimizeAllocationReinforcementLearning
					
					
					else {
						int loopSize = overUtilizedHostList.size();
						int loopMax = loopSize;
						int loopCounter = 1;
						boolean lastHost = false;
						int tag = CloudSimTags.VM_MIGRATE;
						
						for(int i=0; i <= loopMax; i++) {
						//for (PowerHostUtilizationHistory h: overUtilizedHostList) {
							
							//Log.printLine("Should be moving this host");
							//Log.printLine(h.getId());
						
						//FOR EACH HOST WE WANT TO PASS IT TO OPTIMIZE ALLOCATION AND THEN DO THE MIGRATON OF IT 
						//WE WANT THIS TO RETURN A MIGRATION MAP
						//
						
							List<PowerHostUtilizationHistory> overUtilizedHost = new ArrayList<PowerHostUtilizationHistory>();
							
							if(i!=loopMax) {
								overUtilizedHost.add(overUtilizedHostList.get(i));

	
							}
							else {
								//Log.printLine("Only now should we be going in to do the underutilized hosts");
								lastHost = true;	
								tag = CloudSimTags.VM_MIGRATE_UNDER;
							}
							
							//if(loopSize==loopCounter) {
								//lastHost = true;
							//}
						
							//Log.printLine("Do we get to this part just befofe we call optimizeAllocation- Check the following variables lasHost should be True");
							//Log.printLine(lastHost);
							//Log.printLine(loopCounter);
							//Log.printLine(loopSize);
							
							List<Map<String, Object>> migrationMap = getVmAllocationPolicy().optimizeAllocationRL(overUtilizedHost, overUtilizedHostList, lastHost);
							loopCounter+=1;
							
							if (migrationMap != null) {
								for (Map<String, Object> migrate : migrationMap) {
									Vm vm = (Vm) migrate.get("vm");
									PowerHost targetHost = (PowerHost) migrate.get("host");
									PowerHost oldHost = (PowerHost) vm.getHost();
			
									if (oldHost == null) {
										//Log.formatLine(
												//"%.2f: Migration of VM #%d to Host #%d is started",
												//currentTime,
												//vm.getId(),
												//targetHost.getId());
									} else {
										//Log.formatLine(
												//"%.2f: Migration of VM #%d from Host #%d to Host #%d is started",
												//currentTime,
												//vm.getId(),
												//oldHost.getId(),
												//targetHost.getId());
									}
			
									targetHost.addMigratingInVm(vm);
									incrementMigrationCount();
			
									/** VM migration delay = RAM / bandwidth **/
									// we use BW / 2 to model BW available for migration purposes, the other
									// half of BW is for VM communication
									// around 16 seconds for 1024 MB using 1 Gbit/s network
									send(
											getId(),
											vm.getRam() / ((double) targetHost.getBw() / (2 * 8000)),
											tag,
											migrate);
									
	
								}
						
						}
						
						
		
						}
					}
				
				// schedules an event to the next time
				if (minTime != Double.MAX_VALUE) {
					//Log.printLine("DO WE GET TO THIS SECOND SEND EVENT?");
					CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
					send(getId(), getSchedulingInterval(), CloudSimTags.VM_DATACENTER_EVENT);
				}
	
				setLastProcessTime(currentTime);
				}			

			}
		}
		
	}

	/**
	 * Update cloudet processing without scheduling future events.
	 * 
	 * @return the double
	 */
	protected double updateCloudetProcessingWithoutSchedulingFutureEvents() {
		if (CloudSim.clock() > getLastProcessTime()) {
			return updateCloudetProcessingWithoutSchedulingFutureEventsForce();
		}
		return 0;
	}

	/**
	 * Update cloudet processing without scheduling future events.
	 * 
	 * @return the double
	 */
	protected double updateCloudetProcessingWithoutSchedulingFutureEventsForce() {
		double currentTime = CloudSim.clock();
		double minTime = Double.MAX_VALUE;
		double timeDiff = currentTime - getLastProcessTime();
		double timeFrameDatacenterEnergy = 0.0;

		//Log.printLine("\n\n--------------------------------------------------------------\n\n");
		//Log.formatLine("New resource usage for the time frame starting at %.2f:", currentTime);

		for (PowerHost host : this.<PowerHost> getHostList()) {
			//Log.printLine();

			double time = host.updateVmsProcessing(currentTime); // inform VMs to update processing
			if (time < minTime) {
				minTime = time;
			}

			//Log.formatLine(
					//"%.2f: [Host #%d] utilization is %.2f%%",
					//currentTime,
					//host.getId(),
					//host.getUtilizationOfCpu() * 100);
		}

		if (timeDiff > 0) {
			//Log.formatLine(
					//"\nEnergy consumption for the last time frame from %.2f to %.2f:",
					//getLastProcessTime(),
					//currentTime);

			for (PowerHost host : this.<PowerHost> getHostList()) {
				double previousUtilizationOfCpu = host.getPreviousUtilizationOfCpu();
				double utilizationOfCpu = host.getUtilizationOfCpu();
				double timeFrameHostEnergy = host.getEnergyLinearInterpolation(
						previousUtilizationOfCpu,
						utilizationOfCpu,
						timeDiff);
				timeFrameDatacenterEnergy += timeFrameHostEnergy;

				
						
				//Log.printLine();
				/*
				Log.formatLine(
						"%.2f: [Host #%d] utilization at %.2f was %.2f%%, now is %.2f%%",
						currentTime,
						host.getId(),
						getLastProcessTime(),
						previousUtilizationOfCpu * 100,
						utilizationOfCpu * 100);
				Log.formatLine(
						"%.2f: [Host #%d] energy is %.2f W*sec",
						currentTime,
						host.getId(),
						timeFrameHostEnergy);
						*/
						
			}
			
			/*
			Log.formatLine(
					"\n%.2f: Data center's energy is %.2f W*sec\n",
					currentTime,
					timeFrameDatacenterEnergy);
				*/
					
		}
						 	

		setPower(getPower() + timeFrameDatacenterEnergy);

		checkCloudletCompletion();

		/** Remove completed VMs **/
		for (PowerHost host : this.<PowerHost> getHostList()) {
			for (Vm vm : host.getCompletedVms()) {
				getVmAllocationPolicy().deallocateHostForVm(vm);
				getVmList().remove(vm);
				//Log.printLine("VM #" + vm.getId() + " has been deallocated from host #" + host.getId());
			}
		}

		//Log.printLine();

		setLastProcessTime(currentTime);
		return minTime;
	}

	/*
	 * (non-Javadoc)
	 * @see org.cloudbus.cloudsim.Datacenter#processVmMigrate(org.cloudbus.cloudsim.core.SimEvent,
	 * boolean)
	 */
	@Override
	protected void processVmMigrate(SimEvent ev, boolean ack) {
		updateCloudetProcessingWithoutSchedulingFutureEvents();
		super.processVmMigrate(ev, ack);
		SimEvent event = CloudSim.findFirstDeferred(getId(), new PredicateType(CloudSimTags.VM_MIGRATE));
		if (event == null || event.eventTime() > CloudSim.clock()) {
			updateCloudetProcessingWithoutSchedulingFutureEventsForce();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.Datacenter#processCloudletSubmit(cloudsim.core.SimEvent, boolean)
	 */
	@Override
	protected void processCloudletSubmit(SimEvent ev, boolean ack) {
		super.processCloudletSubmit(ev, ack);
		setCloudletSubmitted(CloudSim.clock());
	}

	/**
	 * Gets the power.
	 * 
	 * @return the power
	 */
	public double getPower() {
		return power;
	}

	/**
	 * Sets the power.
	 * 
	 * @param power the new power
	 */
	protected void setPower(double power) {
		this.power = power;
	}

	/**
	 * Checks if PowerDatacenter is in migration.
	 * 
	 * @return true, if PowerDatacenter is in migration
	 */
	protected boolean isInMigration() {
		boolean result = false;
		for (Vm vm : getVmList()) {
			if (vm.isInMigration()) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * Checks if is disable migrations.
	 * 
	 * @return true, if is disable migrations
	 */
	public boolean isDisableMigrations() {
		return disableMigrations;
	}

	/**
	 * Sets the disable migrations.
	 * 
	 * @param disableMigrations the new disable migrations
	 */
	public void setDisableMigrations(boolean disableMigrations) {
		this.disableMigrations = disableMigrations;
	}

	/**
	 * Checks if is cloudlet submited.
	 * 
	 * @return true, if is cloudlet submited
	 */
	protected double getCloudletSubmitted() {
		return cloudletSubmitted;
	}

	/**
	 * Sets the cloudlet submited.
	 * 
	 * @param cloudletSubmitted the new cloudlet submited
	 */
	protected void setCloudletSubmitted(double cloudletSubmitted) {
		this.cloudletSubmitted = cloudletSubmitted;
	}

	/**
	 * Gets the migration count.
	 * 
	 * @return the migration count
	 */
	public int getMigrationCount() {
		return migrationCount;
	}

	/**
	 * Sets the migration count.
	 * 
	 * @param migrationCount the new migration count
	 */
	protected void setMigrationCount(int migrationCount) {
		this.migrationCount = migrationCount;
	}

	/**
	 * Increment migration count.
	 */
	protected void incrementMigrationCount() {
		setMigrationCount(getMigrationCount() + 1);
	}

}
