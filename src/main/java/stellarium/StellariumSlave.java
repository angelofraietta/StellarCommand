package stellarium;


import stellarstructures.AltAz;
import stellarstructures.ObservationalPoint;
import stellarstructures.RaDec;
import stellarstructures.StellarConversions;
import org.json.JSONObject;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StellariumSlave  {

    static public String DEFAULT_STELLARIUM_HOST = "localhost";
    static public int DEFAULT_STELLARIUM_PORT = 8090;

    double currentAz = 0;
    double currentAlt = 0; // zero is level. 1 is Up, -1 is down, 2 is behind
    double newLatitude = 0, newLongitude = 0, newAltitude = 0;
    String newPlanet = "";


    boolean exitThread = false;


    /**
     * Define our thread synchronisation objects. We will notifiy on exit
     */
    final private Object fovSynchroniser = new Object();
    final private Object timerateSynchroniser = new Object();
    final private Object targetSynchroniser = new Object();
    final private Object magnitudeSynchroniser = new Object();
    final private Object pollSynchroniser = new Object();
    final private Object lrMoveSynchroniser = new Object();
    final private Object upMoveSynchroniser = new Object();
    final private Object altAzSynchroniser = new Object();
    final private Object locationSynchroniser = new Object();
    final private Object setTimeSynchroniser = new Object();


    private double fieldOfView  = 1;

    // Define our cahced JSON objects
    JSONObject mainStatus = null;
    JSONObject propertiesValue = null;
    JSONObject mainView = null;

    StellariumLocation lastStellariumLocation = null;
    StellariumTime lastStellariumTime = null;
    double newJulianDate = 0;

    /**
     * Get the stellarium Properties. Note that this could be null
     * @return the last polled stellarium Properties
     */
    public StellariumProperty getStellariumProperties() {
        return lastStellariumProperty;
    }

    StellariumProperty lastStellariumProperty = null;

    String stellariumDevice = DEFAULT_STELLARIUM_HOST;

    int stellariumPort = DEFAULT_STELLARIUM_PORT;

    /**
     * Cause a synchronised notify of an object
     * @param object
     */
    void notifyObject (final Object object){
        synchronized (object){
            object.notify();
        }
    }

    /**
     * exit all our threads in the slave
     */
    public void exitSlave(){
        exitThread = true;

        notifyObject(fovSynchroniser);
        notifyObject(timerateSynchroniser);
        notifyObject(targetSynchroniser);
        notifyObject(magnitudeSynchroniser);
        notifyObject(pollSynchroniser);
        notifyObject(lrMoveSynchroniser);
        notifyObject(upMoveSynchroniser);
        notifyObject(altAzSynchroniser);
        notifyObject(locationSynchroniser);
        notifyObject(setTimeSynchroniser);

    }

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


    private double LRMovementAmount = 0;
    private double UDMovementAmount =  0;


    String lastAltAz = "";


    /**
     * Set how often we will rest between polling stellarium
     * @param pollTime time in milliseconds
     */
    public void setPollTime(long pollTime) {
        this.pollTime = pollTime;
        pollStellarium();
    }

    /**
     * Cause stellarium to poll server
     */
    public void pollStellarium(){

        synchronized (pollSynchroniser){
            pollSynchroniser.notify();
        }
    }

    /**
     * Set stellarium to this Julian day
     * @param julian_day the julian day to set stellarium to
     */
    public void setTime(double julian_day){
        synchronized (setTimeSynchroniser) {
            newJulianDate = julian_day;
            setTimeSynchroniser.notify();
        }
    }
    // define the time to rest between polling stellarium for status
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
     * Adds a listener that will receive notification of a latitude Change
     * @param listener the listener that will be notified of the change
     */
    public void addViewListener(StellariumViewListener listener){
        stellariumViewListeners.add(listener);
    }

    /**
     * Removes a listener
     * @param listener the listener to remove
     */
    public void removeViewListener(StellariumViewListener listener){
        stellariumViewListeners.remove(listener);
    }

    /**
     * Erases all listeners
     */
    public void eraseViewListeners(){
        stellariumViewListeners.clear();
    }

    void createThreads(){
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
                if (exitThread)
                    break;

                sendMoveLR(LRMovementAmount);
            }
        }).start();

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

        new Thread(() -> {
            while (!exitThread) {
                synchronized (fovSynchroniser) {
                    try {
                        fovSynchroniser.wait();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (exitThread)
                    break;

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
                if (exitThread)
                    break;
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

                if (exitThread)
                    break;

                // we should just have
                //Add the function you need to execute here
                String api = "main/time";
                Map<String,Object> params = new LinkedHashMap<>();

                params.put("timerate", timeRate);
                sendPostMessage(api, params);
            }
        }).start();


        new Thread(() -> {
            while (!exitThread) {
                synchronized (altAzSynchroniser){
                    try {
                        altAzSynchroniser.wait();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (exitThread)
                    break;

                // we should just have
                sendAltAz(currentAz, currentAlt);
            }
        }).start();


        new Thread(() -> {
            while (!exitThread) {
                synchronized (locationSynchroniser){
                    try {
                        locationSynchroniser.wait();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (exitThread)
                    break;

                // we should just have
                sendLocation(newLatitude, newLongitude, newAltitude, newPlanet);
            }
        }).start();


         new Thread(() -> {
            while (!exitThread) {
                synchronized (setTimeSynchroniser){
                    try {
                        setTimeSynchroniser.wait();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (exitThread)
                    break;


                //Add the function you need to execute here
                String api = "main/time";
                Map<String,Object> params = new LinkedHashMap<>();

                params.put("time", newJulianDate);
                sendPostMessage(api, params);
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

                if (exitThread)
                    break;

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


                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //Add the function you need to execute here
                if (exitThread)
                    break;

                pollView();

            }
        }).start(); /* end synchronizedThread */
    }

    /**
     * Send the new Location
     * @param latitude
     * @param longitude
     * @param altitude the altitude we are viewing from
     * @param planet the planet we are viewing from
     * @return true if able to send
     */
    private boolean sendLocation(double latitude, double longitude, double altitude, String planet) {
        String api = "location/setlocationfields";
        Map<String,Object> params = new LinkedHashMap<>();
        params.put("latitude", latitude);
        params.put("longitude", longitude);
        params.put("altitude", altitude);
        params.put("country", "");
        params.put("name", "");
        params.put("planet", planet);

        return sendPostMessage(api, params);
    }


    /**
     * Force Module to do a poll of stellarium and send status changes
     * We will do this via the Synchronised thread
     */
    private void pollView(){
        if (readMainStatus() && readMainView()) {
            float fov = readFieldOfView();
            double[] coordinates = readView();

            if (readPropertiesStatus())
            {
                lastStellariumProperty = new StellariumProperty(propertiesValue);
            }

            if (coordinates != null) {
                StellariumLocation stellariumLocation = readObservationPoint();
                StellariumTime time = readStellariumTime();
                double julianDate =  time.getJulianDay();



                if (stellariumLocation != null) {
                    lastStellariumLocation = stellariumLocation;

                    RaDec raDec = new RaDec(coordinates[0], coordinates[1], coordinates[2]);

                    //System.out.println(raDec.rightAscension + " " + raDec.declination);

                    StellariumView stellariumView = new StellariumView(fov, raDec);
                    for (StellariumViewListener listener :
                            stellariumViewListeners) {
                        listener.viewRead(stellariumView);
                        listener.locationRead(stellariumLocation);
                        listener.timeRead(time);
                    }
                }
            }
        }
    }

    /**
     * Get the JSON Object for Main View and store in mainView
     * @return true if able to read from
     */
    private boolean readMainView() {
        mainView = sendGetMessage("main/view");
        return  mainView != null;
    }

    /**
     * Read current view position of stellarium in three dimensional spherical points
     * Using J2000 as a double array of x, y, z
     * @return three dimensional spherical points as an array of doubles. Returns NULL on error
     */
    private double [] readView(){
        //curl -G http://localhost:8090/api/main/view
        double [] ret = null;

        if (mainView != null) {
            //System.out.println(message_val);
            String val = (String) mainView.get("j2000");
            // our string will look like this
            // [0.960286, 0.191647, -0.202787]
            // Let us strip it into an array

            val = val.replace("[", "");
            val = val.replace("]", "");
            String [] values = val.split(",");

            // now that we have a string array, let us get the values

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
     * Get the stellarium time from last poll
     * @return the stellarium time
     */
    public StellariumTime readStellariumTime(){
        StellariumTime ret = null;

        try {

            if (mainStatus != null) {
                JSONObject time = mainStatus.getJSONObject("time");
                if (time != null) {
                    ret = new StellariumTime(time);
                    lastStellariumTime = ret;
                }

            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

        return ret;
    }
    /**
     * Read the viewer location of  stellarium
     * @return the view location
     */
    public StellariumLocation readObservationPoint(){
        StellariumLocation ret = null;

        try {

            if (mainStatus != null) {
                JSONObject location = mainStatus.getJSONObject("location");
                if (location != null) {
                    ret = new StellariumLocation(location);
                }

            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

        return ret;
    }


    /**
     * Read the Status From the stellarium RemoteApi
     * @return true if able to read
     * @see  <a href="http://stellarium.org/doc/head/remoteControlApi.html"http://stellarium.org/doc/head/remoteControlApi.html</a>
     */
    boolean readMainStatus(){
        boolean ret = false;


        try {
            mainStatus = sendGetMessage("main/status");

            ret = (mainStatus != null);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

        return ret;
    }

    /**
     * Read the Properties From the stellarium RemoteApi
     * @return true if able to read
     * @see  <a href="http://stellarium.org/doc/head/remoteControlApi.html"http://stellarium.org/doc/head/remoteControlApi.html</a>
     */
    boolean readPropertiesStatus(){
        boolean ret = false;


        try {
            propertiesValue = sendGetMessage("stelproperty/list");

            ret = (propertiesValue != null);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

        return ret;
    }

    /**
     * Read the field of view from stellarium and notify any listeners of its value
     * @return the field of view in stellarium. A return of zero indicates some sort of error
     */
    public float readFieldOfView() {
        float ret = 0;

        try {
            if (mainStatus != null) {
                JSONObject view = mainStatus.getJSONObject("view");
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
     * Set the time rate that our sky is moving by in Julian days per second.
     * A value of one is means we are travelling at one julian day per second.
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

    /**
     * Cause stellarium to view the target
     * @param target_name the name of the object we are targeting. EG. Saturn
     */
    public void setTargetName(String target_name){
        targetName = target_name;

        synchronized (targetSynchroniser){
            targetSynchroniser.notify();
        }
    }

    /**
     * Set the field of view we want stellarium to display
     * @param field_of_view the new field of view we want to display
     */
    public void setFieldOfView(double field_of_view){
        fieldOfView = field_of_view;

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

    /**
     * Set the altitude in degrees. +90 is zenith, -90 is nadir
     * @param degrees
     */
    public void setAltitude(double degrees){
        synchronized (altAzSynchroniser){
            currentAlt = degrees  * Math.PI / 180;
            altAzSynchroniser.notify();
        }

    }

    /**
     * Set the Azimuth in degrees
     * @param degrees the degrees of azimuth. North is zero, east is 90
     */
    public void setAzimuth(double degrees){
        synchronized (altAzSynchroniser){
            currentAz = (degrees + 180)  * Math.PI / 180 * -1;
            altAzSynchroniser.notify();
        }
    }

    /**
     * Set the Azimuth and Altitude based on RA and Dec
     * @param raDec RA/Dec structure
     */
    public void setRaDec(RaDec raDec){
        ObservationalPoint observationalPoint = new ObservationalPoint(lastStellariumLocation.latitude, lastStellariumLocation.longitude, lastStellariumTime.utcTime());

        AltAz calc = StellarConversions.convertRaDecToAltAz(raDec, observationalPoint);
        setAltAz(calc);
    }

    /**
     * Set Altitude and Azimuth
     * @param altAz Altitude and azimuth in degrees
     */
    public void setAltAz(AltAz altAz){
        synchronized (altAzSynchroniser){
            // We need to switch this around so 0 points north, 90 - east, 180 south, 270 west
            currentAz = (altAz.getAzimuth() + 180)  * Math.PI / 180 * -1;
            currentAlt =  altAz.getAltitude() * Math.PI / 180;
            altAzSynchroniser.notify();
        }

    }
    /**
     * Set the new longitude and latitude of our observation point
     * @param latitude new latitude
     * @param longitude new longitude
     * @param altitude the new altitude
     * @param planet the new planet
     */
    public void setLongitudeAndLatitude(double latitude, double longitude, double altitude, String planet){
        synchronized (locationSynchroniser) {
            newLatitude = latitude;
            newLongitude = longitude;
            newAltitude = altitude;
            newPlanet = planet;
            locationSynchroniser.notify();
        }
    }
    /**
     * Set stellarium to new client
     * @param name the name of our stellarium client
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
     * Send a post message to stellarium
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
     * Send the get message to stellarium
     * @param api te api we are sending
     * @return a JSON object with the data
     */
    synchronized JSONObject sendGetMessage(String api){
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
     * Run the stellarium script
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
        try {
            if (mainView != null) {

                Object altAz = mainView.get("altAz");
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

        class listener implements StellariumViewListener{

            @Override
            public void viewRead(StellariumView stellariumView) {
                System.out.println("FOV: " + stellariumView.fieldOfView + " RA Dec " + stellariumView.raDec.rightAscension + " " + stellariumView.raDec.declination);
            }

            @Override
            public void locationRead(StellariumLocation stellariumView) {
                System.out.println("Lat: " + stellariumView.latitude + " long: " + stellariumView.longitude);
            }

            @Override
            public void timeRead(StellariumTime stellariumTime) {

            }
        }


        slave.addViewListener(new listener());

        slave.setPollTime(4000);


        ZonedDateTime dateTime = ZonedDateTime.parse(Instant.now().toString()  , DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        double julian_date = ObservationalPoint.calulateJulianDay(dateTime);
        slave.setTime(julian_date);

        while (true){


        }

    }

}
