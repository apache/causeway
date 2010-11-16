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

import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.stringable.OidStringifier;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.runtime.persistence.PersistenceSession;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;

public class DropDownChoicesForObjectAdapterMementos extends
        DropDownChoice<ObjectAdapterMemento> {
    
    private final static class ObjectAdapterMementoRenderer implements
            IChoiceRenderer<ObjectAdapterMemento> {
        
        private static final long serialVersionUID = 1L;

        @Override
        public Object getDisplayValue(ObjectAdapterMemento nom) {
            return nom.getObjectAdapter().titleString();
        }

        @Override
        public String getIdValue(ObjectAdapterMemento nom,
                int index) {
            final Oid oid = nom.getObjectAdapter().getOid();
            return getOidStringifier().enString(oid);
        }

        protected OidStringifier getOidStringifier() {
            return getPersistenceSession().getOidGenerator().getOidStringifier();
        }
        protected PersistenceSession getPersistenceSession() {
            return IsisContext.getPersistenceSession();
        }
    }

    private static final long serialVersionUID = 1L;

    public DropDownChoicesForObjectAdapterMementos(String id,
            IModel<ObjectAdapterMemento> model,
            IModel<? extends List<? extends ObjectAdapterMemento>> choices) {
        this(id, model, choices, new ObjectAdapterMementoRenderer());
    }

    private DropDownChoicesForObjectAdapterMementos(String id,
            IModel<ObjectAdapterMemento> model,
            IModel<? extends List<? extends ObjectAdapterMemento>> choices,
            IChoiceRenderer<? super ObjectAdapterMemento> renderer) {
        super(id, model, choices, renderer);
    }

    @Override
    protected boolean wantOnSelectionChangedNotifications() {
        return true;
    }
}