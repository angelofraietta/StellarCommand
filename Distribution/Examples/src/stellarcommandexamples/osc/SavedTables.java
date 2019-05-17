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
import java.util.ArrayList;
import java.util.List;

/**
 * This calss allows you to navigate using field of view and alt az controls
 * It will return the stars within field of view
 */
public class SavedTables implements HBAction {
    InetSocketAddress stellarCommandInetSocketAddress = null;

    StellarCommandDriver commandLoader = null;

    OSCUDPSender oscudpSender = new OSCUDPSender();

    List<String> starAttributeNames = new ArrayList<String>();

    int bundleNumber = 0;
    int totalBundles = 0;
    int totalStarCount = 0;

    // Display our minimum and Maximum Magnitude
    float minimumMagnitude = Float.MAX_VALUE;
    float maximumMagnitude= Float.MIN_VALUE;

    // We are actually going to get this value from the arameters names by Looking for Hpmag in the name WHEN THEY COME IN
    int magnitudeParameter =  -1;


    @Override
    public void action(HB hb) {
        TextControl display_text = new TextControlSender(this, "Diagnostics", "").setDisplayType(DynamicControl.DISPLAY_TYPE.DISPLAY_DEFAULT);

        TextControl bundle_info = new TextControlSender(this, "Bundle Info", "");
        TextControl star_properties = new TextControlSender(this, "Star Properties", "").setDisplayType(DynamicControl.DISPLAY_TYPE.DISPLAY_DEFAULT);
        TextControl star_info = new TextControlSender(this, "Star Info", "").setDisplayType(DynamicControl.DISPLAY_TYPE.DISPLAY_DEFAULT);


        commandLoader = new StellarCommandDriver();

        // disable display of OSC Diag messages from command loader. Just read ones in this class
        commandLoader.setDisableOscDiag(true);
        stellarCommandInetSocketAddress = commandLoader.loadStellarCommand();

        if (stellarCommandInetSocketAddress == null) {
            display_text.setValue("Unable to Load Stellar Command");
        }


        TextControl storedTable = new TextControlSender(this, "Stored Table", "data/tables/Canopus.txt").setDisplayType(DynamicControl.DISPLAY_TYPE.DISPLAY_DEFAULT);


        TriggerControl loadTable = new TriggerControl(this, "Load Table") {
            @Override
            public void triggerEvent() {// Write your DynamicControl code below this line
                // Let us find the exactt location of our file
                File stored_file =  new File(storedTable.getValue());
                if (stored_file.exists()){
                    String file_path = stored_file.getAbsolutePath();
                    OSCMessage msg = OSCMessageBuilder.createOscMessage(
                            commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.LOAD_TABLE),
                            file_path);

                    oscudpSender.send(msg, stellarCommandInetSocketAddress);
                    String oscAsText = StellarOSCVocabulary.getOscAsText(msg);
                    System.out.println(oscAsText);
                    display_text.setValue(oscAsText);
                }

                // Write your DynamicControl code above this line 
            }
        };// End DynamicControl loadTable code 

        // type osclistener to create this code
        OSCUDPListener oscudpListener = new OSCUDPListener(commandLoader.RECEIVE_PORT) {
            @Override
            public void OSCReceived(OSCMessage oscMessage, SocketAddress socketAddress, long time) {
                // type your code below this line
                // Display our Text to StdOut
                String oscAsText = StellarOSCVocabulary.getOscAsText(oscMessage);

                System.out.println(oscAsText);

                if (oscMessage.getName().equalsIgnoreCase(commandLoader.buildOscName(StellarOSCVocabulary.ClientMessages.BUNDLE_COUNT))){
                    bundleNumber = (int) oscMessage.getArg(0);
                    totalBundles = (int)oscMessage.getArg(1);
                    if (bundleNumber == 0){
                        String diag_text = "Expect " + totalBundles + " bundles of stars";
                        System.out.println(diag_text);
                        bundle_info.setValue(diag_text);
                        resetStars();
                        star_info.setValue("");
                        star_properties.setValue("");

                    }
                }
                else if (oscMessage.getName().equalsIgnoreCase(commandLoader.buildOscName(StellarOSCVocabulary.ClientMessages.STAR_NAMES))){
                    // we will store the names in our array only if it is the first bundle
                    if (bundleNumber == 0){
                        String display_val = "";
                        for (int i = 0; i < oscMessage.getArgCount(); i++){
                            String attribute_name = (String)oscMessage.getArg(i);
                            starAttributeNames.add(attribute_name);
                            display_val += attribute_name + " ";

                            if (attribute_name.startsWith("Hpmag")){
                                magnitudeParameter = i;
                            }
                        }
                        star_properties.setValue(display_val.trim());

                    }

                }
                else if (oscMessage.getName().equalsIgnoreCase(commandLoader.buildOscName(StellarOSCVocabulary.ClientMessages.STAR_VALUES))){
                    totalStarCount++;

                    if (magnitudeParameter >= 0){
                        float magnitude = (float)oscMessage.getArg(magnitudeParameter);

                        if (magnitude > maximumMagnitude){
                            maximumMagnitude = magnitude;
                        }

                        if (magnitude < minimumMagnitude){
                            minimumMagnitude = magnitude;
                        }
                    }
                    star_info.setValue(totalStarCount + " Stars. Max / Min mag " + minimumMagnitude + " " + maximumMagnitude);

                }
                // type your code above this line
            }
        };
        if (oscudpListener.getPort() < 0) { //port less than zero is an error
            String error_message = oscudpListener.getLastError();
            System.out.println("Error opening port " + commandLoader.RECEIVE_PORT + " " + error_message);
        } // end oscListener code


        // First set our display so we don't have any art, ground or atmosphere. We will have star labels
        oscudpSender.send(OSCMessageBuilder.createOscMessage(commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.SHOW_GROUND), false), stellarCommandInetSocketAddress);
        oscudpSender.send(OSCMessageBuilder.createOscMessage(commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.SHOW_ATMOSPHERE), false), stellarCommandInetSocketAddress);
        oscudpSender.send(OSCMessageBuilder.createOscMessage(commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.SHOW_CONSTELATION_ART), false), stellarCommandInetSocketAddress);
        oscudpSender.send(OSCMessageBuilder.createOscMessage(commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.SHOW_STAR_LABELS), true), stellarCommandInetSocketAddress);

        // also, we will deselect any stars that are selected
        oscudpSender.send(OSCMessageBuilder.createOscMessage(commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.VIEW_OBJECT), ""), stellarCommandInetSocketAddress);

        // write your code above this line
    }

    /**
     * Reset all the star parameters and start loading
     */
    private void resetStars() {
        starAttributeNames.clear();
        totalStarCount = 0;
        minimumMagnitude = Float.MAX_VALUE;
        maximumMagnitude= Float.MIN_VALUE;

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
