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

package org.apache.isis.viewer.wicket.ui.components.widgets.dropdownchoices;

import java.util.List;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;

import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;

public class DropDownChoicesForValueMementos extends DropDownChoice<ObjectAdapterMemento> {

    private final static class ValueMementoRenderer implements IChoiceRenderer<ObjectAdapterMemento> {

        private static final long serialVersionUID = 1L;

        @Override
        public Object getDisplayValue(final ObjectAdapterMemento nom) {
            return nom.getObjectAdapter(ConcurrencyChecking.NO_CHECK).titleString();
        }

        @Override
        public String getIdValue(final ObjectAdapterMemento nom, final int index) {
            return String.valueOf(index);
        }

    }

    private static final long serialVersionUID = 1L;

    public DropDownChoicesForValueMementos(final String id, final IModel<ObjectAdapterMemento> model, final IModel<? extends List<? extends ObjectAdapterMemento>> choices) {
        this(id, model, choices, new ValueMementoRenderer());
    }

    private DropDownChoicesForValueMementos(final String id, final IModel<ObjectAdapterMemento> model, final IModel<? extends List<? extends ObjectAdapterMemento>> choices, final IChoiceRenderer<? super ObjectAdapterMemento> renderer) {
        super(id, model, choices, renderer);
    }

    @Override
    protected boolean wantOnSelectionChangedNotifications() {
        return false;// true;
    }
}