package com.stellarcommand;

import StellarStructures.RaDec;
import StellarStructures.StellarDataRow;
import StellarStructures.StellarDataTable;
import Stellarium.StellariumLocation;
import Stellarium.StellariumSlave;
import Stellarium.StellariumView;
import Stellarium.StellariumViewListener;
import de.sciss.net.OSCBundle;
import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import vizier.VizierQuery;

import java.io.*;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.List;

/**
 * Sends Stellarium OSC Data back to client
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

    StellariumView lastStellariumfView = null;

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
     * Set the frequency of polling Stellarium for changes in view
     * @param poll_time the time in milliseconds between re-polling Stellarium
     */
    void setPollTime(int poll_time){
        stellariumSlave.setPollTime(poll_time);
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
        //stellariumSlave.addViewListener(this::viewRead);
        stellariumSlave.addViewListener(this);
        //vizierQuery.addFilter("Hpmag", "<", 5);
    }

    @Override
    public void viewRead(StellariumView stellariumView) {
        if (!stellariumView.equals(lastStellariumfView)) {
            RaDec raDec = stellariumView.getRaDec();
            System.out.println("FOV: " + stellariumView.getFieldOfView() + " RA Dec " + raDec.rightAscension + " " + raDec.declination);

            lastStellariumfView = stellariumView;

            sendStellariumView(lastStellariumfView);

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

        boolean ret = number_bundles > 0;

        int bundle_num = 0; // The next bundle number we will send

        int table_size =  dataRows.size();
        for (int i = 0; i < table_size && ret; i++){

            if (i % MAXIMUM_ROWS == 0){
                if (oscBundle != null){
                    ret = oscSender.send(oscBundle, oscClient, targetPort);
                }
                bundle_num++;
                oscBundle = new OSCBundle();
                oscBundle.addPacket(OSCMessageBuilder.createOscMessage(oscNamespace + "/" + StellarOSCVocabulary.ClientMessages.STAR_NAMES, (Object[])columnNames));
                oscBundle.addPacket(OSCMessageBuilder.createOscMessage(oscNamespace + "/" +  StellarOSCVocabulary.ClientMessages.BUNDLE_COUNT, bundle_num, number_bundles));

            }
            StellarDataRow row = dataRows.get(i);
            List<Float> row_data = row.vizierData;
            oscBundle.addPacket(OSCMessageBuilder.createOscMessage(oscNamespace + "/" + StellarOSCVocabulary.ClientMessages.STAR_VALUES, row_data.toArray()));
        }

        if (oscBundle != null){
            ret = oscSender.send(oscBundle, oscClient, targetPort);
        }

        return ret;
    }
    /**
     * Set the name of where to request HTTP data from Stellarium
     * @param stellariumHost HTTP host name
     */
    public void setStellariumDevice(String stellariumHost) {
        stellariumSlave.setStellariumDevice(stellariumHost);
    }

    /**
     * Set the port to send to Stellarium to get Stellarium data
     * @param stellariumPort the port we will use to send to Stellarium if we are not using default
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
                if (command.equalsIgnoreCase(StellarOSCVocabulary.CommandMessages.VIEW_LOCATION)) {
                    sendStellariumView(lastStellariumfView);
                }

                else if (command.equalsIgnoreCase(StellarOSCVocabulary.CommandMessages.POLL)) {
                    // we only send to the port we are actually configured to. Not necessarily to who is calling us
                    oscSender.send(OSCMessageBuilder.createOscMessage(oscNamespace + "/" + StellarOSCVocabulary.ClientMessages.OSC_PORT, oscReceiver.getPort()), oscClient, targetPort);
                }

                else if (command.equalsIgnoreCase(StellarOSCVocabulary.CommandMessages.EXIT)) {
                    exitServer();
                }
                else if (command.equalsIgnoreCase(StellarOSCVocabulary.CommandMessages.FIELD_OF_VIEW)) {
                    setFieldOfView(msg);
                }

                else if (command.equalsIgnoreCase(StellarOSCVocabulary.CommandMessages.FILTER)) {
                    addFilter(addresses, msg);
                } else if (command.equalsIgnoreCase(StellarOSCVocabulary.CommandMessages.RESET_FILTERS)) {
                    vizierQuery.clearFilters();
                } else if (command.equalsIgnoreCase(StellarOSCVocabulary.CommandMessages.SEND_STARS)) {

                    if (msg.getArgCount() > 0 && msg.getArg(0) instanceof Integer){
                        queryVizier = ((int)msg.getArg(0)) != 0;
                    }
                    else {
                        System.out.println("Invalid Command " + getOscMessageDisplay(msg));
                    }
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

                else if (command.equalsIgnoreCase(StellarOSCVocabulary.CommandMessages.SHOW_STAR_LABELS)){
                    boolean show = ((int) msg.getArg(0) == 0) ?false:true;
                    stellariumSlave.showAtmosphere(show);
                }

                else if (command.equalsIgnoreCase(StellarOSCVocabulary.CommandMessages.SHOW_CONSTELATION_ART)){
                    boolean show = ((int) msg.getArg(0) == 0) ?false:true;
                    stellariumSlave.showConstellationArt(show);
                }
                else if (command.equalsIgnoreCase(StellarOSCVocabulary.CommandMessages.SHOW_ATMOSPHERE)){
                    boolean show = ((int) msg.getArg(0) == 0) ?false:true;
                    stellariumSlave.showAtmosphere(show);
                }
                else if (command.equalsIgnoreCase(StellarOSCVocabulary.CommandMessages.SHOW_GROUND)){
                    boolean show = ((int) msg.getArg(0) == 0) ?false:true;
                    stellariumSlave.showGround(show);
                }
                else if (command.equalsIgnoreCase(StellarOSCVocabulary.CommandMessages.SET_VIEWER_LOCATION)){
                    setLocation(msg);
                }



            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

    }

    private void setFieldOfView(OSCMessage msg) {
        try
        {
            float field_of_view = (float)msg.getArg(0);
            stellariumSlave.setFieldOfView(field_of_view);
        }
        catch(Exception ex){}
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
     * Send the Stellarium RA/Dec and Field of view information
     * @param stellariumView the view we are sending
     */
    private void sendStellariumView(StellariumView stellariumView) {
        RaDec raDec = stellariumView.getRaDec();
        oscSender.send(OSCMessageBuilder.createOscMessage(oscNamespace + "/" + StellarOSCVocabulary.ClientMessages.DISPLAY_VIEW, stellariumView.getFieldOfView(), raDec.rightAscension, raDec.declination), oscClient, targetPort);

    }

    boolean setLocation(OSCMessage msg){
        boolean ret = false;

        stellariumSlave.readLocation();
        return ret;
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
     * Get a String display of OSC message
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
