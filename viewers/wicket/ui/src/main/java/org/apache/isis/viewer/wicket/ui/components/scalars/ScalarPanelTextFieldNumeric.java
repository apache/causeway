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
package org.apache.isis.viewer.wicket.ui.components.scalars;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;

/**
 * Panel for rendering numeric scalars.
 */
public abstract class ScalarPanelTextFieldNumeric<T extends Serializable>
extends ScalarPanelTextFieldAbstract<T> {

    private static final long serialVersionUID = 1L;

    /**
     * The converter that is going to be used for the regular view of the panel, i.e. for the text field.
     * The same converter should be used to render the compact view as well, to show the same precision and scale
     * for the floating point types
     */
    private final IConverter<T> converter;

    public ScalarPanelTextFieldNumeric(final String id, final ScalarModel scalarModel, final Class<T> cls, final IConverter<T> converter) {
        super(id, scalarModel, cls);

        this.converter = converter;
    }

    @Override
    protected Component createComponentForCompact() {
        Fragment compactFragment = getCompactFragment(CompactType.SPAN);
        final Label label = new Label(ID_SCALAR_IF_COMPACT, newTextFieldValueModel()) {

            private static final long serialVersionUID = 1L;

            @Override
            public <C> IConverter<C> getConverter(final Class<C> type) {
                return _Casts.uncheckedCast(converter);
            }
        };

        label.setEnabled(false);

        compactFragment.add(label);
        return label;
    }

    @Override
    protected IModel<String> obtainInlinePromptModel() {
        return super.toStringConvertingModelOf(converter);
    }

}
