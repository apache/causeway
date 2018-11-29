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

package org.apache.isis.core.metamodel.facets.value.datetimejodalocal;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.EncodingException;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.config.ConfigurationConstants;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueSemanticsProviderAndFacetAbstract;
import org.apache.isis.core.metamodel.services.ServicesInjector;


public class JodaLocalDateTimeValueSemanticsProvider extends ValueSemanticsProviderAndFacetAbstract<LocalDateTime> implements JodaLocalDateTimeValueFacet {

    public static final int MAX_LENGTH = 36;
    public static final int TYPICAL_LENGTH = 22;

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
     * Key to indicate how LocalDateTime should be parsed/rendered.
     *
     * <p>
     * eg:
     * <pre>
     * isis.value.format.datetime=iso
     * </pre>
     *
     * <p>
     * A pre-determined list of values is available, specifically 'iso_encoding', 'iso' and 'medium' (see
     * {@link #NAMED_TITLE_FORMATTERS}).  Alternatively,  can also specify a mask, eg <tt>dd-MMM-yyyy</tt>.
     *
     * @see #NAMED_TITLE_FORMATTERS
     */
    public final static String CFG_FORMAT_KEY = ConfigurationConstants.ROOT + "value.format.datetime";


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

    private final static ThreadLocal<String> OVERRIDE_TITLE_PATTERN = new ThreadLocal<String>() {
        @Override
        protected String initialValue() {
            return null;
        }
    };


    private final static List<DateTimeFormatter> PARSE_FORMATTERS = _Lists.newArrayList();
    static {
        PARSE_FORMATTERS.add(DateTimeFormat.forStyle("LL"));
        PARSE_FORMATTERS.add(DateTimeFormat.forStyle("MM"));
        PARSE_FORMATTERS.add(DateTimeFormat.forStyle("SS"));
        PARSE_FORMATTERS.add(ISODateTimeFormat.basicDateTimeNoMillis());
        PARSE_FORMATTERS.add(ISODateTimeFormat.basicDateTime());
    }



    public static Class<? extends Facet> type() {
        return JodaLocalDateTimeValueFacet.class;
    }


    // no default
    private static final LocalDateTime DEFAULT_VALUE = null;


    private final DateTimeFormatter encodingFormatter = ISODateTimeFormat.dateHourMinuteSecondMillis();

    private DateTimeFormatter titleStringFormatter;
    private String titleStringFormatNameOrPattern;


    // //////////////////////////////////////
    // constructor
    // //////////////////////////////////////

    /**
     * Required because implementation of {@link Parser} and
     * {@link EncoderDecoder}.
     */
    public JodaLocalDateTimeValueSemanticsProvider() {
        this(null, null);
    }

    /**
     * Uses {@link #type()} as the facet type.
     */
    public JodaLocalDateTimeValueSemanticsProvider(final FacetHolder holder, final ServicesInjector context) {
        super(type(), holder, LocalDateTime.class, TYPICAL_LENGTH, MAX_LENGTH, Immutability.IMMUTABLE, EqualByContent.HONOURED, DEFAULT_VALUE, context);

        String configuredNameOrPattern = getConfiguration().getString(CFG_FORMAT_KEY, "medium").toLowerCase().trim();
        updateTitleStringFormatter(configuredNameOrPattern);
    }


    private void updateTitleStringFormatter(String titleStringFormatNameOrPattern) {
        titleStringFormatter = NAMED_TITLE_FORMATTERS.get(titleStringFormatNameOrPattern);
        if (titleStringFormatter == null) {
            titleStringFormatter = DateTimeFormat.forPattern(titleStringFormatNameOrPattern);
        }
        this.titleStringFormatNameOrPattern = titleStringFormatNameOrPattern;
    }


    // //////////////////////////////////////////////////////////////////
    // Parsing
    // //////////////////////////////////////////////////////////////////

    @Override
    protected LocalDateTime doParse(
            final String entry,
            final Object context) {

        updateTitleStringFormatterIfOverridden();

        LocalDateTime contextDateTime = (LocalDateTime) context;

        final String dateString = entry.trim().toUpperCase();
        if (dateString.startsWith("+") && contextDateTime != null) {
            return JodaLocalDateTimeUtil.relativeDateTime(contextDateTime, dateString, true);
        } else if (dateString.startsWith("-")  && contextDateTime != null) {
            return JodaLocalDateTimeUtil.relativeDateTime(contextDateTime, dateString, false);
        } else {
            return parseDateTime(dateString, contextDateTime);
        }
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

    private LocalDateTime parseDateTime(final String dateStr, final Object original) {
        return JodaLocalDateTimeUtil.parseDate(dateStr, PARSE_FORMATTERS);
    }


    // ///////////////////////////////////////////////////////////////////////////
    // TitleProvider
    // ///////////////////////////////////////////////////////////////////////////

    @Override
    public String titleString(final Object value) {
        if (value == null) {
            return null;
        }
        final LocalDateTime dateTime = (LocalDateTime) value;
        final DateTimeFormatter f = titleStringFormatter.withLocale(Locale.getDefault());
        return JodaLocalDateTimeUtil.titleString(f, dateTime);
    }

    @Override
    public String titleStringWithMask(final Object value, final String usingMask) {
        final LocalDateTime dateTime = (LocalDateTime) value;
        return JodaLocalDateTimeUtil.titleString(DateTimeFormat.forPattern(usingMask), dateTime);
    }


    // //////////////////////////////////////////////////////////////////
    // EncoderDecoder
    // //////////////////////////////////////////////////////////////////

    @Override
    protected String doEncode(final Object object) {
        final LocalDateTime date = (LocalDateTime) object;
        return encode(date);
    }

    private synchronized String encode(final LocalDateTime date) {
        return encodingFormatter.print(date);
    }

    @Override
    protected LocalDateTime doRestore(final String data) {
        try {
            return parse(data);
        } catch (final IllegalArgumentException e) {
            throw new EncodingException(e);
        }
    }

    private synchronized LocalDateTime parse(final String data) {
        return encodingFormatter.parseLocalDateTime(data);
    }

    // //////////////////////////////////////////////////////////////////
    // JodaLocalDateValueFacet
    // //////////////////////////////////////////////////////////////////

    @Override
    public final LocalDateTime dateValue(final ObjectAdapter object) {
        return (LocalDateTime) (object == null ? null : object.getPojo());
    }

    @Override
    public final ObjectAdapter createValue(final LocalDateTime date) {
        return getObjectAdapterProvider().adapterFor(date);
    }


    // //////////////////////////////////////

    @Override
    public String toString() {
        return "JodaLocalDateTimeValueSemanticsProvider: " + titleStringFormatter;
    }

}
