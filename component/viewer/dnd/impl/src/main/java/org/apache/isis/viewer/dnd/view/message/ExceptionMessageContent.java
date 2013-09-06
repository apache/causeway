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

package org.apache.isis.viewer.dnd.view.message;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.exceptions.IsisApplicationException;
import org.apache.isis.core.commons.lang.StringExtensions;
import org.apache.isis.core.commons.lang.ThrowableExtensions;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.dnd.drawing.Image;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.UserActionSet;

public class ExceptionMessageContent implements MessageContent {

    protected String message;
    protected String name;
    protected String trace;
    protected String title;
    private final String icon;

    public ExceptionMessageContent(final Throwable error) {
        String fullName = error.getClass().getName();
        fullName = fullName.substring(fullName.lastIndexOf('.') + 1);
        name = StringExtensions.asNaturalName2(fullName);
        message = error.getMessage();
        trace = ThrowableExtensions.stackTraceFor(error);
        if (trace.indexOf("\tat") != -1) {
            trace = trace.substring(trace.indexOf("\tat"));
        }

        if (name == null) {
            name = "";
        }
        if (message == null) {
            message = "";
        }
        if (trace == null) {
            trace = "";
        }

        if (error instanceof IsisApplicationException) {
            title = "Application Error";
            icon = "application-error";
        } else if (error instanceof ConcurrencyException) {
            title = "Concurrency Error";
            icon = "concurrency-error";
        } else {
            title = "System Error";
            icon = "system-error";
        }

    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getDetail() {
        return trace;
    }

    @Override
    public String getIconName() {
        return icon;
    }

    @Override
    public Consent canDrop(final Content sourceContent) {
        return Veto.DEFAULT;
    }

    @Override
    public void contentMenuOptions(final UserActionSet options) {
    }

    @Override
    public void debugDetails(final DebugBuilder debug) {
    }

    @Override
    public ObjectAdapter drop(final Content sourceContent) {
        return null;
    }

    @Override
    public String getDescription() {
        return name;
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public Image getIconPicture(final int iconHeight) {
        return null;
    }

    @Override
    public String getId() {
        return "message-exception";
    }

    @Override
    public ObjectAdapter getAdapter() {
        return null;
    }

    @Override
    public ObjectAdapter[] getOptions() {
        return null;
    }

    @Override
    public ObjectSpecification getSpecification() {
        return null;
    }

    @Override
    public boolean isCollection() {
        return false;
    }

    @Override
    public boolean isObject() {
        return false;
    }

    @Override
    public boolean isOptionEnabled() {
        return false;
    }

    @Override
    public boolean isPersistable() {
        return false;
    }

    @Override
    public boolean isTransient() {
        return false;
    }

    @Override
    public boolean isTextParseable() {
        return false;
    }

    public void parseTextEntry(final String entryText) {
    }

    @Override
    public String title() {
        return name;
    }

    @Override
    public void viewMenuOptions(final UserActionSet options) {
    }

    @Override
    public String windowTitle() {
        return title;
    }

}
