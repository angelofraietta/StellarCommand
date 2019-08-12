package stellarium;

import org.json.JSONObject;

/**
 * The graphic properties displayed in stellarium. Only the very minimum are
 * defined. Add more as project progresses
 */
public class StellariumProperty extends StellariumJSONClass {

    public class Show {
        public static final String ATMOSTPHERE = "actionShow_Atmosphere";
        public static final String GROUND = "actionShow_Ground";
        public static final String STAR_LABELS = "actionShow_Stars_Labels";
        public static final String CONSTELATION_ART = "actionShow_Constellation_Art";
        public static final String CARDINAL_POINTS = "actionShow_Cardinal_Points";
        public static final String EQUATORIAL_GRID = "actionShow_Equatorial_Grid";

        public static final String ASTERISM_LABELS = "actionShow_Asterism_Labels";
        public static final String ASTERISM_LINES =  "actionShow_Asterism_Lines";
        public static final String ASTRO_CALC_WINDOW =  "actionShow_AstroCalc_Window_Global";
        public static final String ASZIMUTHAL_GRID =   "actionShow_Azimuthal_Grid";
        public static final String BOOKMARKS_WINDOW =  "actionShow_Bookmarks_Window_Global";
        public static final String CELESTIAL_J2000_POLES =  "actionShow_Celestial_J2000_Poles";
        public static final String CELESTIAL_POLES =  "actionShow_Celestial_Poles";
        public static final String CIRCUMPOLAR_CIRCLES =  "actionShow_Circumpolar_Circles";
        public static final String COLURE_LINES =  "actionShow_Colure_Lines";
        public static final String CONFIGURATION_WINDOW =  "actionShow_Configuration_Window_Global";
        public static final String CONSTELLATION_BOUNDARIES =   "actionShow_Constellation_Boundaries";
        public static final String CONSTELLATION_ISOLATED =  "actionShow_Constellation_Isolated";
        public static final String CONSTELLATION_LABELS =   "actionShow_Constellation_Labels";
        public static final String CONSTELLATION_LINES =  "actionShow_Constellation_Lines";
        public static final String DSO_TEXTURES =  "actionShow_DSO_Textures";
        public static final String DATE_TIME_WINDOW_GLOBAL =   "actionShow_DateTime_Window_Global";
        public static final String ECLIPTIC_GRID =  "actionShow_Ecliptic_Grid";
        public static final String ECLIPTIC_J2000_GRID =  "actionShow_Ecliptic_J2000_Grid";
        public static final String ECLIPTIC_J2000_LINE =  "actionShow_Ecliptic_J2000_Line";
        public static final String ECLIPTIC_J2000_POLES =  "actionShow_Ecliptic_J2000_Poles";
        public static final String ECLIPTIC_LINE =  "actionShow_Ecliptic_Line";
        public static final String ECLIPTIC_POLES = "actionShow_Ecliptic_Poles";
        public static final String EQUATOR_J2000_LINE =  "actionShow_Equator_J2000_Line";
        public static final String EQUATOR_LINE =  "actionShow_Equator_Line";
        public static final String EQUATORIAL_J2000_GRID =  "actionShow_Equatorial_J2000_Grid";
        public static final String EQUINOX_J2000_POINTS =  "actionShow_Equinox_J2000_Points";
        public static final String EQUINOX_POINTS = "actionShow_Equinox_Points";
        public static final String EXOPLANETS =  "actionShow_Exoplanets";
        public static final String EXOPLANETS_CONFIG_DIALOG =  "actionShow_Exoplanets_ConfigDialog";
        public static final String FOG =  "actionShow_Fog";
        public static final String GALACTIC_EQUATOR_LINE =  "actionShow_Galactic_Equator_Line";
        public static final String GALACTIC_GRID =  "actionShow_Galactic_Grid";
        public static final String GALACTIC_POLES =  "actionShow_Galactic_Poles";
        public static final String GRIDLINES =  "actionShow_Gridlines";
        public static final String HELP_WINDOW_GLOBAL =  "actionShow_Help_Window_Global";
        public static final String HIPS_SURVEYS =  "actionShow_Hips_Surveys";
        public static final String HORIZON_LINE =  "actionShow_Horizon_Line";
        public static final String LANDSCAPE_ILLUMINATION =  "actionShow_LandscapeIllumination";
        public static final String LANDSCAPE_LABELS =  "actionShow_LandscapeLabels";
        public static final String LIGHTPOLUTION_FROM_DATABASE =  "actionShow_LightPollutionFromDatabase";
        public static final String LOCATION_WINDOW_GLOBAL =  "actionShow_Location_Window_Global";
        public static final String LONGITUDE_LINE =  "actionShow_Longitude_Line";
        public static final String MERIDIAN_LINE =  "actionShow_Meridian_Line";
        public static final String METEOR_SHOWERS =  "actionShow_MeteorShowers";
        public static final String METEOR_SHOWERS_CONFIG_DIALOG =  "actionShow_MeteorShowers_config_dialog";
        public static final String METEOR_SHOWERS_LABELS =  "actionShow_MeteorShowers_labels";
        public static final String METEOR_SHOWERS_SEARCH_DIALOG = "actionShow_MeteorShowers_search_dialog";
        public static final String MILKY_WAY =  "actionShow_MilkyWay";
        public static final String NEBULAS =  "actionShow_Nebulas";
        public static final String NIGHT_MODE =   "actionShow_Night_Mode";
        public static final String NOVAE_CONFIG_DIALOG =  "actionShow_Novae_ConfigDialog";
        public static final String OCULAR =  "actionShow_Ocular";
        public static final String OCULAR_CROSSHAIRS =  "actionShow_Ocular_Crosshairs";
        public static final String PLANETS =  "actionShow_Planets";
        public static final String PLANETS_HINTS =  "actionShow_Planets_Hints";
        public static final String PLANETS_LABELS =  "actionShow_Planets_Labels";
        public static final String PLANETS_NOMENCLATURE =  "actionShow_Planets_Nomenclature";
        public static final String PLANETS_ORBITS =  "actionShow_Planets_Orbits";
        public static final String PLANETS_POIMNTERS =  "actionShow_Planets_Pointers";
        public static final String PLANETS_TRAILS =  "actionShow_Planets_Trails";
        public static final String PRECESSION_CIRCLES =  "actionShow_Precession_Circles";
        public static final String PRIME_VERTICAL_LINE =  "actionShow_Prime_Vertical_Line";
        public static final String RAY_HELPERS =  "actionShow_Ray_Helpers";
        public static final String REMOTE_CONTROL =  "actionShow_Remote_Control";
        public static final String SATELLITE_CONFIG_DIALOG_GLOBAL =  "actionShow_Satellite_ConfigDialog_Global";
        public static final String SATELLITE_HINTS =  "actionShow_Satellite_Hints";
        public static final String SATELLITE_LABELS =  "actionShow_Satellite_Labels";
        public static final String SCRIPTCONSOLE_WINDOW_GLOBAL =  "actionShow_ScriptConsole_Window_Global";
        public static final String SEARCH_WINDOW_GLOBAL =  "actionShow_Search_Window_Global";
        public static final String SENSOR =  "actionShow_Sensor";
        public static final String SHORTCUTS_WINDOW_GLOBAL =  "actionShow_Shortcuts_Window_Global";
        public static final String SKYVIEW_WINDOW_GLOBAL =  "actionShow_SkyView_Window_Global";
        public static final String SKYCULTURE_NATIVE_PLANET_NAMES =  "actionShow_Skyculture_NativePlanetNames";
        public static final String SOLSTICE_J2000_POINTS =  "actionShow_Solstice_J2000_Points";
        public static final String SOLSTICE_POINTS =  "actionShow_Solstice_Points";
        public static final String STARS =  "actionShow_Stars";

