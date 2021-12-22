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
package org.apache.isis.viewer.wicket.ui.components.widgets.checkbox;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.GenericToggleboxColumn.BulkToggle;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

/**
 * A button contained within its own form.
 */
public class ContainedToggleboxPanel
extends PanelAbstract<Boolean, Model<Boolean>> {

    private static final long serialVersionUID = 1L;

    private static final String ID_CONTAINER = "container";
    private static final String ID_FORM = "form";
    private static final String ID_TOGGLEBOX = "togglebox";

    private final AjaxCheckBox checkbox;

    public ContainedToggleboxPanel(final String id) {
        this(id, Model.of(false));
    }

    public ContainedToggleboxPanel(final String id, final IModel<Boolean> model) {
        super(id);

        final WebMarkupContainer markupContainer = new WebMarkupContainer(ID_CONTAINER);
        add(markupContainer);

        final Form<Object> form = new Form<Object>(ID_FORM);
        markupContainer.add(form);

        checkbox = new AjaxCheckBox(ID_TOGGLEBOX, model) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(final AjaxRequestTarget target) {
                ContainedToggleboxPanel.this.onUpdate(target);
            }

        };
        form.add(checkbox);

    }

    /**
     * Hook method for (typically anonymous) subclasses to override.
     */
    protected void onUpdate(final AjaxRequestTarget target) {
    }

    protected boolean isChecked() {
        return checkbox.getModelObject();
    }

    protected void setModel(final boolean isChecked) {
        checkbox.setModelObject(isChecked);
    }

    public void set(
            final BulkToggle bulkToggle,
            final AjaxRequestTarget target) {
        setModel(bulkToggle.isSetAll());
        target.add(this);
    }

}
