#CloudSim change log

# Changes from CloudSim 2.1 to CloudSim 3.0 #


## What's new ##

  * **NEW VM SCHEDULER.** VmSchedulerTimeSharedOverSubscription models a scheduler that allows unbounded number of VMS to be deployed in a single VM, regardless its requirements in terms of number of MIPS. Notice that this was the behaviour of VmSchedulerTimeShared in CloudSim 1.0 Beta, but this behaviour had changed in CloudSim 2.0 to accommodate requests with specific amount of MIPS.

  * **NEW DATACENTER NETWORK MODEL.** A internal network model has been added to CloudSim 3.0. It supports definition of switches connecting hosts in arbitrary network topologies. New Vm classes and Cloudlet classes were added to take advantage of this feature without breaking compatibility of older code. This new feature also enables modelling of message-passing applications. They are included in the package 'network.datacenter'.

  * **NEW VM ALLOCATION AND SELECTION POLICIES.** 6 new VM allocation and 4 VM selection policies were added to the power package. To find more details about the policies please refer to the following paper:

> Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive Heuristics for
> Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in Cloud Data Centers", Concurrency
> and Computation: Practice and Experience, ISSN: 1532-0626, Wiley Press, New York, USA, 2011, DOI: 10.1002/cpe.1867

  * **NEW POWER MODELS.** 7 power models of real servers using the data from SPECpower were added to the power package. They are PowerModelSpecPowerHpProLiantMl110G3PentiumD930, PowerModelSpecPowerHpProLiantMl110G4Xeon3040, PowerModelSpecPowerHpProLiantMl110G5Xeon3075, PowerModelSpecPowerIbmX3250XeonX3470, PowerModelSpecPowerIbmX3250XeonX3480, PowerModelSpecPowerIbmX3550XeonX5670, and PowerModelSpecPowerIbmX3550XeonX5675.

  * **NEW WORKLOAD TRACES.** Workload traces from the PlanetLab project were added and used in the examples of the power package. The data have originally been provided as a part of the CoMon project, a monitoring infrastructure for PlanetLab (http://comon.cs.princeton.edu/).

  * **SUPPORT FOR EXTERNAL WORKLOADS.** External workloads written in the Standard Workload Format (SWF, from the Parallel Workload Archive) can be loaded and read by CloudSim. Relevant classes are WorkloadModel and WorkloadFileReader ('util' package).

  * **SUPPORT FOR USER-DEFINED END OF SIMULATION.** Users are now able to specify a given event that causes the simulation to finish. We thanks Gaston Keller for developing this new feature.


## Removed classes ##

  * CloudCoordinator
  * Sensor
  * CloudCoordinator
  * PowerPe
  * Power.PeList


## API changes ##

  * CloudSim.terminateSimulation(long time) was changed to CloudSim.terminateSimulation(double time), to comply with clock field of CloudSim class.
  * PowerModel was moved from PowerPe to PowerHost
  * VM allocation policies in the power package were replaced by a completely new implementation.


## Bugfixes and improvements ##

  * Fixed [issue 20](https://code.google.com/p/cloudsim/issues/detail?id=20): HostDynamicWorkload.updateVmsProcessing() throws NullPointerException.
  * Fixed [issue 19](https://code.google.com/p/cloudsim/issues/detail?id=19): CloudSimTags class should be abstract, not final.
  * Fixed [issue 17](https://code.google.com/p/cloudsim/issues/detail?id=17): "Something is wrong, the VM can's be restored" should be an Exception.
  * Fixed [issue 16](https://code.google.com/p/cloudsim/issues/detail?id=16): cloudletsSubmitted in DatacenterBroker should be protected, not private.
  * Fixed [issue 15](https://code.google.com/p/cloudsim/issues/detail?id=15): Simulation failed by the RAM where it should not fail.
  * Fixed [issue 14](https://code.google.com/p/cloudsim/issues/detail?id=14): Rounding problem in VMSchedulerTimeShared.
  * Fixed [issue 13](https://code.google.com/p/cloudsim/issues/detail?id=13): Output error when setDisableMigrations is set to true.
  * Fixed [issue 12](https://code.google.com/p/cloudsim/issues/detail?id=12): Dynamically created space-shared VMs fail to process cloudlets.
  * Fixed [issue 11](https://code.google.com/p/cloudsim/issues/detail?id=11): Wrong sharing of MIs among VMs by VmSchedulerTimeShared.
  * Fixed [issue 10](https://code.google.com/p/cloudsim/issues/detail?id=10): Access modifier of DatacenterBroker::finishExecution().
  * Fixed [issue 9](https://code.google.com/p/cloudsim/issues/detail?id=9):  Access modifier of DatacenterBroker::cloudletsSubmitted.
  * Fixed [issue 7](https://code.google.com/p/cloudsim/issues/detail?id=7):  Problem in manifests of Maven-generated jar files.
  * Fixed [issue 5](https://code.google.com/p/cloudsim/issues/detail?id=5):  Abrupt termination of the simulation caused by VmAllocationPolicySimple.
  * Fixed [issue 4](https://code.google.com/p/cloudsim/issues/detail?id=4):  Network examples do not load topology file.
  * Fixed [issue 2](https://code.google.com/p/cloudsim/issues/detail?id=2):  Turn the 'future' queue of CloudSim class protected.
  * Fixed [issue 1](https://code.google.com/p/cloudsim/issues/detail?id=1):  VM is created without check on host's storage capacity.
  * The power package and corresponding examples were heavily updated, as well as new PlanetLab workload data were added.
  * Fix in Datacenter: one initial update step was been skipped, what caused malfunctions in the schedulers.
  * Fixed issue affecting all CloudletSchedulers: if updated was less then 1, due to a small timespam, processing was never updated, because it was always rounded to 0.
  * Datacenter: Fixed problem with very short intervals between events.


# Changes from CloudSim 2.0 to CloudSim 2.1 #

  * The project has been migrated to using Apache Maven (http://maven.apache.org/). Maven simplifies java project management by providing various tools and plugins for project building, testing, and packaging, dependency management, etc.
  * The directory structure has been changed to comply with the Maven specification
  * The VmSchedulerTimeSharedWithPriority has been removed as well as the priority field from the Vm class. Users can have the same functionality by using the VmSchedulerTimeShared class and setting the MIPS requirements for VMs. See examples 2 and 3.
  * Bug fixes, refactoring and removing obsolete code.


# Changes from CloudSim 1.0 beta to CloudSim 2.0 #

## What's new ##

  * **NEW SIMULATION CORE**. CloudSim 2.0 does not rely on SimJava to process simulation. Therefore, creation of threads was controlled, race conditions found in CloudSim beta were removed, scalability and performance of CloudSim improved. Moreover, support for dynamic creation and destruction of simulation entities was added.
  * **IMPROVEMENT IN SCHEDULERS**, which enhances accuracy of simulation results.
  * **NEW FEATURES**, including power-aware simulation, federation simulation, and network simulation.
  * **PACKAGE ORGANIZATION CHANGES**, including changes in class names, removal of classes, and changes in interfaces. Next, the main changes that affect CloudSim beta users are summarized.


## Changes in class names ##

The following classes had their names changed to better reflect its functionalities and/or to adhere to naming standards:

  * (previous name) VMScheduler -> (current name) CloudletScheduler
  * (previous name) TimeSharedVMScheduler -> (current name) CloudletSchedulerTimeShared
  * (previous name) SpaceSharedVMScheduler -> (current name) CloudletSchedulerSpaceShared
  * (previous name) VMMAllocationPolicy -> (current name) VmScheduler
  * (previous name) TimeSharedAllocationPolicy -> (current name) VmSchedulerTimeShared
  * (previous name) TimeSpaceSharedAllocationPolicy -> (current name) VmSchedulerOportunisticSpaceShared
  * (previous name) TimeSharedWithPriorityAllocationPolicy -> (current name) VmSchedulerTimeShared
  * (previous name) SpaceSharedAllocationPolicy -> (current name) VmSchedulerTimeSharedWithPriority
  * (previous name) VMProvisioner -> (current name) VmAllocationPolicy
  * (previous name) SimpleVMProvisioner -> (current name) VmAllocationPolicySimple
  * (previous name) DataCenter -> (current name) Datacenter
  * (previous name) PE -> (current name) Pe
  * (previous name) VirtualMachine -> (current name) Vm
  * (previous name) SimpleMemoryProvisioner -> (current name) provisioners.RamProvisionerSimple
  * (previous name) MemoryProvisioner -> (current name) provisioners.RamProvisioner
  * (previous name) BWProvisioner -> (current name) provisioners.BwProvisioner
  * (previous name) SimpleBWProvisioner -> (current name) provisioners.BwProvisionerSimple
  * (previous name) DataCenterTags -> (current name) core.CloudSimTags
  * (previous name) SANStorage -> (current name) HardDriveStorage


## Removed classes ##

Previous classes that implemented list operations were removed from CloudSim. Instead of this classes, users should use standard lists from java.utils

  * CloudletList
  * VirtualMachineList

The following classes were removed because they were redundant:

  * VMCharacteristics


## Interface changes ##

Because dependencies from SimJava were removed, and due to other code optimizations, some classes suffered major changes in their interfaces. The main ones are listed below:

  * Vm (previous VirtualMachine) now receives directly all the relevant VM parameters (before, it was done through a VMCharacteristics object).
  * Simulation entities (Datacenter, FederatedDatacenter, DatacenterBroker) have now simpler constructors. Please, refer to examples or Javadoc for new constructors.


## New features ##

Below there is a list of CloudSim's new features. Please, refer to Javadoc and examples for instructions on how to use this new features.

  * Network effects added through determination of network link latency and bandwidth;
  * Support for simulation of Federation of Data centers (new classes FederatedDatacenter, CloudCoordinator, and Sensor);
  * Support for modeling of energy-aware cloud computing.