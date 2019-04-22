package StellarStructures;

import java.util.List;

/**
 * A row of data that is sorted
 */
public class FilteredData implements Comparable{

    boolean sortAzimuth = true;
    /**
     * Constructor for a row of data
     * @param alt altitude
     * @param az azimuth
     * @param star_data stellar data
     */
    public FilteredData (double alt, double az, StellarDataRow star_data){
        azimuth = az;
        altitude = alt;
        dataRow = star_data;
    }

    final public double azimuth, altitude;
    final public StellarDataRow dataRow;

    // We will just sort by azimuth and altitude for now
    @Override
    public int compareTo(Object o) {
        FilteredData r = (FilteredData)o;

        if (sortAzimuth) {
            if (azimuth < r.azimuth) {
                return -1;
            } else if (azimuth > r.azimuth) {
                return 1;
            } else if (altitude < r.altitude) {
                return -1;

            } else if (altitude > r.altitude) {
                return 1;
            }
        }
        else
        {
            if (altitude < r.altitude) {
                return -1;

            } else if (altitude > r.altitude) {
                return 1;
            }
            else if (azimuth < r.azimuth) {
                return -1;
            } else if (azimuth > r.azimuth) {
                return 1;
            }
        }

        return 0;
    }
}


