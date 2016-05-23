Table of Contents
-----------------

1. Directory structure of the CloudSim Toolkit
2. Software requirements: Java version 8 or newer 
3. Installation and running the CloudSim Toolkit
4. Running the CloudSim examples
5. Learning CloudSim



1. Directory structure of the CloudSim Toolkit
----------------------------------------------

cloudsim/                -- top level CloudSim directory
	docs/            -- CloudSim API Documentation
	examples/        -- CloudSim examples
	jars/            -- CloudSim jar archives
	sources/         -- CloudSim source code
	tests/           -- CloudSim unit tests


2. Software requirements: Java version 8 or newer
---------------------------------------------------

CloudSim has been tested and ran on Sun's Java version 8 or newer.
Older versions of Java are not compatible.
If you have non-Sun Java version, such as gcj or J++, they may not be compatible.
You also need to install Ant to compile CloudSim (explained in more details later).


3. Installation and running the CloudSim Toolkit
------------------------------------------------

You just need to unpack the CloudSim file to install.
If you want to remove CloudSim, then remove the whole cloudsim directory.
You do not need to compile CloudSim source code. The JAR files are
provided to compile and to run CloudSim applications:

  * jars/cloudsim-<VERSION>.jar                    -- contains the CloudSim class files
  * jars/cloudsim-<VERSION>-sources.jar            -- contains the CloudSim source code files
  * jars/cloudsim-examples-<VERSION>.jar           -- contains the CloudSim examples class files
  * jars/cloudsim-examples-<VERSION>-sources.jar   -- contains the CloudSim examples source code files


4. Running the CloudSim examples
--------------------------------

Please read how to run the CloudSim examples in examples.txt


5. Learning CloudSim
--------------------

To understand how to use CloudSim, please go through the examples provided
in the examples/ directory.

