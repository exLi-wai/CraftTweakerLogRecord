package com.lw.CrtLogFilter.record;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FatalLogAppender extends AbstractAppender {

    private final Writer writer;
    private final PrintWriter printWriter;

    public FatalLogAppender(File output) {
        super("FatalLogAppender", null, null, false);
        try {
            File parent = output.getParentFile();
            if(parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            writer = new OutputStreamWriter(new FileOutputStream(output, true), StandardCharsets.UTF_8);
            printWriter = new PrintWriter(writer);
        } catch(IOException e) {
            throw new RuntimeException("Could not open fatal log file " + output, e);
        }
    }

    @Override
    public void append(LogEvent event) {
        if(event.getLevel() != Level.FATAL) {
            return;
        }
        try {
            writer.write("[FATAL] " + event.getMessage().getFormattedMessage() + "\n");
            if(event.getThrown() != null) {
                event.getThrown().printStackTrace(printWriter);
            }
            writer.flush();
        } catch(IOException ignored) {
        }
    }
}
