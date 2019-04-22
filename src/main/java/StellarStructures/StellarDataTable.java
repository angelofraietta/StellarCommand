package StellarStructures;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Class that reads and parses VizieR Data Table
 */
public class StellarDataTable {
    final static String TEST_FILENAME = "data/tables/larawag.txt";
    final static String CLOSE_TEST_FILENAME = "data/tables/Antares.txt"; // THis table has data that overflows

    final String COLUMN_START = "---";
    final String END_DATA = "#END#";

    // The details about our query are in this
    //#INFO -c=105.756134-23.833292,rd=40.

    final String INFO_DATA = "#INFO -c=";
    final String RADIUS_DEGREES = "rd=";
    /**
     * Get the names of the columns
     * @return array of column names
     */
    public String[] getColumnNames() {
        return columnNames;
    }


    /**
     * Get the radius of our centre point query
     * @return the radius
     */
    public float getQueryRadius() {
        return queryRadius;
    }

    float queryRadius = 0;

    public String getCentreType() {
        return centreType;
    }

    /**
     * Get the centre point of our Query. Will return null if not defined
     * @return the centre point of our query as an RaDec
     */
    public RaDec getCentrePoint() {
        return centrePoint;
    }

    RaDec centrePoint = null;

    String centreType = "";
    /**
     * Get the Data Table
     * @return Data table
     */
    public List<StellarDataRow> getDataTable() {
        return dataTable;
    }

    String [] columnNames = new String[]{};

    List <StellarDataRow> dataTable =  new ArrayList<>();

    /**
     * Gets the details about the actual query
     * eg #INFO -c=105.756134-23.833292,rd=40.
     * @return the centre points and query type. rd is radius in degrees
     */
    public String getDataQuery() {
        return dataQuery;
    }

    String dataQuery = "";

    /**
     * Load the VizieR Data from a BufferedReader
     * @param reader the BufferedReader
     * @throws IOException exception if unable to load the table
     */
    public void loadTable(BufferedReader reader) throws IOException {
        boolean column_read_started = false;
        boolean reading_data = false;
        boolean read_complete = false;

        String readline  = "";

        List<Integer> column_widths = new ArrayList<>();

        //double scheduler_time = HBScheduler.getGlobalScheduler().getSchedulerTime();

        while ((readline = reader.readLine())!= null && !read_complete){
            if (readline.isEmpty()){
                continue;
            }

            if (readline.startsWith(INFO_DATA)){
                dataQuery = readline.replace(INFO_DATA, "").trim();
                try {
                    String[] params = dataQuery.split("[" + Pattern.quote("-") + Pattern.quote("+") + ",]");
                    if (params.length > 1) {

                        float ra = Float.parseFloat(params[0]);
                        int ra_sttr_len = params[0].length();

                        // we need to see if our sign is positive or negative

                        float dec = Float.parseFloat(params[1]);
                        String sign =  dataQuery.substring(ra_sttr_len, ra_sttr_len + 1);
                        if (sign.endsWith("-")){
                            dec *= -1;
                        }

                        centrePoint = new RaDec(ra, dec);
                    }
                    if(params.length > 2){
                        centreType = params[2];
                        if (centreType.startsWith(RADIUS_DEGREES)){
                            String radius_txt = centreType.replace(RADIUS_DEGREES, "");
                            if (radius_txt.endsWith(".")){
                                radius_txt = radius_txt.substring(0, radius_txt.length()-1);
                            }
                            queryRadius = Float.parseFloat(radius_txt);
                        }
                    }
                }
                catch (Exception ex){}
            }
            else if (readline.startsWith(COLUMN_START)){
                if (!column_read_started){
                    column_read_started = true;

                    String [] col_widths = readline.split(" ");
                    columnNames = new String[col_widths.length];

                    for (int i = 0; i < col_widths.length; i++){
                        column_widths.add(col_widths[i].length());
                        columnNames[i] = "";
                    }
                }
                else {
                    reading_data = true;
                    //System.out.println("Start Data");
                }

            }
            else if (readline.startsWith(END_DATA)){
                read_complete = true;
            }
            else {
                if (!reading_data){
                    if (column_read_started) {
                        int cursor = 0;
                        // we are reading column information
                        for (int i = 0; i < column_widths.size(); i++) {
                            int col_width = column_widths.get(i);
                            String column_text = readline.substring(cursor, cursor + col_width).trim();
                            cursor += col_width + 1;

                            // we have read that column, now append to name
                            String existing_name = columnNames [i];

                            existing_name += column_text;
                            columnNames[i] = existing_name;
                        }
                    }
                }
                else{
                    if (!read_complete) {
                        try{
                            List<Float> row_data = new ArrayList<>();

                            int cursor = 0;
                            // we are reading column information
                            for (int i = 0; i < column_widths.size(); i++) {
                                int col_width = column_widths.get(i);
                                String column_text = readline.substring(cursor, cursor + col_width ).trim();
                                cursor += col_width + 1;

                                float data = Float.parseFloat(column_text);
                                row_data.add(data);

                            }
                            // now add or row of floats to our list
                            dataTable.add(new StellarDataRow(row_data));
                        }catch (Exception ex){}
                    }
                }
            }

        }
    }
    /**
     * Load the VizieR Data from a file
     * @param filename filename to load
     * @return returns true if able to load
     */
    public boolean loadTable(String filename){
        File f = new File(filename);

        boolean ret = false;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(f));
            //double elapsed_time = HBScheduler.getGlobalScheduler().getSchedulerTime() - scheduler_time;
            loadTable(reader);
            ret = true;
            //System.out.println("Time to parse: " + elapsed_time);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }

    /**
     * Print The contents of table to stdout
     */
    void printTable() {
        // print each row with type and data
        for (StellarDataRow data_row :
                dataTable) {

            String row_text = "";
            // Get each Name
            for (int i = 0; i < columnNames.length; i++) {


                String name = columnNames[i];
                float data = data_row.vizierData.get(i);

                row_text += name + "=" + data + " ";
            }


            System.out.println(row_text);

        }
        System.out.println(dataTable.size() + " Rows");
    }

    /**
     * Load the test table
     * @return the test table
     */
    public static StellarDataTable loadTestTable(){
        StellarDataTable table = new StellarDataTable();
        table.loadTable(TEST_FILENAME);
        return table;
    }
    /**
     * Load the test table that is near our test table
     * @return
     */
    private static StellarDataTable loadCloseTestTable(){
        StellarDataTable table = new StellarDataTable();
        table.loadTable(CLOSE_TEST_FILENAME);
        return table;
    }


    /*
    Function to test
     */
    public static void main(String[] args) {
        StellarDataTable table = loadTestTable();

        Collection<StellarDataRow> noDups = new HashSet<StellarDataRow>();

        for (StellarDataRow data_row :
                table.dataTable) {
            if (noDups.contains(data_row)){
                System.out.println("Error - Duplicate found");
            }

            noDups.add(data_row);

        }

        // Now create an Identical Table and ensure everything there
        StellarDataTable dup_table = loadTestTable();


        for (StellarDataRow data_row :
                dup_table.dataTable) {
            if (!noDups.contains(data_row)){
                System.out.println("Error - Entry Not found");
            }

        }


        // Now compare Anteres table
        StellarDataTable close_table = loadCloseTestTable();


        int num_match = 0, num_not_match = 0;

        for (StellarDataRow data_row :
                close_table.dataTable) {
            if (noDups.contains(data_row)){
                num_match++;
            }
            else {
                num_not_match++;
            }

        }

        table.printTable();

        System.out.println("Match:" + num_match + " Non-match:" + num_not_match);




    }
}
