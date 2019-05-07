package stellarium;

import org.json.JSONObject;

/**
 * The graphic properties displayed in stellarium. Only the very minimum are
 * defined. Add more as project progresses
 */
public class StellariumProperty extends StellariumJSONClass {
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
        return getPropertyBooleanValue("actionShow_Atmosphere");
    }

    /**
     * Return whether we are showing ground
     * @return true if showing, otherwise, false
     */
    public boolean getShowGround(){
        return getPropertyBooleanValue("actionShow_Ground");
    }

    /**
     * Return whether we are showing ground
     * @return true if showing, otherwise, false
     */
    public boolean getShowStarLabels(){
        return getPropertyBooleanValue("actionShow_Stars_Labels");
    }

    /**
     * Return whether we are showing constellation art
     * @return true if showing, otherwise, false
     */
    public boolean getShowConstellationArt(){
        return getPropertyBooleanValue("actionShow_Constellation_Art");
    }

}
