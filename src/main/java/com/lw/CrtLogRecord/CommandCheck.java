package com.lw.CrtLogRecord;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

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
        String loaderName = args.length > 0 ? args[0] : SyntaxCheckRunner.DEFAULT_LOADER;
        SyntaxCheckRunner.Result result = SyntaxCheckRunner.run(loaderName);
        String color = result.isSuccess() ? "\u00a7a" : "\u00a7c";

        for(String line : result.getLines()) {
            sender.sendMessage(new TextComponentString(color + line));
        }
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
