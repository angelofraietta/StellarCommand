package com.stellarcommand;

import stellarium.StellariumSlave;

import java.net.InetAddress;

public class Main {

    // define the start command we are looking for
    static final String CMD_OSC_ADDRESS = "osc=";
    static final String CMD_OSC_CLIENT = "client=";
    static final String CMD_OSC_PORT = "port=";
    static final String CMD_OSC_TRY_PORT = "tryport=";
    static final String CMD_STELLARIUM_ADDRESS = "stellarium=";
    static final String CMD_STELLARIUM_PORT = "stellariumport=";
    static final String CMD_STELLARIUM_POLL_TIME = "stellariumpoll=";


    public static void main(String[] args) {
	// write your code here
        System.out.println("Welcome to Stellar Command " + Version.getversionText());

        try {
            int clientPort = 0;
            String stellariumHost = StellariumSlave.DEFAULT_STELLARIUM_HOST;
            int stellariumPort = StellariumSlave.DEFAULT_STELLARIUM_PORT;
            InetAddress oscClient = InetAddress.getByName("localhost");
            String oscAddress = "/Stellar";
            OSCUDPSender oscSender = new OSCUDPSender();
            int pollTime = 1000; // default poll time for stellarium
            int [] inputPorts =  null;


            for (String arg : args
                    ) {
                System.out.println(arg);
                arg = arg.trim();

                if (arg.startsWith(CMD_OSC_PORT)) {
                    String param = arg.replace(CMD_OSC_PORT, "");
                    clientPort = Integer.parseInt(param);
                }
                else if (arg.startsWith(CMD_OSC_CLIENT)) {
                    String param = arg.replace(CMD_OSC_CLIENT, "");
                    if (!param.isEmpty()) {
                        oscClient = InetAddress.getByName(param);
                    }
                    else
                    {
                        System.out.println("Skip empty arg " + arg);
                    }
                }
                else if (arg.startsWith(CMD_OSC_ADDRESS)) {
                    String param = arg.replace(CMD_OSC_ADDRESS, "");
                    oscAddress = param;
                }
                else if (arg.startsWith(CMD_STELLARIUM_ADDRESS)) {
                    String param = arg.replace(CMD_STELLARIUM_ADDRESS, "");
                    if (!param.isEmpty()) {
                        stellariumHost = param;
                    }
                    else
                    {
                        System.out.println("Skip empty arg " + arg);
                    }
                }
                else if (arg.startsWith(CMD_STELLARIUM_PORT)) {
                    String param = arg.replace(CMD_STELLARIUM_PORT, "");
                    if (!param.isEmpty()) {
                    stellariumPort = Integer.parseInt(param);
                    }
                    else
                    {
                        System.out.println("Skip empty arg " + arg);
                    }
                }
                else if (arg.startsWith(CMD_STELLARIUM_POLL_TIME)) {
                    String param = arg.replace(CMD_STELLARIUM_POLL_TIME, "");
                    if (!param.isEmpty()) {
                    pollTime = Integer.parseInt(param);
                    }
                    else
                    {
                        System.out.println("Skip empty arg " + arg);
                    }
                }
                else if (arg.startsWith(CMD_OSC_TRY_PORT)) {
                    String param = arg.replace(CMD_OSC_TRY_PORT, "");
                    String [] ports = param.split(",");
                    inputPorts = new int[ports.length];
                    System.out.println("Try input ports");
                    for (int i = 0; i < ports.length; i++){
                        System.out.println("Try " + ports[i]);
                        inputPorts[i] = Integer.parseInt(ports[i]);
                    }

                }

                //OSCBundle bundle = new OSCBundle();
                //bundle.addPacket(new OSCMessage());
            }

            System.out.println("Send OSC to " + oscClient.toString());
            System.out.println("Send OSC on port " + clientPort);
            System.out.println("Send OSC name " + oscAddress);
            System.out.println("stellarium host is " + stellariumHost);
            System.out.println("stellarium port is " + stellariumPort);

            StellariumOSCServer stellariumOSCServer = new StellariumOSCServer(oscAddress, oscClient, clientPort, inputPorts);
            stellariumOSCServer.setPollTime(pollTime);

            StellariumSlave slave = new StellariumSlave();
            stellariumOSCServer.setStellariumDevice(stellariumHost);
            stellariumOSCServer.setStellariumPort(stellariumPort);

            slave.setPollTime(pollTime);

            stellariumOSCServer.waitForExit();
            System.exit(0);
        }
        catch (Exception ex){
            System.out.println("Unable to start with arguments");
            for (String arg : args
                    ) {
                System.out.println(arg);
            }

            ex.printStackTrace();
        }
    }




}
