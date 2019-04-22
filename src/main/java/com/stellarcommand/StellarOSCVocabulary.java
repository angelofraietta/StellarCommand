package com.stellarcommand;

/**
 * Define vocabulary that we use for OSC message transmission
 */
public final class StellarOSCVocabulary {
    public final class SendMessages{
        public static final String OSC_PORT = "/osc";
        public static final String DISPLAY_VIEW = "/view";
        public static final String STAR_NAMES = "/names";
        public static final String STAR_VALUES = "/values";
    }

    /**
     * Direct where our message will be filtered
     */
    public final class ReceiveMessages{
        /**
         * Defines a new filter parameter. All filters are ANDed together  <br>\
         * eg filter/Hpmagr/less 6 followed by filter/Hpmagr/greater 1 will filter Hpmagr between 6 and 1
         */
        public static final String FILTER = "filter";

        /**
         * Reset all filters
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
        public static final String LESS = "less";
        public static final String GREATER = "greater";
        /**
         * RESET will only filter the parameter <br>
         * eg Hpmagr/reset will only reset Hpmagr filters
         */
        public static final String RESET = "reset";
    }
}
