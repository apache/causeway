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
import java.util.Optional;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;

import org.apache.causeway.applib.locale.UserLocale;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.viewer.wicket.ui.components.text.TextFieldWithConverter;

import lombok.NonNull;
import de.agilecoders.wicket.core.util.Attributes;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.datetime.DatetimePickerConfig;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.datetime.DatetimePickerIconConfig;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.icon.FontAwesome6IconType;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.references.DatetimePickerCssReference;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.references.DatetimePickerJsReference;
import de.agilecoders.wicket.jquery.Config;

/**
 * A text input field that is used as a date or date/time picker.
 * It uses <a href="https://github.com/Eonasdan/bootstrap-datetimepicker">Bootstrap Datetime picker</a>
 * JavaScript widget.
 * For options (5.39.0) see <a href="https://getdatepicker.com/5-4/Options/">https://getdatepicker.com/5-4/Options/</a>
 *
 * @param <T> The type of the date/time
 */
public class TextFieldWithDateTimePicker<T>
extends TextFieldWithConverter<T> {

    private static final long serialVersionUID = 1L;

    private final DateTimeConfig config;

    public TextFieldWithDateTimePicker(
            final @NonNull String id,
            final @NonNull IModel<T> model,
            final @NonNull Class<T> type,
            final boolean isRequired,
            final @NonNull IConverter<T> converter,
            final @NonNull String editingPattern) {
        super(id, model, type, Optional.of(converter));
        setOutputMarkupId(true);

        this.config = createDatePickerConfig(
                editingPattern,
                !isRequired);

        /* debug
                new IConverter<T>() {

            @Override
            public T convertToObject(final String value, final Locale locale) throws ConversionException {
                System.err.printf("convertToObject %s%n", value);
                try {
                    var obj = converter.convertToObject(value, locale);
                    System.err.printf("convertedToObject %s%n", obj);
                    return obj;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public String convertToString(final T value, final Locale locale) {
                var s =  converter.convertToString(value, locale);
                System.err.printf("convertedToString %s%n", s);
                return s;
            }
        };
        */
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
            final String temporalPattern,
            final boolean isInputNullable) {
        var config = new DateTimeConfig();
        var mmc = MetaModelContext.instanceElseFail();

        config.useLocale(mmc.currentUserLocale()
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

        var causewayDatePickerConfig = mmc.getConfiguration().getViewer().getWicket().getDatePicker();

        //XXX future extensions might allow to set bounds on a per member basis (via ValueSemantics annotation)
        config.minDate(causewayDatePickerConfig.getMinDate());
        config.maxDate(causewayDatePickerConfig.getMaxDate());
        config.allowInputToggle(causewayDatePickerConfig.isPopupOnFocus());
        return config;
    }

    /**
     * Returns the initializer script.
     */
    private CharSequence createScript(final Config config) {

        var script = $(this).chain("datetimepicker", config).get();
        //debug
        //System.err.printf("script: %s%n", script);

        return script;
    }

}
