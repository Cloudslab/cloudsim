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

    /** Denotes the default baud rate for CloudSim entities. */
    public static final int DEFAULT_BAUD_RATE = 9600;

    // ================== DataCloud constants ==================

    /** Default Maximum Transmission Unit (MTU) of a link in bytes. */
    public static final int DEFAULT_MTU = 1500;

    /** The default packet size (in byte) for sending events to other entity. */
    public static final int PKT_SIZE = DEFAULT_MTU * 100;  // in bytes

    /** The default storage size (10 GB in byte). */
    public static final int DEFAULT_STORAGE_SIZE = 10000000;


    /** One million. */
    public static final int MILLION = 1000000;

    // ================== Time constants ==================
    /** One minute time in seconds. */
    public static final int MINUTE = 60;
    /** One hour time in seconds. */
    public static final int HOUR = 60 * MINUTE;
    /** One day time in seconds. */
    public static final int DAY = 24 * HOUR;
    /** One week time in seconds. */
    public static final int WEEK = 7 * DAY;

    // ================== OS constants ==================
    /** Constant for *nix Operating Systems. */
    public static final String NIX_OS = "Linux/Unix";
    /** Constant for Windows Operating Systems. */
    public static final String WINDOWS = "Windows";
}
