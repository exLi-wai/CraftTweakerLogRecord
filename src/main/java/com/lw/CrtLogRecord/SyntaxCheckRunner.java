package com.lw.CrtLogRecord;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.runtime.CrTTweaker;
import crafttweaker.socket.SingleError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SyntaxCheckRunner {

    public static final String DEFAULT_LOADER = "crafttweaker";

    private SyntaxCheckRunner() {
    }

    public static Result run(String loaderName) {
        String resolvedLoaderName = normalizeLoaderName(loaderName);
        List<String> lines = new ArrayList<>();
        List<SingleError> errors = new ArrayList<>();

        lines.add("[CrtLog] Running syntax check for: " + resolvedLoaderName);
        CrtLogUtil.log(CrtLogUtil.INFO, "Starting syntax check for loader: " + resolvedLoaderName);

        try {
            ((CrTTweaker) CraftTweakerAPI.tweaker).loadScript(true, errors, false, resolvedLoaderName);
        } catch(Exception e) {
            String message = "[CrtLog] Syntax check crashed: " + e.getMessage();
            CrtLogUtil.log(CrtLogUtil.ERROR, "Syntax check failed: " + e.getMessage());
            lines.add(message);
            return new Result(resolvedLoaderName, false, 1, 0, lines);
        }

        if(errors.isEmpty()) {
            String message = "[CrtLog] Syntax OK!";
            CrtLogUtil.log(CrtLogUtil.INFO, "No errors found in loader \"" + resolvedLoaderName + "\"");
            lines.add(message);
            return new Result(resolvedLoaderName, true, 0, 0, lines);
        }

        int errorCount = 0;
        int warnCount = 0;

        for(SingleError err : errors) {
            String location = (err.fileName != null ? err.fileName : "?") +
                    (err.line >= 0 ? ":" + err.line : "") +
                    (err.offset >= 0 ? ":" + err.offset : "");
            String message = "[" + err.level + "] " + location + " -- " + err.explanation;
            lines.add(message);

            switch(err.level) {
                case ERROR:
                    CrtLogUtil.log(CrtLogUtil.ERROR, message);
                    errorCount++;
                    break;
                case WARN:
                    CrtLogUtil.log(CrtLogUtil.WARN, message);
                    warnCount++;
                    break;
                default:
                    CrtLogUtil.log(CrtLogUtil.INFO, message);
                    break;
            }
        }

        String summary = errorCount + " errors, " + warnCount + " warnings in \"" + resolvedLoaderName + "\"";
        CrtLogUtil.log(errorCount > 0 ? CrtLogUtil.ERROR : CrtLogUtil.INFO, "Syntax check: " + summary);
        lines.add("[CrtLog] " + summary);

        return new Result(resolvedLoaderName, errorCount == 0, errorCount, warnCount, lines);
    }

    private static String normalizeLoaderName(String loaderName) {
        if(loaderName == null || loaderName.trim().isEmpty()) {
            return DEFAULT_LOADER;
        }
        return loaderName.trim();
    }

    public static final class Result {
        private final String loaderName;
        private final boolean success;
        private final int errorCount;
        private final int warningCount;
        private final List<String> lines;

        private Result(String loaderName, boolean success, int errorCount, int warningCount, List<String> lines) {
            this.loaderName = loaderName;
            this.success = success;
            this.errorCount = errorCount;
            this.warningCount = warningCount;
            this.lines = Collections.unmodifiableList(new ArrayList<>(lines));
        }

        public String getLoaderName() {
            return loaderName;
        }

        public boolean isSuccess() {
            return success;
        }

        public int getErrorCount() {
            return errorCount;
        }

        public int getWarningCount() {
            return warningCount;
        }

        public List<String> getLines() {
            return lines;
        }

        public String toTerminalText() {
            StringBuilder builder = new StringBuilder();
            for(String line : lines) {
                builder.append(line).append('\n');
            }
            return builder.toString();
        }
    }
}
