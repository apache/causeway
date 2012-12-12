package org.apache.isis.tool.mavenplugin.util;

import java.util.Enumeration;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public final class Log4j {
    
    private Log4j(){}

    public static void configureIfRequired() {
        if(isConfigured()) return;
        BasicConfigurator.configure();
        LogManager.getRootLogger().setLevel(Level.INFO);
    }
    
    private static boolean isConfigured() {
        Enumeration<?> appenders = LogManager.getRootLogger().getAllAppenders();
        if (appenders.hasMoreElements()) {
            return true;
        } 
        Enumeration<?> loggers = LogManager.getCurrentLoggers();
        while (loggers.hasMoreElements()) {
            Logger c = (Logger) loggers.nextElement();
            if (c.getAllAppenders().hasMoreElements())
                return true;
        }
        return false;
    }


}
