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
package org.apache.isis.runtime.headless.logging;

import java.io.PrintStream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;


public class LogConfig {
    private final String loggingPropertyFile;
    private final Level testLoggingLevel;
    private final PrintStream fixtureTracing;

    public LogConfig(
            final Level testLoggingLevel,
            final Level fixtureTracingLevel,
            final Logger fixtureTracingLogger) {
        this(testLoggingLevel, fixtureTracingLevel, fixtureTracingLogger, null);
    }
    public LogConfig(
            final Level testLoggingLevel,
            final Level fixtureTracingLevel,
            final Logger fixtureTracingLogger,
            final String loggingPropertyFile) {
        this(testLoggingLevel, LogStream.logPrintStream(fixtureTracingLogger, fixtureTracingLevel), loggingPropertyFile);
    }
    public LogConfig(
            final Level testLoggingLevel) {
        this(testLoggingLevel, (String)null);
    }
    public LogConfig(
            final Level testLoggingLevel,
            final String loggingPropertyFile) {
        this(testLoggingLevel, null, loggingPropertyFile);
    }
    public LogConfig(
            final Level testLoggingLevel,
            final PrintStream fixtureTracing) {
        this(testLoggingLevel, fixtureTracing, null);
    }
    public LogConfig(
            final Level testLoggingLevel,
            final PrintStream fixtureTracing,
            final String loggingPropertyFile) {
        this.testLoggingLevel = testLoggingLevel != null ? testLoggingLevel : Level.INFO;
        this.fixtureTracing = fixtureTracing != null ? fixtureTracing : System.out;
        this.loggingPropertyFile =
                loggingPropertyFile != null ? loggingPropertyFile : "logging-integtest.properties";
    }

    public Level getTestLoggingLevel() {
        return testLoggingLevel;
    }

    public PrintStream getFixtureTracing() {
        return fixtureTracing;
    }

    public String getLoggingPropertyFile() {
        return loggingPropertyFile;
    }
}
