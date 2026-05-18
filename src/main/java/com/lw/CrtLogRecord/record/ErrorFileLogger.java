package com.lw.CrtLogRecord.record;

import crafttweaker.api.player.IPlayer;
import crafttweaker.runtime.ILogger;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;

import java.io.*;
import java.util.regex.Pattern;

public class ErrorFileLogger implements ILogger {

    private static final Pattern FORMATTING_CODE_PATTERN = Pattern.compile("(?i)" + String.valueOf('§') + "[0-9A-FK-OR]");
    private final Writer writer;
    private final PrintWriter printWriter;
    private boolean isDefaultDisabled = false;

    public ErrorFileLogger(File output) {
        try {
            File parent = output.getParentFile();
            if(parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            if(output.isDirectory()) {
                boolean success = output.delete();
                if(!success) {
                    throw new RuntimeException("Failed to delete log file-as-directory " + output);
                }
            }
            writer = new OutputStreamWriter(new FileOutputStream(output), "utf-8");
            printWriter = new PrintWriter(writer);
        } catch(UnsupportedEncodingException ex) {
            throw new RuntimeException("What the heck?");
        } catch(FileNotFoundException ex) {
            throw new RuntimeException("Could not open log file " + output);
        }
    }

    @Override
    public void logCommand(String message) {
    }

    @Override
    public void logInfo(String message) {
    }

    @Override
    public void logWarning(String message) {
    }

    @Override
    public void logError(String message) {
        logError(message, null);
    }

    @Override
    public void logError(String message, Throwable exception) {
        try {
            writer.write("[" + Loader.instance().getLoaderState() + "][" + FMLCommonHandler.instance().getEffectiveSide() + "][ERROR] " + stripMessage(message) + "\n");
            if(exception != null) {
                exception.printStackTrace(printWriter);
            }
            writer.flush();
        } catch(IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void logPlayer(IPlayer player) {
    }

    private String stripMessage(String message) {
        return message == null ? null : FORMATTING_CODE_PATTERN.matcher(message).replaceAll("");
    }

    @Override
    public void logDefault(String message) {
    }

    @Override
    public boolean isLogDisabled() {
        return isDefaultDisabled;
    }

    @Override
    public void setLogDisabled(boolean logDisabled) {
        this.isDefaultDisabled = logDisabled;
    }
}
