package stellarcommandexamples.osc;

import com.stellarcommand.OSCMessageBuilder;
import com.stellarcommand.OSCUDPSender;
import com.stellarcommand.StellarOSCVocabulary;
import de.sciss.net.OSCMessage;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.OSCUDPListener;
import net.happybrackets.core.control.*;
import net.happybrackets.device.HB;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class StellariumScript implements HBAction {
    // We will put the script in our Scripts folder and get the real path later
    final String SCRIPT_NAME =  "data/scripts/stellariumscript/bennett.ssc";

    InetSocketAddress stellarCommandInetSocketAddress = null;

    StellarCommandDriver commandLoader = null;

    OSCUDPSender oscudpSender = new OSCUDPSender();

    @Override
    public void action(HB hb) {
        hb.reset(); //Clears any running code on the device
        //Write your sketch below

        TextControl display_text = new TextControlSender(this, "Diagnostics", "").setDisplayType(DynamicControl.DISPLAY_TYPE.DISPLAY_DEFAULT);


        TextControl scriptPath = new TextControlSender(this, "Script Path", SCRIPT_NAME).setDisplayType(DynamicControl.DISPLAY_TYPE.DISPLAY_DEFAULT);

        commandLoader = new StellarCommandDriver();
        stellarCommandInetSocketAddress = commandLoader.loadStellarCommand();

        if (stellarCommandInetSocketAddress == null) {
            display_text.setValue("Unable to Load Stellar Command");
        }

        
        new BooleanControl(this, "Run script", false) {
            @Override
            public void valueChanged(Boolean control_val) {// Write your DynamicControl code below this line 
                OSCMessage msg;

                if (control_val) {
                    File stored_file = new File(scriptPath.getValue());

                    String file_path = stored_file.getAbsolutePath();
                    msg = OSCMessageBuilder.createOscMessage(
                            commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.SCRIPT),
                            file_path);

                }
                else
                {
                    msg = OSCMessageBuilder.createOscMessage(
                            commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.SCRIPT),
                            "stop");
                }

                oscudpSender.send(msg, stellarCommandInetSocketAddress);
                display_text.setValue(StellarOSCVocabulary.getOscAsText(msg));

                // Write your DynamicControl code above this line 
            }
        };// End DynamicControl booleanControl code 

        // Type booleanControlSender to generate this code 
        BooleanControl scriptStatus = new BooleanControlSender(this, "Script is running", false);


        new TriggerControl(this, "Reqeust Status") {
            @Override
            public void triggerEvent() {// Write your DynamicControl code below this line

                OSCMessage msg = OSCMessageBuilder.createOscMessage(
                        commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.SCRIPT));


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
                        commandLoader.buildOscName(StellarOSCVocabulary.ClientMessages.SCRIPT))) {
                    int status = (int)oscMessage.getArg(0);

                    scriptStatus.setValue(status != 0);
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
