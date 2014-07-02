#!/bin/bash

#############################################################
# REFRESH ECLIPSE CONFIGURATION
# ie When updating SBT, this script will ensure that any
# new dependencies are reflected in associated Eclipse 
# project.
#############################################################

JAVA_8_HOME=/media/d2/java/jdk1.8.0

echo "Resetting JAVA_HOME to point to Java 8 ..."
export JAVA_HOME=$JAVA_8_HOME
echo "JAVA_HOME=$JAVA_HOME"

echo "Starting Activator (debug Mode) ..."
./activator eclipse

