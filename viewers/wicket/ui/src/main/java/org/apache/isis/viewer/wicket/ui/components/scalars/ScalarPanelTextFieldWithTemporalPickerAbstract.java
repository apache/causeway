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

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;

import org.apache.isis.core.metamodel.facets.objectvalue.renderedadjusted.RenderedAdjustedFacet;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.datepicker.TextFieldWithDateTimePicker;

/**
 * Panel for rendering scalars representing dates, along with a date picker.
 */
public abstract class ScalarPanelTextFieldWithTemporalPickerAbstract<T extends Serializable>
extends ScalarPanelTextFieldWithValueSemanticsAbstract<T>  {

    private static final long serialVersionUID = 1L;

    public ScalarPanelTextFieldWithTemporalPickerAbstract(
            final String id, final ScalarModel scalarModel, final Class<T> cls) {
        super(id, scalarModel, cls);
    }

    protected int getAdjustBy() {
        final RenderedAdjustedFacet facet = getModel().getFacet(RenderedAdjustedFacet.class);
        return facet != null? facet.value(): 0;
    }

    @Override
    protected final TextField<T> createTextField(final String id) {
        return new TextFieldWithDateTimePicker<T>(
                super.getCommonContext(), id, newTextFieldValueModel(), cls, getConverter(scalarModel));
    }


    @Override
    protected String createTextFieldFragmentId() {
        return "date";
    }

    @Override
    protected IModel<String> obtainInlinePromptModel() {
        return super.toStringConvertingModelOf(getConverter(scalarModel));
    }

}
