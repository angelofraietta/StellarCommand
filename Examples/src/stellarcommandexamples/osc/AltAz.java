package stellarcommandexamples.osc;

import com.stellarcommand.OSCMessageBuilder;
import com.stellarcommand.OSCUDPSender;
import com.stellarcommand.StellarOSCVocabulary;
import de.sciss.net.OSCMessage;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.OSCUDPListener;
import net.happybrackets.core.control.*;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class AltAz implements HBAction {
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


        FloatControl azimuthControl = new FloatBuddyControl(this, "Azimuth", 0, 0, 360) {
            @Override
            public void valueChanged(double control_val) {// Write your DynamicControl code below this line 

                // Write your DynamicControl code above this line 
            }
        };// End DynamicControl azimuthControl code 


        FloatControl altitudeControl = new FloatBuddyControl(this, "Altitude", 0, -90, 90) {
            @Override
            public void valueChanged(double control_val) {// Write your DynamicControl code below this line 

                // Write your DynamicControl code above this line 
            }
        };// End DynamicControl altitudeSender code 


        // type osclistener to create this code
        OSCUDPListener oscudpListener = new OSCUDPListener(commandLoader.RECEIVE_PORT) {
            @Override
            public void OSCReceived(OSCMessage oscMessage, SocketAddress socketAddress, long time) {
                // type your code below this line

                if (oscMessage.getName().equalsIgnoreCase(commandLoader.buildOscName(StellarOSCVocabulary.ClientMessages.DISPLAY_VIEW))){

                }
                // type your code above this line
            }
        };
        if (oscudpListener.getPort() < 0) { //port less than zero is an error
            String error_message = oscudpListener.getLastError();
            System.out.println("Error opening port " + commandLoader.RECEIVE_PORT + " " + error_message);
        } // end oscListener code


        TriggerControl triggerControl = new TriggerControl(this, "Send Position") {
            @Override
            public void triggerEvent() {// Write your DynamicControl code below this line
                OSCMessage msg = OSCMessageBuilder.createOscMessage(commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.VIEW_ALTAZ),
                        altitudeControl.getValue(), azimuthControl.getValue());

                oscudpSender.send(msg, stellarCommandInetSocketAddress);
                display_text.setValue(StellarOSCVocabulary.getOscAsText(msg));
                // Write your DynamicControl code above this line
            }
        };// End DynamicControl triggerControl code

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
