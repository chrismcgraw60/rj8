#!/bin/bash

#############################################################
# Set up H2 tcp mode
#############################################################

echo "Starting H2 DB Server (TCP Mode) ..."
java -cp ~/.ivy2/cache/com.h2database/h2/jars/h2-1.3.172.jar org.h2.tools.Server
echo "H2 Started."
echo

