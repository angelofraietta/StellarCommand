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
public class AltAz implements HBAction {

    // we need to include our Stellarium
    StellariumSlave stellariumSlave = new StellariumSlave();

    @Override
    public void action(HB hb) {
        hb.reset(); //Clears any running code on the device
        //Write your sketch below

        FloatControl azimuthControl = new FloatBuddyControl(this, "Azimuth", 0, 0, 360) {
            @Override
            public void valueChanged(double control_val) {// Write your DynamicControl code below this line

                stellariumSlave.setAzimuth(control_val);
                // Write your DynamicControl code above this line
            }
        };// End DynamicControl azimuthControl code


        FloatControl altitudeControl = new FloatBuddyControl(this, "Altitude", 0, -90, 90) {
            @Override
            public void valueChanged(double control_val) {// Write your DynamicControl code below this line
                stellariumSlave.setAltitude(control_val);
                // Write your DynamicControl code above this line
            }
        };// End DynamicControl altitudeSender code

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




        // add our listener for Stellarium
        stellariumSlave.addViewListener(new StellariumViewListener() {
            @Override
            public void viewRead(StellariumView stellariumView) {

                raValue.setValue(stellariumView.getRaDec().rightAscension);
                decValue.setValue(stellariumView.getRaDec().declination);
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
