package com.lw.CrtLogRecord;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.runtime.CrTTweaker;
import crafttweaker.socket.SingleError;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandCheck extends CommandBase {

    @Override
    public String getName() {
        return "crtcheck";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/crtcheck [loaderName] - Check CraftTweaker script syntax";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        String loaderName = args.length > 0 ? args[0] : "crafttweaker";

        CrtLogUtil.log(CrtLogUtil.INFO, "Starting syntax check for loader: " + loaderName);
        sender.sendMessage(new TextComponentString("§e[CrtLog] Running syntax check for: " + loaderName));

        List<SingleError> errors = new ArrayList<>();

        try {
            ((CrTTweaker) CraftTweakerAPI.tweaker).loadScript(true, errors, false, loaderName);
        } catch(Exception e) {
            CrtLogUtil.log(CrtLogUtil.ERROR, "Syntax check failed: " + e.getMessage());
            sender.sendMessage(new TextComponentString("§c[CrtLog] Syntax check crashed: " + e.getMessage()));
            return;
        }

        if(errors.isEmpty()) {
            CrtLogUtil.log(CrtLogUtil.INFO, "No errors found in loader \"" + loaderName + "\"");
            sender.sendMessage(new TextComponentString("§a[CrtLog] Syntax OK!"));
            return;
        }

        int errorCount = 0;
        int warnCount = 0;

        for(SingleError err : errors) {
            String location = (err.fileName != null ? err.fileName : "?") +
                    (err.line >= 0 ? ":" + err.line : "") +
                    (err.offset >= 0 ? ":" + err.offset : "");
            String message = "[" + err.level + "] " + location + " -- " + err.explanation;

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

        String summary = errorCount + " errors, " + warnCount + " warnings in \"" + loaderName + "\"";
        CrtLogUtil.log(errorCount > 0 ? CrtLogUtil.ERROR : CrtLogUtil.INFO, "Syntax check: " + summary);
        sender.sendMessage(new TextComponentString(
                (errorCount > 0 ? "§c" : "§a") + "[CrtLog] " + summary
        ));
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("ctcheck");
    }
}
