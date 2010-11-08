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


package org.apache.isis.extensions.dnd.view.lookup;

import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.consent.Consent;
import org.apache.isis.metamodel.consent.Veto;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.extensions.dnd.drawing.Image;
import org.apache.isis.extensions.dnd.drawing.ImageFactory;
import org.apache.isis.extensions.dnd.view.Content;
import org.apache.isis.extensions.dnd.view.content.AbstractContent;


public class OptionContent extends AbstractContent {
    private final ObjectAdapter adapter;

    public OptionContent(final ObjectAdapter adapter) {
        this.adapter = adapter;
    }

    public Consent canDrop(final Content sourceContent) {
        return Veto.DEFAULT;
    }

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
        return adapter.getIconName();
    }

    public Image getIconPicture(final int iconHeight) {
        if (adapter instanceof ObjectAdapter) {
            final ObjectAdapter adapter = this.adapter;
            if (adapter == null) {
                return ImageFactory.getInstance().loadIcon("empty-field", iconHeight, null);
            }
            final ObjectSpecification specification = adapter.getSpecification();
            Image icon = ImageFactory.getInstance().loadIcon(specification, iconHeight, null);
            if (icon == null) {
                icon = ImageFactory.getInstance().loadDefaultIcon(iconHeight, null);
            }
            return icon;
        } else {
            return null;
        }
    }

    public String getId() {
        return "OptionContent " + adapter;
    }

    public ObjectAdapter getAdapter() {
        return adapter;
    }

    public ObjectAdapter[] getOptions() {
        return null;
    }

    public ObjectSpecification getSpecification() {
        return adapter.getSpecification();
    }

    public boolean isOptionEnabled() {
        return false;
    }

    public boolean isTransient() {
        return false;
    }

    public void parseTextEntry(final String entryText) {}

    public String title() {
        return adapter.titleString();
    }
}

