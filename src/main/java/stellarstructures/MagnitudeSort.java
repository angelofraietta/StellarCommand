package stellarstructures;

public class MagnitudeSort implements Comparable {

    float rowMagnitude;
    StellarDataRow dataRow;

    /**
     * Create a sortable object of StellarData rows based on magnitude
     * @param magnitude magnitude.
     * @param star_data the StellarData
     */
    public MagnitudeSort (float magnitude, StellarDataRow star_data){

        dataRow = star_data;
        rowMagnitude = magnitude;
    }
    @Override
    public int compareTo(Object o) {
        MagnitudeSort r = (MagnitudeSort)o;

        if (rowMagnitude < r.rowMagnitude){
            return -1;
        }
        else if (rowMagnitude > r.rowMagnitude){
            return 1;
        }
        return 0;
    }
}
