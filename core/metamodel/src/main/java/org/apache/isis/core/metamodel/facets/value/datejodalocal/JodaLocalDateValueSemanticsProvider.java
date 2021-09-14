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
package org.apache.isis.core.metamodel.facets.value.datejodalocal;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.EncodingException;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueSemanticsProviderAndFacetAbstract;
import org.apache.isis.core.metamodel.spec.ManagedObject;


public class JodaLocalDateValueSemanticsProvider extends ValueSemanticsProviderAndFacetAbstract<LocalDate> implements JodaLocalDateValueFacet {

    public static final int MAX_LENGTH = 12;
    public static final int TYPICAL_LENGTH = MAX_LENGTH;

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



    private static final Class<? extends Facet> type() {
        return JodaLocalDateValueFacet.class;
    }


    // no default
    private static final LocalDate DEFAULT_VALUE = null;


    private final DateTimeFormatter encodingFormatter = DateTimeFormat.forPattern("yyyyMMdd");

    private DateTimeFormatter titleStringFormatter;
    private String titleStringFormatNameOrPattern;


    // //////////////////////////////////////
    // constructor
    // //////////////////////////////////////

    /**
     * Required because implementation of {@link Parser} and
     * {@link EncoderDecoder}.
     */
    public JodaLocalDateValueSemanticsProvider() {
        this(null);
    }

    /**
     * Uses {@link #type()} as the facet type.
     */
    public JodaLocalDateValueSemanticsProvider(final FacetHolder holder) {
        super(type(), holder, LocalDate.class, TYPICAL_LENGTH, MAX_LENGTH, Immutability.IMMUTABLE, EqualByContent.HONOURED, DEFAULT_VALUE);

        String configuredNameOrPattern = getConfiguration().getValueTypes().getJoda().getLocalDate().getFormat();

        updateTitleStringFormatter(configuredNameOrPattern);
    }


    private void updateTitleStringFormatter(final String titleStringFormatNameOrPattern) {
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
    protected LocalDate doParse(
            final Parser.Context context,
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
        return JodaLocalDateUtil.parseDate(dateStr, PARSE_FORMATTERS);
    }


    // ///////////////////////////////////////////////////////////////////////////
    // TitleProvider
    // ///////////////////////////////////////////////////////////////////////////

    @Override
    public String titleString(final Object value) {
        if (value == null) {
            return null;
        }
        final LocalDate date = (LocalDate) value;
        DateTimeFormatter f = titleStringFormatter.withLocale(Locale.getDefault());
        return JodaLocalDateUtil.titleString(f, date);
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

    // //////////////////////////////////////////////////////////////////
    // JodaLocalDateValueFacet
    // //////////////////////////////////////////////////////////////////

    @Override
    public final LocalDate dateValue(final ManagedObject object) {
        return (LocalDate) (object == null ? null : object.getPojo());
    }

    @Override
    public final ManagedObject createValue(final LocalDate date) {
        return getObjectManager().adapt(date);
    }


    // //////////////////////////////////////

    @Override
    public String toString() {
        return "JodaLocalDateValueSemanticsProvider: " + titleStringFormatter;
    }

}
