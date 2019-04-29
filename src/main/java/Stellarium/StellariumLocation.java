package Stellarium;

import StellarStructures.RaDec;
import org.json.JSONObject;

/**
 * Displays what we are seeing on view as far as field of view ands RaDec
 */
public class StellariumLocation {

    /* From API
        location : {
        name,
        role,
        planet,
        latitude,
        longitude,
        altitude,
        country,
        state,
        landscapeKey
    }
     */

    private JSONObject apiObject = null;
    float latitude, longitude;

    /**
     * Get latitude in degrees
     * @return viewer latitude
     */
    public float getLatitude() {
        return latitude;
    }


    /**
     * Get longitude in degrees
     * @return viewer longitude
     */
    public float getLongitude() {
        return longitude;
    }




    /**
     * Get the LandscapeKey of observation Point
     * @return LandscapeKey observation point name
     */
    public String getLandcapeKey(){
        String ret = "";

        if (apiObject != null){
            Object o = apiObject.get("landscapeKey");
            if (o != null) {
                ret = o.toString();
            }
        }
        return ret;
    }


    /**
     * Get the state name of observation Point
     * @return state observation point name
     */
    public String getState(){
        String ret = "";

        if (apiObject != null){
            Object o = apiObject.get("state");
            if (o != null) {
                ret = o.toString();
            }
        }
        return ret;
    }

    /**
     * Get the country name of observation Point
     * @return country observation point name
     */
    public String getCountry(){
        String ret = "";

        if (apiObject != null){
            Object o = apiObject.get("planet");
            if (o != null) {
                ret = o.toString();
            }
        }
        return ret;
    }

    /**
     * Get the Stellarium planhet we are observing from
     * @return Stellarium planet
     */
    public String getPlanet(){
        String ret = "";

        if (apiObject != null){
            Object o = apiObject.get("planet");
            if (o != null) {
                ret = o.toString();
            }
        }
        return ret;
    }


    /**
     * Get the Stellarium role of observation Point
     * @return Stellarium role name
     */
    public String getRole(){
        String ret = "";

        if (apiObject != null){
            Object o = apiObject.get("role");
            if (o != null) {
                ret = o.toString();
            }
        }
        return ret;
    }

    /**
     * Get the Stellarium name of observation Point
     * @return Stellarium observation point name
     */
    public String getName(){
        String ret = "";

        if (apiObject != null){
            Object o = apiObject.get("name");
            if (o != null) {
                ret = o.toString();
            }
        }
        return ret;
    }

    /**
     * Get the altitude from Stellarium Observation point
     * @return altitude
     */
    public float getAltitude(){
        float ret = 0;

        if (apiObject != null){
            Object o = apiObject.get("altitude");
            if (o != null) {
                ret = Float.parseFloat(o.toString());
            }
        }
        return ret;
    }

    /**
     * Constructor based on JSONObject obtained from StellariumRemoteControl API call
     * @param jsonObject the JSONObject returned from StellariumAPI
     */
    public StellariumLocation(JSONObject jsonObject){
        apiObject = jsonObject;

        Object lat = jsonObject.get("latitude");
        if (lat != null) {
            String val_str = lat.toString();
            latitude = Float.parseFloat(val_str);
        }

        Object lon = jsonObject.get("longitude");
        if (lon != null) {
            String val_str = lon.toString();
            longitude = Float.parseFloat(val_str);
        }

    }
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
