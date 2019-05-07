package stellarstructures;

/**
 * Structure to contain Alt and Azimuth
 */
public class AltAz {
    public double getAltitude() {
        return altitude;
    }

    public double getAzimuth() {
        return azimuth;
    }

    double altitude;
    double azimuth;

    /**
     * Constructor using alt and dec
     * @param alt altitude
     * @param az azimuth
     */
    public AltAz(double alt, double az){
        altitude = alt;
        azimuth = az;
    }

}
