package StellarStructures;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

/**
 * Contains a row of data plus a way of doing a fast compare
 */
public class StellarDataRow {
    public final List<Float> vizierData;

    public StellarDataRow (List<Float> data){
        vizierData = data;
    }

    // Code idea from https://stackoverflow.com/questions/27581/what-issues-should-be-considered-when-overriding-equals-and-hashcode-in-java

    @Override
    public int hashCode() {
        HashCodeBuilder hash_start = new HashCodeBuilder(199, 131); // two randomly chosen prime numbers

        for (int i = 0; i < vizierData.size(); i++){
            hash_start.append(vizierData.get(i));
        }

        return hash_start.toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof StellarDataRow))
            return false;
        if (obj == this)
            return true;


        StellarDataRow rhs = (StellarDataRow) obj;

        if (vizierData.size() != rhs.vizierData.size()){
            return false;
        }
        EqualsBuilder equalsBuilder = new EqualsBuilder();

        for (int i = 0; i < vizierData.size(); i++){
            equalsBuilder.append(vizierData.get(i), rhs.vizierData.get(i));
        }
        return equalsBuilder.isEquals();
    }
}
