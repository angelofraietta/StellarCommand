package StellarStructures;

import java.util.*;

public class StellarFilteredTable {

    public enum SortType{
        SORT_AZIMUTH,
        SORT_ALTITUDE
    }

    final String MAG_COLUMN_START =  "Hpmag";
    final String COLOUR_COLUMN_INDEX = "B-V";

    static final String TABLE_PATH = "data/tables/";

    /**
     * Get whether we sort by azimuth or altitude
     * @return
     */
    public SortType getSortType() {
        return sortType;
    }

    /**
     * Set whether we sort by azimuth or altitude
     * @param sortType the type of sort
     */
    public void setSortType(SortType sortType) {
        this.sortType = sortType;
    }

    SortType sortType = SortType.SORT_AZIMUTH;

    double magnitudeLimit;
    ObservationalPoint observationalPoint;

    List<String> columnNames = new ArrayList<>();

    public List<FilteredData> sortedList = new ArrayList<>();

    float queryRadius; // we will get this from table - but we might want to change it

    final StellarDataTable table;
    final RaDec centrePoint;
    final AltAz centreAltAz;
    String[] dataColumnNames;

    public double getMinAz() {
        return minAz;
    }

    public double getMaxAz() {
        return maxAz;
    }

    public double getMinAlt() {
        return minAlt;
    }

    public double getMaxAlt() {
        return maxAlt;
    }

    // Define Max an Min AZ and Alt for data table
    double minAz = 360;
    double maxAz = 0;
    double minAlt = 180;
    double maxAlt = -180;

    // Define Max an Min AZ and Alt for Field of view
    double minSizeAz = 360;
    double maxSizeAz = 0;
    double minSizeAlt = 180;
    double maxSizeAlt = -180;

    /**
     * Maximum magnitude in this table
     * @return max magnitude
     */
    public float getMaxMagnitude() {
        return maxMagnitude;
    }

    /**
     * Minimum Magnitude in table
     * @return minimum magnitude
     */
    public float getMinMagnitude() {
        return minMagnitude;
    }

    float maxMagnitude = Float.MIN_VALUE;
    float minMagnitude = Float.MAX_VALUE;

    public float getMaxColour() {
        return maxColour;
    }

    public float getMinColour() {
        return minColour;
    }

    float maxColour = Float.MIN_VALUE;
    float minColour = Float.MAX_VALUE;

    /**
     * The min Azimuth of FOV
     * @return min azimuth
     */
    public double getMinSizeAz() {
        return minSizeAz;
    }

    /**
     * The maximum Azimuth of FOV
     * @return max azimuth
     */
    public double getMaxSizeAz() {
        return maxSizeAz;
    }

    /**
     * The minimum FOV altitude
     * @return min altitude
     */
    public double getMinSizeAlt() {
        return minSizeAlt;
    }

    /**
     * The maximum FOV altitude
     * @return
     */
    public double getMaxSizeAlt() {
        return maxSizeAlt;
    }



    /**
     * Constructor
     * @param data_table The StellarTable that has all the unfiltered data
     * @param mag_limit Items with magnitude greater than this number will be filtered out
     * @param observational_point The ObservationPoint and time
     */
    public StellarFilteredTable (StellarDataTable data_table, double mag_limit, ObservationalPoint observational_point) {
        table = data_table;
        magnitudeLimit = mag_limit;
        observationalPoint = observational_point;
        queryRadius = data_table.getQueryRadius();
        centrePoint = table.getCentrePoint();
        // Let us find our centre Alt and Az
        centreAltAz = StellarConversions.convertRaDecToAltAz(centrePoint, observationalPoint);
        dataColumnNames = table.getColumnNames();
    }

    /**
     * Get the data column that has the magnitude
     * @return index with magnitude. If not available, returns -1
     */
    public int getMagnitudeColumn(){
        int ret = -1;
        for (int i = 0; i < dataColumnNames.length && ret < 0; i++){
            if (dataColumnNames[i].startsWith(MAG_COLUMN_START))
            {
                ret =  i;
            }
        }
        return ret;
    }

    /**
     * Get the data column that has the colour
     * @return index with colour. If not available, returns -1
     */
    public int getColourColumn(){
        int ret = -1;
        for (int i = 0; i < dataColumnNames.length && ret < 0; i++){
            if (dataColumnNames[i].startsWith(COLOUR_COLUMN_INDEX))
            {
                ret =  i;
            }
        }
        return ret;
    }

    /**
     * Build a table sorted by azimuth
     * @return complete table
     */
    public int  buildTable(){
        return buildTable(-1);
    }

