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

/**
 * Monitor Celestial Position and field of view. Stars will be printed in the console
 */
public class StellarPosition implements HBAction {
    InetSocketAddress stellarCommandInetSocketAddress = null;

    StellarCommandDriver commandLoader = null;

    OSCUDPSender oscudpSender = new OSCUDPSender();

    @Override
    public void action(HB hb) {
        hb.reset(); //Clears any running code on the device
        //Write your sketch below


        TextControl display_text = new TextControlSender(this, "Diagnostics", "").setDisplayType(DynamicControl.DISPLAY_TYPE.DISPLAY_DEFAULT);


        commandLoader = new StellarCommandDriver();
        try {
            stellarCommandInetSocketAddress = commandLoader.loadStellarCommand();

            if (stellarCommandInetSocketAddress == null) {
                display_text.setValue("Unable to Load Stellar Command");
            } else {


                // If we check this we will disable updating so we can send values
                BooleanControl disableUpdate = new BooleanControl(this, "Disable Update", false) {
                    @Override
                    public void valueChanged(Boolean control_val) {// Write your DynamicControl code below this line

                        // Write your DynamicControl code above this line
                    }
                };// End DynamicControl disableUpdate code


                FloatControl fieldOfView = new FloatBuddyControl(this, "Field Of view", 20, 0.01, 120) {
                    @Override
                    public void valueChanged(double control_val) {// Write your DynamicControl code below this line 
                        OSCMessage msg = OSCMessageBuilder.createOscMessage(commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.FIELD_OF_VIEW), control_val);

                        oscudpSender.send(msg, stellarCommandInetSocketAddress);
                        display_text.setValue(StellarOSCVocabulary.getOscAsText(msg));
                        // Write your DynamicControl code above this line 
                    }
                };// End DynamicControl fieldOfView code 


                FloatTextControl raValue = new FloatTextControl(this, "RA", 0) {
                    @Override
                    public void valueChanged(double control_val) {// Write your DynamicControl code below this line

                        // Write your DynamicControl code above this line
                    }
                };// End DynamicControl code raValue


                FloatTextControl decValue = new FloatTextControl(this, "Dec.", 0) {
                    @Override
                    public void valueChanged(double control_val) {// Write your DynamicControl code below this line

                        // Write your DynamicControl code above this line
                    }
                };// End DynamicControl code decValue




                new TriggerControl(this, "Send Saturn") {
                    @Override
                    public void triggerEvent() {// Write your DynamicControl code below this line
                        OSCMessage msg = OSCMessageBuilder.createOscMessage(commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.VIEW_OBJECT), "Saturn");

                        oscudpSender.send(msg, stellarCommandInetSocketAddress);
                        display_text.setValue(StellarOSCVocabulary.getOscAsText(msg));
                        // Write your DynamicControl code above this line
                    }
                };// End DynamicControl sendSaturn code


                new TriggerControl(this, "Send Canopus by Ra / Dec") {
                    @Override
                    public void triggerEvent() {// Write your DynamicControl code below this line

                        // First we need to make sure we do not have an item focused
                        OSCMessage msg = OSCMessageBuilder.createOscMessage(commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.VIEW_OBJECT), "");

                        oscudpSender.send(msg, stellarCommandInetSocketAddress);
                        display_text.setValue(StellarOSCVocabulary.getOscAsText(msg));

                        // These are the RA and Dec for Canopus
                        float ra = 96.49851f, dec = -52.682182f;

                        msg = OSCMessageBuilder.createOscMessage(commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.VIEW_RA_DEC),
                                ra, dec);

                        oscudpSender.send(msg, stellarCommandInetSocketAddress);
                        display_text.setValue(StellarOSCVocabulary.getOscAsText(msg));
                        // Write your DynamicControl code above this line
                    }
                };// End DynamicControl triggerControl code


                new TriggerControl(this, "Send Ra / Dec") {
                    @Override
                    public void triggerEvent() {// Write your DynamicControl code below this line

                        // First we need to make sure we do not have an item focused
                        OSCMessage msg = OSCMessageBuilder.createOscMessage(commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.VIEW_OBJECT), "");

                        oscudpSender.send(msg, stellarCommandInetSocketAddress);
                        display_text.setValue(StellarOSCVocabulary.getOscAsText(msg));

                        msg = OSCMessageBuilder.createOscMessage(commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.VIEW_RA_DEC), raValue.getValue(), decValue.getValue());

                        oscudpSender.send(msg, stellarCommandInetSocketAddress);
                        display_text.setValue(StellarOSCVocabulary.getOscAsText(msg));
                        // Write your DynamicControl code above this line
                    }
                };// End DynamicControl triggerControl code


                // type osclistener to create this code
                OSCUDPListener oscudpListener = new OSCUDPListener(commandLoader.RECEIVE_PORT) {
                    @Override
                    public void OSCReceived(OSCMessage oscMessage, SocketAddress socketAddress, long time) {
                        // type your code below this line

                        // Display our Text to StdOut
                        String oscAsText = StellarOSCVocabulary.getOscAsText(oscMessage);

                        System.out.println(oscAsText);

                        if (oscMessage.getName().equalsIgnoreCase(
                                commandLoader.buildOscName(StellarOSCVocabulary.ClientMessages.DISPLAY_VIEW))) {
                            float fov = (float) oscMessage.getArg(0);
                            float Ra = (float) oscMessage.getArg(1);
                            float Dec = (float) oscMessage.getArg(2);

                            // if we have our checkbox checked, we will disable the update
                            if (!disableUpdate.getValue()) {
                                fieldOfView.setValue(fov);
                                raValue.setValue(Ra);
                                decValue.setValue(Dec);
                            }
                        }
                        // type your code above this line
                    }
                };
                if (oscudpListener.getPort() < 0) { //port less than zero is an error
                    String error_message = oscudpListener.getLastError();
                    System.out.println("Error opening port " + commandLoader.RECEIVE_PORT + " " + error_message);
                } // end oscListener code

                // Let us get our position at the very start
                oscudpSender.send(OSCMessageBuilder.createOscMessage(commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.DISPLAY_VIEW)), stellarCommandInetSocketAddress);

            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
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
