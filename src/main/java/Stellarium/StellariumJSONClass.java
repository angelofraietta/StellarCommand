package Stellarium;

import org.json.JSONObject;

public class StellariumJSONClass {


    JSONObject apiObject = null;


    public StellariumJSONClass(JSONObject jsonObject){
        apiObject = jsonObject;
    }

    /**
     * Get a float value for a parameter
     * @param name name of parameter
     * @return a float value for it. Returns zero if does not exists
     */
    public float getFloatVal(String name){
        float ret = 0;

        if (apiObject != null){
            Object o = apiObject.get(name);
            if (o != null) {
                ret = Float.parseFloat(o.toString());
            }
        }
        return ret;
    }

    /**
     * Get a String value for a parameter
     * @param name name of parameter
     * @return a String value for it. Returns "" if does not exists
     */
    public String getStringVal(String name){
        String ret = "";

        if (apiObject != null){
            Object o = apiObject.get(name);
            if (o != null) {
                ret = o.toString();
            }
        }
        return ret;
    }
}