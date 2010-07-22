Directory Structure of CloudSim Toolkit 2.0
---------------------------------------

$CLOUDSIM/			-- top level CloudSim directory
	classes/		-- The CloudSim class files
	doc/			-- CloudSim API Documentation
	examples/		-- CloudSim examples and Class Diagram
	jar/			-- CloudSim jar archives
	lib/			-- external libraries
	src/			-- CloudSim source code
	test			-- CloudSim unit tests

Software Requirements : Java version 1.6 or newer
---------------------
CloudSim has been tested and ran on Sun's Java version 1.6.0 or newer.
Older versions of Java are not compatible.
If you have non-Sun Java version, such as gcj or J++, they may not be compatible.
You also need to install Ant to compile CloudSim (explained in more details later).


Installation and Running CloudSim Toolkit
----------------------------------------
You just need to unpack the CloudSim file to install.
If you want to remove CloudSim, then remove the whole $CLOUDSIM directory.

NOTE: You do not need to compile CloudSim source code. The JAR file is
      provided to compile and to run CloudSim applications.

* cloudsim.jar -- contains CloudSim class files only

To compile and run CloudSim applications, do the following step:
1) Go the directory where the CloudSim's Examples reside
   In Unix or Linux: cd $CLOUDSIM/examples/
   In Windows:       cd %CLOUDSIM%\examples\

2) Compile the Java source file
   In Unix or Linux: javac -classpath $CLOUDSIM/jar/cloudsim-2.0.jar:. cloudsim/examples/CloudSimExampleX.java
   In Windows:       javac -classpath %CLOUDSIM%\jar\cloudsim-2.0.jar;. cloudsim\examples\CloudSimExampleX.java

3) Running the Java class file
   In Unix or Linux: java -classpath $CLOUDSIM/jar/cloudsim-2.0.jar:. cloudsim.examples.CloudSimExampleX
   In Windows:       java -classpath %CLOUDSIM%\jar\cloudsim-2.0.jar;. cloudsim.examples.CloudSimExampleX

NOTE:
* $CLOUDSIM or %CLOUDSIM% is the location of the CloudSim Toolkit package.


Learning CloudSim
-----------------
To understand how to use CloudSim, please go through the examples provided
in the $CLOUDSIM/examples/ directory.

Compiling CloudSim : Using Ant
------------------
This release contains a simple buildfile for compiling CloudSim classes.
You need to have ant installed (http://ant.apache.org/).
Ant can be used in both Windows and Unix/Linux environment.

Usage:
* type 'ant' to compile all CloudSim source files and put them into
  classes/ directory
* type 'ant makejar' to compile the source files (if necessary) and to create
  a new jar file called "new_cloudsim.jar" into jars/ directory.

NOTE:
* You need to set up PATH for ant in Windows and/or Unix.
* rule for javadoc is not included yet. Use javadoc.sh script on Unix instead.

