package com.stellarcommand;

import de.sciss.net.OSCMessage;

/**
 * Define vocabulary that we use for OSC message transmission
 */
public final class StellarOSCVocabulary {

    /**
     * Get a String version of OSC message so we can display it
     * @param msg the OSC message
     * @return teh OSC name and arguments as a string
     */
    public static String getOscAsText(OSCMessage msg){
        String ret =  msg.getName();
        for (int i = 0; i < msg.getArgCount(); i++){
            ret += " " + msg.getArg(i);
        }

        return ret;
    }

    /**
     * ClientMessages are the messages sent from StellarCommand to the OSC client
     */
    public final class ClientMessages {
        /**
         * Notifies client of the UDP port that StellarCommand is listening to.
         *
         * The message contains a single int argumenr
         */
        public static final String OSC_PORT = "osc";

        /**
         * Returns the version information as Major, minor and build
         * <br>Major - int
         * <br> Minor - int
         * <br> Buils - int
         */
        public static final String VERSION = "version";
        /**
         * Notifies Client of the display being viewed in stellarium as three floats <br>
         * The OSC arguments are three floats measured in decimal degrees:
         * <br>The field of view being viewed
         * <br>The Right Ascension (Ra) of the centre of the display
         * <br> The Declination (Dec.) of the centre of the display
         */
        public static final String DISPLAY_VIEW = "view";

        /**
         * The message is used to indicate the number of bundles that are being used to send all the Stellar Data for a single query.
         * The reason is that there could be thousands of stars being returned which would be too large to fit a single bundle.
         * <br>The OSC arguments are two integers:
         * <br><br>The Bundle number for this section of the data. Zero based index count - eg, first bundle will have a value of zero
         * <br>The total number of bundles for current query
         */
        public static final String BUNDLE_COUNT = "bundleCount";

        /**
         * The list of the the column names for the data that will be in the values message
         * <br>OSC arguments will be a series of strings. EG
         * <br><br>RArad (deg) <br>DErad (deg) <br>pmRA(mas/yr) <br>pmDE(mas/yr) <br>Hpmag (mag) <br>B-V(mag)
         */
        public static final String STAR_NAMES = "names";

        /**
         * The data for a row of stars as floats. The column values correlate to the values in star names.
         * <br> For example, the following OSC argument 133.50998 -34.824913 -10.56 8.17 9.0556 0.042 would correlate to
         *<br>RA Dec. pmRA pmDE Hpmag ad  B-V
         */
        public static final String STAR_VALUES = "values";

        /**
         * The location that we are viewing for stellarium
         * OSC arguments will be latitude, longitude.
         * <br>latitude -  Degrees as float
         * <br>longitude - degrees as float
         * <br>altitude - meters as float
         * <br>planet - planet as string
         */
        public static final String OBSERVATION_POINT = "viewerObservationPoint";


        /**
         * The time that stellarium will be displaying
         * The format of the time will be a string
         * <br>UTC as String
         * <br> Local Time as String
         * <br> GMT Times shift as float in julian days - eg, a value of 0.5 indicates half a day ahead of GMT
         * <br>Time rate as float - Time rate multiplier. Eg if we are going 2x speed will have a value of 2
         */
        public static final String STELLAR_TIME = "time";


    }

    /**
     * CommandMessages are sent from OSC client to StellarCommand
     * Direct where our message will be filtered by StellarCommand
     */
    public final class CommandMessages {

        /**
         * See if stellarium is active. we will send back our OSC port and version informaition
         * Will cause current status to be sent
         */
        public static final String POLL = "poll";

        /**
         * Request the current view location.
         */
        public static final String DISPLAY_VIEW = "getView";

        /**
         * Set the Field of view in stellarium
         */
        public static final String FIELD_OF_VIEW = "fieldOfView";

        /**
         * Cause StellarCommand to exit
         */
        public static final String EXIT = "exit";

        /**
         * Defines a new filter parameter. All filters are ANDed together  <br>\
         * eg filter/Hpmagr/less 6 followed by filter/Hpmagr/greater 1 will filter Hpmagr between 6 and 1
         */
        public static final String FILTER = "filter";

        /**
         * Reset all filters on the query
         */
        public static final String RESET_FILTERS = "resetFilters";

        /**
         * Flag to indicate we want stars sent when a view changes. <br>
         * Add an OSC arg of zero to stop sending stars or non zero to send
         */
            public static final String SEND_STARS = "sendStars";

        /**
         * Request that a pre-stored VizieR table be loaded from file and sent
         */
        public static final String LOAD_TABLE = "loadTable";

        /**
         * Request that VizieR current table be saved to file
         */
        public static final String SAVE_TABLE = "saveTable";

        /**
         * The location that we want to view from in stellarium
         * If there are no OSC arguments, the current location will be sent to Client, otherwise
         * OSC arguments will be latitude, longitude, altitude and planet
         * <br>latitude -  Degrees as float
         * <br>longitude - degrees as float
         * <br>altitude - (optional)meters as float
         * <br>planet - (optional)planet as string
         */
        public static final String OBSERVATION_POINT = "viewerObservationPoint";

