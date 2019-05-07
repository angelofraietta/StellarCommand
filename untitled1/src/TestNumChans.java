import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.IntegerControl;
import net.happybrackets.core.control.IntegerControlSender;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

public class TestNumChans implements HBAction {
    @Override
    public void action(HB hb) {
        hb.reset(); //Clears any running code on the device
        //Write your sketch below


        IntegerControl chans = new IntegerControlSender(this, "Chanels", HB.getNumOutChannels());

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
