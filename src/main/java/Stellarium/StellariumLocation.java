package Stellarium;

import StellarStructures.RaDec;

/**
 * Displays what we are seeing on view as far as field of view ands RaDec
 */
public class StellariumLocation {
    /**
     * Get latitude in degrees
     * @return viewer latitude
     */
    public float getLatitude() {
        return latitude;
    }

    /**
     * set latitude in degrees
     * @param latitude the latitude of viewer
     */
    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    /**
     * Get longitude in degrees
     * @return viewer longitude
     */
    public float getLongitude() {
        return longitude;
    }

    /**
     * set longitude in degrees
     * @param longitude the longitude of viewer
     */
    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }


    float latitude;
    float longitude;


    /**
     * Constructor
     * @param latitude the latitude of viewer in stellarium in degrees
     * @param longitude the latitude of viewer in stellarium in degrees
     */
    public StellariumLocation(float latitude, float longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof StellariumLocation))
            return false;
        if (obj == this)
            return true;


        StellariumLocation rhs = (StellariumLocation) obj;

        return rhs.latitude == latitude && rhs.longitude == longitude;
    }
}
