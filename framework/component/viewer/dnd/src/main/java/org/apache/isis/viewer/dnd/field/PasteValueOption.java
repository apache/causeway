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

package org.apache.isis.viewer.dnd.field;

import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.Workspace;

public class PasteValueOption extends AbstractValueOption {

    public PasteValueOption(final AbstractField field) {
        super(field, "Paste");
    }

    @Override
    public Consent disabled(final View view) {
        final Consent changable = view.canChangeValue();
        if (changable.isVetoed()) {
            return changable;
        } else {
            return changable.setDescription(String.format("Replace field content with '%s' from clipboard", getClipboard(view)));
        }
    }

    @Override
    public void execute(final Workspace workspace, final View view, final Location at) {
        field.pasteFromClipboard();
    }

    private String getClipboard(final View view) {
        return (String) view.getViewManager().getClipboard(String.class);
    }

    @Override
    public String toString() {
        return "PasteValueOption";
    }
}
