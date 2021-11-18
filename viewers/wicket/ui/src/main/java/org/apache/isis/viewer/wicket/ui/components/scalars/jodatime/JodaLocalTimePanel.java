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
package org.apache.isis.viewer.wicket.ui.components.scalars.jodatime;

import org.apache.wicket.markup.html.form.AbstractTextComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.util.convert.IConverter;
import org.joda.time.LocalTime;

import org.apache.isis.core.metamodel.commons.ScalarRepresentation;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelTextFieldAbstract;
import org.apache.isis.viewer.wicket.ui.components.scalars.TextFieldValueModel;

import lombok.NonNull;

/**
 * Panel for rendering scalars of type {@link LocalTime}.
 */
public class JodaLocalTimePanel
extends ScalarPanelTextFieldAbstract<LocalTime> {

    private static final long serialVersionUID = 1L;

    public JodaLocalTimePanel(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel, LocalTime.class);
    }

    @Override
    protected AbstractTextComponent<LocalTime> createTextField(final String id) {
        final TextFieldValueModel<LocalTime> textFieldValueModel = new TextFieldValueModel<>(this);
        return new TextField<LocalTime>(id, textFieldValueModel, LocalTime.class) {
            private static final long serialVersionUID = 1L;

            @SuppressWarnings("unchecked")
            @Override
            public <C> IConverter<C> getConverter(final Class<C> type) {
                return (IConverter<C>) (type == LocalTime.class ? new ConverterForJodaLocalTime() : super.getConverter(type));
            }
        };
    }

    //FIXME[ISIS-2882] wire up correctly
    @Override
    protected IConverter<LocalTime> getConverter(@NonNull final ObjectFeature propOrParam,
            final @NonNull ScalarRepresentation scalarRepresentation) {
        // TODO Auto-generated method stub
        return null;
    }

}
