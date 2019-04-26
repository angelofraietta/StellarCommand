package Stellarium;


import StellarStructures.RaDec;
import org.json.JSONObject;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StellariumSlave  {

    static public String DEFAULT_STELLARIUM_HOST = "localhost";
    static public int DEFAULT_STELLARIUM_PORT = 8090;

    double currentAz = 0;
    double currentAlt = 0; // zero is level. 1 is Up, -1 is down, 2 is behind

    Object altAzSynchroniser = new Object();
    boolean exitThread = false;


    Object fovSynchroniser = new Object();
    Object timerateSynchroniser = new Object();
    Object targetSynchroniser = new Object();
    Object magnitudeSynchroniser = new Object();

    Object pollSynchroniser = new Object();

    private double fieldOfView  = 1;

    String stellariumDevice = DEFAULT_STELLARIUM_HOST;

    int stellariumPort = DEFAULT_STELLARIUM_PORT;

    /**
     * The port to communicate on HTTP to get to stellarium
     * @return the HTTP port we are using
     */
    public int getStellariumPort() {
        return stellariumPort;
    }

    /**
     * Set the HTTP port we are using for communication with stellarium
     * The defult port is 8090
     * @param stellariumPort the new port to use.
     */
    public void setStellariumPort(int stellariumPort) {
        this.stellariumPort = stellariumPort;
    }



    private double timeRate = 0;
    private String targetName = "";
    // this is default magnitude
    private double magnitude = 13;

    private Object lrMoveSynchroniser = new Object();
    private Object upMoveSynchroniser = new Object();

    private double LRMovementAmount = 0;
    private double UDMovementAmount =  0;


    String lastAltAz = "";


    /**
     * Set how often we will rest between polling Stellarium
     * @param pollTime time in milliseconds
     */
    public void setPollTime(long pollTime) {
        this.pollTime = pollTime;
        pollStellarium();
    }

    /**
     * Cause Stellarium to poll server
     */
    public void pollStellarium(){

        synchronized (pollSynchroniser){
            pollSynchroniser.notify();
        }
    }

    // define the time to rest between polling Stellarium for status
    private long pollTime =  Long.MAX_VALUE;

    /**
     * Constructor. It will create the syncroniser object threads
     */
    public StellariumSlave(){
        createThreads();
    }


    // set a current field of view
    float currentFieldOfView = 0;

    List<StellariumViewListener> stellariumViewListeners = new ArrayList<>();


    /**
     * Adds a listener that will receive notification of a fieldOfView Change
     * @param listener the listener that will be notified of the change
     */
    public void addFieldOfViewListener(StellariumViewListener listener){
        stellariumViewListeners.add(listener);
    }

    void createThreads(){
        /***********************************************************
         * Create a runnable thread object
         * simply type synchronizedThread to generate this code
         ***********************************************************/
        new Thread(() -> {
            while (!exitThread) {
                synchronized (lrMoveSynchroniser) {
                    try {
                        lrMoveSynchroniser.wait();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // we should just have
                //Add the function you need to execute here
                sendMoveLR(LRMovementAmount);
            }
        }).start();

        /***********************************************************
         * Create a runnable thread object
         * simply type synchronizedThread to generate this code
         ***********************************************************/
        new Thread(() -> {
            while (!exitThread) {
                synchronized (upMoveSynchroniser) {
                    try {
                        upMoveSynchroniser.wait();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // we should just have
                //Add the function you need to execute here
                sendMoveUD(UDMovementAmount);
            }
        }).start();
        /***********************************************************
         * Create a runnable thread object
         * simply type synchronizedThread to generate this code
         ***********************************************************/
        new Thread(() -> {
            while (!exitThread) {
                synchronized (fovSynchroniser) {
                    try {
                        fovSynchroniser.wait();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // we should just have
                //Add the function you need to execute here
                String api = "main/fov";
                Map<String,Object> params = new LinkedHashMap<>();

                params.put("fov", fieldOfView);
                try {
                    if (sendPostMessage(api, params)){
                        readFieldOfView();
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                }

            }
        }).start();


        new Thread(() -> {
            while (!exitThread) {
                synchronized (targetSynchroniser) {
                    try {
                        targetSynchroniser.wait();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // we should just have
                //Add the function you need to execute here
                String api = "main/focus";
                Map<String,Object> params = new LinkedHashMap<>();

                params.put("target", targetName);
                sendPostMessage(api, params);
            }
        }).start();


        new Thread(() -> {
            while (!exitThread) {
                synchronized (timerateSynchroniser) {
                    try {
                        timerateSynchroniser.wait();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // we should just have
                //Add the function you need to execute here
                String api = "main/time";
                Map<String,Object> params = new LinkedHashMap<>();

                params.put("timerate", timeRate);
                sendPostMessage(api, params);
            }
        }).start();

        /***********************************************************
         * Create a runnable thread object
         * simply type synchronizedThread to generate this code
         ***********************************************************/
        new Thread(() -> {
            while (!exitThread) {
                synchronized (altAzSynchroniser){
                    try {
                        altAzSynchroniser.wait();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // we should just have
                sendAltAz(currentAz, currentAlt);
            }
        }).start();


        new Thread(() -> {
            while (!exitThread) {
                synchronized (magnitudeSynchroniser){
                    try {
                        magnitudeSynchroniser.wait();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                String property = "StelSkyDrawer.customStarMagLimit";
                sendStelProperty(property, magnitude);
            }
        }).start();


        // we will have a wait object
        new Thread(() -> {
            while (!exitThread) {
                synchronized (pollSynchroniser) {
                    try {
                        pollSynchroniser.wait(pollTime);

                        pollView();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //Add the function you need to execute here

            }
        }).start(); /* end synchronizedThread */
    }

    void pollView(){
        float fov = readFieldOfView();
        double [] coordinates = readView();

        if (coordinates != null) {
            RaDec raDec = new RaDec(coordinates[0], coordinates[1], coordinates[2]);

            //System.out.println(raDec.rightAscension + " " + raDec.declination);

            StellariumView stellariumView = new StellariumView(fov, raDec);
            for (StellariumViewListener listener :
                    stellariumViewListeners) {
                listener.viewChanged(stellariumView);
            }
        }
    }
    /**
     * Read current view position of Stellarium in three dimensional spherical points
     * Using J2000 as a double array of x, y, z
     * @return three dimensional spherical points as an array of doubles. Returns NULL on error
     */
    private double [] readView(){
        //curl -G http://localhost:8090/api/main/view
        double [] ret = null;
        JSONObject message_val = sendGetMessage("main/view");

        if (message_val != null) {
            //System.out.println(message_val);
            String val = (String) message_val.get("j2000");
            // our string will look like this
            // [0.960286, 0.191647, -0.202787]
            // Let us strip t into an array

            val = val.replace("[", "");
            val = val.replace("]", "");
            String [] values = val.split(",");

            // now that we have a string array, let us

            try {


                if (values.length == 3) {
                    ret = new double[values.length];
                    for (int i = 0; i < values.length; i++) {
                        ret[i] = Double.parseDouble(values[i]);
                    }
                }
            }
            catch (Exception ex){
                ex.printStackTrace();
                ret = null;
            }
        }

        return ret;
    }

    /**
     * Read the field of view from Stellarium and notify any listeners of its value
     * @return the field of view in Stellarium. A return of zero indicates some sort of error
     */
    public float readFieldOfView() {
        float ret = 0;

        try {
            JSONObject message_val = sendGetMessage("main/status");

            if (message_val != null) {
                JSONObject view = message_val.getJSONObject("view");
                if (view != null) {
                    Object fov = view.get("fov");
                    if (fov != null) {

                        // we need to convert value to a string and then parse.
                        // It could be any type of number
                        String fov_str = fov.toString();

                        ret = Float.parseFloat(fov_str);

                    }
                }
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

        return ret;
    }


    /**
     * Set the time rate that our sky is moving by.
     * A value of one is means we are travelling at normal time.
     * A value of zero is fixed time. A negative value is reverse.
     * We can speed up or slow down by setting this value
     * @param timeRate the time rate at which our sky is moving
     */
    public void setTimeRate(double timeRate){
        this.timeRate = timeRate;
        synchronized (timerateSynchroniser){
            timerateSynchroniser.notify();
        }
    }

    public void setTargetName(String control_val){
        targetName = control_val;

        synchronized (targetSynchroniser){
            targetSynchroniser.notify();
        }
    }

    public void setFieldOfView(double control_val){
        fieldOfView = control_val;

        synchronized (fovSynchroniser){
            fovSynchroniser.notify();
        }
    }
    /**
     * Clears stellarium Display
     */
    public void clearDisplay(){
        String api = "main/focus";
        Map<String,Object> params = new LinkedHashMap<>();

        sendPostMessage(api, params);

        scriptStatus();

    }

    /**
     * Show or hide the star labels.
     * @param display set true to display
     */
    public void showStarLabels(boolean display){
        sendStelProperty("actionShow_Stars_Labels", display);
    }

    /**
     * Show or hide the ground
     * @param show set true to show, false to hide
     */
    public void showGround(boolean show){
        sendStelProperty("actionShow_Ground", show);
    }

    /**
     * Show or hide the constellation art
     * @param show set true to show, false to hide
     */
    public void showConstellationArt(boolean show){
        sendStelProperty("actionShow_Constellation_Art", show);
    }

    /**
     * Show or hide the atmosphere
     * @param show set true to show atmosphere, false to hide
     */
    public void showAtmosphere(boolean show){
        sendStelProperty("actionShow_Atmosphere", show);
    }

    public void setAltitude(double control_val){
        synchronized (altAzSynchroniser){
            currentAlt = control_val  / 2 * Math.PI;
            altAzSynchroniser.notify();
        }

    }

    public void setAzimuth(double control_val){
        synchronized (altAzSynchroniser){
            currentAz = control_val  * Math.PI;
            altAzSynchroniser.notify();
        }
    }
    /**
     * Set Stellarium to new client
     * @param name the name of our Stellarium client
     */
    public void setStellariumDevice(String name){
        stellariumDevice = name;
    }

    /**
     * Move altitude up or down
     * @param control_val amount to move
     */
    public void upDownMovement(double control_val){
        UDMovementAmount = control_val;
        synchronized (upMoveSynchroniser){
            upMoveSynchroniser.notify();
        }
    }
    /**
     * Move display Left / right an amount
     * @param control_val the amount we are moving the control
     */
    public void leftRightMovement(double control_val){
        LRMovementAmount = control_val;
        synchronized (lrMoveSynchroniser){
            lrMoveSynchroniser.notify();
        }
    }
    /**
     * Send a post message to Stellarium
     * Code fragments from https://stackoverflow.com/questions/4205980/java-sending-http-parameters-via-post-method-easily
     * @param api the API we are sending to
     * @param params the post parameters
     * @return true on success
     */
    synchronized boolean sendPostMessage(String api, Map<String,Object> params) {
        boolean ret = false;

        try {
            URL url = new URL("http://" + stellariumDevice + ":"+ stellariumPort +"/api/" + api);
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, Object> param : params.entrySet()) {
                if (postData.length() != 0) {
                    postData.append('&');
                }

                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
            byte[] post_data_bytes = postData.toString().getBytes("UTF-8");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(post_data_bytes.length));
            conn.setDoOutput(true);
            conn.getOutputStream().write(post_data_bytes);

            Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0; )
                sb.append((char) c);
            String response = sb.toString();
            ret = response.equalsIgnoreCase("ok");
            //System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
            ret = false;
        }

        return ret;

    }


    /**
     * Send the get message to Stellarium
     * @param api te api we are sending
     * @return a JSON object with the data
     */
    JSONObject sendGetMessage(String api){
        JSONObject ret = null;

        try {
            URL url = new URL("http://" + stellariumDevice + ":"+ stellariumPort +"/api/" + api);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0; )
                sb.append((char) c);
            String response = sb.toString();
            ret = new JSONObject(response);;
            //System.out.println(response);
        } catch (Exception e) {
            System.out.println("Unable to connect");
            ret = null;
        }

        return ret;

    }

    /**
     * Enable limiting of Star magnitude
     * @param enable_limit true if we want to limit magnitude
     * @return true if it was sent to stellarium
     */
    boolean setLimitMagnitude(boolean enable_limit){
        String property = "StelSkyDrawer.flagStarMagnitudeLimit";
        return sendStelProperty(property, enable_limit);
    }

    /**
     * Set the new Magnitude limit
     * @param control_val the new magnitude we want
     */
    void setMagnitudeLimitAmount(double control_val){
        magnitude = control_val;
        synchronized (magnitudeSynchroniser){
            magnitudeSynchroniser.notify();
        }

    }

    /**
     * Set to defined altAz coordinates
     * @param coordinate the coordinates to go to
     * @return true on success
     */
    boolean sendAltAz(String coordinate){
        String api = "main/view";
        Map<String,Object> params = new LinkedHashMap<>();
        params.put("altAz", coordinate);

        return sendPostMessage(api, params);
    }

    /**
     * Send new altaz position to stellarium
     * @param az new azimuth in radians
     * @param alt new altitude in radians
     * @return status message
     */
    boolean sendAltAz(double az, double alt){
        String api = "main/view";
        Map<String,Object> params = new LinkedHashMap<>();
        params.put("az", az);
        params.put("alt", alt);

        return sendPostMessage(api, params);
    }

    /**
     * Run the Stellarium script
     * @param script_name the name of the script to run
     * @return true if message was send
     */
    boolean runScript(String script_name){
        String api = "scripts/run";
        Map<String,Object> params = new LinkedHashMap<>();

        params.put("id", script_name);

        System.out.println("Run script " + script_name);
        return sendPostMessage(api, params);
    }

    /**
     * Stops the current script
     * @return true if message was sent
     */
    boolean stopScript(){
        String api = "scripts/stop";
        Map<String,Object> params = new LinkedHashMap<>();

        return sendPostMessage(api, params);
    }

    /**
     * Query if we have a script running
     * @return true if a script is running
     */
    boolean scriptStatus(){
        boolean ret = false;
        //curl -G http://localhost:8090/api/scripts/status
        String api = "scripts/status";

        try {
            JSONObject message_val = sendGetMessage(api);
            if (message_val != null) {

                ret = message_val.getBoolean("scriptIsRunning");
                //System.out.println("Get Script status " + ret);
            }
        }
        catch (Exception ex){}
        return ret;
    }

    /**
     * Sends a stell property value
     * @param name the name of the parameter
     * @param val the value of the parameter
     * @return true if successful
     */
    boolean sendStelProperty(String name, Object val){
        String api = "stelproperty/set";
        Map<String,Object> params = new LinkedHashMap<>();
        params.put("id", name);
        params.put("value", val);

        return sendPostMessage(api, params);
    }

    /**
     * Make stellarium move left or right in azimuth simulating arrow keys
     * a negative value signifies left
     * @param qty qty to move
     * @return true on success
     */
    boolean sendMoveLR(double qty){
        String api = "main/move";
        Map<String,Object> params = new LinkedHashMap<>();
        params.put("x", qty);

        return sendPostMessage(api, params);
    }

    /**
     * Make stellarium move up or down simulating arrow keys
     * a negative value signifies down
     * @param qty qty to move
     * @return true on success
     */
    boolean sendMoveUD(double qty){
        String api = "main/move";
        Map<String,Object> params = new LinkedHashMap<>();
        params.put("y", qty);

        return sendPostMessage(api, params);
    }
    /**
     * get the current coordinates of
     * @return current coordinates in x,y,z
     */
    String requestCoordinated(){
        String ret = "";

        String api = "main/view";

        try {
            JSONObject message_val = sendGetMessage(api);
            if (message_val != null) {

                Object altAz = message_val.get("altAz");
                if (altAz != null){
                    ret = altAz.toString();
                }

            }
        }
        catch (Exception ex){}
        return ret;
    }

    /**
     * This function is used when running sketch in IntelliJ IDE for debugging or testing
     *
     * @param args standard args required
     */
    public static void main(String[] args) {

        StellariumSlave slave = new StellariumSlave();

        slave.addFieldOfViewListener(view -> {
            RaDec raDec = view.raDec;
            System.out.println("FOV: " + view.fieldOfView + " RA Dec " + raDec.rightAscension + " " + raDec.declination);
        });
        slave.setPollTime(4000);

        while (true){


        }

    }


}