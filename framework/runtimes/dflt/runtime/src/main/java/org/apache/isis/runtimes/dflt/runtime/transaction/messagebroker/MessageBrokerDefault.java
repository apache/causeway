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

package org.apache.isis.runtimes.dflt.runtime.transaction.messagebroker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.lang.StringUtils;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.MessageBroker;

public class MessageBrokerDefault implements MessageBroker, DebuggableWithTitle {

    private final List<String> messages = Lists.newArrayList();
    private final List<String> warnings = Lists.newArrayList();

    public MessageBrokerDefault() {
    }

    // //////////////////////////////////////////////////
    // Reset / ensureEmpty
    // //////////////////////////////////////////////////

    public void reset() {
        warnings.clear();
        messages.clear();
    }

    @Override
    public void ensureEmpty() {
        if (warnings.size() > 0) {
            throw new IsisException("Message broker still has warnings");
        }
        if (messages.size() > 0) {
            throw new IsisException("Message broker still has messages");
        }
    }

    // //////////////////////////////////////////////////
    // Messages
    // //////////////////////////////////////////////////

    @Override
    public List<String> getMessages() {
        return copyAndClear(messages);
    }

    @Override
    public void addMessage(final String message) {
        messages.add(message);
    }

    @Override
    public String getMessagesCombined() {
        final List<String> x = messages;
        final String string = StringUtils.combine(x);
        return string;
    }

    // //////////////////////////////////////////////////
    // Warnings
    // //////////////////////////////////////////////////

    @Override
    public List<String> getWarnings() {
        return copyAndClear(warnings);
    }

    @Override
    public void addWarning(final String message) {
        warnings.add(message);
    }

    @Override
    public String getWarningsCombined() {
        final List<String> x = warnings;
        final String string = StringUtils.combine(x);
        return string;
    }

    // //////////////////////////////////////////////////
    // Debugging
    // //////////////////////////////////////////////////

    @Override
    public void debugData(final DebugBuilder debug) {
        debugArray(debug, "Messages", messages);
        debugArray(debug, "Warnings", messages);
    }

    private void debugArray(final DebugBuilder debug, final String title, final List<String> vector) {
        debug.appendln(title);
        debug.indent();
        if (vector.size() == 0) {
            debug.appendln("none");
        } else {
            for (final String text : vector) {
                debug.appendln(text);
            }
        }
        debug.unindent();
    }

    @Override
    public String debugTitle() {
        return "Simple Message Broker";
    }

    // //////////////////////////////////////////////////
    // Helpers
    // //////////////////////////////////////////////////

    private List<String> copyAndClear(final List<String> messages) {
        final List<String> copy = Collections.unmodifiableList(new ArrayList<String>(messages));
        messages.clear();
        return copy;
    }

}
