package stellarcommandexamples.osc;

import com.stellarcommand.OSCUDPSender;
import com.stellarcommand.StellarOSCVocabulary;
import de.sciss.net.OSCMessage;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.OSCUDPListener;
import net.happybrackets.core.control.FloatControl;
import net.happybrackets.core.control.FloatControlSender;
import net.happybrackets.core.control.TextControl;
import net.happybrackets.core.control.TextControlSender;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class TimeFunctions implements HBAction {
    InetSocketAddress stellarCommandInetSocketAddress = null;

    StellarCommandDriver commandLoader = null;

    OSCUDPSender oscudpSender = new OSCUDPSender();

    @Override
    public void action(HB hb) {
        hb.reset(); //Clears any running code on the device
        //Write your sketch below

        TextControl display_text = new TextControlSender(this, "Diagnostics", "");


        commandLoader = new StellarCommandDriver();
        stellarCommandInetSocketAddress = commandLoader.loadStellarCommand();

        if (stellarCommandInetSocketAddress == null) {
            display_text.setValue("Unable to Load Stellar Command");
        }


        TextControl gmtTimeControl = new TextControlSender(this, "GMT Time", "");


        TextControl localTimeControl = new TextControlSender(this, "Local Time", "");


        FloatControl gmtShiftControl = new FloatControlSender(this, "GMT Time Shift", 0);


        // type osclistener to create this code
        OSCUDPListener oscudpListener = new OSCUDPListener(commandLoader.RECEIVE_PORT) {
            @Override
            public void OSCReceived(OSCMessage oscMessage, SocketAddress socketAddress, long time) {
                // type your code below this line

                if (oscMessage.getName().equalsIgnoreCase(commandLoader.buildOscName(StellarOSCVocabulary.ClientMessages.STELLAR_TIME))){
                    String utc = (String) oscMessage.getArg(0);
                    String local = (String) oscMessage.getArg(1);
                    float gmtShift = (float) oscMessage.getArg(2);

                    gmtTimeControl.setValue(utc);
                    localTimeControl.setValue(local);
                    gmtShiftControl.setValue(gmtShift);
                }
                // type your code above this line
            }
        };
        if (oscudpListener.getPort() < 0) { //port less than zero is an error
            String error_message = oscudpListener.getLastError();
            System.out.println("Error opening port " + commandLoader.RECEIVE_PORT + " " + error_message);
        } // end oscListener code
        // write your code above this line
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
