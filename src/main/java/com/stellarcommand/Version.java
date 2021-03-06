package com.stellarcommand;

/**
 * Contains veriosn info about Stellar Command
 */
public class Version {

    static public int MAJOR = 1;
    static public int MINOR = 1;
    static public int BUILD = 1;

    /**
     * The Major, min or and build as a string
     * @return the version details as text
     */
    public static String getversionText(){
        return MAJOR + "." + MINOR + "." + BUILD;
    }
}
