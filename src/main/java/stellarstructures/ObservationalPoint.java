package stellarstructures;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Structure to contain Location and date time
 */
public class ObservationalPoint {


    double geographicLongitude;
    double geographicLatitude;
    double localSiderealTime;
    ZonedDateTime observationDate;

    public static final ZonedDateTime J200_REFERENCE_DATE = ZonedDateTime.parse("2000-01-01T12:00:00Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    public static final double J200_REFERENCE_JDAY = 2451545;


    /**
     * Calculate our Julian Day based on dat time provided
     * @param time the Time we are comparing to
     * @return the Julian Day value
     */
    public static double calulateJulianDay(ZonedDateTime time){
        Duration time_diff = Duration.between(J200_REFERENCE_DATE, time);
        double seconds = time_diff.getSeconds();
        double nanoseconds =  time_diff.getNano();

        double calc_days = seconds / 24d / 60d / 60d;

        // we need to now add our nano seconds to the amount.
        nanoseconds = nanoseconds / 24d / 60d / 60d / 1000000000d;

        return J200_REFERENCE_JDAY + calc_days + nanoseconds;
    }

    /**
     * Just do a test to check ACrux at Brazil Time
     "latitude":-22.902780532836914,"longitude":-43.207500457763672,
     * @param args NA
     */
    public static void main(String[] args) {

        ObservationalPoint observationalPoint = ObservationalPoint.brazilRepublicPoint();
        // 0h25m19.34s/-77°11'13.0\"
        double RA_hours = 0;
        double RA_min = 20;
        double RA_sec = 48.12;

        double DEC_deg = -77;
        double DEC_min = 47;
        double DEC_sec = 42.4;

        double test_ra = (RA_hours + RA_min / 60d + RA_sec / 3600) * 15;

        double test_dec;
        if (DEC_deg < 0){
            test_dec = DEC_deg - DEC_min / 60 - DEC_sec / 3600;
        }
        else
        {
            test_dec = DEC_deg + DEC_min / 60 + DEC_sec / 3600;
        }

        AltAz calc = StellarConversions.convertRaDecToAltAz(new RaDec(test_ra, test_dec), observationalPoint);

        System.out.println("Alt: "  + calc.altitude + " Az: " + calc.azimuth);


        // try Spica
        /*
        <h2>Spica (Azimech)<br />α Vir - 67 Vir - HIP 65474 - SAO 157923 - HD 116658 - HR 5056 - WDS J13252-1110AB</h2>Type: <b>variable star, double star</b> (ELL+BCEP)<br />Magnitude: <b>0.95</b><br />Absolute Magnitude: -3.47<br />Color Index (B-V): <b>-0.25</b><br />Magnitude range: <b>0.95</b>÷<b>1.05/1.03</b> (Photometric system: V)<br />
        RA/Dec (J2000.0):    13h25m11.90s/-11°09'36.7\"<br>RA/Dec (on date):    13h19m24.62s/-10°35'16.4\"<br>HA/Dec:    23h03m12.73s/-10°35'16.4\"  <br>Az./Alt.: +50°05'46.8\"/+71°41'00.7\"  <br>Gal. long./lat.: -43°53'07.0\"/+50°50'43.4\"<br>Supergal. long./lat.: +129°15'48.8\"/+4°06'24.0\"<br>Ecl. long./lat. (J2000.0): +203°50'31.8\"/-2°03'10.7\"<br>Ecl. long./lat. (on date): +202°18'35.0\"/-2°02'45.8\"<br>Ecliptic obliquity (on date): +23°27'12.1\"<br>Mean Sidereal Time: 12h22m38.5s<br>Apparent Sidereal Time: 12h22m37.3s<br>IAU Constellation: Vir<br>Distance: 249.74 ly<br />Spectral Type: B1IV<br />Parallax: 0.01306\"<br />Period: 4.0146 days<br />Next maximum light: 1889-11-18 03:20:46 UTC<br />Position angle (2007): 32.00°<br />Separation (2007): 152.500\" (+0°02'32\")<br />Proper motions by axes: -54.7 -36.8 (mas/yr)<br />Position angle of the proper motion: 236.1°<br />Angular speed of the proper motion: 65.9 (mas/yr)","time":{"deltaT":-7.9562905016417136e-05,"gmtShift":-0.12949074308077493,"isTimeNow":false,"jday":2411321.9836574094,"local":"1889-11-15T08:30:00.000","timeZone":"UTC-03:06","timerate":0,"utc":"1889-11-15T11:36:28.000Z"},"view":{"fov":113.73351327853156}}Angelos-MacBook-Pro:StellariumScripts angelofraietta$

         */
        RA_hours = 13;
        RA_min = 25;
        RA_sec = 11.9;

        DEC_deg = -11;
        DEC_min = 9;
        DEC_sec = 36.7;

        test_ra = (RA_hours + RA_min / 60d + RA_sec / 3600) * 15;


        if (DEC_deg < 0){
            test_dec = DEC_deg - DEC_min / 60 - DEC_sec / 3600;
        }
        else
        {
            test_dec = DEC_deg + DEC_min / 60 + DEC_sec / 3600;
        }


         calc = StellarConversions.convertRaDecToAltAz(new RaDec(test_ra, test_dec), observationalPoint);

        System.out.println("Alt: "  + calc.altitude + " Az: " + calc.azimuth);

    }

    /**
     * Constructor
     * @param latitude geographic latitude
     * @param longitude geographic longitude
     * @param utc_observation_date the observation date we are using in UTC
     */
    public ObservationalPoint (double latitude, double longitude, ZonedDateTime utc_observation_date){
        geographicLatitude = latitude;
        geographicLongitude = longitude;
        observationDate = utc_observation_date;
        final double dayOffset = ChronoUnit.DAYS.between(J200_REFERENCE_DATE, observationDate);
        localSiderealTime = (100.46 + 0.985647 * dayOffset + longitude + 15 * (observationDate.getHour() + observationDate.getMinute() / 60d + observationDate.getSecond() / 3600) + 360) % 360;


    }

    /**
     * Gets locational data of 'Rio de Janeiro, Brazil' '1889-11-15T08:30:00', 'local'
     * "latitude":-22.902780532836914,"longitude":-43.207500457763672,"name":"Rio de Janeiro"
     * @return Julian date and geographic location of Rio at Announcement of republic
     */
    public static ObservationalPoint brazilRepublicPoint(){
        // do a test with     core.setObserverLocation('Rio de Janeiro, Brazil');
        //    core.setDate ('1889-11-15T08:30:00', 'local');
        final ZonedDateTime observation_date = ZonedDateTime.parse("1889-11-15T11:36:28Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        final double LATITUDE = -22.902780532836914;
        final double LONGITUDE = -43.207500457763672;

        return new ObservationalPoint(LATITUDE, LONGITUDE, observation_date);
    }

}
