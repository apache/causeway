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

package org.apache.isis.logging;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.layout.PatternLayout;

import lombok.val;

public class IsisLoggingConfigurer {

    private final Level rootLoggerLevelIfFallback;

    private boolean loggingSetup;

    public IsisLoggingConfigurer() {
        this(Level.WARN);
    }

    public IsisLoggingConfigurer(Level rootLoggerLevelIfFallback) {
        this.rootLoggerLevelIfFallback = rootLoggerLevelIfFallback;
    }


    /**
     * As per {@link #configureLogging(String)}.
     *
     * <p>
     * The root logging level can also be adjusted using command line arguments.
     */
    public void configureLogging(final String configDirectory, final String[] args) {
        configureLoggingWithFile(configDirectory + "/" + LoggingConstants.LOGGING_CONFIG_FILE, args);
    }

    public void configureLoggingWithFile(final String configFile, final String[] args) {
        if (loggingSetup) {
            return;
        }
        loggingSetup = true;
        configureLogging(configFile);
        applyLoggingLevelFromCommandLine(args);
    }

    private static void applyLoggingLevelFromCommandLine(final String[] args) {
        val loggingLevel = loggingLevel(args);
        if (loggingLevel != null) {
        	Configurator.setLevel(LogManager.getRootLogger().getName(), loggingLevel);
        }
    }

    /**
     * Sets up logging using either a logging file or (if cannot be found) some
     * sensible defaults.
     *
     * <p>
     * If a {@link LoggingConstants#LOGGING_CONFIG_FILE logging config file} can
     * be located in the provided directory, then that is used. Otherwise, will
     * set up the {@link Log4jLogger#getRootLogger() root logger} to
     * {@link Level#WARN warning}, a typical {@link PatternLayout} and logging
     * to the {@link ConsoleAppender console}.
     */
    private void configureLogging(final String configFile) {
        final Properties properties = new Properties();
        FileInputStream inStream = null;
        try {
            inStream = new FileInputStream(configFile);
            properties.load(inStream);
        } catch (final IOException ignore) {
            // ignore
        } finally {
            closeSafely(inStream);
        }

        if (properties.size() == 0) {
            InputStream inStream2 = null;
            try {
                final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                inStream2 = classLoader.getResourceAsStream(configFile);
                if (inStream2 != null) {
                    properties.load(inStream2);
                }
            } catch (final IOException ignore) {
            } finally {
                closeSafely(inStream2);
            }
        }
        
        ConfigurationBuilder<BuiltConfiguration> builder = 
    			ConfigurationBuilderFactory.newConfigurationBuilder();

        if (properties.size() > 0) {
        	properties.forEach((k, v)->builder.addProperty((String)k, (String)v));
            //PropertyConfigurator.configure(properties); // log4j v1
        } else {
            configureFallbackLogging(builder);
        }
        
        Configurator.initialize(builder.build());
    }

    private static void closeSafely(final Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (final Exception ignore) {
                // ignore
            }
        }
    }

    private void configureFallbackLogging(ConfigurationBuilder<BuiltConfiguration> builder) {
    	
    	val appenderBuilder = builder.newAppender("Stdout", "CONSOLE")
    			.addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT);
    	
    	appenderBuilder.add(builder.newLayout("PatternLayout")
    		    .addAttribute("pattern", "%d [%t] %-5level: %msg%n%throwable")); //log4j v1 ..."%-5r [%-25.25c{1} %-10.10t %-5.5p]  %m%n"
                
        Configurator.setLevel(LogManager.getRootLogger().getName(), rootLoggerLevelIfFallback);
        Configurator.setLevel("ui", Level.OFF);
    }

    private static Level loggingLevel(final String[] args) {
        Level level = null;
        for (final String arg : args) {
            if (arg.equals("-" + LoggingConstants.DEBUG_OPT)) {
                level = Level.DEBUG;
                break;
            } else if (arg.equals("-" + LoggingConstants.QUIET_OPT)) {
                level = Level.ERROR;
                break;
            } else if (arg.equals("-" + LoggingConstants.VERBOSE_OPT)) {
                level = Level.INFO;
                break;
            }
        }
        return level;
    }

}
