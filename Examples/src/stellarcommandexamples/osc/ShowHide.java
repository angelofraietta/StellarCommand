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

public class ShowHide implements HBAction {
    InetSocketAddress stellarCommandInetSocketAddress = null;

    StellarCommandDriver commandLoader = null;

    OSCUDPSender oscudpSender = new OSCUDPSender();

    @Override
    public void action(HB hb) {
        hb.reset(); //Clears any running code on the device
        //Write your sketch below

        TextControl display_text = new TextControlSender(this, "Diagnostics", "").setDisplayType(DynamicControl.DISPLAY_TYPE.DISPLAY_DEFAULT);


        commandLoader = new StellarCommandDriver();
        stellarCommandInetSocketAddress = commandLoader.loadStellarCommand();

        if (stellarCommandInetSocketAddress == null) {
            display_text.setValue("Unable to Load Stellar Command");
        }
        else{

            BooleanControl goundControl = new BooleanControl(this, "Show Ground", true) {
                @Override
                public void valueChanged(Boolean control_val) {// Write your DynamicControl code below this line 
                    // First we need to make sure we do not have an item focused
                    OSCMessage msg = OSCMessageBuilder.createOscMessage(commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.SHOW_GROUND), control_val);

                    oscudpSender.send(msg, stellarCommandInetSocketAddress);
                    display_text.setValue(StellarOSCVocabulary.getOscAsText(msg));
                    // Write your DynamicControl code above this line 
                }
            };// End DynamicControl showGround code 

            BooleanControl artControl = new BooleanControl(this, "Show Constellation Art", false) {
                @Override
                public void valueChanged(Boolean control_val) {// Write your DynamicControl code below this line
                    // First we need to make sure we do not have an item focused
                    OSCMessage msg = OSCMessageBuilder.createOscMessage(commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.SHOW_CONSTELATION_ART), control_val);

                    oscudpSender.send(msg, stellarCommandInetSocketAddress);
                    display_text.setValue(StellarOSCVocabulary.getOscAsText(msg));
                    // Write your DynamicControl code above this line
                }
            };// End DynamicControl showGround code

            BooleanControl atmosphereControl = new BooleanControl(this, "Show Atmosphere", true) {
                @Override
                public void valueChanged(Boolean control_val) {// Write your DynamicControl code below this line
                    // First we need to make sure we do not have an item focused
                    OSCMessage msg = OSCMessageBuilder.createOscMessage(commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.SHOW_ATMOSPHERE), control_val);

                    oscudpSender.send(msg, stellarCommandInetSocketAddress);
                    display_text.setValue(StellarOSCVocabulary.getOscAsText(msg));
                    // Write your DynamicControl code above this line
                }
            };// End DynamicControl showGround code

            BooleanControl starLabelsControl = new BooleanControl(this, "Show Star Labels", true) {
                @Override
                public void valueChanged(Boolean control_val) {// Write your DynamicControl code below this line
                    // First we need to make sure we do not have an item focused
                    OSCMessage msg = OSCMessageBuilder.createOscMessage(commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.SHOW_STAR_LABELS), control_val);

                    oscudpSender.send(msg, stellarCommandInetSocketAddress);
                    display_text.setValue(StellarOSCVocabulary.getOscAsText(msg));
                    // Write your DynamicControl code above this line
                }
            };// End DynamicControl showGround code


            // type osclistener to create this code
            OSCUDPListener oscudpListener = new OSCUDPListener(commandLoader.RECEIVE_PORT) {
                @Override
                public void OSCReceived(OSCMessage oscMessage, SocketAddress socketAddress, long time) {
                    // type your code below this line

                    if (oscMessage.getName().equalsIgnoreCase(commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.SHOW_STAR_LABELS))) {
                        int i_val = (int)oscMessage.getArg(0);

                        starLabelsControl.setValue(i_val == 0? false:true);
                    }
                    else if (oscMessage.getName().equalsIgnoreCase(commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.SHOW_ATMOSPHERE))) {
                        int i_val = (int)oscMessage.getArg(0);

                        atmosphereControl.setValue(i_val == 0? false:true);
                    }
                    else if (oscMessage.getName().equalsIgnoreCase(commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.SHOW_GROUND))) {
                        int i_val = (int)oscMessage.getArg(0);

                        goundControl.setValue(i_val == 0? false:true);
                    }
                    else if (oscMessage.getName().equalsIgnoreCase(commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.SHOW_CONSTELATION_ART))) {
                        int i_val = (int)oscMessage.getArg(0);

                        artControl.setValue(i_val == 0? false:true);
                    }
                    // type your code above this line
                }
            };
            if (oscudpListener.getPort() < 0) { //port less than zero is an error
                String error_message = oscudpListener.getLastError();
                System.out.println("Error opening port " + commandLoader.RECEIVE_PORT + " " + error_message);
            } // end oscListener code
        }


        // let us get the initial values of these controls

        TriggerControl triggerControl = new TriggerControl(this, "Load Properties") {
            @Override
            public void triggerEvent() {// Write your DynamicControl code below this line
                oscudpSender.send(OSCMessageBuilder.createOscMessage(commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.SHOW_GROUND)), stellarCommandInetSocketAddress);
                oscudpSender.send(OSCMessageBuilder.createOscMessage(commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.SHOW_ATMOSPHERE)), stellarCommandInetSocketAddress);
                oscudpSender.send(OSCMessageBuilder.createOscMessage(commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.SHOW_CONSTELATION_ART)), stellarCommandInetSocketAddress);
                oscudpSender.send(OSCMessageBuilder.createOscMessage(commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.SHOW_STAR_LABELS)), stellarCommandInetSocketAddress);
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
