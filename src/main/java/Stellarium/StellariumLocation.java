package Stellarium;

import StellarStructures.RaDec;
import org.json.JSONObject;

/**
 * Displays what we are seeing on view as far as field of view ands RaDec
 */
public class StellariumLocation  extends StellariumJSONClass{

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
        return getStringVal("landscapeKey");
    }


    /**
     * Get the state name of observation Point
     * @return state observation point name
     */
    public String getState(){
        return getStringVal("state");
    }

    /**
     * Get the country name of observation Point
     * @return country observation point name
     */
    public String getCountry(){
        return getStringVal("country");
    }

    /**
     * Get the Stellarium planhet we are observing from
     * @return Stellarium planet
     */
    public String getPlanet(){
        return getStringVal("planet");
    }


    /**
     * Get the Stellarium role of observation Point
     * @return Stellarium role name
     */
    public String getRole(){
        return getStringVal("role");
    }

    /**
     * Get the Stellarium name of observation Point
     * @return Stellarium observation point name
     */
    public String getName(){

        return getStringVal("name");
    }

    /**
     * Get the altitude from Stellarium Observation point
     * @return altitude
     */
    public float getAltitude(){
        return getFloatVal("altitude");
    }

    /**
     * Constructor based on JSONObject obtained from StellariumRemoteControl API call
     * @param jsonObject the JSONObject returned from StellariumAPI
     */
    public StellariumLocation(JSONObject jsonObject){
        super(jsonObject);
        latitude = getFloatVal("latitude");

        longitude = getFloatVal("longitude");

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
