package com.stellarcommand;

import StellarStructures.RaDec;
import Stellarium.StellariumSlave;
import Stellarium.StellariumView;
import Stellarium.StellariumViewListener;
import vizier.VizierQuery;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Sends Stellarium OSC Data back to client
 */
public class StellariumOSCServer implements StellariumViewListener {

    OSCUDPReceiver oscReceiver = null;
    StellariumSlave stellariumSlave = new StellariumSlave();
    OSCUDPSender oscSender = new OSCUDPSender();

    VizierQuery vizierQuery = new VizierQuery();

    boolean queryVizier = true;
    /*
    String stellariumHost = StellariumSlave.DEFAULT_STELLARIUM_HOST;
    int stellariumPort = StellariumSlave.DEFAULT_STELLARIUM_PORT;
    InetAddress oscClient = InetAddress.getByName("localhost");
    String oscAddress = "\\Stellar";
    */

    String oscNamespace; // this is what our OSC messages will start as
    InetAddress oscClient;
    int targetPort;


    StellariumView lastFieldOfView = null;

    /**
     * Set the frequency of polling Stellarium for changes in view
     * @param poll_time the time in milliseconds between re-polling Stellarium
     */
    void setPollTime(int poll_time){
        stellariumSlave.setPollTime(poll_time);
    }

    /**
     * Create an OSC server with minimum number of required parameters
     * @param oscName the OSC address name
     * @param oscTargetAddress the OSC client INET address
     * @param target_port the target OSC port to send messages to
     * @param inputPorts a list of ports we will try to listen to. If null or unable to open one, we will open our own port
     */
    public StellariumOSCServer(String oscName, InetAddress oscTargetAddress, int target_port, int [] inputPorts ){
        oscNamespace = oscName;
        oscClient = oscTargetAddress;
        targetPort = target_port;
        int source_port = 0;
        if (inputPorts != null){
            for (int i = 0; i < inputPorts.length && oscReceiver == null; i++){
                try {
                    oscReceiver = new OSCUDPReceiver(inputPorts[i]);
                    source_port = oscReceiver.getPort();
                } catch (IOException e) {
                    System.out.println("Unable to open input port " + inputPorts[i]);
                }
            }
        }
        // see if we managed to open a port. If not, we need to open first one available
        if (oscReceiver == null){
            try {
                oscReceiver = new OSCUDPReceiver(0);
                source_port = oscReceiver.getPort();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (oscReceiver == null){
            System.out.println("Unable to open any port");
        }
        else
        {
            System.out.println("Opened port " + source_port);
        }

        oscSender.send(OSCMessageBuilder.createOscMessage(oscNamespace + StellarOSCVocabulary.SendMessages.OSC_PORT, source_port), oscClient, targetPort);
        stellariumSlave.addFieldOfViewListener(this::viewChanged);
        vizierQuery.addFilter("Hpmag=<5");
    }

    @Override
    public void viewChanged(StellariumView stellariumView) {
        if (!stellariumView.equals(lastFieldOfView)) {
            RaDec raDec = stellariumView.getRaDec();
            System.out.println("FOV: " + stellariumView.getFieldOfView() + " RA Dec " + raDec.rightAscension + " " + raDec.declination);
            oscSender.send(OSCMessageBuilder.createOscMessage(oscNamespace + StellarOSCVocabulary.SendMessages.DISPLAY_VIEW, stellariumView.getFieldOfView(), raDec.rightAscension, raDec.declination), oscClient, targetPort);
            lastFieldOfView = stellariumView;
            if (queryVizier){

                String centre =  raDec.rightAscension + " " + raDec.declination;
                String vizierData = vizierQuery.readVizierCentre(centre, stellariumView.getFieldOfView());
                System.out.println(vizierData);

            }
        }
    }

    /**
     * Set the name of where to request HTTP data from Stellarium
     * @param stellariumHost HTTP host name
     */
    public void setStellariumDevice(String stellariumHost) {
        stellariumSlave.setStellariumDevice(stellariumHost);
    }

    /**
     * Set the port to send to Stellarium to get Stellarium data
     * @param stellariumPort
     */
    public void setStellariumPort(int stellariumPort) {
        stellariumSlave.setStellariumPort(stellariumPort);
    }
}
