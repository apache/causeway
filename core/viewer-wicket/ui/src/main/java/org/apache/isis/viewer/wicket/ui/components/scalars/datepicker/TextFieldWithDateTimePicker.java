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
package org.apache.isis.viewer.wicket.ui.components.scalars.datepicker;

import java.util.Locale;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.util.convert.IConverter;

import org.apache.isis.config.IsisConfigurationLegacy;
import org.apache.isis.runtime.system.context.IsisContext;
import org.apache.isis.runtime.system.session.IsisSessionFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.DateConverter;

import static de.agilecoders.wicket.jquery.JQuery.$;

import de.agilecoders.wicket.core.util.Attributes;

/**
 * A text input field that is used as a date or date/time picker.
 * It uses <a href="https://github.com/Eonasdan/bootstrap-datetimepicker">Bootstrap Datetime picker</a>
 * JavaScript widget
 *
 * @param <T> The type of the date/time
 */
public class TextFieldWithDateTimePicker<T> extends TextField<T> implements IConverter<T> {

    private static final long serialVersionUID = 1L;

    /**
     * As per http://eonasdan.github.io/bootstrap-datetimepicker/Options/#mindate, in ISO format (per https://github.com/moment/moment/issues/1407).
     */
    private static final String KEY_DATE_PICKER_MIN_DATE = "isis.viewer.wicket.datePicker.minDate";
    private static final String KEY_DATE_PICKER_MIN_DATE_DEFAULT = "1900-01-01T00:00:00.000Z";

    /**
     * As per http://eonasdan.github.io/bootstrap-datetimepicker/Options/#maxdate, in ISO format (per https://github.com/moment/moment/issues/1407).
     */
    private static final String KEY_DATE_PICKER_MAX_DATE = "isis.viewer.wicket.datePicker.maxDate";
    private static final String KEY_DATE_PICKER_MAX_DATE_DEFAULT = "2100-01-01T00:00:00.000Z";

    protected final DateConverter<T> converter;

    private final DateTimeConfig config;

    public TextFieldWithDateTimePicker(String id, IModel<T> model, Class<T> type, DateConverter<T> converter) {
        super(id, model, type);

        DateTimeConfig config = new DateTimeConfig();

        setOutputMarkupId(true);

        this.converter = converter;

        // if this text field is for a LocalDate, then note that pattern obtained will just be a simple date format
        // (with no hour/minute components).
        final String dateTimePattern = converter.getDateTimePattern(getLocale());
        final String pattern = convertToMomentJsFormat(dateTimePattern);
        config.withFormat(pattern);

        boolean patternContainsTimeComponent = pattern.contains("HH");
        if (patternContainsTimeComponent) {
            // no longer do this, since for sidebar actions takes up too much real estate.
            //config.sideBySide(true);
        }

        config.calendarWeeks(true);
        config.useCurrent(false);

        // seems not to do anything...
        //config.allowKeyboardNavigation(true);

        final String datePickerMinDate = getConfiguration().getString(KEY_DATE_PICKER_MIN_DATE, KEY_DATE_PICKER_MIN_DATE_DEFAULT);
        final String datePickerMaxDate = getConfiguration().getString(KEY_DATE_PICKER_MAX_DATE, KEY_DATE_PICKER_MAX_DATE_DEFAULT);

        config.minDate(datePickerMinDate);
        config.maxDate(datePickerMaxDate);

        this.config = config;
    }

    private String convertToMomentJsFormat(String javaDateTimeFormat) {
        String momentJsFormat = javaDateTimeFormat;
        momentJsFormat = momentJsFormat.replace('d', 'D');
        momentJsFormat = momentJsFormat.replace('y', 'Y');
        return momentJsFormat;
    }

    @Override
    public T convertToObject(String value, Locale locale) {
        return converter.convertToObject(value, locale);
    }

    @Override
    public String convertToString(T value, Locale locale) {
        return converter.convertToString(value, locale);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C> IConverter<C> getConverter(Class<C> type) {
        // we use isAssignableFrom rather than a simple == to handle
        // the persistence of JDO/DataNucleus:
        // if persisting a java.sql.Date, the object we are given is actually a
        // org.datanucleus.store.types.simple.SqlDate (a subclass of java.sql.Date)
        if (converter.getConvertableClass().isAssignableFrom(type)) {
            return (IConverter<C>) this;
        }
        return super.getConverter(type);
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);

        checkComponentTag(tag, "input");
        Attributes.set(tag, "type", "text");
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);

        response.render(CssHeaderItem.forReference(new CssResourceReference(TextFieldWithDateTimePicker.class, "css/bootstrap-datetimepicker.css")));

        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(TextFieldWithDateTimePicker.class, "js/moment.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(TextFieldWithDateTimePicker.class, "js/bootstrap-datetimepicker.js")));

        response.render(OnDomReadyHeaderItem.forScript(createScript(config)));
    }

    /**
     * creates the initializer script.
     *
     * @return initializer script
     */
    private CharSequence createScript(final DateTimeConfig config) {
        return $(this).chain("datetimepicker", config).get();
    }

    IsisConfigurationLegacy getConfiguration() {
        return IsisContext.getConfiguration();
    }

    IsisSessionFactory getIsisSessionFactory() {
        return IsisContext.getSessionFactory();
    }

}
