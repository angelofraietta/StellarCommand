#!/bin/bash


PORT=1234
OSC=/Stellar
TRYPORTS="3333,4444,5555"
CLIENT=
STELLARIUM=
STELLARIUMPOLL=
STELLARIUMPORT=




echo “Running Stellar Command”

java -jar ../jars/StellarCommand.jar port=$PORT osc=$OSC tryport=$TRYPORTS client=$CLIENT stellarium=$STELLARIUM stellariumpoll=$STELLARIUMPOLL stellariumport=$STELLARIUMPORT
