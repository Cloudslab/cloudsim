# FAQ #

Here, you find answer to the most recurrent questions in the CloudSim mailing list.




---

[Top](FAQ.md)
## Getting started ##

### 1. What is CloudSim? What does it do and what doesn't it do? ###

CloudSim is a toolkit (library) for simulation of Cloud computing scenarios. It provides basic classes for describing data centers, virtual machines, applications, users, computational resources, and policies for management of diverse parts of the system (e.g., scheduling and provisioning).

These components can be put together for users to evaluate new strategies in utilization of Clouds (policies, scheduling algorithms, mapping and load balancing policies, etc). It can also be used to evaluate efficiency of strategies from different perspectives, from cost/profit to speed up of application execution time. It also supports evaluation of Green IT policies.

The above are some common scenarios we envisioned and that users have been explored. Nevertheless, there is no limit on the utilization you can make from it: classes can be extended or replaced, new policies can be added and new scenarios for utilization can be coded. Think of it as the building blocks for your own simulated Cloud environment.

Therefore, CloudSim is not a ready-to-use solution were you set parameters and collect results for use in your project. Being a library, CloudSim requires that you write a Java program using its components to compose the desired scenario. Nevertheless, CloudSim can be used to build such a ready-to-use solution. see, for example:

Bhathiya Wickremasinghe, Rodrigo N. Calheiros, and Rajkumar Buyya. CloudAnalyst: A CloudSim-based Visual Modeller for Analysing Cloud Computing Environments and Applications. in: Proceedings of the 24th International Conference on Advanced Information Networking and Applications (AINA 2010), 2010.

### 2. Is CloudSim the right tool for my project? ###

CloudSim is a simulator, so it doesn't run any actual software technology. Simulation can be defined as "running a model of a software in a model of hardware". As it's all about models, specific technology details are abstracted. More information about differences between simulation, emulation, and other experiments methodologies can be found in:

Gustedt, J.; Jeannot, E.; and Quinson, M. Experimental methodologies for large-scale systems: a survey, Parallel Processing Letters, World Scientific, 2009, 19(3), 399-418.

Therefore, if the goal of your project is running an actual software that you developed, or plan to develop (a package, an algorithm) in an actual system perhaps you'd rather develop your software and test it in a small-scale data center, containing a few hosts running [Aneka](http://www.manjrasoft.com/products.html).

### 3. What do I need to use CloudSim? How can I install it? ###

