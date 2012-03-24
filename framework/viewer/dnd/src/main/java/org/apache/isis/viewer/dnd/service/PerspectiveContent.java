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

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.userprofile.PerspectiveEntry;
import org.apache.isis.viewer.dnd.drawing.Image;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.content.AbstractContent;

public class PerspectiveContent extends AbstractContent {
    private final PerspectiveEntry perspective;

    public PerspectiveContent(final PerspectiveEntry perspective) {
        this.perspective = perspective;
    }

    @Override
    public void debugDetails(final DebugBuilder debug) {
        debug.appendln("perspective", perspective);
    }

    @Override
    public ObjectAdapter getAdapter() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public String getId() {
        return "";
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
    public boolean isObject() {
        return false;
    }

    @Override
    public boolean isOptionEnabled() {
        return false;
    }

    @Override
    public boolean isTransient() {
        return false;
    }

    @Override
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

    @Override
    public Consent canDrop(final Content sourceContent) {
        return Veto.DEFAULT;
    }

    @Override
    public ObjectAdapter drop(final Content sourceContent) {
        return null;
    }

    @Override
    public String getIconName() {
        return "icon";
    }

    @Override
    public Image getIconPicture(final int iconHeight) {
        return null;
    }

    public void parseTextEntry(final String entryText) {
    }

    public PerspectiveEntry getPerspective() {
        return perspective;
    }
}
