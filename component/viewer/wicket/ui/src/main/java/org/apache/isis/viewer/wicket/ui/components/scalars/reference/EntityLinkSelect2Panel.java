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

package org.apache.isis.viewer.wicket.ui.components.scalars.reference;

import com.vaynberg.wicket.select2.Select2Choice;

import org.apache.wicket.markup.html.form.FormComponentPanel;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.ui.components.actions.ActionInvokeHandler;
import org.apache.isis.viewer.wicket.ui.components.widgets.formcomponent.CancelHintRequired;
import org.apache.isis.viewer.wicket.ui.components.widgets.formcomponent.FormComponentPanelAbstract;

/**
 * {@link FormComponentPanel} representing a reference to an entity: a link and
 * (optionally) an autocomplete field.
 */
class EntityLinkSelect2Panel extends FormComponentPanelAbstract<ObjectAdapter> implements CancelHintRequired, ActionInvokeHandler  {

    private static final long serialVersionUID = 1L;

    /**
     * This component may be null if there are no choices or autoComplete, or if in read-only mode.
     */
    Select2Choice<ObjectAdapterMemento> select2Field;

    ReferencePanel owningPanel;

    public EntityLinkSelect2Panel(final String id, final ReferencePanel owningPanel) {
        super(id, owningPanel.getModel());
        this.owningPanel = owningPanel;
        
        setType(ObjectAdapter.class);
        syncWithInput();
    }

    /**
     * Necessary because {@link FormComponentPanel} overrides this as <tt>true</tt>, whereas we want to
     * report on the state of the underlying {@link Select2Choice}.
     */
    @Override
    public boolean checkRequired() {
        if(select2Field != null) {
            return select2Field.checkRequired();
        }
        return super.checkRequired();
    }

    /**
     * Since we override {@link #convertInput()}, it is (apparently) enough to
     * just return a value that is suitable for error reporting.
     * 
     * @see DateField#getInput() for reference
     */
    @Override
    public String getInput() {
        final ObjectAdapter pendingElseCurrentAdapter = owningPanel.getModel().getPendingElseCurrentAdapter();
        return pendingElseCurrentAdapter != null? pendingElseCurrentAdapter.titleString(null): "(no object)";
    }

    @Override
    protected void convertInput() {
        this.owningPanel.convertInput(this);
    }

    @Override
    protected void onBeforeRender() {
        syncWithInput();
        super.onBeforeRender();
    }

    private void syncWithInput() {
        this.owningPanel.syncWithInput(this);
    }

    @Override
    public void onClick(final ActionModel actionModel) {
    }

    @Override
    public void onCancel() {
        owningPanel.getModel().clearPending();
    }

}
