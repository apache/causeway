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

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.datetime.DatetimePickerConfig;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.datetime.DatetimePickerIconConfig;
import de.agilecoders.wicket.jquery.AbstractConfig;
import de.agilecoders.wicket.jquery.IKey;

/**
 * Configuration holder for all {@link TextFieldWithDateTimePicker} configurations.
 * Provides settings for <a href="https://github.com/tempusdominus/bootstrap-4">Tempus Dominus Bootstrap 4 Datetime Picker</a>
 * JavaScript widget
 * @see DatetimePickerConfig
 */
public class DateTimeConfig extends AbstractConfig {
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    private static final IKey<Boolean> Readonly = newKey("readonly", false);

    /**
     *
     */
    private static final IKey<Boolean> IgnoreReadonly = newKey("ignoreReadonly", false);

    /**
     *
     */
    private static final IKey<Boolean> KeepOpen = newKey("keepOpen", false);

    /**
     * The earliest date that may be selected; all earlier dates will be disabled.
     */
    private static final IKey<String> StartDate = newKey("startDate", null);

    /**
     * The latest date that may be selected; all later dates will be disabled.
     */
    private static final IKey<String> EndDate = newKey("endDate", null);

    /**
     * The view that the datepicker should show when it is opened. Accepts values of
     * 0 or 'month' for month view (the default); 1 or 'year' for the 12-month overview,
     * and 2 or 'decade' for the 10-year overview. Useful for date-of-birth datepickers.
     */
    private static final IKey<String> _ViewMode = newKey("viewMode", ViewMode.days.name());

    /**
     * If true, displays a "Today" button at the bottom of the datepicker to select
     * the current date. If true, the "Today" button will only move the current date
     * into view;
     */
    private static final IKey<TodayButton> ShowTodayButton = newKey("todayBtn", TodayButton.FALSE);

    /**
     * Whether or not to allow date navigation by arrow keys.
     */
    private static final IKey<Boolean> KeyboardNavigation = newKey("keyboardNavigation", true);

    /**
     * show the date and time picker side by side
     */
    private static final IKey<Boolean> SideBySide = newKey("sideBySide", false);

    /**
     * @see <a href="http://eonasdan.github.io/bootstrap-datetimepicker/Options/#usecurrent">online reference</a>
     */
    private static final IKey<Boolean> UseCurrent = newKey("useCurrent", true);

    /**
     * @see <a href="http://eonasdan.github.io/bootstrap-datetimepicker/Options/#mindate">online reference</a>
     */
    private static final IKey<String> MinDate = newKey("minDate", null);

    /**
     * @see <a href="http://eonasdan.github.io/bootstrap-datetimepicker/Options/#maxdate">online reference</a>
     */
    private static final IKey<String> MaxDate = newKey("maxDate", null);


//    /**
//     * The two-letter code of the language to use for month and day names.
//     * These will also be used as the input's value (and subsequently sent to the
//     * server in the case of form submissions). Currently ships with English ('en');
//     * German ('de'), Brazilian ('br'), and Spanish ('es') translations, but others
//     * can be added (see I18N below). If an unknown language code is given, English
//     * will be used.
//     */
//    private static final IKey<String> Language = newKey("language", "en");

    private static final IKey<String> Locale = newKey("locale", null);

    /**
     * @param locale The moment.js locale
     * @return current instance
     */
    public DateTimeConfig useLocale(final Locale locale) {
        put(Locale, locale.getLanguage());
        return this;
    }


    /**
     * The date format, combination of d, dd, m, mm, M, MM, yy, yyyy.
     */
    private static final IKey<String> Format = newKey("format", null);

    /**
     * Day of the week start. 0 (Sunday) to 6 (Saturday)
     */
    private static final IKey<Integer> WeekStart = newKey("weekStart", 0);

    /**
     * If true, highlights the current date.
     */
    private static final IKey<Boolean> HighlightToday = newKey("todayHighlight", false);

