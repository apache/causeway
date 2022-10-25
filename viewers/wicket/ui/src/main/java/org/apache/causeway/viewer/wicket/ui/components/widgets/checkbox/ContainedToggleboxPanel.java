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
package org.apache.causeway.viewer.wicket.ui.components.widgets.checkbox;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.danekja.java.util.function.serializable.SerializableBiConsumer;

import org.apache.causeway.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.GenericToggleboxColumn.BulkToggle;
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

import lombok.val;

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

    public ContainedToggleboxPanel(
            final String id,
            final IModel<Boolean> model) {
        this(id, model, (x,y)->{/*noop*/});
    }

    public ContainedToggleboxPanel(
            final String id,
            final IModel<Boolean> model,
            final SerializableBiConsumer<Boolean, AjaxRequestTarget> onUpdate) {
        super(id);
        val markupContainer = Wkt.containerAdd(this, ID_CONTAINER);
        val form = Wkt.formAdd(markupContainer, ID_FORM);
        this.checkbox = Wkt.checkboxAdd(
                form, ID_TOGGLEBOX, model, ajaxTarget->onUpdate.accept(model.getObject(), ajaxTarget));
    }

    public void set(
            final BulkToggle bulkToggle,
            final AjaxRequestTarget target) {
        checkbox.setModelObject(bulkToggle.isSetAll());
        target.add(this);
    }

}
