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
package org.apache.isis.viewer.wicket.ui.components.scalars.composite;

import java.util.EnumSet;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.FormComponentPanel;

import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelFormFieldAbstract;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract.FormatModifier;
import org.apache.isis.viewer.wicket.ui.components.scalars.markup.MarkupComponent;

public class CompositeValuePanel<T>
extends ScalarPanelFormFieldAbstract<T> {

    private static final long serialVersionUID = 1L;

    public CompositeValuePanel(
            final String id,
            final ScalarModel scalarModel,
            final Class<T> valueType) {

        super(id, scalarModel, valueType);
    }

    @Override
    protected void setupFormatModifiers(final EnumSet<FormatModifier> modifiers) {
        modifiers.add(FormatModifier.COMPOSITE);
        modifiers.add(FormatModifier.READONLY);
    }

    @Override
    protected Component createComponentForOutput(final String id) {
        return new MarkupComponent(id, scalarModel());
    }

    @Override
    protected FormComponent<T> createFormComponent(final String id, final ScalarModel scalarModel) {
        return new FormComponentPanel<>(id) {
            private static final long serialVersionUID = 1L;

        };

//        return new AjaxButton(id, labelModel) {
//            private static final long serialVersionUID = 1L;
//            @Override public void onSubmit(final AjaxRequestTarget target) {
//                onClick.accept(this, target);
//            }
//        };
    }


}
