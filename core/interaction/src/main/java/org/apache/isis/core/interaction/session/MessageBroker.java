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

package org.apache.isis.core.interaction.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.isis.commons.internal.collections._Lists;

public class MessageBroker implements Serializable {

    private static final long serialVersionUID = 1L;

    // -- constructor, fields

    private final List<String> messages = _Lists.newArrayList();
    private final List<String> warnings = _Lists.newArrayList();
    private String applicationError;

    public MessageBroker() {
    }


    // -- reset

    public void reset() {
        warnings.clear();
        messages.clear();
        applicationError = null;
    }



    // -- messages

    public List<String> getMessages() {
        return copyAndClear(messages);
    }

    public void addMessage(final String message) {
        messages.add(message);
    }



    // -- warnings

    public List<String> getWarnings() {
        return copyAndClear(warnings);
    }

    public void addWarning(final String message) {
        if(warnings.contains(message)) {
            // just ignore it...
            return;
        }
        warnings.add(message);
    }



    // -- applicationError
    public String getApplicationError() {
        final String error = applicationError;
        setApplicationError(null);
        return error;
    }

    public void setApplicationError(String applicationError) {
        this.applicationError = applicationError;
    }



    // -- helpers

    private List<String> copyAndClear(final List<String> messages) {
        final List<String> copy = Collections.unmodifiableList(new ArrayList<>(messages));
        messages.clear();
        return copy;
    }




}
