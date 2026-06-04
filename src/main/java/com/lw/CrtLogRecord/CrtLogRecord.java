package com.lw.CrtLogRecord;

import com.lw.CrtLogRecord.record.ErrorFileLogger;
import com.lw.CrtLogRecord.record.FatalLogAppender;
import com.lw.crt_log_record.Tags;
import crafttweaker.CrafttweakerImplementationAPI;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import java.io.File;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION, dependencies = "required-after:crafttweaker")
public class CrtLogRecord {

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {

        File logFile = new File("CrtLogRecord/crafttweaker-Record.log");

        ErrorFileLogger errorLogger = new ErrorFileLogger(logFile);
        CrafttweakerImplementationAPI.logger.addLogger(errorLogger);

        FatalLogAppender fatalAppender = new FatalLogAppender(logFile);
        fatalAppender.start();
        ((Logger) LogManager.getLogger("Crafttweaker")).addAppender(fatalAppender);
    }

    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandCheck());
    }
}
