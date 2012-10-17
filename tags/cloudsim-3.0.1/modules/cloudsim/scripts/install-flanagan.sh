#!/bin/sh
wget http://www.ee.ucl.ac.uk/~mflanaga/java/flanagan.jar
mvn install:install-file -DgroupId=org.flanagan -DartifactId=flanagan -Dversion=1.0 -Dfile=flanagan.jar -Dpackaging=jar -DgeneratePom=true
rm flanagan.jar
