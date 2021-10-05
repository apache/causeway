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
package org.apache.isis.core.metamodel.valuesemantics.temporal.legacy.joda;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Named;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.EncodingException;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.adapters.Renderer;
import org.apache.isis.applib.adapters.ValueSemanticsAbstact;
import org.apache.isis.applib.adapters.ValueSemanticsProvider;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.schema.common.v2.ValueType;

@Component
@Named("isis.val.JodaLocalDateValueSemantics")
public class JodaLocalDateValueSemantics
extends ValueSemanticsAbstact<LocalDate>
implements
    EncoderDecoder<LocalDate>,
    Parser<LocalDate>,
    Renderer<LocalDate> {

    public static final int MAX_LENGTH = 12;
    public static final int TYPICAL_LENGTH = MAX_LENGTH;

    @Override
    public Class<LocalDate> getCorrespondingClass() {
        return LocalDate.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.JODA_LOCAL_DATE;
    }

    /**
     * Introduced to allow BDD tests to provide a different format string "mid-flight".
     *
     * <p>
     * REVIEW: This seems only to have any effect if 'propertyType' is set to 'date'.
     *
     * @see #setTitlePatternOverride(String)
     * @deprecated - because 'propertyType' parameter is never used
     */
    @Deprecated
    public static void setFormat(final String propertyType, final String pattern) {
        setTitlePatternOverride(pattern);
    }
    /**
     * A replacement for {@link #setFormat(String, String)}.
     */
    public static void setTitlePatternOverride(final String pattern) {
        OVERRIDE_TITLE_PATTERN.set(pattern);
    }

    /**
     * Keys represent the values which can be configured, and which are used for the rendering of dates.
     *
     */
    private static Map<String, DateTimeFormatter> NAMED_TITLE_FORMATTERS = _Maps.newHashMap();
    static {
        NAMED_TITLE_FORMATTERS.put("iso_encoding", DateTimeFormat.forPattern("yyyyMMdd"));
        NAMED_TITLE_FORMATTERS.put("iso", DateTimeFormat.forPattern("yyyy-MM-dd"));
        NAMED_TITLE_FORMATTERS.put("long", DateTimeFormat.forStyle("L-"));
        NAMED_TITLE_FORMATTERS.put("medium", DateTimeFormat.forStyle("M-"));
        NAMED_TITLE_FORMATTERS.put("short", DateTimeFormat.forStyle("S-"));
    }

    /**
     * @deprecated possible memory leak issue, because this one is never cleared up
     */
    @Deprecated
    private static final ThreadLocal<String> OVERRIDE_TITLE_PATTERN = ThreadLocal.withInitial(()->null);


    private static final List<DateTimeFormatter> PARSE_FORMATTERS = _Lists.newArrayList();
    static {
        PARSE_FORMATTERS.add(DateTimeFormat.forStyle("L-"));
        PARSE_FORMATTERS.add(DateTimeFormat.forStyle("M-"));
        PARSE_FORMATTERS.add(DateTimeFormat.forStyle("S-"));
        PARSE_FORMATTERS.add(DateTimeFormat.forPattern("yyyy-MM-dd"));
        PARSE_FORMATTERS.add(DateTimeFormat.forPattern("yyyyMMdd"));
    }

    private final DateTimeFormatter encodingFormatter = DateTimeFormat.forPattern("yyyyMMdd");

    private DateTimeFormatter titleStringFormatter;
    private String titleStringFormatNameOrPattern;


    // //////////////////////////////////////
    // constructor
    // //////////////////////////////////////

    public JodaLocalDateValueSemantics(final IsisConfiguration config) {
        String configuredNameOrPattern = config
                .getValueTypes().getJoda().getLocalDate().getFormat();

        updateTitleStringFormatter(configuredNameOrPattern);
    }

    @Override
    public int typicalLength() {
        return TYPICAL_LENGTH;
    }

    @Override
    public int maxLength() {
        return MAX_LENGTH;
    }


    private void updateTitleStringFormatter(final String titleStringFormatNameOrPattern) {
        titleStringFormatter = NAMED_TITLE_FORMATTERS.get(titleStringFormatNameOrPattern);
        if (titleStringFormatter == null) {
            titleStringFormatter = DateTimeFormat.forPattern(titleStringFormatNameOrPattern);
        }
        this.titleStringFormatNameOrPattern = titleStringFormatNameOrPattern;
    }

    // -- PARSER

    @Override
    public String parseableTextRepresentation(final Context context, final LocalDate value) {
        return toEncodedString(value);
    }

    @Override
    public LocalDate parseTextRepresentation(
            final ValueSemanticsProvider.Context context,
            final String entry) {

        updateTitleStringFormatterIfOverridden();

        //LocalDate contextDate = (LocalDate) context;

        final String dateString = entry.trim().toUpperCase();
//        if (dateString.startsWith("+") && contextDate != null) {
//            return JodaLocalDateUtil.relativeDate(contextDate, dateString, true);
//        } else if (dateString.startsWith("-")  && contextDate != null) {
//            return JodaLocalDateUtil.relativeDate(contextDate, dateString, false);
//        } else {
            return parseDate(dateString);
//        }
    }

    private void updateTitleStringFormatterIfOverridden() {
        final String overridePattern = OVERRIDE_TITLE_PATTERN.get();
        if (overridePattern == null ||
                titleStringFormatNameOrPattern.equals(overridePattern)) {
            return;
        }

        // (re)create format
        updateTitleStringFormatter(overridePattern);
    }

    private LocalDate parseDate(final String dateStr) {
        return _JodaLocalDateUtil.parseDate(dateStr, PARSE_FORMATTERS);
    }


    // ///////////////////////////////////////////////////////////////////////////
    // TitleProvider
    // ///////////////////////////////////////////////////////////////////////////

    @Override
    public String simpleTextRepresentation(final Context context, final LocalDate value) {
        if (value == null) {
            return null;
        }
        final LocalDate date = value;
        DateTimeFormatter f = titleStringFormatter.withLocale(Locale.getDefault());
        return _JodaLocalDateUtil.titleString(f, date);
    }

    // //////////////////////////////////////////////////////////////////
    // EncoderDecoder
    // //////////////////////////////////////////////////////////////////

    @Override
    public String toEncodedString(final LocalDate date) {
        return encode(date);
    }

    private synchronized String encode(final LocalDate date) {
        return encodingFormatter.print(date);
    }

    @Override
    public LocalDate fromEncodedString(final String data) {
        try {
            return parse(data);
        } catch (final IllegalArgumentException e) {
            throw new EncodingException(e);
        }
    }

    private synchronized LocalDate parse(final String data) {
        return encodingFormatter.parseLocalDate(data);
    }

}
