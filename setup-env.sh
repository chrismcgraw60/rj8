#!/bin/bash

#####################################################################
# Runs all required scripts to get RJ* application environment set up
#####################################################################

# Run Activator
gnome-terminal --title "Activator (DEBUG)" -e ./start-activator.sh

# Run H2 in tcp mode
gnome-terminal --title "H2 (Rj8)" -e ./start-h2.sh
