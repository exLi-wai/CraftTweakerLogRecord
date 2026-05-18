package com.lw.CrtLogFilter;

import com.lw.CrtLogFilter.record.ErrorFileLogger;
import com.lw.CrtLogFilter.record.FatalLogAppender;
import com.lw.crt_log_filter.Tags;
import crafttweaker.CrafttweakerImplementationAPI;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import java.io.File;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION, dependencies = "required-after:crafttweaker")
public class CrtLogFilter {

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {

        File logFile = new File("CrtLogFilter/crafttweaker-error.log");

        ErrorFileLogger errorLogger = new ErrorFileLogger(logFile);
        CrafttweakerImplementationAPI.logger.addLogger(errorLogger);

        FatalLogAppender fatalAppender = new FatalLogAppender(logFile);
        fatalAppender.start();
        ((Logger) LogManager.getLogger("Crafttweaker")).addAppender(fatalAppender);
    }
}
