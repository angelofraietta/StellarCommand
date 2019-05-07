package stellarstructures;

/**
 * Utility class for performing Conversions
 */
public class StellarConversions {

    /**
     * Get the RA and dec of a position given x, y and z from stellarium 3D Vector
     * eg j2000":"[-0.449409, -0.0523899, -0.891789]
     * @param x x axis
     * @param y y axix
     * @param z z axis
     * @return
     */
    static RaDec convertStellariumView(double x, double y, double z){
        double dec =  Math.asin(z) / Math.PI * 180;

        double ra = (((Math.atan2(x, y) * 180 / Math.PI -90) * -1 / 360 * 24) + 24) % 24 * 15;
        return new RaDec(ra, dec);

    }


    /**
     * Just do a test to check ACrux at Brazil Time
     * Ra :186.64924916933296 dec:-63.09891984127304
     * @param args NA
     */
    public static void main(String[] args) {
        //"j2000":"[-0.449386, -0.0524037, -0.891799]
        double x = -0.449386, y = -0.0524037, z =-0.891799;
        RaDec raDec =  convertStellariumView(x, y, z);

        ObservationalPoint observationalPoint = ObservationalPoint.brazilRepublicPoint();

        System.out.println("Ra :" + raDec.rightAscension + " dec:" + raDec.declination);

        AltAz altAz = convertRaDecToAltAz(raDec, observationalPoint);
        System.out.println("Az:" + altAz.azimuth + "Alt:" + altAz.altitude);

        // "j2000":"[-0.544319, -0.130689, 0.828636]
        x = -0.538491;
        y = -0.117401;
        z = 0.834413;


        raDec =  convertStellariumView(x, y, z);
        altAz = convertRaDecToAltAz(raDec, observationalPoint);
        System.out.println("Az:" + altAz.azimuth + "Alt:" + altAz.altitude);
    }



    /**
     * Get an Objects Altitude and azimuth given its RA, Dec, geographic Location and date
     * @param raDec Right ascension and declination
     * @param observationalPoint geographic location and julian date
     * @return Altitude and azimuth
     */
    public static AltAz convertRaDecToAltAz(RaDec raDec, ObservationalPoint observationalPoint){
        // Hour Angle
        final double DEG_CONVERT =  Math.PI / 180;

        double HA = (observationalPoint.localSiderealAngle - raDec.rightAscension + 360) % 360;

        double sinHa = Math.sin(HA * DEG_CONVERT);

        //sin(ALT) = sin(DEC)*sin(LAT)+cos(DEC)*cos(LAT)*cos(HA)
        double a_alt = Math.sin(raDec.declination * DEG_CONVERT) * Math.sin(observationalPoint.geographicLatitude * DEG_CONVERT) + Math.cos(DEG_CONVERT * raDec.declination) * Math.cos(observationalPoint.geographicLatitude * DEG_CONVERT) * Math.cos(HA * DEG_CONVERT);
        double alt = Math.asin(a_alt) / DEG_CONVERT;


        /*
                       sin(DEC) - sin(ALT)*sin(LAT)
        cos(A)   =   ---------------------------------
                        cos(ALT)*cos(LAT)

        A = acos(A)

         */
        double a_az = (Math.sin(raDec.declination * DEG_CONVERT) - Math.sin(alt * DEG_CONVERT) * Math.sin(observationalPoint.geographicLatitude * DEG_CONVERT))/
                (Math.cos(alt * DEG_CONVERT) * Math.cos(observationalPoint.geographicLatitude * DEG_CONVERT));

        double az =  Math.acos(a_az)  / DEG_CONVERT;
        if (sinHa > 0){
            az = 360 - az;
        }

        return new AltAz(alt, az);
    }
}
