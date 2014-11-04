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

package org.apache.isis.core.commons.authentication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.google.common.collect.Lists;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;

public class MessageBroker implements Serializable, DebuggableWithTitle {

    private static final long serialVersionUID = 1L;
    
    private final List<String> messages = Lists.newArrayList();
    private final List<String> warnings = Lists.newArrayList();
    private String applicationError;

    //region > acquire (factory method)

    public static MessageBroker acquire(final AuthenticationSession authenticationSession) {
        MessageBroker messageBroker;
        synchronized (authenticationSession) {
            messageBroker = authenticationSession.getMessageBroker();
            if(messageBroker == null) {
                messageBroker = new MessageBroker();
                authenticationSession.setMessageBroker(messageBroker);
            }
        }
        return messageBroker;
    }

    private MessageBroker() {
    }
    //endregion

    //region > reset

    public void reset() {
        warnings.clear();
        messages.clear();
        applicationError = null;
    }

    //endregion

    //region > messages

    public List<String> getMessages() {
        return copyAndClear(messages);
    }

    public void addMessage(final String message) {
        messages.add(message);
    }

    //endregion

    //region > warnings

    public List<String> getWarnings() {
        return copyAndClear(warnings);
    }

    public void addWarning(final String message) {
        warnings.add(message);
    }

    //endregion

    public String getApplicationError() {
        final String error = applicationError;
        setApplicationError(null);
        return error;
    }

    public void setApplicationError(String applicationError) {
        this.applicationError = applicationError;
    }

    //region > debugging

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

    //endregion

    //region > helpers

    private List<String> copyAndClear(final List<String> messages) {
        final List<String> copy = Collections.unmodifiableList(new ArrayList<>(messages));
        messages.clear();
        return copy;
    }

    //endregion


}
