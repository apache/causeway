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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.IConverter;

import org.apache.isis.applib.services.i18n.LocaleProvider;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.objectvalue.renderedadjusted.RenderedAdjustedFacet;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.datepicker.TextFieldWithDateTimePicker;

/**
 * Panel for rendering scalars representing dates, along with a date picker.
 */
public abstract class ScalarPanelTextFieldDatePickerAbstract<T extends Serializable> extends ScalarPanelTextFieldAbstract<T>  {

    private static final long serialVersionUID = 1L;

    protected DateConverter<T> converter;

    public ScalarPanelTextFieldDatePickerAbstract(final String id, final ScalarModel scalarModel, final Class<T> cls) {
        super(id, scalarModel, cls);
    }

    /**
     * Expected to be in subclasses' constructor.
     *
     * <p>
     * Is not passed into constructor only to allow subclass to read from injected {@link #getSettings()}.
     */
    protected void init(DateConverter<T> converter) {
        this.converter = converter;
    }

    protected int getAdjustBy() {
        final RenderedAdjustedFacet facet = getModel().getFacet(RenderedAdjustedFacet.class);
        return facet != null? facet.value(): 0;
    }

    @Override
    protected TextField<T> createTextField(final String id) {
        return new TextFieldWithDateTimePicker<>(id, newTextFieldValueModel(), cls, converter);
    }


    @Override
    protected String createTextFieldFragmentId() {
        return "date";
    }

    @Override
    protected Component createComponentForCompact() {
        Fragment compactFragment = getCompactFragment(CompactType.SPAN);
        final Label label = new Label(ID_SCALAR_IF_COMPACT, newTextFieldValueModel()) {
            @Override
            public <C> IConverter<C> getConverter(Class<C> type) {
                return (IConverter<C>) converter;
            }
        };
        label.setEnabled(false);

        // adding an amount because seemed to truncate in tables in certain circumstances
        final int lengthAdjust =
                getLengthAdjustHint() != null ? getLengthAdjustHint() : 1;
                final String dateTimePattern = converter.getDateTimePattern(getLocale());
                final int length = dateTimePattern.length() + lengthAdjust;
                label.add(new AttributeModifier("size", Model.of("" + length)));

                compactFragment.add(label);

                return label;
    }

    @Override
    protected IModel<String> obtainInlinePromptModel() {
        return new Model<String>() {
            @Override public String getObject() {
                ObjectAdapter object = scalarModel.getObject();
                final T value = object != null ? (T) object.getPojo() : null;
                final String str =
                        value != null
                        ? converter.convertToString(value, getLocaleProvider().getLocale())
                                : null;
                        return str;
            }
        };
    }

    /**
     * Optional override for subclasses to explicitly indicate desired amount to adjust compact form of textField
     */
    protected Integer getLengthAdjustHint() {
        return null;
    }



    @com.google.inject.Inject
    WicketViewerSettings settings;
    @Override
    protected WicketViewerSettings getSettings() {
        return settings;
    }

    private LocaleProvider getLocaleProvider() {
        return IsisContext.getSessionFactory().getServicesInjector().lookupService(LocaleProvider.class).orElse(null);
    }


}
