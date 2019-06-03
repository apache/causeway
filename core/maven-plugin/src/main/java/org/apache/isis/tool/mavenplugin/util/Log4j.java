/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.tool.mavenplugin.util;

import java.util.Enumeration;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;


public final class Log4j {

    private Log4j(){}

    public static void configureIfRequired() {
        if(isConfigured()) return;
        //org.apache.log4j.BasicConfigurator.configure(); //log4j v1
        Configurator.setLevel(LogManager.getRootLogger().getName(), Level.INFO);
    }

    private static boolean isConfigured() {
    	return LoggerContext.getContext().isInitialized();
    	
//log4j v1    	
//        Enumeration<?> appenders = LogManager.getRootLogger(). getAllAppenders();
//        if (appenders.hasMoreElements()) {
//            return true;
//        }
//        Enumeration<?> loggers = LogManager.getCurrentLoggers();
//        while (loggers.hasMoreElements()) {
//            Logger c = (Logger) loggers.nextElement();
//            if (c.getAllAppenders().hasMoreElements())
//                return true;
//        }
//        return false;
    }


}
