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


package org.apache.isis.extensions.dnd.view.content;

import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.consent.Consent;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.extensions.dnd.drawing.Image;
import org.apache.isis.extensions.dnd.view.Content;
import org.apache.isis.extensions.dnd.view.UserActionSet;


public class NullContent implements Content {

    private final String title;

    public NullContent() {
        this("");
    }

    public NullContent(final String title) {
        this.title = title;
    }

    public Consent canDrop(final Content sourceContent) {
        return null;
    }

    public void contentMenuOptions(final UserActionSet options) {}

    public void debugDetails(final DebugString debug) {}

    public ObjectAdapter drop(final Content sourceContent) {
        return null;
    }

    public String getDescription() {
        return null;
    }

    public String getHelp() {
        return null;
    }

    public String getIconName() {
        return null;
    }

    public Image getIconPicture(final int iconHeight) {
        return null;
    }

    public String getId() {
        return null;
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

    public boolean isOptionEnabled() {
        return false;
    }

    public boolean isPersistable() {
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
        return title;
    }

    public void viewMenuOptions(final UserActionSet options) {}

    public String windowTitle() {
        return title;
    }

}

