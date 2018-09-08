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

package org.apache.isis.core.runtime.logging;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;

public class IsisLoggingConfigurer {

    private final Level rootLoggerLevelIfFallback;

    private boolean loggingSetup;

    public IsisLoggingConfigurer() {
        this(org.apache.log4j.Level.WARN);
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
        final org.apache.log4j.Level loggingLevel = loggingLevel(args);
        if (loggingLevel != null) {
            org.apache.log4j.Logger.getRootLogger().setLevel(loggingLevel);
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

        if (properties.size() > 0) {
            org.apache.log4j.PropertyConfigurator.configure(properties);
        } else {
            configureFallbackLogging();
        }
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

    private void configureFallbackLogging() {
        final org.apache.log4j.Layout layout = new org.apache.log4j.PatternLayout("%-5r [%-25.25c{1} %-10.10t %-5.5p]  %m%n");
        final org.apache.log4j.Appender appender = new org.apache.log4j.ConsoleAppender(layout);
        org.apache.log4j.BasicConfigurator.configure(appender);
        org.apache.log4j.Logger.getRootLogger().setLevel(rootLoggerLevelIfFallback);
        org.apache.log4j.Logger.getLogger("ui").setLevel(org.apache.log4j.Level.OFF);
    }

    private static org.apache.log4j.Level loggingLevel(final String[] args) {
        org.apache.log4j.Level level = null;
        for (final String arg : args) {
            if (arg.equals("-" + LoggingConstants.DEBUG_OPT)) {
                level = org.apache.log4j.Level.DEBUG;
                break;
            } else if (arg.equals("-" + LoggingConstants.QUIET_OPT)) {
                level = org.apache.log4j.Level.ERROR;
                break;
            } else if (arg.equals("-" + LoggingConstants.VERBOSE_OPT)) {
                level = org.apache.log4j.Level.INFO;
                break;
            }
        }
        return level;
    }

}
