package Stellarium;

import StellarStructures.RaDec;

/**
 * Create a listener for field of view changes from Stellarium
 */
public interface StellariumViewListener {
    void viewChanged(StellariumView stellariumView);
}