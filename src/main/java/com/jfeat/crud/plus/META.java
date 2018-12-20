package com.jfeat.crud.plus;

/**
 * Used to support MBDS ( Meta Based Development Service )
 */
public class META {
    public static final String statusKey = "status";
    public static final String createTime = "createTime";
    public static final String lastModifiedTime = "lastModifiedTime";

    /**
     * ignore status
     */
    private static boolean ignoreStatus = true;
    public static boolean ignoreStatus(boolean ignore){
        ignoreStatus = ignore;
        return ignoreStatus();
    }
    public static boolean ignoreStatus(){
        return ignoreStatus;
    }

    /**
     * ignore timestamp
     */
    private static boolean ignoreTimestamp = true;
    public static boolean ignoreTimestamp(boolean ignore){
        ignoreTimestamp = ignore;
        return ignoreTimestamp();
    }
    public static boolean ignoreTimestamp(){
        return ignoreTimestamp;
    }
}
