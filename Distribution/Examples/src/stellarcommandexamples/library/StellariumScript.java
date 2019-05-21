package stellarcommandexamples.library;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.TriggerControl;
import net.happybrackets.device.HB;
import stellarium.StellariumSlave;

import java.lang.invoke.MethodHandles;

public class StellariumScript implements HBAction {
    final String SCRIPT_NAME =  "bennett.scc";

    @Override
    public void action(HB hb) {
        hb.reset(); //Clears any running code on the device
        //Write your sketch below
        StellariumSlave stellariumSlave = new StellariumSlave();

        
        TriggerControl triggerControl = new TriggerControl(this, "Run Bennet") {
            @Override
            public void triggerEvent() {// Write your DynamicControl code below this line 
                stellariumSlave.runScript(SCRIPT_NAME);
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
