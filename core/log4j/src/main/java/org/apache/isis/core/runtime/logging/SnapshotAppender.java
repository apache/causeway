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

import java.util.Date;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.util.CyclicBuffer;
import org.apache.logging.log4j.core.util.OptionConverter;


class DefaultEvaluator implements LogEventEvaluator {
    @Override
    public boolean isTriggeringEvent(final LogEvent event) {
        return event.getLevel().isMoreSpecificThan(Level.ERROR);
    }
}

public abstract class SnapshotAppender extends AbstractAppender {
    private int bufferSize = 512;
    protected CyclicBuffer buffer = new CyclicBuffer(SnapshotAppender.class, bufferSize);
    private boolean locationInfo = false;
    protected LogEventEvaluator triggerEvaluator;
    private boolean addInfo;

    /**
     * The default constructor will instantiate the appender with a
     * {@link LogEventEvaluator} that will trigger on events with level
     * ERROR or higher.
     */
    public SnapshotAppender() {
        this(new DefaultEvaluator());
    }

    public SnapshotAppender(final LogEventEvaluator evaluator) {
        this.triggerEvaluator = evaluator;
    }

    @Override
    public void append(final LogEvent event) {
        if (shouldAppend()) {
            event.getThreadName();
            event.getNDC();
            if (locationInfo) {
                event.getLocationInformation();
            }
            buffer.add(event);
            if (triggerEvaluator.isTriggeringEvent(event)) {
                writeSnapshot(buffer);
            }
        }
    }

    public void forceSnapshot() {
        writeSnapshot(buffer);
    }

    /**
     * Send the contents of the cyclic buffer as an web server posting.
     */
    private void writeSnapshot(final CyclicBuffer buffer) {
        final StringBuilder details = new StringBuilder();
        final byte[] header = getLayout().getHeader();
        if (header != null) {
            details.append(new String(header));
        }

        if (addInfo) {
            final String user = System.getProperty("user.name");
            final String system = System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ") " + System.getProperty("os.version");
            final String java = System.getProperty("java.vm.name") + " " + System.getProperty("java.vm.version");
            final String version = getFrameworkVersion();

            final LogEvent infoEvent = new Log4jLogEvent("", LogManager.getRootLogger(), Level.INFO, "Snapshot:- " + new Date() + "\n\t" + user + "\n\t" + system + "\n\t" + java + "\n\t" + version, null);
            details.append(getLayout().format(infoEvent));
        }

        final int len = buffer.length();
        String message = "";
        for (int i = 0; i < len; i++) {
            final LogEvent event = buffer.get();
            message = event.getLoggerName() + ": " + event.getMessage();
            details.append(getLayout().toSerializable(event));
            if (getLayout().ignoresThrowable()) {
                final String s = event.getThrownProxy().getExtendedStackTraceAsString();
                if (s != null) {
                    details.append(s);
                    details.append('\n');
                }
            }
        }

        final String footer = getLayout().getFooter();
        if (footer != null) {
            details.append(footer);
        }

        writeSnapshot(message, details.toString());
    }

    // REVIEW: copied down from AboutIsis...
    private String getFrameworkVersion() {
        return "${project.version}-r${buildNumber}";
    }

    protected abstract void writeSnapshot(final String message, final String details);

    @Override
    synchronized public void close() {
        this.closed = true;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public String getEvaluatorClass() {
        return triggerEvaluator == null ? null : triggerEvaluator.getClass().getName();
    }

    public boolean getLocationInfo() {
        return locationInfo;
    }

    /**
     * returns true to show that this appender requires a
     * {@linkorg.apache.log4j.Layout layout}.
     */
    @Override
    public boolean requiresLayout() {
        return true;
    }

    public void setBufferSize(final int bufferSize) {
        this.bufferSize = bufferSize;
        buffer.resize(bufferSize);
    }

    public void setEvaluatorClass(final String value) {
        triggerEvaluator = (LogEventEvaluator) OptionConverter.instantiateByClassName(value, LogEventEvaluator.class, triggerEvaluator);
    }

    public void setAddInfo(final boolean addInfo) {
        this.addInfo = addInfo;
    }

    public void setLocationInfo(final boolean locationInfo) {
        this.locationInfo = locationInfo;
    }

    protected boolean shouldAppend() {
        if (triggerEvaluator == null) {
            errorHandler.error("No TriggeringEventEvaluator is set for appender [" + name + "].");
            return false;
        }

        if (layout == null) {
            errorHandler.error("No layout set for appender named [" + name + "].");
            return false;
        }

        return true;
    }
}
