package com.cloudcore.master.core;

public class Config {


    /* Constant Fields */

    public static final String MODULE_NAME = "CloudCoin-Safe";

    public static final int EXPIRATION_YEARS = 2;

    public static final String TAG_DETECTED = "Detected";
    public static final String TAG_EXPORT = "Export";
    public static final String TAG_IMPORT = "Import";
    public static final String TAG_SUSPECT = "Suspect";

    public static final String TAG_BANK = "Bank";
    public static final String TAG_FRACKED = "Fracked";
    public static final String TAG_COUNTERFEIT = "Counterfeit";
    public static final String TAG_LOST = "Lost";

    public static final String TAG_COMMAND = "Command";
    public static final String TAG_LOGS = "Logs";
    public static final String TAG_TEMPLATES = "Templates";

    public static final String TAG_DEFAULT = "DefaultUser";


    /* Fields */

    public static int milliSecondsToTimeOutDetect = 2000;
    public static int milliSecondsToTimeOutFixing = 5000;
    public static int multiDetectLoad = 200;
    public static int nodeCount = 25;
    public static int passCount = 16;
}
