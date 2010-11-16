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


package org.apache.isis.core.runtime.transaction.messagebroker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.isis.core.commons.debug.DebugInfo;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.lang.StringUtils;


public class MessageBrokerDefault implements MessageBroker, DebugInfo {
    
    private final List<String> messages = new ArrayList<String>();
    private final List<String> warnings = new ArrayList<String>();

    public MessageBrokerDefault() {
    }

    
    ////////////////////////////////////////////////////
    // Reset / ensureEmpty
    ////////////////////////////////////////////////////

    public void reset() {
        warnings.clear();
        messages.clear();
    }
    
    public void ensureEmpty() {
        if (warnings.size() > 0) {
            throw new IsisException("Message broker still has warnings");
        }
        if (messages.size() > 0) {
            throw new IsisException("Message broker still has messages");
        }
    }

    ////////////////////////////////////////////////////
    // Messages
    ////////////////////////////////////////////////////
    
    public List<String> getMessages() {
        return copyAndClear(messages);
    }

    public void addMessage(final String message) {
        messages.add(message);
    }

    public String getMessagesCombined() {
        List<String> x = messages;
        String string = StringUtils.combine(x);
        return string;
    }


    ////////////////////////////////////////////////////
    // Warnings
    ////////////////////////////////////////////////////

    public List<String> getWarnings() {
        return copyAndClear(warnings);
    }

    public void addWarning(final String message) {
        warnings.add(message);
    }
    
    public String getWarningsCombined() {
        List<String> x = warnings;
        String string = StringUtils.combine(x);
        return string;
    }

    ////////////////////////////////////////////////////
    // Debugging
    ////////////////////////////////////////////////////

    public void debugData(final DebugString debug) {
        debugArray(debug, "Messages", messages);
        debugArray(debug, "Warnings", messages);
    }

    private void debugArray(final DebugString debug, final String title, final List<String> vector) {
        debug.appendln(title);
        debug.indent();
        if (vector.size() == 0) {
            debug.appendln("none");
        } else {
            for(String text: vector) {
                debug.appendln(text);
            }
        }
        debug.unindent();
    }

    public String debugTitle() {
        return "Simple Message Broker";
    }


    ////////////////////////////////////////////////////
    // Helpers
    ////////////////////////////////////////////////////

    private List<String> copyAndClear(List<String> messages) {
        List<String> copy = Collections.unmodifiableList(new ArrayList<String>(messages));
        messages.clear();
        return copy;
    }



}
