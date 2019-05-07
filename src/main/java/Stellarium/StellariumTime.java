package Stellarium;

import StellarStructures.ObservationalPoint;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
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
timerate //the current time rate (in julian days per second secs)
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
    public double getJulianDay(){
        return getDoubleVal("jday");
    }

    /**
     * Get current deltaT as determined by the current dT algorithm
     * @return current deltaT as determined by the current dT algorithm
     */
    public float getDeltaT(){
        return getFloatVal("deltaT");
    }

    /**
     * Gets the current time rate in Julian days per second
     * @return Jukian days per second
     */
    public double getTimeRate(){return getDoubleVal("timerate");}
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
    public ZonedDateTime utcTime(){
        ZonedDateTime ret = null;

        String utc_time = utcString();
        if (!utc_time.isEmpty()){
            ret = ZonedDateTime.parse(utc_time, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
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

        double current_jday = getJulianDay();
        double prev_j_day = rhs.getJulianDay();
        boolean ret = current_jday == prev_j_day;

        double j_day_def = ObservationalPoint.calulateJulianDay(utcTime());
        System.out.println(j_day_def);
        return ret;
    }
}
