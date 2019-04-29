package stellarcommandexamples.osc;

import com.stellarcommand.OSCMessageBuilder;
import com.stellarcommand.OSCUDPSender;
import com.stellarcommand.StellarOSCVocabulary;
import de.sciss.net.OSCMessage;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.BooleanControl;
import net.happybrackets.core.control.TextControl;
import net.happybrackets.core.control.TextControlSender;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;

public class ShowHide implements HBAction {
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
        else{

            new BooleanControl(this, "Show Ground", true) {
                @Override
                public void valueChanged(Boolean control_val) {// Write your DynamicControl code below this line 
                    // First we need to make sure we do not have an item focused
                    OSCMessage msg = OSCMessageBuilder.createOscMessage(commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.SHOW_GROUND), control_val);

                    oscudpSender.send(msg, stellarCommandInetSocketAddress);
                    display_text.setValue(StellarOSCVocabulary.getOscAsText(msg));
                    // Write your DynamicControl code above this line 
                }
            };// End DynamicControl showGround code 

            new BooleanControl(this, "Show Constellation Art", false) {
                @Override
                public void valueChanged(Boolean control_val) {// Write your DynamicControl code below this line
                    // First we need to make sure we do not have an item focused
                    OSCMessage msg = OSCMessageBuilder.createOscMessage(commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.SHOW_CONSTELATION_ART), control_val);

                    oscudpSender.send(msg, stellarCommandInetSocketAddress);
                    display_text.setValue(StellarOSCVocabulary.getOscAsText(msg));
                    // Write your DynamicControl code above this line
                }
            };// End DynamicControl showGround code

            new BooleanControl(this, "Show Atmosphere", true) {
                @Override
                public void valueChanged(Boolean control_val) {// Write your DynamicControl code below this line
                    // First we need to make sure we do not have an item focused
                    OSCMessage msg = OSCMessageBuilder.createOscMessage(commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.SHOW_ATMOSPHERE), control_val);

                    oscudpSender.send(msg, stellarCommandInetSocketAddress);
                    display_text.setValue(StellarOSCVocabulary.getOscAsText(msg));
                    // Write your DynamicControl code above this line
                }
            };// End DynamicControl showGround code

            new BooleanControl(this, "Show Star Labels", true) {
                @Override
                public void valueChanged(Boolean control_val) {// Write your DynamicControl code below this line
                    // First we need to make sure we do not have an item focused
                    OSCMessage msg = OSCMessageBuilder.createOscMessage(commandLoader.buildOscName(StellarOSCVocabulary.CommandMessages.SHOW_STAR_LABELS), control_val);

                    oscudpSender.send(msg, stellarCommandInetSocketAddress);
                    display_text.setValue(StellarOSCVocabulary.getOscAsText(msg));
                    // Write your DynamicControl code above this line
                }
            };// End DynamicControl showGround code

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