The only previous knowledge you need to use CloudSim is basic Java programming (as CloudSim is written in Java) and some knowledge about Cloud computing. Knowledge on programming IDEs like [Eclipse](http://www.eclipse.org/) or [NetBeans](http://netbeans.org/) is also handy, as they simplifies a lot application development tasks. Alternatively, [Ant](http://ant.apache.org/) or [Maven](http://maven.apache.org/) can be used for compilation of applications.

CloudSim does not have to be installed: you just unpack the downloaded package on any directory, add it to your Java classpath and it is ready to be used. If you use Eclipse or NetBeans, you can add CloudSim as a project, and it will be available for further projects created via the IDE.

### 4. How can I learn more about CloudSim? ###

You can start by the examples available in the CloudSim package. They start with simple scenarios and progress to more complex ones. After that, you will have a good idea on how you can put components together to build your own scenarios.

As the basic components are not sufficient for your project, you may start to study the API, so you will be able to use advanced features, and to extend or replace components.

Other source of information is the [CloudSim group](http://groups.google.com/group/cloudsim?pli=1).

### 5. Can CloudSim run my application X? ###

No, CloudSim is a simulator -- it does not run real applications. It is intended to be used for simulating and experimenting with various scheduling and VM allocation algorithms.


---

[Top](FAQ.md)
## CloudSim components, communication, and events ##

### 1. What are the default behavior of components provided in CloudSim package? How can I change them? ###

Datacenter behaves like an IaaS provider: it receives requests for VMs from brokers and create the VMs in hosts.

The Broker provided in CloudSim only submits a list of VMs to be created and schedules Cloudlets sequentially on them. **Broker is provided as an example and it should be replace by your own scheduling policy and/or policy for generation of VM requests and Cloudlets**.

To change default behavior, you can either extend these classes to add the intended behavior, or implement new ones from scratch. In the latter case, these new entities have to extend SimEntity and implement the processEvent(Simevent) method.

### 2. How can I code a periodic behavior to be adopted by entities? ###

This is done by setting an internal event to be set periodically. Upon reception of the event, the handler for it is called, and the desired behavior is implemented in such handler method. Below we show how to do it for Datacenter class. The same steps can be used to enable such behavior in Broker as well.

1. Extend Datacenter

2. Define a new tag to describe periodic event

3. Override processOtherEvent, to detect the periodic event and call a handler for it

4. Implement the handler method. Eventually, this method also schedules the next call for the event.

**Important: your code must contain a condition for stopping generation of internal events, otherwise simulation will never finish.**

```
class NewDatacenter extends Datacenter {

 public static final int PERIODIC_EVENT = 67567; //choose any unused value you want to represent the tag.

  :

 @Override
 protected void processOtherEvent(SimEvent ev) {
   if (ev == null){
     Log.printLine("Warning: "+CloudSim.clock()+": "+this.getName()+": Null event ignored.");
   } else {
     int tag = ev.getTag();
     switch(tag){
       case PERIODIC_EVENT: processPeriodicEvent(ev); break;
       default: Log.printLine("Warning: "+CloudSim.clock()+":"+this.getName()+": Unknown event ignored. Tag:" +tag);
     }
   }
 }

  :

 private void processPeriodicEvent(SimEvent ev) {
    //your code here
     :
   float delay; //contains the delay to the next periodic event
   boolean generatePeriodicEvent; //true if new internal events have to be generated
   if (generatePeriodicEvent) send(getId(),delay,PERIODIC_EVENT,data);
 }

 :

}
```

### 3. How can I create my own type of messages? How to make them be received by other entities? ###

The process is similar to the previous one. First, a new message tag has to be declared somewhere. Then, a handler for this message have to be added in the receiver of the message. The code is similar to the previous, with the exception of the handler, that will not generate the event internally, but instead it will wait for some entity to send the message.


---

[Top](FAQ.md)
## Policies and algorithms ##

### 1. What are the default scheduling policies and how can I change them? ###

CloudSim models scheduling of CPU resources at two levels: Host and VM.

At Host level, the host shares fractions of each processor element (PE) to each VM running on it. Because resources are shared among VMs, this scheduler is called VmScheduler. The scheduler a host uses is a parameter of the Host constructor.

In the VM level, each virtual machine divides the resources received from the host among Cloudlets running on it. Because in this level resources are shared among Cloudlets, this scheduler is called CloudletScheduler. The scheduler a VM uses is a parameter of its constructor.

In both levels, there are two default policies available: the first policy, xSpaceShared (x stands for VmScheduler or CloudletScheduler), required PEs by Cloudlets/VMs are exclusively allocated. It means that if there are more running elements (VMs or Cloudlets) than available PEs, the last elements to arrive wait on a queue until enough resources are free. In the second policy, xTimeShared, fraction of available PEs are shared among running elements, and all the elements run simultaneously.

Policies for VM scheduling and Cloudlet scheduling can be used in any combination. For example, you can use VmSchedulerTimeShared and CloudletSchedulerSpaceShared, or you can use VmSchedulerTimeShared and CloudletSchedulerTimeShared. It is possible even having a host running VMs with different Cloudlet scheduling policies, or a data center with hosts with different VM Scheduling policies.

To define your own policy, you have to extend either VmScheduler or CloudletScheduler, create the methods for deciding sharing of PEs and pass the new class during construction of the relevant object.

### 2. What scheduling decisions should be implemented at VM level and what scheduling decisions should be implemented at broker level? ###

The VmScheduler models the behavior of scheduling at virtual machine level like VMMs such as Xen and VMware. Therefore, if you want to model behavior of this kind of software regarding distribution of resources among VMs running in the same host, this is the place where your new policy sahould be implemented.

Similarly, CloudletScheduler models the behavior of scheduling at operating system level: given a number of applications currently running in the system, how available CPU resources should be divided among them? If you want to model this behavior, CloudletScheduler is the class to be extended.

There is one point that is not considered by either scheduler: given a number of Cloudlets from a broker, which one should execute first? This kind of decision, which represents application-level behavior, should be defined at Broker level.

### 3. What is the default provisioning policy and how can I change it? ###

The provisioning problem consists of defining, among the available hosts in the data center, which one should receive a new machine requested by a user. Provisioning of hosts to VMs in data centers follows a simple strategy where the host with less running VMs receives the next VM. This behavior is defined in the VMAllocationPolicySimple class. To change this behavior, extend VMAllocationPolicy to define the new provisioning behavior, and pass this object in the initialization of Datacenter.

### 4. What class should I modify to implement my algorithm? ###

There are several places in CloudSim where you can implement your algorithm depending on what the algorithm is intended to do. Here are several examples of classes that you may need to modify or extend:

  1. DatacenterBroker -- modifying the way VM provisioning requests are submitted to data centers and the way cloudlets are submitted and assigned to VMs.
  1. VmAllocatonPolicy -- you need to extend this abstract class to implement your own algorithms for deciding which host a new VM should be placed on. You can also implement dynamic VM reallocation algorithms by implementing the optimizeAllocation method, which is called at every time frame and passed with the full set of current VMs in the data center.
  1. VmScheduler -- implementing algorithms for resource allocation to VMs within a single host.
  1. CloudletScheduler -- implementing algorithms for scheduling cloudlets within a single VM.
  1. PowerVmAllocationPolicyMigrationAbstract -- a template class for implementing power-aware dynamic VM consolidation algorithms that use VM live migration to dynamically reallocate VMs at every time frame. The main method to be overridden is optimizeAllocation.


---

[Top](FAQ.md)
## Advanced features ##

### 1. How can I code VM migration inside a data center? ###

VM migrations are triggered inside the data center, by an internal data center event. Therefore, triggering a migration means receiving and processing a VM\_MIGRATION event:

```
send(this.getId(),delay,CloudSimTags.VM_MIGRATE,vm);
```

The `delay` field contains the estimated migration completion time. Therefore, when using it, the method that starts the migration process has to provide estimated completion time. After the delay, the event is received by the data center, which is interpreted as migration completed: therefore, from this time on the VM is available in the destination host.


---

[Top](FAQ.md)
## Getting help ##

### 1. I have a question. What should I do? ###

The first thing you should do is reading this FAQ. As we are periodically adding new answers here, we may find the answer to your problem here.

If your question is not answered here, you should try next previous discussions from [CloudSim group](http://groups.google.com/group/cloudsim?pli=1). Fragments of code that solve typical problems can be found there.

Finally, if nether from the solves your problem, send an e-mail to the discussion group. Please, try to be clear about your question, as it is likely to speed up the answer.

### 2. How long does it take to my question to be answered? ###

We do our best to answer all the questions we receive. Nevertheless, some factors may affect it, such as:

  1. Message has to be forwarded to another developer who is more familiar with the specific subject;
  1. Question is not clear or too generic, and we have first to try to understand it;
  1. Message reports bug or undesirable behavior of code (you should use the [issue tracker](http://code.google.com/p/cloudsim/issues/list) to speed up the process);
  1. Question has been answered previously in either this FAQ or discussion group;
  1. Message has been shadowed by other submission using the same thread. If there were too many of these messages, some of them may be missed. This can be avoided by using specific Subject of the message that clearly describes your problem.

### 3. How do I report bugs, desirable features, unexpected behavior and other issues? ###

Please, use the [issue tracker](http://code.google.com/p/cloudsim/issues/list) for that. This helps to speed up update process. Issues reported in the discussion group may take longer time to be added to the issue tracker.

### 4. Can you implement the specific feature X, required by my project/assignment? ###

Because we are a small team of developers, we can't add support to every scenario envisioned by users. But this is our intention: we provide generic classes and features that can be broadly used, and users develop case-specific behavior. Suggestion for new features that may be useful for significant number of users are welcomed and can be posted in the [issue tracker](http://code.google.com/p/cloudsim/issues/list). Classes and features that are narrow in applicability and are intended to solve specific problems, though, are unlikely to be developed.