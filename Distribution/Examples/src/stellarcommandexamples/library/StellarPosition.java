package stellarcommandexamples.library;


import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.FloatBuddyControl;
import net.happybrackets.core.control.FloatControl;
import net.happybrackets.core.control.FloatTextControl;
import net.happybrackets.core.control.TriggerControl;
import net.happybrackets.device.HB;
import stellarium.*;
import stellarstructures.RaDec;

import java.lang.invoke.MethodHandles;

/**
 * This class will demonstrate stellar positions
 */
public class StellarPosition implements HBAction {

    // we need to include our Stellarium
    StellariumSlave stellariumSlave = new StellariumSlave();

    @Override
    public void action(HB hb) {
        hb.reset(); //Clears any running code on the device
        //Write your sketch below
        FloatControl fieldOfView = new FloatBuddyControl(this, "Field Of view", 20, 0.01, 120) {
            @Override
            public void valueChanged(double control_val) {// Write your DynamicControl code below this line

                stellariumSlave.setFieldOfView(control_val);
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

                stellariumSlave.setTargetName("Saturn");
                // Write your DynamicControl code above this line
            }
        };// End DynamicControl sendSaturn code


        new TriggerControl(this, "Send Canopus by Ra / Dec") {
            @Override
            public void triggerEvent() {// Write your DynamicControl code below this line

                // First we need to make sure we do not have an item focused
                stellariumSlave.setTargetName("");
                // These are the RA and Dec for Canopus
                float ra = 96.49851f, dec = -52.682182f;

                stellariumSlave.setRaDec(new RaDec(ra, dec));
                // Write your DynamicControl code above this line
            }
        };// End DynamicControl triggerControl code


        new TriggerControl(this, "Send Ra / Dec") {
            @Override
            public void triggerEvent() {// Write your DynamicControl code below this line

                // First we need to make sure we do not have an item focused
                stellariumSlave.setTargetName("");

                stellariumSlave.setRaDec(new RaDec(raValue.getValue(), decValue.getValue()));
                // Write your DynamicControl code above this line
            }
        };// End DynamicControl triggerControl code

        // add our listener for Stellarium
        stellariumSlave.addViewListener(new StellariumViewListener() {
            @Override
            public void viewRead(StellariumView stellariumView) {

                raValue.setValue(stellariumView.getRaDec().rightAscension);
                decValue.setValue(stellariumView.getRaDec().declination);
                fieldOfView.setValue(stellariumView.getFieldOfView());
            }

            @Override
            public void locationRead(StellariumLocation stellariumLocation) {

            }

            @Override
            public void timeRead(StellariumTime stellariumTime) {

            }
        });

        stellariumSlave.setPollTime(1000);
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
