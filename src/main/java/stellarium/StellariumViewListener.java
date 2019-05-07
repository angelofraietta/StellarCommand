package stellarium;

/**
 * Create a listener for field of view changes from stellarium
 */
public interface StellariumViewListener {
    void viewRead(StellariumView stellariumView);
    void locationRead(StellariumLocation stellariumView);
    void timeRead(StellariumTime stellariumTime);
}
