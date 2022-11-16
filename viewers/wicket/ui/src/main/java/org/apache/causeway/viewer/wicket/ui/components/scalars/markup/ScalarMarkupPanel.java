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
package org.apache.causeway.viewer.wicket.ui.components.scalars.markup;

import java.util.EnumSet;

import org.apache.wicket.Component;

import org.apache.causeway.viewer.wicket.model.models.ScalarModel;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarPanelTextFieldWithValueSemantics;

/**
 * Panel for rendering scalars of type {@link org.apache.causeway.applib.value.Markup}.
 */
public class ScalarMarkupPanel<T>
extends ScalarPanelTextFieldWithValueSemantics<T> {

    private static final long serialVersionUID = 1L;
    private final MarkupComponentFactory<ScalarModel> markupComponentFactory; // serializable!

    public ScalarMarkupPanel(
            final String id,
            final ScalarModel scalarModel,
            final Class<T> valueType,
            final MarkupComponentFactory<ScalarModel> markupComponentFactory) {
        super(id, scalarModel, valueType);
        this.markupComponentFactory = markupComponentFactory;
    }

    @Override
    protected void setupFormatModifiers(final EnumSet<FormatModifier> modifiers) {
        modifiers.add(FormatModifier.MARKUP);
        modifiers.add(FormatModifier.MULTILINE);
    }

    @Override
    protected Component createComponentForOutput(final String id) {
        return createMarkupComponent(id);
    }

    protected final MarkupComponent createMarkupComponent(final String id) {
        return markupComponentFactory.newMarkupComponent(id, scalarModel());
    }

}
