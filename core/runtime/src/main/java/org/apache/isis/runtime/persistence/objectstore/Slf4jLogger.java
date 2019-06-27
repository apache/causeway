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

package org.apache.isis.runtime.persistence.objectstore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class Slf4jLogger {

    public static final String PROPERTY_ROOT = "isis.logging.";

    enum LogLevel {
        TRACE,
        DEBUG,
        INFO,
        WARN,
        ERROR,
    }

    private Logger logger;
    private final LogLevel level;

    public Slf4jLogger() {
        this(LogLevel.DEBUG);
    }

    public Slf4jLogger(final String level) {
        this(LogLevel.valueOf(level));
    }

    public Slf4jLogger(final LogLevel level) {
        this.level = level;
    }

    protected abstract Class<?> getDecoratedClass();


    protected void log(final String message) {
        doLog(logger(), level, message);
    }

    protected void log(final String request, final Object result) {
        log(request + "  -> " + result);
    }

    protected void doLog(Logger logger, LogLevel level, String format, Object... argArray) {
        switch (level) {
        case TRACE:
            logger.trace(format, argArray);
            break;
        case DEBUG:
            logger.debug(format, argArray);
            break;
        case INFO:
            logger.info(format, argArray);
            break;
        case WARN:
            logger.warn(format, argArray);
            break;
        case ERROR:
            logger.error(format, argArray);
            break;
        }
    }

    private Logger logger() {
        if (logger == null) {
            logger = LoggerFactory.getLogger(getDecoratedClass());
        }
        return logger;
    }
}