        public static final String SUPERGALACTIC_EQUATOR_LINE =  "actionShow_Supergalactic_Equator_Line";
        public static final String SUPERGALACTIC_GRID =  "actionShow_Supergalactic_Grid";
        public static final String SUPERGALACTIC_POLES =  "actionShow_Supergalactic_Poles";
        public static final String TELRAD =  "actionShow_Telrad";
        public static final String TOAST_SURVEY = "actionShow_Toast_Survey";
        public static final String NADIR =  "actionShow_Zenith_Nadir";
        public static final String ZODIACAL_LIGHT =  "actionShow_ZodiacalLight";

    }

    public StellariumProperty(JSONObject jsonObject) {
        super(jsonObject);
    }

    /**
     * Get the boolean property vale for given property
     * @param property_name name of property
     * @return the boolean value of property
     */
    boolean getPropertyBooleanValue(String property_name){
        boolean ret = false;
        if (apiObject != null){
            JSONObject o = apiObject.getJSONObject(property_name);
            if (o != null) {
                Object val = o.get("value");
                if (val != null){
                    ret = (boolean)val;

                }
            }
        }
        return ret;
    }

    /**
     * Return whether we are showing atmosphere
     * @return true if showing, otherwise, false
     */
    public boolean getShowAtmosphere(){
        return getPropertyBooleanValue(Show.ATMOSTPHERE);
    }

    /**
     * Return whether we are showing ground
     * @return true if showing, otherwise, false
     */
    public boolean getShowGround(){
        return getPropertyBooleanValue(Show.GROUND);
    }

    /**
     * Return whether we are showing ground
     * @return true if showing, otherwise, false
     */
    public boolean getShowStarLabels(){
        return getPropertyBooleanValue(Show.STAR_LABELS);
    }

    /**
     * Return whether we are showing constellation art
     * @return true if showing, otherwise, false
     */
    public boolean getShowConstellationArt(){
        return getPropertyBooleanValue(Show.CONSTELATION_ART);
    }


    /**
     * Return whether we are showing cardinal Points
     * @return true if showing, otherwise, false
     */
    public boolean getShowCardinalPoints(){
        return getPropertyBooleanValue(Show.CARDINAL_POINTS);
    }

    /**
     * Return whether we are showing equatorial grid
     * @return true if showing, otherwise, false
     */
    public boolean getShowEquatorialGrid(){
        return getPropertyBooleanValue(Show.EQUATORIAL_GRID);
    }


}
