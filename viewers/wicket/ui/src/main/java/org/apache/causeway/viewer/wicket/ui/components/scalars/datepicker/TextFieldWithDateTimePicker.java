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
package org.apache.causeway.viewer.wicket.ui.components.scalars.datepicker;

import static de.agilecoders.wicket.jquery.JQuery.$;

import java.util.Locale;
import java.util.Map;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.util.convert.IConverter;

import org.apache.causeway.applib.locale.UserLocale;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.viewer.wicket.model.models.ScalarModel;
import org.apache.causeway.viewer.wicket.model.value.ConverterBasedOnValueSemantics;

import de.agilecoders.wicket.core.util.Attributes;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.datetime.DatetimePickerConfig;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.datetime.DatetimePickerIconConfig;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.icon.FontAwesome6IconType;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.references.DatetimePickerCssReference;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.references.DatetimePickerJsReference;
import de.agilecoders.wicket.jquery.Config;
import lombok.val;

/**
 * A text input field that is used as a date or date/time picker.
 * It uses <a href="https://github.com/Eonasdan/bootstrap-datetimepicker">Bootstrap Datetime picker</a>
 * JavaScript widget.
 * For options (5.39.0) see <a href="https://getdatepicker.com/5-4/Options/">https://getdatepicker.com/5-4/Options/</a>
 *
 * @param <T> The type of the date/time
 */
public class TextFieldWithDateTimePicker<T>
extends TextField<T>
implements IConverter<T> {

    private static final long serialVersionUID = 1L;

    protected final IConverter<T> converter;

    private final DateTimeConfig config;

    public TextFieldWithDateTimePicker(
            final String id,
            final ScalarModel scalarModel,
            final Class<T> type,
            final IConverter<T> converter) {
        super(id, scalarModel.unwrapped(type), type);
        setOutputMarkupId(true);

        this.config = createDatePickerConfig(
                scalarModel.getMetaModelContext(),
                ((ConverterBasedOnValueSemantics<T>) converter).getEditingPattern(),
                !scalarModel.isRequired());

        this.converter = converter;

        /* debug
                new IConverter<T>() {

            @Override
            public T convertToObject(final String value, final Locale locale) throws ConversionException {
                System.err.printf("convertToObject %s%n", value);
                try {
                    val obj = converter.convertToObject(value, locale);
                    System.err.printf("convertedToObject %s%n", obj);
                    return obj;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public String convertToString(final T value, final Locale locale) {
                val s =  converter.convertToString(value, locale);
                System.err.printf("convertedToString %s%n", s);
                return s;
            }
        };
        */
    }

    @Override
    public T convertToObject(final String value, final Locale locale) {
        return converter.convertToObject(value, locale);
    }

    @Override
    public String convertToString(final T value, final Locale locale) {
        return converter.convertToString(value, locale);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C> IConverter<C> getConverter(final Class<C> type) {
        // we use isAssignableFrom rather than a simple == to handle
        // the persistence of JDO/DataNucleus:
        // if persisting a java.sql.Date, the object we are given is actually a
        // org.datanucleus.store.types.simple.SqlDate (a subclass of java.sql.Date)
        if (super.getType().isAssignableFrom(type)) {
            return (IConverter<C>) this;
        }
        return super.getConverter(type);
    }

    @Override
    protected void onComponentTag(final ComponentTag tag) {
        super.onComponentTag(tag);

        if(!isEnabled()) {
            return;
        }

        checkComponentTag(tag, "input");
        Attributes.addClass(tag, "datetimepicker-input");
        Attributes.set(tag, "type", "text");
        Attributes.set(tag, "data-toggle", "datetimepicker");
        Attributes.set(tag, "data-target", getMarkupId());
        Attributes.set(tag, "autocomplete", "off");
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);

        if(!isEnabled()) {
            return;
        }

        response.render(DatetimePickerCssReference.asHeaderItem());
        response.render(DatetimePickerJsReference.asHeaderItem());
        response.render(OnDomReadyHeaderItem.forScript(createScript(config)));
    }

    // -- HELPER

    private DateTimeConfig createDatePickerConfig(
            final MetaModelContext commonContext,
            final String temporalPattern,
            final boolean isInputNullable) {
        val config = new DateTimeConfig();

        config.useLocale(commonContext.currentUserLocale()
                .map(UserLocale::getLanguageLocale)
                .orElse(Locale.US));

        config.withFormat(_TimeFormatUtil.convertToMomentJsFormat(temporalPattern));
        config.useCalendarWeeks(true);
        config.useCurrent(false);

        config.withButtons(Map.of(
                DatetimePickerConfig.BTN_SHOW_TODAY, true,
                DatetimePickerConfig.BTN_SHOW_CLEAR, isInputNullable,
                DatetimePickerConfig.BTN_SHOW_CLOSE, true));

        //config.highlightToday(true);

        /*
        time: 'far fa-clock',
        date: 'far fa-calendar',
        up: 'far fa-arrow-up',
        down: 'far fa-arrow-down',
        previous: 'far fa-chevron-left',
        next: 'far fa-chevron-right',
        today: 'far fa-calendar-check-o',
        clear: 'far fa-trash',
        close: 'far fa-times'
         */

        config.withIcons(new DatetimePickerIconConfig()
                .useTimeIcon(FontAwesome6IconType.clock_r)
                .useDateIcon(FontAwesome6IconType.calendar_r)
                .useUpIcon(FontAwesome6IconType.arrow_up_s)
                .useDownIcon(FontAwesome6IconType.arrow_down_s)
                .usePreviousIcon(FontAwesome6IconType.chevron_left_s)
                .useNextIcon(FontAwesome6IconType.chevron_right_s)
                .useTodayIcon(FontAwesome6IconType.calendar_check_r)
                .useClearIcon(FontAwesome6IconType.trash_can_r)
                .useCloseIcon(FontAwesome6IconType.check_s)
                );

        //XXX future extensions might allow to set bounds on a per member basis (via ValueSemantics annotation)
        //config.withMinDate(commonContext.getConfiguration().getViewer().getWicket().getDatePicker().minDateAsJavaUtilDate());
        //config.withMaxDate(commonContext.getConfiguration().getViewer().getWicket().getDatePicker().maxDateAsJavaUtilDate());
        config.minDate(commonContext.getConfiguration().getViewer().getWicket().getDatePicker().getMinDate());
        config.maxDate(commonContext.getConfiguration().getViewer().getWicket().getDatePicker().getMaxDate());
        return config;
    }

    /**
     * Returns the initializer script.
     */
    private CharSequence createScript(final Config config) {

        val script = $(this).chain("datetimepicker", config).get();
        //debug
        //System.err.printf("script: %s%n", script);

        return script;
    }

}
