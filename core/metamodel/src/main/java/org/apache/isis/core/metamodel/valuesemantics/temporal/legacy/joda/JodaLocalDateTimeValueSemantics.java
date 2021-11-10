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

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.EncodingException;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.adapters.Renderer;
import org.apache.isis.applib.adapters.ValueSemanticsAbstract;
import org.apache.isis.applib.adapters.ValueSemanticsProvider;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.schema.common.v2.ValueType;

@Component
@Named("isis.val.JodaLocalDateTimeValueSemantics")
public class JodaLocalDateTimeValueSemantics
extends ValueSemanticsAbstract<LocalDateTime>
implements
    EncoderDecoder<LocalDateTime>,
    Parser<LocalDateTime>,
    Renderer<LocalDateTime> {

    public static final int MAX_LENGTH = 36;
    public static final int TYPICAL_LENGTH = 22;

    @Override
    public Class<LocalDateTime> getCorrespondingClass() {
        return LocalDateTime.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.JODA_LOCAL_DATE_TIME;
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
        NAMED_TITLE_FORMATTERS.put("iso_encoding", ISODateTimeFormat.basicDateTime());
        NAMED_TITLE_FORMATTERS.put("iso", ISODateTimeFormat.basicDateTimeNoMillis());
        NAMED_TITLE_FORMATTERS.put("long", DateTimeFormat.forStyle("LL"));
        NAMED_TITLE_FORMATTERS.put("medium", DateTimeFormat.forStyle("MM"));
        NAMED_TITLE_FORMATTERS.put("short", DateTimeFormat.forStyle("SS"));
    }

    /**
     * @deprecated possible memory leak issue, because this one is never cleared up
     */
    @Deprecated
    private static final ThreadLocal<String> OVERRIDE_TITLE_PATTERN = ThreadLocal.withInitial(()->null);

    private static final List<DateTimeFormatter> PARSE_FORMATTERS = _Lists.newArrayList();
    static {
        PARSE_FORMATTERS.add(DateTimeFormat.forStyle("LL"));
        PARSE_FORMATTERS.add(DateTimeFormat.forStyle("MM"));
        PARSE_FORMATTERS.add(DateTimeFormat.forStyle("SS"));
        PARSE_FORMATTERS.add(ISODateTimeFormat.basicDateTimeNoMillis());
        PARSE_FORMATTERS.add(ISODateTimeFormat.basicDateTime());
    }

    private final DateTimeFormatter encodingFormatter = ISODateTimeFormat.dateHourMinuteSecondMillis();

    private DateTimeFormatter titleStringFormatter;
    private String titleStringFormatNameOrPattern;

    // //////////////////////////////////////
    // constructor
    // //////////////////////////////////////

    public JodaLocalDateTimeValueSemantics(final IsisConfiguration config) {
        String configuredNameOrPattern = config
                .getValueTypes().getJoda().getLocalDateTime().getFormat();
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
    public String parseableTextRepresentation(final Context context, final LocalDateTime value) {
        return toEncodedString(value);
    }

    @Override
    public LocalDateTime parseTextRepresentation(
            final ValueSemanticsProvider.Context context,
            final String entry) {

        updateTitleStringFormatterIfOverridden();

        //LocalDateTime contextDateTime = (LocalDateTime) context;

        final String dateString = entry.trim().toUpperCase();
//        if (dateString.startsWith("+") && contextDateTime != null) {
//            return JodaLocalDateTimeUtil.relativeDateTime(contextDateTime, dateString, true);
//        } else if (dateString.startsWith("-")  && contextDateTime != null) {
//            return JodaLocalDateTimeUtil.relativeDateTime(contextDateTime, dateString, false);
//        } else {
            return parseDateTime(dateString);
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

    private LocalDateTime parseDateTime(final String dateStr) {
        return _JodaLocalDateTimeUtil.parseDate(dateStr, PARSE_FORMATTERS);
    }


    // ///////////////////////////////////////////////////////////////////////////
    // TitleProvider
    // ///////////////////////////////////////////////////////////////////////////

    @Override
    public String simpleTextPresentation(final Context context, final LocalDateTime value) {
        if (value == null) {
            return null;
        }
        final LocalDateTime dateTime = value;
        final DateTimeFormatter f = titleStringFormatter.withLocale(Locale.getDefault());
        return _JodaLocalDateTimeUtil.titleString(f, dateTime);
    }

    // //////////////////////////////////////////////////////////////////
    // EncoderDecoder
    // //////////////////////////////////////////////////////////////////

    @Override
    public String toEncodedString(final LocalDateTime localDateTime) {
        return encode(localDateTime);
    }

    private synchronized String encode(final LocalDateTime date) {
        return encodingFormatter.print(date);
    }

    @Override
    public LocalDateTime fromEncodedString(final String data) {
        try {
            return parse(data);
        } catch (final IllegalArgumentException e) {
            throw new EncodingException(e);
        }
    }

    private synchronized LocalDateTime parse(final String data) {
        return encodingFormatter.parseLocalDateTime(data);
    }


}
