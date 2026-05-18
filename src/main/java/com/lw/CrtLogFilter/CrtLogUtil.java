package com.lw.CrtLogFilter;

import crafttweaker.CraftTweakerAPI;
import org.apache.logging.log4j.LogManager;

public class CrtLogUtil {

    static final int ERROR = 0;
    static final int FATAL = 1;
    static final int WARN = 2;
    static final int INFO = 3;
    static final int TRACE = 4;

    private static final String PREFIX = "[CrtLog] ";

    private CrtLogUtil() {
    }

    static void log(int level, String message) {
        String msg = PREFIX + message;
        switch(level) {
            case ERROR:
                CraftTweakerAPI.logError(msg);
                break;
            case FATAL:
                LogManager.getLogger("Crafttweaker").fatal(msg);
                break;
            case WARN:
                CraftTweakerAPI.logWarning(msg);
                break;
            case INFO:
                CraftTweakerAPI.logInfo(msg);
                break;
            case TRACE:
                LogManager.getLogger("Crafttweaker").trace(msg);
                break;
        }
    }

    static String format(String message, String[] args) {
        return String.format(message, (Object[]) args);
    }

    static String repeat(String message, int index, int total) {
        return "(" + index + "/" + total + ") " + message;
    }
}
