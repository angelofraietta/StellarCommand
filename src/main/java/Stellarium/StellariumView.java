package Stellarium;

import StellarStructures.RaDec;

/**
 * Displays what we are seeing on view as far as field of view ands RaDec
 */
public class StellariumView {
    /**
     * Get field of view in degrees
     * @return field f view
     */
    public float getFieldOfView() {
        return fieldOfView;
    }

    /**
     * set field of view in degrees
     * @param fieldOfView the field of view we want to set Stellarium to use
     */
    public void setFieldOfView(float fieldOfView) {
        this.fieldOfView = fieldOfView;
    }

    /**
     * get Right ascension and declination
     * @return Ra and Dec.
     */
    public RaDec getRaDec() {
        return raDec;
    }

    /**
     * Set Right ascension and declination
     * @param raDec the RaDec we are seting Stellarium to
     */
    public void setRaDec(RaDec raDec) {
        this.raDec = raDec;
    }

    float fieldOfView;
    RaDec raDec;

    /**
     * Constructor
     * @param field_of_view the field of view in stellarium in degrees
     * @param ra_dec the Right ascension and declination
     */
    public StellariumView (float field_of_view, RaDec ra_dec){
        this.fieldOfView = field_of_view;
        this.raDec = ra_dec;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof StellariumView))
            return false;
        if (obj == this)
            return true;


        StellariumView rhs = (StellariumView) obj;

        return rhs.fieldOfView == fieldOfView && rhs.raDec.equals(raDec);
    }
}
