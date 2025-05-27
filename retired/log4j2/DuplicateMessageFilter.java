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
package org.apache.causeway.applib.log4j2;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

/**
 * <pre>
 * &lt;Configuration status=&quot;WARN&quot; packages=&quot;org.apache.causeway.applib.log4j2&quot; &gt;
 *
 *   &lt;Appenders&gt;
 *     &lt;Console name=&quot;Console&quot;
 *         target=&quot;SYSTEM_OUT&quot; follow=&quot;true&quot;&gt;
 *       &lt;DuplicateMessageFilter/&gt;
 *       &lt;PatternLayout pattern=&quot;${sys:CONSOLE_LOG_PATTERN}&quot; /&gt;
 *     &lt;/Console&gt;
 *     &lt;Console name=&quot;DuplicateMessages&quot;
 *         target=&quot;SYSTEM_OUT&quot; follow=&quot;true&quot;&gt;
 *       &lt;PatternLayout pattern=&quot;${sys:CONSOLE_LOG_PATTERN}&quot; /&gt;
 *     &lt;/Console&gt;
 *   &lt;/Appenders&gt;
 *
 *   &lt;Loggers&gt;
 *     &lt;Logger name=&quot;org.apache.causeway.applib.log4j2.DuplicateMessageFilter&quot;
 *         level=&quot;info&quot; additivity=&quot;false&quot;&gt;
 *       &lt;AppenderRef ref=&quot;DuplicateMessages&quot;/&gt;
 *     &lt;/Logger&gt;
 *     &lt;Root level=&quot;info&quot;&gt;
 *       &lt;AppenderRef ref=&quot;Console&quot; /&gt;
 *     &lt;/Root&gt;
 *   &lt;/Loggers&gt;
 * &lt;/Configuration&gt;
 * </pre>
 */
@Plugin(name = "DuplicateMessageFilter", category = Node.CATEGORY, elementType = Filter.ELEMENT_TYPE, printObject = true)
public class DuplicateMessageFilter extends AbstractFilter {

    private static final Logger log = LogManager.getLogger(DuplicateMessageFilter.class);

    private volatile Message lastMessage = null;
    private final AtomicInteger repeatCount = new AtomicInteger(0);

    public DuplicateMessageFilter() {
        super(Result.DENY, Result.NEUTRAL);
    }

    @Override
    public Result filter(org.apache.logging.log4j.core.Logger logger, Level level, Marker marker, Message msg, Throwable t) {
        return super.filter(logger, level, marker, msg, t);
    }

    @Override
    public Result filter(org.apache.logging.log4j.core.Logger logger, Level level, Marker marker, Object msg, Throwable t) {
        return super.filter(logger, level, marker, msg, t);
    }

    @Override
    public Result filter(org.apache.logging.log4j.core.Logger logger, Level level, Marker marker, String msg, Object... params) {
        return super.filter(logger, level, marker, msg, params);
    }

    @Override
    public Result filter(LogEvent event) {
        Message currentMessage = event.getMessage();

        synchronized (this) {
            if (lastMessage != null && lastMessage.getFormattedMessage().equals(currentMessage.getFormattedMessage())) {
                repeatCount.incrementAndGet();
                return Result.DENY;
            } else {
                if (repeatCount.get() > 0) {
                    log.info(String.format("... repeated %d times", repeatCount.get()));
                }
                lastMessage = currentMessage;
                repeatCount.set(0);
                return Result.NEUTRAL;
            }
        }
    }

    @PluginFactory
    public static DuplicateMessageFilter createFilter() {
        return new DuplicateMessageFilter();
    }
}
