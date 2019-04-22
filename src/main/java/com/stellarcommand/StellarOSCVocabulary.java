package com.stellarcommand;

/**
 * Define vocabulary that we use for OSC message transmission
 */
public final class StellarOSCVocabulary {
    /**
     * SendMessages are the messages sent from StellarCommand to the OSC client
     */
    public final class SendMessages{
        /**
         * Notifies client of the UDP port that StellarCommand is listening to.
         *
         * The message contains a single int argumenr
         */
        public static final String OSC_PORT = "/osc";

        /**
         * Notifies Client of the display being viewed in Stellarium as three floats <br>
         * The OSC arguments are three floats measured in decimal degrees:
         * <br>The field of view being viewed
         * <br>The Right Ascension (Ra) of the centre of the display
         * <br> The Declination (Dec.) of the centre of the display
         */
        public static final String DISPLAY_VIEW = "/view";

        /**
         * The message is used to indicate the number of bundles that are being used to send all the Stellar Data for a single query.
         * The reason is that there could be thousands of stars being returned which would be too large to fit a single bundle.
         * <br>The OSC arguments are two integers:
         * <br><br>The Bundle number for this section of the data. One based index count - eg, first bundle will have a value of 1
         * <br>The total number of bundles for current query
         */
        public static final String BUNDLE_COUNT = "/bundlecount";

        /**
         * The list of the the column names for the data that will be in the values message
         * <br>OSC arguments will be a series of strings. EG
         * <br><br>RArad (deg) <br>DErad (deg) <br>pmRA(mas/yr) <br>pmDE(mas/yr) <br>Hpmag (mag) <br>B-V(mag)
         */
        public static final String STAR_NAMES = "/names";

        /**
         * The data for a row of stars as floats. The column values correlate to the values in star names.
         * <br> For example, the following OSC argument 133.50998 -34.824913 -10.56 8.17 9.0556 0.042 would correlate to
         *<br>RA Dec. pmRA pmDE Hpmag ad  B-V
         */
        public static final String STAR_VALUES = "/values";
    }

    /**
     * ReceiveMessages are sent from OSC client to StellarCommand
     * Direct where our message will be filtered
     */
    public final class ReceiveMessages{
        /**
         * Defines a new filter parameter. All filters are ANDed together  <br>\
         * eg filter/Hpmagr/less 6 followed by filter/Hpmagr/greater 1 will filter Hpmagr between 6 and 1
         */
        public static final String FILTER = "filter";

        /**
         * Reset all filters on the query
         */
        public static final String RESET_FILTERS = "resetfilters";

        /**
         * Flag to indicate we want stars sent when a view changes. <br>
         * Add an OSC arg of zero to stop sending stars or non zero to send
         */
        public static final String SEND_STARS = "sendstars";

        /**
         * Request that a pre-stored VizieR table be loaded from file and sent
         */
        public static final String LOAD_TABLE = "loadtable";

        /**
         * Request that VizieR current table be saved to file
         */
        public static final String SAVE_TABLE = "savetable";
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
