#!/bin/bash

#############################################################
# Run installed ANt to create test Junit results.

# There is  aknown issue with Ant1.8.x and Eclispe whereby the 
# XSL transform stage of the Junit Reports throws an error.
# Our workaround is to obtain a 1.9.x version of Ant and run it
# from this shell script. 
#############################################################

JAVA_8_HOME=/media/d2/java/jdk1.8.0
ANT_INSTALL=/media/d2/skunk/activator/activator-1.1.0_projects/apache-ant-1.9.3
TARGET_BUILD_FILE=/media/d2/skunk/activator/activator-1.1.0_projects/rj8/test/test.xml

echo "Resetting JAVA_HOME to point to Java 8 ..."
export JAVA_HOME=$JAVA_8_HOME
echo "JAVA_HOME=$JAVA_HOME"
echo

echo "Running Ant..."
$ANT_INSTALL/bin/ant -buildfile $TARGET_BUILD_FILE

