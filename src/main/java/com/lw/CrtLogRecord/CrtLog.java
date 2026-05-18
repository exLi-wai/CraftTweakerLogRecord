package com.lw.CrtLogRecord;

import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.crt_log_record.CrtLog")
@ZenRegister
public class CrtLog {

    // ERROR

    @ZenMethod
    public static boolean error(String message) {
        CrtLogUtil.log(CrtLogUtil.ERROR, message);
        return true;
    }

    @ZenMethod
    public static boolean error(boolean condition, String message) {
        if(condition) {
            CrtLogUtil.log(CrtLogUtil.ERROR, message);
        }
        return condition;
    }

    @ZenMethod
    public static String error(String message, String[] args) {
        String formatted = CrtLogUtil.format(message, args);
        CrtLogUtil.log(CrtLogUtil.ERROR, formatted);
        return formatted;
    }

    @ZenMethod
    public static int error(int code, String message) {
        CrtLogUtil.log(CrtLogUtil.ERROR, "[" + code + "] " + message);
        return code;
    }

    @ZenMethod
    public static int error(String message, int repeat) {
        for(int i = 0; i < repeat; i++) {
            CrtLogUtil.log(CrtLogUtil.ERROR, CrtLogUtil.repeat(message, i, repeat));
        }
        return repeat;
    }

    @ZenMethod
    public static double error(double value, String message) {
        CrtLogUtil.log(CrtLogUtil.ERROR, "[" + value + "] " + message);
        return value;
    }

    @ZenMethod
    public static long error(long timestamp, String message) {
        CrtLogUtil.log(CrtLogUtil.ERROR, "[" + timestamp + "] " + message);
        return timestamp;
    }

    @ZenMethod
    public static String error(Object object) {
        String msg = String.valueOf(object);
        CrtLogUtil.log(CrtLogUtil.ERROR, msg);
        return msg;
    }

    // FATAL

    @ZenMethod
    public static boolean fatal(String message) {
        CrtLogUtil.log(CrtLogUtil.FATAL, message);
        return true;
    }

    @ZenMethod
    public static boolean fatal(boolean condition, String message) {
        if(condition) {
            CrtLogUtil.log(CrtLogUtil.FATAL, message);
        }
        return condition;
    }

    @ZenMethod
    public static String fatal(String message, String[] args) {
        String formatted = CrtLogUtil.format(message, args);
        CrtLogUtil.log(CrtLogUtil.FATAL, formatted);
        return formatted;
    }

    @ZenMethod
    public static int fatal(int code, String message) {
        CrtLogUtil.log(CrtLogUtil.FATAL, "[" + code + "] " + message);
        return code;
    }

    @ZenMethod
    public static int fatal(String message, int repeat) {
        for(int i = 0; i < repeat; i++) {
            CrtLogUtil.log(CrtLogUtil.FATAL, CrtLogUtil.repeat(message, i, repeat));
        }
        return repeat;
    }

    @ZenMethod
    public static double fatal(double value, String message) {
        CrtLogUtil.log(CrtLogUtil.FATAL, "[" + value + "] " + message);
        return value;
    }

    @ZenMethod
    public static long fatal(long timestamp, String message) {
        CrtLogUtil.log(CrtLogUtil.FATAL, "[" + timestamp + "] " + message);
        return timestamp;
    }

    @ZenMethod
    public static String fatal(Object object) {
        String msg = String.valueOf(object);
        CrtLogUtil.log(CrtLogUtil.FATAL, msg);
        return msg;
    }

    // WARN

    @ZenMethod
    public static boolean warn(String message) {
        CrtLogUtil.log(CrtLogUtil.WARN, message);
        return true;
    }

    @ZenMethod
    public static boolean warn(boolean condition, String message) {
        if(condition) {
            CrtLogUtil.log(CrtLogUtil.WARN, message);
        }
        return condition;
    }

    @ZenMethod
    public static int warn(int code, String message) {
        CrtLogUtil.log(CrtLogUtil.WARN, "[" + code + "] " + message);
        return code;
    }

    @ZenMethod
    public static String warn(Object object) {
        String msg = String.valueOf(object);
        CrtLogUtil.log(CrtLogUtil.WARN, msg);
        return msg;
    }
}