    /**
     * Build the table returning the brightest stars
     * @param max_stars the max number of stars
     * @return a list sorted by azimuth unless sort type has been set
     */
    public int  buildTable(int max_stars){
        columnNames.clear();
        sortedList.clear();
        boolean azimuth_sort = sortType == SortType.SORT_AZIMUTH;

        List<MagnitudeSort> sortedMagnitudeList = new ArrayList<>();

        // Calulate our Max and Min Sizes
        minSizeAz = centreAltAz.azimuth - queryRadius;
        maxSizeAz = centreAltAz.azimuth + queryRadius;
        minSizeAlt = centreAltAz.altitude - queryRadius / 2;
        maxSizeAlt = centreAltAz.altitude + queryRadius / 2;

        int filter_column =  getMagnitudeColumn();
        int colour_column = getColourColumn();
        columnNames.add("Az");
        columnNames.add("Alt");

        String[] col_names =  table.getColumnNames();
        for (int i = 0; i < col_names.length; i++){
            String col_name = col_names[i].trim();
            columnNames.add(col_name);
        }

        String row = "";
        // print names
        for (String name:
                columnNames) {
            row += "\t" + name;
        }

        List<StellarDataRow> data_table = table.getDataTable();

        // Lets us do a sort by magnitude first
        int num_skipped = 0;

        for (StellarDataRow row_data:
                data_table) {
            if (filter_column >= 0) {
                float mag_val = row_data.vizierData.get(filter_column);
                if (mag_val > magnitudeLimit) {
                    //System.out.println("Filter Mag " + mag_val);
                    num_skipped++;
                    continue;
                }
                sortedMagnitudeList.add(new MagnitudeSort(mag_val, row_data));

            }
        }



        Collections.sort(sortedMagnitudeList);

        for (MagnitudeSort mag_sort:
             sortedMagnitudeList) {

            StellarDataRow row_data =  mag_sort.dataRow;
            float mag_val = mag_sort.rowMagnitude;

            if (mag_val > maxMagnitude) maxMagnitude = mag_val;
            if (mag_val < minMagnitude) minMagnitude = mag_val;

            if (colour_column >= 0){
                float col_val = row_data.vizierData.get(colour_column);

                if (col_val > maxColour) maxColour = col_val;
                if (col_val < minColour) minColour = col_val;
            }
            // we have filtered out. Now add to or list
            double ra = row_data.vizierData.get(0);
            double dec = row_data.vizierData.get(1);
            RaDec raDec = new RaDec(ra, dec);
            AltAz altAz = StellarConversions.convertRaDecToAltAz(raDec, observationalPoint);

            // Get the angle off our centre
            double altitude = altAz.altitude;// - centreAltAz.altitude;

            double azimuth = altAz.azimuth;// - centreAltAz.azimuth;
            // let us see if within our range
            //if ( Math.abs(altitude) <= centreAltAz.altitude - queryRadius / 2) {

            //if (altitude >= minMagnitude && altitude <= maxMagnitude){
                // Our height was fine. Now let us look at Azimuth
                FilteredData new_row = new FilteredData(altitude, azimuth, row_data);

                // set out sort type

                new_row.sortAzimuth = azimuth_sort;

                sortedList.add(new_row);
                if (new_row.azimuth > maxAz) maxAz =  new_row.azimuth;
                if (new_row.azimuth < minAz) minAz =  new_row.azimuth;
                if (new_row.altitude > maxAlt) maxAlt =  new_row.altitude;
                if (new_row.altitude < minAlt) minAlt = new_row.altitude;
            //}

            if (max_stars > 0 && sortedList.size() == max_stars){
                break;
            }
        }
        // iterate through list and filter out
        System.out.println(row.trim());


        Collections.sort(sortedList);
        //System.out.println("Filtered " + num_skipped);
        return sortedList.size();
    }

    /*
    Function to test
    */
    public static void main(String[] args) {
        StellarDataTable table = new StellarDataTable();
        table.loadTable(TABLE_PATH + "Larawag" + ".txt");
        float MAGNITUDE = 3.6f;



        StellarFilteredTable filteredTable = new StellarFilteredTable(table, MAGNITUDE, ObservationalPoint.brazilRepublicPoint());

        int table_size = filteredTable.buildTable();

        double min_az = 360;
        double max_az = 0;
        double min_alt = 180;
        double max_alt = -180;

        for (FilteredData data:
                filteredTable.sortedList) {
            String display_text = "Az:" + data.azimuth + " Alt:" + data.altitude;

            if (data.azimuth > max_az) max_az =  data.azimuth;
            if (data.azimuth < min_az) min_az =  data.azimuth;
            if (data.altitude < min_alt) min_alt =  data.altitude;
            if (data.altitude > max_alt) max_alt =  data.altitude;


            int col_index =  filteredTable.getMagnitudeColumn();
            if (col_index >= 0){
                display_text += " Mag:" + data.dataRow.vizierData.get(col_index);
            }

            col_index =  filteredTable.getColourColumn();
            if (col_index >= 0){
                display_text += " Col:" + data.dataRow.vizierData.get(col_index);
            }

            long id =  data.dataRow.hashCode();

            System.out.println(display_text);
        }

        System.out.println(table_size + " Radius:" + filteredTable.queryRadius + " Centre Az:" + filteredTable.centreAltAz.getAzimuth() + " Centre Alt:" + filteredTable.centreAltAz.altitude);

        System.out.println("Min Az:" + min_az + " Max az:" + max_az);
        System.out.println("Min Alt:" + min_alt + " Max alt:" + max_alt);

        System.out.println("Min Az:" + filteredTable.getMinAz() + " Max az:" + filteredTable.getMaxAz());
        System.out.println("Min Alt:" + filteredTable.getMinAlt() + " Max alt:" + filteredTable.getMaxAlt());
        System.out.println("Max Colour:" +  filteredTable.getMaxColour() + " Min Colour:" + filteredTable.getMinColour());
    }
}
