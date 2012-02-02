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


package org.apache.isis.viewer.dnd;

import org.apache.isis.noa.adapter.ObjectAdapter;
import org.apache.isis.noa.reflect.Consent;
import org.apache.isis.noa.reflect.Veto;
import org.apache.isis.noa.spec.ObjectSpecification;
import org.apache.isis.noa.util.DebugString;
import org.apache.isis.viewer.dnd.drawing.Image;


public class ExampleContent implements Content {

    private String iconName;
    private String title;
    private String windowTitle;

    public Consent canDrop(final Content sourceContent) {
        return Veto.DEFAULT;
    }

    public void debugDetails(final DebugString debug) {}

    public ObjectAdapter drop(final Content sourceContent) {
        return null;
    }

    public String getIconName() {
        return iconName;
    }

    public Image getIconPicture(final int iconHeight) {
        return null;
    }

    public ObjectAdapter getAdapter() {
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

    public boolean isTransient() {
        return false;
    }

    public boolean isTextParseable() {
        return false;
    }
    
    public void contentMenuOptions(final UserActionSet options) {}

    public void parseTextEntry(final String entryText) {}

    public void setupIconName(final String iconName) {
        this.iconName = iconName;
    }

    public void setupTitle(final String title) {
        this.title = title;
    }

    public void setupWindowTitle(final String windowTitle) {
        this.windowTitle = windowTitle;
    }

    public String title() {
        return title;
    }

    public String windowTitle() {
        return windowTitle;
    }

    public String getDescription() {
        return "";
    }

    public String getId() {
        return "";
    }

    public void viewMenuOptions(final UserActionSet options) {}

    public String getHelp() {
        return null;
    }

    public ObjectAdapter[] getOptions() {
        return null;
    }

    public boolean isOptionEnabled() {
        return false;
    }
}
