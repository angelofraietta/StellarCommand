package StellarStructures;

/**
 * Structure to contain Right Ascension and Declination
 */
public class RaDec {
    public final double rightAscension;
    public final double declination;

    /**
     * Constructor
     * @param ra Right Ascension
     * @param dec Declination
     */
    public RaDec(double ra, double dec){
        rightAscension = ra;
        declination = dec;
    }

    /**
     * Create RA and Dec based on Stellarium's three dimensional spherical points using J2000
     * ""j2000":"[-0.449409, -0.0523899, -0.891789]
     * @param x x coordinate from Stellarium
     * @param y y coordinate from Stellarium
     * @param z z coordinate from Stellarium
     */
    public RaDec(double x, double y, double z){
        declination =  Math.asin(z) / Math.PI * 180;

        rightAscension = (((Math.atan2(x, y) * 180 / Math.PI -90) * -1 / 360 * 24) + 24) % 24 * 15;

    }

    /**
     * Get our known RA and Dec of Acrux to do comparisons
     * @return RaDec of acrux
     */
    public static RaDec getAcrux(){
        return new RaDec(186.64924916933296, -63.09891984127304);
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RaDec))
            return false;
        if (obj == this)
            return true;


        RaDec rhs = (RaDec) obj;

        return rhs.declination == declination && rhs.rightAscension == rightAscension;
    }

    /**
     * Test that RA and Dec calculate correctly for Acrux focused in Stellarium
     * @param args
     */
    public static void main(String[] args) {
        // We will test for acrux
        // Stellarium should give us
        //""j2000":"[-0.449409, -0.0523899, -0.891789]
        double x = -0.449409, y = -0.0523899, z = -0.891789;
        RaDec celestial_acrux = new RaDec(x, y, z);
        if (celestial_acrux.declination != getAcrux().declination){
            System.out.println("error Dec.");
        }
        if (celestial_acrux.rightAscension != getAcrux().rightAscension){
            System.out.println("error Ra");
        }
        //System.out.println(celestial_acrux.rightAscension + " " + celestial_acrux.declination);

    }
}
