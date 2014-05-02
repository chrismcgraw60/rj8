#!/bin/bash

#############################################################
# TEMPLATE SCRIPT FOR setting JAVA_HOME and Running Activator
#############################################################

JAVA_8_HOME=/media/d2/java/jdk1.8.0

echo "Resetting JAVA_HOME to point to Java 8 ..."
export JAVA_HOME=$JAVA_8_HOME
echo "JAVA_HOME=$JAVA_HOME"

echo "Starting Activator (debug Mode) ..."
./activator -jvm-debug 9998 ~run

