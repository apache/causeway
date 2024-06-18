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
package org.apache.causeway.core.metamodel.services.logwriter;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import lombok.val;

import java.util.Arrays;

import javax.annotation.Priority;
import javax.inject.Inject;

import org.apache.causeway.applib.annotation.PriorityPrecedence;

import org.apache.logging.log4j.Logger;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * Responsible for writing to supplied log.
 *
 * <p>
 *     The framework provides a default implementation as a fallback, but can be replaced by more sophisticated
 *     implementations.
 * </p>
 */
public interface LogWriter {

    void info(Logger log, String template, Object... args);

    @Component
    @Priority(PriorityPrecedence.LATE)
    @ConditionalOnMissingBean(LogWriter.class)
    @RequiredArgsConstructor(onConstructor_ = {@Inject})
    @Log4j2
    class UsingLog4j2 implements LogWriter {

        static final int MAX_ARG_LENGTH_AS_STR = 128;

        private int repeating = 0;
        private LogMessage previous = null;

        @Value(staticConstructor = "of")
        static class LogMessage {
            String template;
            Object[] args;
        }

        @Override
        public void info(Logger log, String template, Object... args) {
            if(log.isInfoEnabled()) {
                val argArray = Arrays.stream(args).map(this::asStrTruncateIfTooLong).toArray();
                val logMessage = LogMessage.of(template, argArray);

                if (logMessage.equals(previous)) {
                    repeating++;
                } else {
                    if (repeating > 0) {
                        log.info("... repeated {} time{}", repeating, (repeating > 1 ? "s" : ""));
                    }

                    repeating = 0;
                    log.info(template, argArray);
                }

                previous = logMessage;
            }
        }

        private String asStrTruncateIfTooLong(Object arg) {
            if(arg == null) {
                return "<null>";
            }
            val argStr = arg.toString();
            if(argStr.length() > MAX_ARG_LENGTH_AS_STR) {
                return argStr.substring(0, MAX_ARG_LENGTH_AS_STR) + "...";
            }
            return argStr;
        }
    }
}
