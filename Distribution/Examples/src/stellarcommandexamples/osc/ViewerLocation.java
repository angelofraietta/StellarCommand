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

public class ViewerLocation implements HBAction {
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

        if (stellarCommandInetSocketAddress == null){
            display_text.setValue("Unable to Load Stellar Command");
        }
        else{

            FloatTextControl latttudeControl = new FloatTextControl(this, "Latitude", 0) {
                @Override
                public void valueChanged(double control_val) {// Write your DynamicControl code below this line 

                    // Write your DynamicControl code above this line 
                }
            };// End DynamicControl code latitudeControl


            FloatTextControl longitudeControl = new FloatTextControl(this, "Longitude", 0) {
                @Override
                public void valueChanged(double control_val) {// Write your DynamicControl code below this line 

                    // Write your DynamicControl code above this line 
                }
            };// End DynamicControl code longitudeControl


            FloatTextControl altitudeControl = new FloatTextControl(this, "Altitude", 0) {
                @Override
                public void valueChanged(double control_val) {// Write your DynamicControl code below this line 

                    // Write your DynamicControl code above this line 
                }
            };// End DynamicControl code altitudeControl 


            TextControl planetControl = new TextControl(this, "Planet", "") {
                @Override
                public void valueChanged(String control_val) {// Write your DynamicControl code below this line 

                    // Write your DynamicControl code above this line 
                }
            };// End DynamicControl planetControl code 


            // type osclistener to create this code
            OSCUDPListener oscudpListener = new OSCUDPListener(commandLoader.RECEIVE_PORT) {
                @Override
                public void OSCReceived(OSCMessage oscMessage, SocketAddress socketAddress, long time) {
                    // type your code below this line

                    // Display our Text to StdOut
                    String oscAsText = StellarOSCVocabulary.getOscAsText(oscMessage);

                    System.out.println(oscAsText);


                    if (oscMessage.getName().equalsIgnoreCase(commandLoader.buildOscName(StellarOSCVocabulary.ClientMessages.OBSERVATION_POINT))){
                        latttudeControl.setValue((float)oscMessage.getArg(0));
                        longitudeControl.setValue((float)oscMessage.getArg(1));
                        altitudeControl.setValue((float)oscMessage.getArg(2));
                        planetControl.setValue((String) oscMessage.getArg(3));
                    }
                    // type your code above this line
                }
            };
            if (oscudpListener.getPort() < 0) { //port less than zero is an error
                String error_message = oscudpListener.getLastError();
                System.out.println("Error opening port " + commandLoader.RECEIVE_PORT + " " + error_message);
            } // end oscListener code


            //Send a new Observation Point based on what has been entered
            new TriggerControl(this, "Change Location") {
                @Override
                public void triggerEvent() {// Write your DynamicControl code below this line 
                    OSCMessage msg = OSCMessageBuilder.createOscMessage(commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.OBSERVATION_POINT),
                            latttudeControl.getValue(), longitudeControl.getValue(),
                            altitudeControl.getValue(), planetControl.getValue());

                    oscudpSender.send(msg, stellarCommandInetSocketAddress);

                    display_text.setValue(StellarOSCVocabulary.getOscAsText(msg));
                    // Write your DynamicControl code above this line 
                }
            };// End DynamicControl locationChangeTrigger code 


            new TriggerControl(this, "Request Location") {
                @Override
                public void triggerEvent() {// Write your DynamicControl code below this line
                    OSCMessage msg = OSCMessageBuilder.createOscMessage(commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.OBSERVATION_POINT));

                    oscudpSender.send(msg, stellarCommandInetSocketAddress);
                    display_text.setValue(StellarOSCVocabulary.getOscAsText(msg));
                    // Write your DynamicControl code above this line 
                }
            };// End DynamicControl triggerControl code 


            // Let us get our position at the very start
            oscudpSender.send(OSCMessageBuilder.createOscMessage(commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.OBSERVATION_POINT)), stellarCommandInetSocketAddress);

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
