package stellarcommandexamples.osc;

import com.stellarcommand.OSCUDPSender;
import com.stellarcommand.StellarOSCVocabulary;
import de.sciss.net.OSCMessage;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.OSCMessageBuilder;
import net.happybrackets.core.OSCUDPListener;
import net.happybrackets.core.control.*;
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


        TextControl gmtTimeControl = new TextControlSender(this, "GMT Time", "").setDisplayType(DynamicControl.DISPLAY_TYPE.DISPLAY_DEFAULT);


        TextControl localTimeControl = new TextControlSender(this, "Local Time", "").setDisplayType(DynamicControl.DISPLAY_TYPE.DISPLAY_DEFAULT);;


        FloatControl gmtShiftControl = new FloatControlSender(this, "GMT Time Shift", 0).setDisplayType(DynamicControl.DISPLAY_TYPE.DISPLAY_DEFAULT);

        FloatControl timerateControl = new FloatControlSender(this, "Time rate", 0).setDisplayType(DynamicControl.DISPLAY_TYPE.DISPLAY_DEFAULT);


        TextControl stringTimeSender = new TextControl(this, "Send Time", "") {
            @Override
            public void valueChanged(String control_val) {// Write your DynamicControl code below this line 

                OSCMessage msg = OSCMessageBuilder.createOscMessage(commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.STELLAR_TIME), control_val);
                oscudpSender.send(msg, stellarCommandInetSocketAddress);
                display_text.setValue(StellarOSCVocabulary.getOscAsText(msg));
                // Write your DynamicControl code above this line 
            }
        };// End DynamicControl stringTimeSender code 


        // type osclistener to create this code
        OSCUDPListener oscudpListener = new OSCUDPListener(commandLoader.RECEIVE_PORT) {
            @Override
            public void OSCReceived(OSCMessage oscMessage, SocketAddress socketAddress, long time) {
                // type your code below this line
                // Display our Text to StdOut
                String oscAsText = StellarOSCVocabulary.getOscAsText(oscMessage);

                System.out.println(oscAsText);


                if (oscMessage.getName().equalsIgnoreCase(commandLoader.buildOscName(StellarOSCVocabulary.ClientMessages.STELLAR_TIME))){
                    String utc = (String) oscMessage.getArg(0);
                    String local = (String) oscMessage.getArg(1);
                    float gmtShift = (float) oscMessage.getArg(2);
                    float timerate = (float)oscMessage.getArg(3);

                    gmtTimeControl.setValue(utc);
                    localTimeControl.setValue(local);
                    gmtShiftControl.setValue(gmtShift);
                    timerateControl.setValue(timerate);
                }
                // type your code above this line
            }
        };
        if (oscudpListener.getPort() < 0) { //port less than zero is an error
            String error_message = oscudpListener.getLastError();
            System.out.println("Error opening port " + commandLoader.RECEIVE_PORT + " " + error_message);
        } // end oscListener code
        // write your code above this line


        FloatControl timeRate = new FloatBuddyControl(this, "Time Rate", 0, -1, 1) {
            @Override
            public void valueChanged(double control_val) {// Write your DynamicControl code below this line
                OSCMessage msg = OSCMessageBuilder.createOscMessage(commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.SET_TIME_RATE), control_val);
                oscudpSender.send(msg, stellarCommandInetSocketAddress);
                display_text.setValue(StellarOSCVocabulary.getOscAsText(msg));

                // Write your DynamicControl code above this line
            }
        };// End DynamicControl timeRate code


        new TriggerControl(this, "Set Local Midday 6 April 2019") {
            @Override
            public void triggerEvent() {// Write your DynamicControl code below this line

                OSCMessage msg = OSCMessageBuilder.createOscMessage(commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.STELLAR_TIME),
                        2019, 04, 06, 12, 0, 0);
                oscudpSender.send(msg, stellarCommandInetSocketAddress);
                display_text.setValue(StellarOSCVocabulary.getOscAsText(msg));
                // Write your DynamicControl code above this line
            }
        };// End DynamicControl sendCurrenttime code

        new TriggerControl(this, "Set GMT + 1 Midday 6 April 2019") {
            @Override
            public void triggerEvent() {// Write your DynamicControl code below this line

                OSCMessage msg = OSCMessageBuilder.createOscMessage(commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.STELLAR_TIME),
                        2019, 04, 06, 12, 0, 0, "+01:00");
                oscudpSender.send(msg, stellarCommandInetSocketAddress);
                display_text.setValue(StellarOSCVocabulary.getOscAsText(msg));
                // Write your DynamicControl code above this line
            }
        };// End DynamicControl sendCurrenttime code

         new TriggerControl(this, "Request Time") {
            @Override
            public void triggerEvent() {// Write your DynamicControl code below this line 

                OSCMessage msg = OSCMessageBuilder.createOscMessage(commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.POLL));
                oscudpSender.send(msg, stellarCommandInetSocketAddress);
                display_text.setValue(StellarOSCVocabulary.getOscAsText(msg));
                // Write your DynamicControl code above this line
            }
        };// End DynamicControl sendPoll code 

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