    /**
     * Whether or not to close the datepicker immediately when a date is selected.
     */
    private static final IKey<Boolean> AutoClose = newKey("autoclose", false);

    /**
     * Whether or not to force parsing of the input value when the picker is closed.
     * That is, when an invalid date is left in the input field by the user, the picker
     * will forcibly parse that value, and set the input's value to the new, valid date,
     * conforming to the given format.
     */
    private static final IKey<Boolean> ForceParse = newKey("forceParse", true);

    /**
     * Whether or not to show week numbers to the left of week rows.
     */
    private static final IKey<Boolean> CalendarWeeks = newKey("calendarWeeks", false);

    private static final IKey<DatetimePickerIconConfig> Icons = newKey("icons", null);

    /**
     * holds all week days in a specific sort order.
     */
    public enum Day {
        Sunday, Monday, Tuesday, Wednesday, Thursday, Friday, Saturday
    }

    /**
     * holds all view options.
     */
    public enum ViewMode {
        months, years, days
    }

    /**
     * Construct.
     */
    public DateTimeConfig() {
        super();
    }

    /**
     * @return the date format as string
     */
    public String getFormat() {
        return getString(Format);
    }

    /**
     * Will cause the date picker to stay open after selecting a date.
     *
     * @param keepOpen whether to keep open
     * @return this instance for chaining
     */
    public DateTimeConfig keepOpen(final boolean keepOpen) {
        put(KeepOpen, keepOpen);
        return this;
    }

    public DateTimeConfig ignoreReadonly(final boolean ignoreReadonly) {
    	put(IgnoreReadonly, ignoreReadonly);
    	return this;
    }

    public DateTimeConfig readonly(final boolean readonly) {
    	put(Readonly, readonly);
    	return this;
    }

    /**
     * The earliest date that may be selected; all earlier dates will be disabled.
     *
     * @param value the earliest start date
     * @return this instance for chaining
     */
    public DateTimeConfig withStartDate(final DateTime value) {
        String format = getFormat();
        String startDate;
        if (Strings.isEmpty(format)) {
            startDate = value.toString();
        } else {
            DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(format);
            startDate = dateTimeFormatter.print(value);
        }
        put(StartDate, startDate);
        return this;
    }

    /**
     * The latest date that may be selected; all later dates will be disabled.
     *
     * @param value the latest end date
     * @return this instance for chaining
     */
    public DateTimeConfig withEndDate(final DateTime value) {
        Args.notNull(value, "value");
        String format = getFormat();
        String endDate;
        if (Strings.isEmpty(format)) {
            endDate = value.toString();
        } else {
            DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(format);
            endDate = dateTimeFormatter.print(value);
        }
        put(EndDate, endDate);
        return this;
    }

    /**
     * The default view to display when the picker is shown.
     * Useful for date-of-birth datepicker.
     *
     * @param value the start view to use
     * @return this instance for chaining
     */
    public DateTimeConfig withView(final ViewMode value) {
        put(_ViewMode, value.name());
        return this;
    }

    /**
     * The date format (java style), combination of d, dd, m, mm, M, MM, yy, yyyy.
     *
     * @param value The date format value (java style)
     * @return this instance for chaining
     */
    public DateTimeConfig withFormat(final String value) {
        put(Format, value);
        return this;
    }

    /**
     * Day of the week start. 0 (Sunday) to 6 (Saturday)
     *
     * @param value the {@link Day} the week starts
     * @return this instance for chaining
     */
    public DateTimeConfig withWeekStart(final Day value) {
        put(WeekStart, value.ordinal());
        return this;
    }

    /**
     * Whether or not to allow date navigation by arrow keys.
     *
     * @param value true, if keyboard navigation is allowed
     * @return this instance for chaining
     */
    public DateTimeConfig allowKeyboardNavigation(final boolean value) {
        put(KeyboardNavigation, value);
        return this;
    }

    public DateTimeConfig sideBySide(final boolean value) {
        put(SideBySide, value);
        return this;
    }