        /**
         * Causes stellarium to slew to the defined object
         * <br><b>Object name</b>String - Object name we want to display - eg Acrux or Saturn
         */
        public static final String VIEW_OBJECT = "viewObject";

        /**
         * Causes stellarium to slew to the RA / DEC
         * <br><b>RA</b> float - The Right Ascension in decimal degrees
         * <br><b>Dec</b> float - The Declination in decimal degrees
         */
        public static final String VIEW_RA_DEC = "viewRADec";

        /**
         * Causes stellarium view to centre to the altitude and azimuth. OSC arguments are:
         * <br><b>Altitude</b> float - altitude in degrees
         * <br><b>Azimuth</b> float - azimuth in degrees
         */
         public static final String VIEW_ALTAZ = "viewAltAz";


        /**
         * Causes stellarium view to centre to the altitude. OSC arguments are:
         * <br><b>Altitude</b> float - altitude in degrees
         */
        public static final String ALTITUDE = "altitude";


        /**
         * Causes stellarium view to centre to the  azimuth. OSC arguments are:
         * <br><b>Azimuth</b> float - altitude in degrees
         */
        public static final String AZIMUTH = "azimuth";

        /**
         * Cause stellarium to show or hide the ground. OSC arguments are:
         * <br><b>Show</b> int -  not zero is true, zero is false
         * <br> If no OSC arguments present, the current value will be returned
         */
        public static final String SHOW_GROUND = "showGround";

        /**
         * Cause stellarium to show or hide the atmosphere. OSC arguments are:
         * <br><b>Show</b> int -  not zero is true, zero is false
         * <br> If no OSC arguments present, the current value will be returned
         */
        public static final String SHOW_ATMOSPHERE = "showAtmosphere";

        /**
         * Cause stellarium to show or hide the constellation art. OSC arguments are:
         * <br><b>Show</b> int -  not zero is true, zero is false
         * <br> If no OSC arguments present, the current value will be returned
         */
        public static final String SHOW_CONSTELATION_ART = "showConstellationart";


        /**
         * Cause stellarium to show or hide the star labels. OSC arguments are:
         * <br><b>Show</b> int -  not zero is true, zero is false
         * <br> If no OSC arguments present, the current value will be returned
         */
        public static final String SHOW_STAR_LABELS = "showStarLabels";


        /**
         * Cause stellarium to move up or down. OSC arguments are:
         * <br><b>Amount</b> float -  the amount to move. A positive value will slew up
         */
        public static final String MOVE_UP_DOWN = "moveUD";

        /**
         * Cause stellarium to move left or right. OSC arguments are:
         * <br><b>Amount</b> float -  the amount to move. A positive value will slew to the right
         */
        public static final String MOVE_LEFT_RIGHT = "moveLR";

        /**
         * Cause Stellarium to progress in time as a mulitiple. Eg, a value of 2 will be 2x sumulation:
         * <br><b>time rate multiplier</b> float -  Normal time multiplier
         */
        public static final String SET_TIME_RATE = "timeRate";


        /**
         * Set the time on stellarium. This can be set with either a single string with full ISO time or as OSC arguments for the date and time
         * <br> If the first OSC agrgument is a string, the code will attempt to decode as ISO string
         * <br>ISO time as String OR Year as Integer
         * <br>Month as Integer
         * <br>Day of month as Integer
         * <br>Hour as Integer in 24 Hour time
         * <br> Minutes as integer
         * <br> Seconds as a float
         * <br> Optional Time zone as Z or +/-HHH:mm as a String. EG "Z" or "+10:00". If no argument, the local time of location will be used
         */
        public static final String STELLAR_TIME = "time";


        /**
         * Perform a VizieR query without querying Stellarium
         * <br>Field of fiew - float as decimal degrees
         *
         * If the second argument is a float, the second and tgird arguments are the RA and Dec
         * <br>If float RA - float as Right ascension in decimal degrees
         * <br>Dec - float as declination decimal degrees
         *
         * <br> if second arg is a string, the celstial object name - eg, Acrux
         */
        public static final String VIZIER_QUERY = "queryVizieR";

    }

    /**
     * The parameters we will use to define our filters <br>
     * The actual parameter filtered will be before with its value as the OSC argument  <br>
     * eg Hpmagr/less 6  will filter Hpmagr for values less than 6  <br>
     * All filters are ANDed together  <br>
     * eg filter/Hpmagr/less 6 followed by filter/Hpmagr/greater 1 will filter Hpmagr between 6 and 1
     */
    public final class FilterDirectives{
        /**
         * A less than filter.
         */
        public static final String LESS = "less";

        /**
         * A greater than filter
         */
        public static final String GREATER = "greater";

        /**
         * RESET will only filter the parameter <br>
         * eg Hpmagr/reset will only reset Hpmagr filters
         */
        public static final String RESET = "reset";
    }
}
