#!/bin/bash


PORT=1234
OSC=/Stellar
TRYPORTS="3333,4444,5555"
CLIENT=
STELLARIUM=




echo “Running Stellar Command”

java -jar StellarCommand.jar port=$PORT osc=$OSC tryport=$TRYPORTS client=$CLIENT stellarium=$STELLARIUM
