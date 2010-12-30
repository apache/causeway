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

import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent2.Consent;
import org.apache.isis.core.metamodel.consent2.Veto;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.dnd.drawing.Image;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.UserActionSet;


public class TextMessageContent implements MessageContent {
    protected final String message;
    protected final String heading;
    protected final String detail;
    protected final String title;

    public TextMessageContent(final String title, final String message) {
        final int pos = message.indexOf(':');
        if (pos > 2) {
            this.heading = message.substring(0, pos).trim();
            this.message = message.substring(pos + 1).trim();
        } else {
            this.heading = "";
            this.message = message;
        }
        this.title = title;
        this.detail = null;
    }

    public String getMessage() {
        return message;
    }

    public String getDetail() {
        return detail;
    }

    public Consent canDrop(final Content sourceContent) {
        return Veto.DEFAULT;
    }

    public void contentMenuOptions(final UserActionSet options) {}

    public void debugDetails(final DebugString debug) {}

    public ObjectAdapter drop(final Content sourceContent) {
        return null;
    }

    public String getDescription() {
        return title;
    }

    public String getHelp() {
        return "";
    }

    public String getIconName() {
        return "message";
    }

    public Image getIconPicture(final int iconHeight) {
        return null;
    }

    public String getId() {
        return "message-exception";
    }

    public ObjectAdapter getAdapter() {
        return null;
    }

    public ObjectAdapter[] getOptions() {
        return null;
    }

    public ObjectSpecification getSpecification() {
        return null;
    }

    public boolean isCollection() {
        return false;
    }

    public boolean isObject() {
        return false;
    }

    public boolean isPersistable() {
        return false;
    }

    public boolean isOptionEnabled() {
        return false;
    }

    public boolean isTransient() {
        return false;
    }

    public boolean isTextParseable() {
        return false;
    }

    public void parseTextEntry(final String entryText) {}

    public String title() {
        return heading;
    }

    public void viewMenuOptions(final UserActionSet options) {}

    public String windowTitle() {
        return title;
    }

}
