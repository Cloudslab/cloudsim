package org.cloudbus.cloudsim;

/**
 * 
 * Defines common constants, used throughout cloudsim.
 * 
 * @author nikolay.grozev
 * 
 */
public final class Consts {

    /** Suppreses intantiation. */
    private Consts() {
    }

    /** One million. */
    public static int MILLION = 1000000;

    // ================== Time constants ==================
    /** One minute time in seconds. */
    public static int MINUTE = 60;
    /** One hour time in seconds. */
    public static int HOUR = 60 * MINUTE;
    /** One day time in seconds. */
    public static int DAY = 24 * HOUR;
    /** One week time in seconds. */
    public static int WEEK = 24 * HOUR;

    // ================== OS constants ==================
    /** Constant for *nix OS-es. */
    public static final String NIX_OS = "Linux/Unix";
    /** Constant for Windows OS-es. */
    public static final String WINDOWS = "Windows";
}
