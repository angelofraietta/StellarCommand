package com.stellarcommand;

import stellarstructures.*;
import stellarium.*;
import de.sciss.net.OSCBundle;
import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import vizier.StellarDataRow;
import vizier.StellarDataTable;
import vizier.VizierQuery;

import java.io.*;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Sends stellarium OSC Data back to client
 */
public class StellariumOSCServer implements StellariumViewListener, OSCListener {

    OSCUDPReceiver oscReceiver = null;
    StellariumSlave stellariumSlave = new StellariumSlave();
    OSCUDPSender oscSender = new OSCUDPSender();

    VizierQuery vizierQuery = new VizierQuery();

    boolean queryVizier = true;

    int MAXIMUM_ROWS = 100;


    String oscNamespace; // this is what our OSC messages will start as
    InetAddress oscClient;
    int targetPort;

    private StellariumView lastStellariumView = null;
    private StellariumLocation lastStellariumLocation = null;
    private StellariumTime lastStellariumTime = null;

    final Object serverWaitObject = new Object(); // we will wait on this to exit


    /**
     * Wait on this object to exit.
     */
    public void waitForExit(){
        synchronized (serverWaitObject){
            try {
                serverWaitObject.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * Set the frequency of polling stellarium for changes in view
     * @param poll_time the time in milliseconds between re-polling stellarium
     */
    void setPollTime(int poll_time){
        stellariumSlave.setPollTime(poll_time);
    }



    /**
     * Convert our OSC message to a float. This is in case someone sends an int or string instead of as float
     * @param arg the OSC argument
     * @return a float version
     */
    static float convertOSCArgToFloat(Object arg){
        float ret = 0;

        if (arg instanceof Float)
        {
            ret = (float)arg;
        }
        else if (arg instanceof Integer)
        {
            ret = (int)arg;
        }
        else if (arg instanceof String){
            String s_val = (String) arg;
            ret =  Float.parseFloat(s_val);
        }
        return ret;
    }

    /**
     * Convert our OSC message to a int. This is in case someone sends a float or string instead of as float
     * @param arg the OSC argument
     * @return an int version
     */
    static int convertOSCArgToInt(Object arg){
        int ret = 0;

        if (arg instanceof Integer)
        {
            ret = (int)arg;
        }
        else if (arg instanceof Float)
        {
            ret = ((Float) arg).intValue();
        }
        else if (arg instanceof String){
            String s_val = (String) arg;
            ret =  Integer.parseInt(s_val);
        }
        return ret;
    }

    /**
     * Clear our stored values and have new values se-sent
     */
    void forceRePollStellarium(){
        lastStellariumView = null;
        lastStellariumLocation = null;
        lastStellariumTime = null;
        stellariumSlave.pollStellarium();
    }
    /**
     * Create an OSC server with minimum number of required parameters
     * @param oscName the OSC address name
     * @param oscTargetAddress the OSC client INET address
     * @param target_port the target OSC port to send messages to
     * @param inputPorts a list of ports we will try to listen to. If null or unable to open one, we will open our own port
     */
    public StellariumOSCServer(String oscName, InetAddress oscTargetAddress, int target_port, int [] inputPorts ){
        oscNamespace = oscName;
        oscClient = oscTargetAddress;
        targetPort = target_port;
        int source_port = 0;
        if (inputPorts != null){
            for (int i = 0; i < inputPorts.length && oscReceiver == null; i++){
                try {
                    oscReceiver = new OSCUDPReceiver(inputPorts[i]);
                    source_port = oscReceiver.getPort();
                } catch (IOException e) {
                    System.out.println("Unable to open input port " + inputPorts[i]);
                }
            }
        }
        // see if we managed to open a port. If not, we need to open first one available
        if (oscReceiver == null){
            try {
                oscReceiver = new OSCUDPReceiver(0);
                source_port = oscReceiver.getPort();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (oscReceiver == null){
            System.out.println("Unable to open any port");
        }
        else
        {
            System.out.println("Opened port " + source_port);
            oscReceiver.addOSCListener(this::messageReceived);
        }

        oscSender.send(OSCMessageBuilder.createOscMessage(oscNamespace + "/" + StellarOSCVocabulary.ClientMessages.OSC_PORT, source_port), oscClient, targetPort);

        oscSender.send(OSCMessageBuilder.createOscMessage(oscNamespace + "/" + StellarOSCVocabulary.ClientMessages.VERSION, Version.MAJOR, Version.MINOR, Version.BUILD), oscClient, targetPort);


        //stellariumSlave.addViewListener(this::viewRead);
        stellariumSlave.addViewListener(this);
        //vizierQuery.addFilter("Hpmag", "<", 5);
    }

    @Override
    public void viewRead(StellariumView stellariumView) {
        if (!stellariumView.equals(lastStellariumView)) {
            RaDec raDec = stellariumView.getRaDec();
            //System.out.println("FOV: " + stellariumView.getFieldOfView() + " RA Dec " + raDec.rightAscension + " " + raDec.declination);

            lastStellariumView = stellariumView;

            sendStellariumView(lastStellariumView);

            if (queryVizier){

                String centre =  raDec.rightAscension + " " + raDec.declination;
                String vizierData = vizierQuery.readVizierCentre(centre, stellariumView.getFieldOfView());
                StellarDataTable table = new StellarDataTable();

                Reader inputString = new StringReader(vizierData);
                BufferedReader reader = new BufferedReader(inputString);

                try {
                    table.loadTable(reader);

                    if (!sendDataTable(table))
                    {
                        System.out.println("Unable to send table");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //System.out.println(vizierData);

            }
        }
    }

    @Override
    public void locationRead(StellariumLocation stellariumView) {
        if (!stellariumView.equals(lastStellariumLocation)){
            lastStellariumLocation = stellariumView;
            sendObservationPoint(lastStellariumLocation);
        }
    }

    @Override
    public void timeRead(StellariumTime stellariumTime) {

        if (!stellariumTime.equals(lastStellariumTime)){
            System.out.println("Read");
            lastStellariumTime = stellariumTime;
            sendStellariumTime(lastStellariumTime);
        }
    }


    /**
     * Send the StellarDataTable via OSC
     * @param table the Stellar Data table we are sending
     * @return true if we were able to send
     */
    boolean sendDataTable(StellarDataTable table){


        String [] columnNames =  table.getColumnNames();

        OSCBundle oscBundle = null;

        List<StellarDataRow> dataRows = table.getDataTable();

        // We want to make sure round up
        int number_bundles = (dataRows.size() + MAXIMUM_ROWS - 1) / MAXIMUM_ROWS;

        boolean ret = dataRows.size() > 0;

        int bundle_num = 0; // The next bundle number we will send

        int table_size =  dataRows.size();
        if (table_size == 0){
            oscBundle = new OSCBundle();
            oscBundle.addPacket(OSCMessageBuilder.createOscMessage(oscNamespace + "/" +  StellarOSCVocabulary.ClientMessages.BUNDLE_COUNT, bundle_num, number_bundles));
            oscBundle.addPacket(OSCMessageBuilder.createOscMessage(oscNamespace + "/" + StellarOSCVocabulary.ClientMessages.STAR_NAMES, (Object[])columnNames));

        }
        else{
            for (int i = 0; i < table_size && ret; i++){

                if (i % MAXIMUM_ROWS == 0){
                    if (oscBundle != null){
                        ret = oscSender.send(oscBundle, oscClient, targetPort);
                    }
                    oscBundle = new OSCBundle();
                    oscBundle.addPacket(OSCMessageBuilder.createOscMessage(oscNamespace + "/" +  StellarOSCVocabulary.ClientMessages.BUNDLE_COUNT, bundle_num, number_bundles));
                    oscBundle.addPacket(OSCMessageBuilder.createOscMessage(oscNamespace + "/" + StellarOSCVocabulary.ClientMessages.STAR_NAMES, (Object[])columnNames));
                    bundle_num++;
                }
                StellarDataRow row = dataRows.get(i);
                List<Float> row_data = row.vizierData;
                oscBundle.addPacket(OSCMessageBuilder.createOscMessage(oscNamespace + "/" + StellarOSCVocabulary.ClientMessages.STAR_VALUES, row_data.toArray()));
            }

        }

        if (oscBundle != null){
            ret = oscSender.send(oscBundle, oscClient, targetPort);
        }

        return ret;
    }
    /**
     * Set the name of where to request HTTP data from stellarium
     * @param stellariumHost HTTP host name
     */
    public void setStellariumDevice(String stellariumHost) {
        stellariumSlave.setStellariumDevice(stellariumHost);
    }

    /**
     * Set the port to send to stellarium to get stellarium data
     * @param stellariumPort the port we will use to send to stellarium if we are not using default
     */
    public void setStellariumPort(int stellariumPort) {
        stellariumSlave.setStellariumPort(stellariumPort);
    }

    @Override
    public void messageReceived(OSCMessage msg, SocketAddress sender, long time) {
        try {
            String oscName = msg.getName();
            if (oscName.startsWith(oscNamespace)) {
                String directive = oscName.replace(oscNamespace + "/", "");
                //System.out.println(directive);
                // strip out all parts of name
                String[] addresses = directive.split("/");

                String command = addresses[0];
                if (command.equalsIgnoreCase(StellarOSCVocabulary.CommandMessages.DISPLAY_VIEW)) {
                    sendStellariumView(lastStellariumView);
                }

                else if (command.equalsIgnoreCase(StellarOSCVocabulary.CommandMessages.POLL)) {
                    // we only send to the port we are actually configured to. Not necessarily to who is calling us
                    oscSender.send(OSCMessageBuilder.createOscMessage(oscNamespace + "/" + StellarOSCVocabulary.ClientMessages.OSC_PORT, oscReceiver.getPort()), oscClient, targetPort);
                    oscSender.send(OSCMessageBuilder.createOscMessage(oscNamespace + "/" + StellarOSCVocabulary.ClientMessages.VERSION, Version.MAJOR, Version.MINOR, Version.BUILD), oscClient, targetPort);
                    forceRePollStellarium();
                }

                else if (command.equalsIgnoreCase(StellarOSCVocabulary.CommandMessages.EXIT)) {
                    exitServer();
                }
                else if (command.equalsIgnoreCase(StellarOSCVocabulary.CommandMessages.FIELD_OF_VIEW)) {
                    setFieldOfView(msg);
                }

                else if (command.equalsIgnoreCase(StellarOSCVocabulary.CommandMessages.FILTER)) {
                    addFilter(addresses, msg);
                    forceRePollStellarium();

                } else if (command.equalsIgnoreCase(StellarOSCVocabulary.CommandMessages.RESET_FILTERS)) {
                    vizierQuery.clearFilters();
                    forceRePollStellarium();
                } else if (command.equalsIgnoreCase(StellarOSCVocabulary.CommandMessages.SEND_STARS)) {

                    if (msg.getArgCount() > 0 && msg.getArg(0) instanceof Integer){
                        queryVizier = ((int)msg.getArg(0)) != 0;
                    }
                    else {
                        System.out.println("Invalid Command " + getOscMessageDisplay(msg));
                    }
                    forceRePollStellarium();
                }
                else if (command.equalsIgnoreCase(StellarOSCVocabulary.CommandMessages.LOAD_TABLE)){
                    String filename = (String)msg.getArg(0);
                    if (!loadVizierTableFromFile(filename)){
                        System.out.println("Unable to load and send " + filename);
                    }
                }
                else if (command.equalsIgnoreCase(StellarOSCVocabulary.CommandMessages.SAVE_TABLE)){
                    String filename = (String)msg.getArg(0);
                    if (!saveVizierTableToFile(filename)){
                        System.out.println("Unable to save " + filename);
                    }
                }
                else if (command.equalsIgnoreCase(StellarOSCVocabulary.CommandMessages.VIEW_OBJECT)){
                    String objectname = (String)msg.getArg(0);
                    stellariumSlave.setTargetName(objectname);
                }

                else if (command.equalsIgnoreCase(StellarOSCVocabulary.CommandMessages.MOVE_UP_DOWN)){
                    float amount = (Float) msg.getArg(0);
                    stellariumSlave.upDownMovement(amount);
                }

                else if (command.equalsIgnoreCase(StellarOSCVocabulary.CommandMessages.MOVE_LEFT_RIGHT)){
                    float amount = (Float) msg.getArg(0);
                    stellariumSlave.leftRightMovement(amount);
                }
                else if (command.equalsIgnoreCase(StellarOSCVocabulary.CommandMessages.SET_TIME_RATE)){
                    float amount = (Float) msg.getArg(0);
                    stellariumSlave.setTimeRate(amount);
                }
                else if (command.equalsIgnoreCase(StellarOSCVocabulary.CommandMessages.STELLAR_TIME)){
                   setTime(msg);
                }

                else if (command.equalsIgnoreCase(StellarOSCVocabulary.CommandMessages.SHOW_STAR_LABELS)){
                    if (msg.getArgCount() < 1){
                        if (stellariumSlave.getStellariumProperties() != null) {
                            boolean value = stellariumSlave.getStellariumProperties().getShowStarLabels();
                            oscSender.send(OSCMessageBuilder.createOscMessage(oscName, value), oscClient, targetPort);
                        }
                    }
                    else {
                        boolean show = ((int) msg.getArg(0) == 0) ? false : true;
                        stellariumSlave.showStarLabels(show);
                    }
                }

                else if (command.equalsIgnoreCase(StellarOSCVocabulary.CommandMessages.SHOW_CONSTELATION_ART)){
                    if (msg.getArgCount() < 1){
                        if (stellariumSlave.getStellariumProperties() != null) {
                            boolean value = stellariumSlave.getStellariumProperties().getShowConstellationArt();
                            oscSender.send(OSCMessageBuilder.createOscMessage(oscName, value), oscClient, targetPort);
                        }
                    }
                    else {
                        boolean show = ((int) msg.getArg(0) == 0) ? false : true;
                        stellariumSlave.showConstellationArt(show);
                    }
                }

                else if (command.equalsIgnoreCase(StellarOSCVocabulary.CommandMessages.SHOW_CARDINAL_POINTS)){
                    if (msg.getArgCount() < 1){
                        if (stellariumSlave.getStellariumProperties() != null) {
                            boolean value = stellariumSlave.getStellariumProperties().getShowCardinalPoints();
                            oscSender.send(OSCMessageBuilder.createOscMessage(oscName, value), oscClient, targetPort);
                        }
                    }
                    else {
                        boolean show = ((int) msg.getArg(0) == 0) ? false : true;
                        stellariumSlave.showCardinalPoints(show);
                    }
                }
                else if (command.equalsIgnoreCase(StellarOSCVocabulary.CommandMessages.SHOW_EQUATORIAL_GRID)){
                    if (msg.getArgCount() < 1){
                        if (stellariumSlave.getStellariumProperties() != null) {
                            boolean value = stellariumSlave.getStellariumProperties().getShowEquatorialGrid();
                            oscSender.send(OSCMessageBuilder.createOscMessage(oscName, value), oscClient, targetPort);
                        }
                    }
                    else {
                        boolean show = ((int) msg.getArg(0) == 0) ? false : true;
                        stellariumSlave.showEquatorialGrid(show);
                    }
                }

                else if (command.equalsIgnoreCase(StellarOSCVocabulary.CommandMessages.SHOW_ATMOSPHERE)){
                    if (msg.getArgCount() < 1){
                        if (stellariumSlave.getStellariumProperties() != null) {
                            boolean value = stellariumSlave.getStellariumProperties().getShowAtmosphere();
                            oscSender.send(OSCMessageBuilder.createOscMessage(oscName, value), oscClient, targetPort);
                        }
                    }
                    else {
                        boolean show = ((int) msg.getArg(0) == 0) ? false : true;

                        stellariumSlave.showAtmosphere(show);
                    }
                }
                else if (command.equalsIgnoreCase(StellarOSCVocabulary.CommandMessages.SHOW_GROUND)){
                    if (msg.getArgCount() < 1){
                        if (stellariumSlave.getStellariumProperties() != null) {
                            boolean value = stellariumSlave.getStellariumProperties().getShowGround();
                            oscSender.send(OSCMessageBuilder.createOscMessage(oscName, value), oscClient, targetPort);
                        }
                    }
                    else {
                        boolean show = ((int) msg.getArg(0) == 0) ? false : true;
                        stellariumSlave.showGround(show);
                    }
                }
                else if (command.equalsIgnoreCase(StellarOSCVocabulary.CommandMessages.OBSERVATION_POINT)){
                    processObservationPoint(msg);
                }
                else if (command.equalsIgnoreCase(StellarOSCVocabulary.CommandMessages.VIEW_RA_DEC)){
                    processRaDec(msg);
                }
                else if (command.equalsIgnoreCase(StellarOSCVocabulary.CommandMessages.ALTITUDE)){
                    processAltitude(msg);
                }
                else if (command.equalsIgnoreCase(StellarOSCVocabulary.CommandMessages.AZIMUTH)){
                    processAzimuth(msg);
                }
                else if (command.equalsIgnoreCase(StellarOSCVocabulary.CommandMessages.VIZIER_QUERY)){
                    processVizierQuerry(msg);
                }
                else if (command.equalsIgnoreCase(StellarOSCVocabulary.CommandMessages.SCRIPT)){
                    processScript(msg);
                }





            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

    }

    /**
     * Process a script message using OSC Archguments
     * @param msg the OSC message with Arguments.
     */
    private void processScript(OSCMessage msg) {
        if (msg.getArgCount() > 0) {
            String script_name = (String) msg.getArg(0);

            if (script_name.equalsIgnoreCase(StellarOSCVocabulary.ScriptDirectives.STOP)) {
                stellariumSlave.stopScript();
            } else {
                stellariumSlave.runScript(script_name);
            }

        }
        boolean script_running = stellariumSlave.scriptStatus();

        OSCMessage ret_msg = OSCMessageBuilder.createOscMessage(oscNamespace + "/" + StellarOSCVocabulary.ClientMessages.SCRIPT,
                script_running);
        //System.out.println(getOscMessageDisplay(msg));

        oscSender.send(ret_msg, oscClient, targetPort);
    }

    /**
     * Perform a VizieR query using paramters in OSC Message
     * @param msg OSC message with arguments RA, Dec. and Field of view
     */
    private void processVizierQuerry(OSCMessage msg) {

        //
        float fov = convertOSCArgToFloat(msg.getArg(0));
        String centre = "";

        Object arg_1 = msg.getArg(1);
        if (arg_1 instanceof Float || arg_1 instanceof Integer) {
            float ra = convertOSCArgToFloat(msg.getArg(0));
            float dec = convertOSCArgToFloat(msg.getArg(1));
            centre =  ra+ " " + dec;
        }
        else if (arg_1 instanceof String){
            centre = (String)arg_1;
        }

        if (queryVizier){


            String vizierData = vizierQuery.readVizierCentre(centre, fov);
            StellarDataTable table = new StellarDataTable();

            Reader inputString = new StringReader(vizierData);
            BufferedReader reader = new BufferedReader(inputString);

            try {
                table.loadTable(reader);

                if (!sendDataTable(table))
                {
                    System.out.println("Unable to send table");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            //System.out.println(vizierData);

        }

    }

    /**
     * Set the stellarium time based on OSC message
     * @param msg the OSC message with the parameters
     * @return true if able to do conversions
     */
    public boolean setTime(OSCMessage msg) {
        boolean ret = true;

        try {
            Object arg_1 = msg.getArg(0);
            if (arg_1 instanceof String){
                String iso_time =  (String)arg_1;
                ZonedDateTime dateTime = ZonedDateTime.parse(iso_time, DateTimeFormatter.ISO_OFFSET_DATE_TIME);

                double julian_date = ObservationalPoint.calulateJulianDay(dateTime);
                stellariumSlave.setTime(julian_date);

            }
            else
            {
                float gmt_shift = 0; // this will only get set IF they do not put a time zone in and we have received the time before
                String offset = "Z";
                int year =  convertOSCArgToInt(msg.getArg(0));
                int month = convertOSCArgToInt(msg.getArg(1));
                int day = convertOSCArgToInt(msg.getArg(2));
                int hour = convertOSCArgToInt(msg.getArg(3));
                int min = convertOSCArgToInt(msg.getArg(4));
                float sec = convertOSCArgToFloat(msg.getArg(5));

                if (msg.getArgCount() > 6){
                    offset = (String) msg.getArg(6);
                }
                else
                {
                    if (lastStellariumTime != null){
                        gmt_shift =  lastStellariumTime.getGMTShift();
                    }
                }

                LocalDateTime local_time =  LocalDateTime.of(year, month, day, hour, min, (int)Math.floor(sec));
                String iso_time = DateTimeFormatter.ISO_DATE_TIME.format(local_time);
                iso_time += offset;

                ZonedDateTime dateTime = ZonedDateTime.parse(iso_time, DateTimeFormatter.ISO_OFFSET_DATE_TIME);


                double julian_date = ObservationalPoint.calulateJulianDay(dateTime);

                stellariumSlave.setTime(julian_date - gmt_shift);

            }
        }
        catch (Exception ex){
            ret = false;
        }
        return ret;
    }

    /**
     * Set the ALT az as OSC Message
     * @param msg OSC Message
     */
    private boolean processAltAz(OSCMessage msg) {
        try {
            float altitude = convertOSCArgToFloat(msg.getArg(0));
            float azimuth = convertOSCArgToFloat(msg.getArg(1));
            stellariumSlave.setAltAz(new AltAz(altitude, azimuth));
            return true;
        }
        catch (Exception ex){
        }
        return false;
    }

    /**
     * Process the Altitude as OSC Message
     * @param msg OSC Message
     */
    private boolean processAltitude(OSCMessage msg) {
        try {
            float altitude = convertOSCArgToFloat(msg.getArg(0));
            stellariumSlave.setAltitude(altitude);
            return true;
        }
        catch (Exception ex){
            return false;
        }
    }

    /**
     * Pocess the Azimuth as OSC Message
     * @param msg OSC Message
     */
    private boolean processAzimuth(OSCMessage msg) {
        try {
            float altitude = convertOSCArgToFloat(msg.getArg(0));
            stellariumSlave.setAzimuth(altitude);
            return true;
        }
        catch (Exception ex){
            return false;
        }
    }

    /**
     * Process Right Ascension / Declination as OSC Message
     * @param msg OSC Message
     */
    private boolean processRaDec(OSCMessage msg) {
        try {
            float ra = convertOSCArgToFloat(msg.getArg(0));
            float dec = convertOSCArgToFloat(msg.getArg(1));
            stellariumSlave.setRaDec(new RaDec(ra, dec));
            return true;
        }
        catch (Exception ex){
            return false;
        }
    }

    private boolean setFieldOfView(OSCMessage msg) {
        try
        {
            Object msg_val = msg.getArg(0);

            float field_of_view = convertOSCArgToFloat(msg_val);
            stellariumSlave.setFieldOfView(field_of_view);
            return true;
        }
        catch(Exception ex){return false;}
    }

    private void exitServer() {

        stellariumSlave.exitSlave();
        stellariumSlave.eraseViewListeners();
        oscReceiver.removeOSCListener(this);
        synchronized (serverWaitObject){

            serverWaitObject.notify();
        }
    }

    /**
     * Send the stellarium RA/Dec and Field of view information
     * @param stellariumView the view we are sending
     * @return true if able to send
     */
    private boolean sendStellariumView(StellariumView stellariumView) {
        RaDec raDec = stellariumView.getRaDec();
        return oscSender.send(OSCMessageBuilder.createOscMessage(oscNamespace + "/" + StellarOSCVocabulary.ClientMessages.DISPLAY_VIEW, stellariumView.getFieldOfView(), raDec.rightAscension, raDec.declination), oscClient, targetPort);

    }

    /**
     * Send the stellarium time information
     * @param stellariumTime the current stellarium we are sending
     * @return true if able to send
     */
    private boolean sendStellariumTime(StellariumTime stellariumTime) {

        return oscSender.send(OSCMessageBuilder.createOscMessage(oscNamespace + "/" + StellarOSCVocabulary.ClientMessages.  STELLAR_TIME,
                stellariumTime.utcString(), stellariumTime.localTimeString(), stellariumTime.getGMTShift(), stellariumTime.getTimeRate()), oscClient, targetPort);

    }



    /**
     * Process the ObservationPoint message from Client. If we have arguments in the oscMessage, it means we are actually setting the value.
     * Otherwise, we will just send our current location
     * @param msg OSCMessage with arguments
     * @return true if able to process the value
     */
    boolean processObservationPoint(OSCMessage msg){
        boolean ret = false;

        if (msg.getArgCount() == 0){
            StellariumLocation location = stellariumSlave.readObservationPoint();
            if (location != null){
                ret = sendObservationPoint(location);
            }
        }
        else{ // we have args so we are actually setting the value

            try{
                float latitude = convertOSCArgToFloat(msg.getArg(0));
                float longitude = convertOSCArgToFloat(msg.getArg(1));
                float altitude =  0;
                String planet = "";

                if (msg.getArgCount() > 2){
                    Object arg =  msg.getArg(2);
                    if (arg instanceof Float)
                    {
                        altitude = (float)arg;
                    }
                    else if (arg instanceof Integer)
                    {
                        altitude = (int)arg;
                    }
                    else if (arg instanceof String){
                        planet = (String)arg;
                    }

                }
                if (msg.getArgCount() > 3){
                    Object arg =  msg.getArg(3);
                    if (arg instanceof Float)
                    {
                        altitude = (float)arg;
                    }
                    else if (arg instanceof Integer)
                    {
                        altitude = (int)arg;
                    }
                    else if (arg instanceof String){
                        planet = (String)arg;
                    }

                }


                stellariumSlave.setLongitudeAndLatitude(latitude, longitude, altitude, planet);
                ret = true;
            }
            catch (Exception ex){

            }
        }

        return ret;
    }

    /**
     * Send our Observation Point
     * @param location our observation point
     * @return true if able to send
     */
    private boolean sendObservationPoint(StellariumLocation location) {
        OSCMessage msg = OSCMessageBuilder.createOscMessage(oscNamespace + "/" + StellarOSCVocabulary.ClientMessages.OBSERVATION_POINT,
                location.getLatitude(), location.getLongitude(),
                location.getAltitude(), location.getPlanet());
        //System.out.println(getOscMessageDisplay(msg));

        return oscSender.send(msg, oscClient, targetPort);
    }

    /**
     * Load a pre-saved VizieR table from file and send stars via OSC
     * @param filename the name of the file that has VizieR data
     * @return true if able to load and send
     */
    boolean loadVizierTableFromFile(String filename){
        boolean ret = false;
        StellarDataTable table = new StellarDataTable();

            if (table.loadTable(filename)) {

                ret = sendDataTable(table);
                if (!ret) {
                    System.out.println("Unable to send table");
                }
            }
            else {
                System.out.println("Unable to load table " + filename);
            }

        return ret;
    }

    /**
     * Save last VizieR table to file
     * @param filename the name of the file to save VizieR data to
     * @return true if able to save file
     */
    boolean saveVizierTableToFile(String filename){
        boolean ret = false;

        String last_vizier =  vizierQuery.getLastVizierDataRead();

        try {
            PrintWriter out = new PrintWriter(filename);
            out.print(last_vizier);
            out.close();
            ret = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return ret;
    }
    /**
     * Get a String display of OSC message. We use this basically for debugging
     * @param msg the OSC message we want to display
     * @return the String representation
     */
    String getOscMessageDisplay(OSCMessage msg){
        String ret = msg.getName();
        for (int i = 0; i < msg.getArgCount(); i++){
            ret += " " + msg.getArg(i);
        }

        return ret;
    }
    /**
     * Add a filter to VizieR
     * @param addresses the qualifiers for the filter <br />
     *                  eg /Hpmag/less will be Hpmag and less
     * @param msg The OSC message with the parameters
     */
    private void addFilter(String[] addresses, OSCMessage msg) {
        String filterName = addresses [1];
        String operand =  addresses [2];

        if (operand.equalsIgnoreCase(StellarOSCVocabulary.FilterDirectives.LESS)){
            Object param =  msg.getArg(0);

            Float filter_param = null;

            if (param instanceof Float){
                filter_param = (Float) param;
            }
            else if (param instanceof Integer){
                filter_param = ((Integer) param).floatValue();
            }

            if (filter_param != null) {
                vizierQuery.addFilter(filterName, "<", filter_param);
            }
            else
            {
                System.out.println("Invalid filter " + getOscMessageDisplay(msg));
            }
        }
        else if (operand.equalsIgnoreCase(StellarOSCVocabulary.FilterDirectives.GREATER)){
            Object param =  msg.getArg(0);

            Float filter_param = null;

            if (param instanceof Float){
                filter_param = (Float) param;
            }
            else if (param instanceof Integer){
                filter_param = ((Integer) param).floatValue();
            }

            if (filter_param != null) {
                vizierQuery.addFilter(filterName, ">", filter_param);
            }
            else
            {
                System.out.println("Invalid filter " + getOscMessageDisplay(msg));
            }
        }
        else if (operand.equalsIgnoreCase(StellarOSCVocabulary.FilterDirectives.RESET)){
            vizierQuery.eraseFilter(filterName);
        }
    }
}
