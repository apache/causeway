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
package org.apache.isis.viewer.wicket.ui.components.scalars.markup;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.string.MultiLineStringPanel;
import org.apache.isis.viewer.wicket.ui.components.widgets.bootstrap.FormGroup;
import org.apache.isis.viewer.wicket.ui.util.Tooltips;

/**
 * Panel for rendering scalars of type {@link org.apache.isis.applib.value.Markup}.
 */
public class ParentedMarkupPanel extends MultiLineStringPanel {

    private static final long serialVersionUID = 1L;
    private final transient MarkupComponentFactory markupComponentFactory;

    public ParentedMarkupPanel(
            final String id,
            final ScalarModel scalarModel,
            final MarkupComponentFactory markupComponentFactory) {

        super(id, scalarModel);
        this.markupComponentFactory = markupComponentFactory;
    }

    @Override
    protected MarkupContainer createScalarIfRegularFormGroup() {

        if(getModel().isEditMode()) {
            // fallback to text editor
            return super.createScalarIfRegularFormGroup();
        }

        final MarkupComponent markupComponent =
                createMarkupComponent("scalarValueContainer");

        getTextField().setLabel(Model.of(getModel().getFriendlyName()));

        final FormGroup formGroup = new FormGroup(ID_SCALAR_IF_REGULAR, getTextField());
        formGroup.add(markupComponent);

        final String labelCaption = getRendering().getLabelCaption(getTextField());
        final Label scalarName = createScalarName(ID_SCALAR_NAME, labelCaption);

        getModel()
        .getDescribedAs()
        .ifPresent(describedAs->Tooltips.addTooltip(scalarName, describedAs));

        formGroup.add(scalarName);

        return formGroup;
    }

    @Override
    protected Component createComponentForCompact() {
        return createMarkupComponent(ID_SCALAR_IF_COMPACT);
    }

    protected MarkupComponent createMarkupComponent(final String id) {
        return markupComponentFactory.newMarkupComponent(id, getModel());
    }

}
