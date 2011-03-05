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


package org.apache.isis.viewer.dnd.service;

import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtimes.dflt.runtime.userprofile.PerspectiveEntry;
import org.apache.isis.viewer.dnd.drawing.Image;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.content.AbstractContent;


public class PerspectiveContent extends AbstractContent {
    private final PerspectiveEntry perspective;

    public PerspectiveContent(PerspectiveEntry perspective) {
        this.perspective = perspective;
    }

    public void debugDetails(final DebugString debug) {
        debug.appendln("perspective", perspective);
    }

    public ObjectAdapter getAdapter() {
        return null;
    }

    public String getDescription() {
        return null;
    }

    public String getHelp() {
        return "";
    }

    public String getId() {
        return "";
    }

    public ObjectAdapter[] getOptions() {
        return null;
    }

    public ObjectSpecification getSpecification() {
        return null;
    }

    @Override
    public boolean isObject() {
        return false;
    }

    public boolean isOptionEnabled() {
        return false;
    }

    public boolean isTransient() {
        return false;
    }

    public String title() {
        return perspective.getTitle();
    }

    @Override
    public String toString() {
        return "Perspective: " + perspective;
    }

    @Override
    public String windowTitle() {
        return perspective.getTitle();
    }

    public Consent canDrop(Content sourceContent) {
        return Veto.DEFAULT;
    }

    public ObjectAdapter drop(Content sourceContent) {
        return null;
    }

    public String getIconName() {
        return "icon";
    }

    public Image getIconPicture(int iconHeight) {
        return null;
    }

    public void parseTextEntry(String entryText) {}

    public PerspectiveEntry getPerspective() {
        return perspective;
    }
}
