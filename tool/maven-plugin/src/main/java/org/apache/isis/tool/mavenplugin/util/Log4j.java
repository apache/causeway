package org.apache.isis.tool.mavenplugin.util;

import java.util.Enumeration;

public final class Log4j {
    
    private Log4j(){}

    public static void configureIfRequired() {
        if(isConfigured()) return;
        org.apache.log4j.BasicConfigurator.configure();
        org.apache.log4j.LogManager.getRootLogger().setLevel(org.apache.log4j.Level.INFO);
    }
    
    private static boolean isConfigured() {
        Enumeration<?> appenders = org.apache.log4j.LogManager.getRootLogger().getAllAppenders();
        if (appenders.hasMoreElements()) {
            return true;
        } 
        Enumeration<?> loggers = org.apache.log4j.LogManager.getCurrentLoggers();
        while (loggers.hasMoreElements()) {
            org.apache.log4j.Logger c = (org.apache.log4j.Logger) loggers.nextElement();
            if (c.getAllAppenders().hasMoreElements())
                return true;
        }
        return false;
    }


}