    /**
     * If true, highlights the current date.
     *
     * @param value If true, highlights the current date.
     * @return this instance for chaining
     */
    public DateTimeConfig highlightToday(final boolean value) {
        put(HighlightToday, value);
        return this;
    }

    /**
     * If true, displays a "Today" button at the bottom of the datepicker to select
     * the current date. If true, the "Today" button will only move the current date
     * into view;
     *
     * @param value whether to show today button or not
     * @return this instance for chaining
     */
    public DateTimeConfig showTodayButton(final TodayButton value) {
        put(ShowTodayButton, value);
        return this;
    }

    /**
     * Whether or not to force parsing of the input value when the picker is closed.
     * That is, when an invalid date is left in the input field by the user, the picker
     * will forcibly parse that value, and set the input's value to the new, valid date,
     * conforming to the given format.
     *
     * @param value Whether or not to force parsing of the input value when the picker is closed
     * @return this instance for chaining
     */
    public DateTimeConfig forceParse(final boolean value) {
        put(ForceParse, value);
        return this;
    }

    /**
     * @param value Whether or not to show week numbers to the left of week rows.
     * @return this instance for chaining
     */
    public DateTimeConfig useCalendarWeeks(final boolean value) {
        put(CalendarWeeks, value);
        return this;
    }

    /**
     * @param value Whether on show, will set the picker to the current date/time.
     */
    public DateTimeConfig useCurrent(final boolean value) {
        put(UseCurrent, value);
        return this;
    }

    public DateTimeConfig minDate(final String value) {
        put(MinDate, value);
        return this;
    }

    public DateTimeConfig maxDate(final String value) {
        put(MaxDate, value);
        return this;
    }

    /**
     * Whether or not to close the datepicker immediately when a date is selected.
     *
     * @param value true, if datepicker should close immediately when date is selected.
     * @return this instance for chaining
     */
    public DateTimeConfig autoClose(final boolean value) {
        put(AutoClose, value);
        return this;
    }


    /**
     * Sets buttons.
     *
     * @param buttons buttons to show/hide
     * @return config instance
     */
    public DateTimeConfig withButtons(final Map<String, Boolean> buttons) {
        put(DatetimePickerConfig.newKey("buttons", null), Map.of(
                DatetimePickerConfig.BTN_SHOW_TODAY,
                Boolean.TRUE.equals(buttons.get(DatetimePickerConfig.BTN_SHOW_TODAY))
                , DatetimePickerConfig.BTN_SHOW_CLEAR,
                Boolean.TRUE.equals(buttons.get(DatetimePickerConfig.BTN_SHOW_CLEAR))
                , DatetimePickerConfig.BTN_SHOW_CLOSE,
                Boolean.TRUE.equals(buttons.get(DatetimePickerConfig.BTN_SHOW_CLOSE))));
        return this;
    }

    /**
     * Set icon config.
     *
     * @param iconConfig icon config
     * @return config instance
     */
    public DateTimeConfig withIcons(final DatetimePickerIconConfig iconConfig) {
        put(Icons, iconConfig);
        return this;
    }

    /**
     * See <a href="http://bootstrap-datepicker.readthedocs.org/en/latest/options.html#todaybtn">docs</a>.
     * Today button could be a boolean or string <em>"linked"</em>:
     * <cite>If true or “linked”, displays a “Today” button at the bottom of the datepicker to select the
     * current date. If true, the “Today” button will only move the current date into view; if “linked”,
     * the current date will also be selected.</cite>
     */
    @JsonSerialize(using = TodayButtonSerializer.class)
    public static enum TodayButton {
        TRUE,
        FALSE,
        LINKED
    }

    /**
     * Serializer for TodayButton
     */
    private static class TodayButtonSerializer extends JsonSerializer<TodayButton> {

        @Override
        public void serialize(final TodayButton value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
            switch (value) {
            case TRUE:
                jgen.writeBoolean(true);
                break;
            case FALSE:
                jgen.writeBoolean(false);
                break;
            case LINKED:
                jgen.writeString("linked");
                break;
            }
        }
    }
}
