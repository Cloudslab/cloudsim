# CloudSim: A Framework For Modeling And Simulation Of Cloud Computing Infrastructures And Services #

Cloud Computing is the leading approach for delivering reliable, secure, fault-tolerant, sustainable, and scalable computational services. Hence timely, repeatable, and controllable methodologies for performance evaluation of new cloud applications and policies before their actual development are required. Because utilization of real testbeds limits the experiments to the scale of the testbed and makes the reproduction of results an extremely difficult undertaking, simulation may be used.

CloudSim's goal is to provide a generalized and extensible simulation framework that enables modeling, simulation, and experimentation of emerging Cloud Computing infrastructures and application services, allowing its users to focus on specific system design issues that they want to investigate, without getting concerned about the low level details related to Cloud-Based infrastructures and services.

CloudSim is developed in [the Cloud Computing and Distributed Systems (CLOUDS) Laboratory](http://cloudbus.org/), at [the Computer Science and Software Engineering Department](http://www.csse.unimelb.edu.au/) of [the University of Melbourne](http://www.unimelb.edu.au/).

More information can be found on the [CloudSim's web site](http://cloudbus.org/cloudsim/).


# Main features #

  * Support for modeling and simulation of large scale Cloud Computing data centers
  * Support for modeling and simulation of virtualized server hosts, with customizable policies for provisioning host resources to Virtual Machines
  * Support for modeling and simulation of application containers
  * Support for modeling and simulation of energy-aware computational resources
  * Support for modeling and simulation of data center network topologies and message-passing applications
  * Support for modeling and simulation of federated clouds
  * Support for dynamic insertion of simulation elements, stop and resume of simulation
  * Support for user-defined policies for allocation of hosts to Virtual Machines and policies for allocation of host resources to Virtual Machines


# Download #

Either clone the repository or download a [release](https://github.com/Cloudslab/cloudsim/releases). The release package contains all the source code, examples, jars, and API html files.

# Installation
**Windows**
1) Install Java JDK21 on your system from the [official website](https://www.oracle.com/in/java/technologies/downloads/#java21) as shown in [JDK installation instructions](https://docs.oracle.com/en/java/javase/23/install/overview-jdk-installation.html)
2) Install Maven as shown on the [official website](https://maven.apache.org/install.html)
4) Compile and Run tests using the command prompt:
  ```prompt
  mvn clean package
  ```
5) Run an example (e.g., CloudSimExample1) in cloudsim-examples using the command prompt:
```prompt
mvn exec:java -pl modules/cloudsim-examples/ -Dexec.mainClass=org.cloudbus.cloudsim.examples.CloudSimExample1
```

**Linux**
  1) Install Java JDK21 on your system:
  - On Debian-based Linux & Windows WSL2: 
    ```bash
    sudo apt install openjdk-21-jdk
    ```
  - On Red Hat-based Linux:  
    ```bash  
    sudo yum install java-21-openjdk
    ```
  2) Set Java JDK21 as default: 
  - On Debian-based Linux & Windows WSL2:
    ```bash
    sudo update-java-alternatives --set java-1.21.0-openjdk-amd64
    ```
  - On Red Hat-based Linux: 
    ```bash
    sudo update-alternatives --config 'java'
    ```
  3) Install Maven as shown on the [Official Website](https://maven.apache.org/install.html)
  4) Compile and run tests using the terminal:
  ```bash
  mvn clean package
  ```
  5) Run an example (e.g., CloudSimExample1) in cloudsim-examples using the terminal:
  ```bash
  mvn exec:java -pl modules/cloudsim-examples/ -Dexec.mainClass=org.cloudbus.cloudsim.examples.CloudSimExample1
  ```

  **Suggestion:** Use an IDE such as IDEA Intellij to faciliate steps 4) and 5)

# Preferred Publication #
  * Remo Andreoli, Jie Zhao, Tommaso Cucinotta, and Rajkumar Buyya, [CloudSim 7G: An Integrated Toolkit for Modeling and Simulation of Future Generation Cloud Computing Environments](https://onlinelibrary.wiley.com/doi/10.1002/spe.3413), Software: Practice and Experience, 2025.
    
# Publications (Legacy) #

  * Anton Beloglazov, and Rajkumar Buyya, [Optimal Online Deterministic Algorithms and Adaptive Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in Cloud Data Centers](http://beloglazov.info/papers/2012-optimal-algorithms-ccpe.pdf), Concurrency and Computation: Practice and Experience, Volume 24, Number 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012.
  * Saurabh Kumar Garg and Rajkumar Buyya, [NetworkCloudSim: Modelling Parallel Applications in Cloud Simulations](http://www.cloudbus.org/papers/NetworkCloudSim2011.pdf), Proceedings of the 4th IEEE/ACM International Conference on Utility and Cloud Computing (UCC 2011, IEEE CS Press, USA), Melbourne, Australia, December 5-7, 2011.
  * **Rodrigo N. Calheiros, Rajiv Ranjan, Anton Beloglazov, Cesar A. F. De Rose, and Rajkumar Buyya, [CloudSim: A Toolkit for Modeling and Simulation of Cloud Computing Environments and Evaluation of Resource Provisioning Algorithms](http://www.buyya.com/papers/CloudSim2010.pdf), Software: Practice and Experience (SPE), Volume 41, Number 1, Pages: 23-50, ISSN: 0038-0644, Wiley Press, New York, USA, January, 2011. (Seminal paper)**
  * Bhathiya Wickremasinghe, Rodrigo N. Calheiros, Rajkumar Buyya, [CloudAnalyst: A CloudSim-based Visual Modeller for Analysing Cloud Computing Environments and Applications](http://www.cloudbus.org/papers/CloudAnalyst-AINA2010.pdf), Proceedings of the 24th International Conference on Advanced Information Networking and Applications (AINA 2010), Perth, Australia, April 20-23, 2010.
  * Rajkumar Buyya, Rajiv Ranjan and Rodrigo N. Calheiros, [Modeling and Simulation of Scalable Cloud Computing Environments and the CloudSim Toolkit: Challenges and Opportunities](http://www.cloudbus.org/papers/CloudSim-HPCS2009.pdf), Proceedings of the 7th High Performance Computing and Simulation Conference (HPCS 2009, ISBN: 978-1-4244-4907-1, IEEE Press, New York, USA), Leipzig, Germany, June 21-24, 2009.




[![](http://www.cloudbus.org/logo/cloudbuslogo-v5a.png)](http://cloudbus.org/)
