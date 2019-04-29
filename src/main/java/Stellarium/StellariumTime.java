package Stellarium;

import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StellariumTime extends StellariumJSONClass{

    /*
    time : {
jday, //current Julian day
deltaT, //current deltaT as determined by the current dT algorithm
gmtShift, //the timezone shift to GMT
timeZone, //the timezone name
utc, //the time in UTC time zone as ISO8601 time string
local, //the time in local time zone as ISO8601 time string
isTimeNow, //if true, the Stellarium time equals the current real-world time
timerate //the current time rate (in secs)
     */

    /**
     * Class containing Time objext from RemoteApi
     * @param jsonObject the JSON object from Stellarium API call
     */
    public StellariumTime(JSONObject jsonObject){
        super(jsonObject);
    }


    /**
     * Get the Julian Day
     * @return julian day
     */
    public float getJulianDay(){
        return getFloatVal("jday");
    }

    /**
     * Get current deltaT as determined by the current dT algorithm
     * @return current deltaT as determined by the current dT algorithm
     */
    public float getDeltaT(){
        return getFloatVal("deltaT");
    }

    /**
     * Get the the timezone shift to GMT
     * @return the timezone shift to GMT
     */
    public float getGMTShift(){
        return getFloatVal("gmtShift");
    }

    /**
     * Get the timezone name
     * @return the timezone name
     */
    String getTimeZone(){
        return getStringVal("timeZone");
    }

    /**
     * Get the UTC time as a string
     * @return the UTC time
     */
    public String utcString(){
        return getStringVal("utc");
    }


    /**
     * Get the Local time as a string
     * @return the Local time
     */
    public String localTimeString(){
        return getStringVal("local");
    }

    /**
     * Get UTC time as a Date/time
     * @return UTC time
     */
    public LocalDateTime utcTime(){
        LocalDateTime ret = null;

        String utc_time = utcString();
        if (!utc_time.isEmpty()){
            ret = LocalDateTime.parse(utc_time, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }

        return ret;
    }


    /**
     * Get Local time as a Date/time
     * @return Local time
     */
    public LocalDateTime localTime(){
        LocalDateTime ret = null;

        String time = localTimeString();
        if (!time.isEmpty()){
            ret = LocalDateTime.parse(time, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }

        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof StellariumTime))
            return false;
        if (obj == this)
            return true;


        StellariumTime rhs = (StellariumTime) obj;

        return utcString().equalsIgnoreCase(rhs.utcString());
    }
}
