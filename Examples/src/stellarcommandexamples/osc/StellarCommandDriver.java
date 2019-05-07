package stellarcommandexamples.osc;

import com.stellarcommand.StellarOSCVocabulary;
import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import net.happybrackets.core.*;
import net.happybrackets.core.control.TriggerControl;
import net.happybrackets.device.HB;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

/**
 * Communicate with StellarCommand. Use this class to load StellarCommand and get
 * the ports to communicate with it
 * You should call this class from other classes. Eg
 *
 * StellarCommandDriver commandLoader = new StellarCommandDriver();
 * InetSocketAddress stellarCommandInetSocketAddress = commandLoader.loadStellarCommand();
 *
 * After this, Create a listener of the RECEIVE_PORT, listen for OSC from StellarCommand
 * and send OSC to StellarCommand via
 * oscSender.send(oscMessage, stellarCommandInetSocketAddress);
 */
public class StellarCommandDriver implements HBAction, OSCListener {


    // port=1234 osc=/Stellar tryport=3333,4444,5555
    public final int RECEIVE_PORT = 1234; // define the port we will listen on
    public final int [] TRY_PORTS = new int []{3333,4444,5555};
    private final String OSC_NAME = "/Stellar";


    InetSocketAddress stellarCommandInetSocketAddress = null;

    /**
     * Get the String that we will use to Spawn Stellar Command
     * @return the commandline to launch StellarCommand
     */
    String getStartStellarCommandline(){
        //Note we are using this path because the default directory for HappyBrackets is under the Device/HappyBrackets folder
        // We have it installed in
        String ret = "java -jar data/jars/StellarCommand.jar";
        ret += " port="+ RECEIVE_PORT;
        ret += " osc="+ OSC_NAME;

        ret += " tryport=";

        for(int i = 0; i < TRY_PORTS.length; i++){
            if (i > 0){
                ret += ",";
            }
            ret += TRY_PORTS[i];
        }

        return ret;
    }

    @Override
    public void action(HB hb) {
        hb.reset(); //Clears any running code on the device
        //Write your sketch below


        new TriggerControl(this, "Send Poll") {
            @Override
            public void triggerEvent() {// Write your DynamicControl code below this line
                OSCMessage pollMessage = OSCMessageBuilder.createOscMessage(buildOscName(StellarOSCVocabulary.CommandMessages.POLL));

                OSCUDPSender oscSender = new OSCUDPSender();

                for (int port:
                        TRY_PORTS) {
                    try {
                        oscSender.send(pollMessage, InetAddress.getByName("localhost"), port);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                }
                // Write your DynamicControl code above this line
            }
        };// End DynamicControl triggerControl code


        new TriggerControl(this, "Exit StellarCommand") {
            @Override
            public void triggerEvent() {// Write your DynamicControl code below this line 
                exitStellarCommand();
                // Write your DynamicControl code above this line 
            }
        };// End DynamicControl triggerControl code 

        loadStellarCommand();
        // write your code above this line
    }

    /**
     * Send an exit message to StellarCommand
     */
    void exitStellarCommand(){
        if (stellarCommandInetSocketAddress != null){

            OSCMessage exitMessage = OSCMessageBuilder.createOscMessage(buildOscName(StellarOSCVocabulary.CommandMessages.EXIT));
            OSCUDPSender oscSender = new OSCUDPSender();
            oscSender.send(exitMessage, stellarCommandInetSocketAddress);
        }

    }

    /**
     * Create the OSC message name based on the OSC address space we are using
     * @param address
     * @return
     */
    public String buildOscName(String address){
        return OSC_NAME + "/" + address;
    }
    /**
     * Get the Socket Address we need to communicate with StellarCommand
     * If StellarCommand is already open with these ports, we will just return the SocketAddress to communicate
     * If StelarCommand is not open, it will try and spawn it
     * @return the SocketAddress to communicate with Stellarium. If unable to, will return null
     */
    public InetSocketAddress loadStellarCommand(){

        final Object stellariumLoadWait = new Object();

        // First see if Stellarium is open on any ports by sending a poll to each of them
        OSCMessage pollMessage = OSCMessageBuilder.createOscMessage(buildOscName(StellarOSCVocabulary.CommandMessages.POLL));

        // type osclistener to create this code 
        OSCUDPListener oscudpListener = new OSCUDPListener(RECEIVE_PORT) {
            @Override
            public void OSCReceived(OSCMessage oscMessage, SocketAddress socketAddress, long time) {
                // type your code below this line 

                if (oscMessage.getName().equalsIgnoreCase(buildOscName(StellarOSCVocabulary.ClientMessages.OSC_PORT))){
                    try{
                        int targetPort = (int)oscMessage.getArg(0);
                        InetAddress stellarCommandClient = ((InetSocketAddress) socketAddress).getAddress();
                        stellarCommandInetSocketAddress = new InetSocketAddress(stellarCommandClient, targetPort);
                        System.out.println(oscMessage.getName() + " received on port " + targetPort);
                        System.out.println(StellarOSCVocabulary.getOscAsText(oscMessage));
                        synchronized (stellariumLoadWait){
                            stellariumLoadWait.notify();
                        }
                    }
                    catch (Exception ex){}
                }

                // type your code above this line
            }
        };
        if (oscudpListener.getPort() < 0) { //port less than zero is an error
            String error_message = oscudpListener.getLastError();
            System.out.println("Error opening port " + RECEIVE_PORT + " " + error_message);
        } // end oscListener code



        OSCUDPSender oscSender = new OSCUDPSender();

        for (int port:
                TRY_PORTS) {
            oscSender.send(pollMessage, "127.0.0.1", port);
        }

        synchronized (stellariumLoadWait){
            try {
                stellariumLoadWait.wait(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // If we do not have a target port at this stage, StelarCommand is not running and wer will need to launch it

        if (stellarCommandInetSocketAddress == null){
            String command = getStartStellarCommandline();

            ShellExecute executor = new ShellExecute().addProcessCompleteListener((shellExecute, exit_value) -> {
                System.out.println("****************************************");
                System.out.println("Commandline");
                System.out.println("****************************************");
                System.out.println("Text: " +  shellExecute.getProcessText());
                System.out.println("Error: " +  shellExecute.getErrorText());
                System.out.println("Exit status: " + exit_value);
                System.out.println("****************************************");
            });

            try {
                System.out.println("About to start executing command " + command);
                executor.executeCommand(command);
            } catch (IOException e) {
                e.printStackTrace();

            }
        }

        // Now wait a second time
        synchronized (stellariumLoadWait){
            try {
                stellariumLoadWait.wait(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Target port will get set  a receiver
        return stellarCommandInetSocketAddress;
    }

    @Override
    public void messageReceived(OSCMessage oscMessage, SocketAddress socketAddress, long l) {

    }


    //<editor-fold defaultstate="collapsed" desc="Debug Start">

    /**
     * This function is used when running sketch in IntelliJ IDE for debugging or testing
     *
     * @param args standard args required
     */
    public static void main(String[] args) {

        try {
            HB.runDebug(MethodHandles.lookup().lookupClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //</editor-fold>
}
