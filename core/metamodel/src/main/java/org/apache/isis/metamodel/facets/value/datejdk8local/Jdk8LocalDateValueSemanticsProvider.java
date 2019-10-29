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

package org.apache.isis.metamodel.facets.value.datejdk8local;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.EncodingException;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.config.IsisConfiguration.Value.FormatIdentifier;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.object.value.vsp.ValueSemanticsProviderAndFacetAbstract;
import org.apache.isis.metamodel.facets.value.datejodalocal.JodaLocalDateValueFacet;
import org.apache.isis.metamodel.spec.ManagedObject;

import static org.apache.isis.metamodel.facets.value.datejdk8local.Jdk8LocalDateUtil.formatterOf;
import static org.apache.isis.metamodel.facets.value.datejdk8local.Jdk8LocalDateUtil.formatterOfStyle;
import static org.apache.isis.metamodel.facets.value.datejdk8local.Jdk8LocalDateUtil.parserOf;
import static org.apache.isis.metamodel.facets.value.datejdk8local.Jdk8LocalDateUtil.parserOfStyle;

public class Jdk8LocalDateValueSemanticsProvider
extends ValueSemanticsProviderAndFacetAbstract<LocalDate>
implements Jdk8LocalDateValueFacet {

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
        override_title_pattern.set(pattern);
    }

    /**
     * Keys represent the values which can be configured, and which are used for the rendering of dates.
     *
     */
    private static Map<String, TimeFormatter> named_title_formatters = _Maps.newHashMap();
    static {
        named_title_formatters.put("iso_encoding", formatterOf(DateTimeFormatter.ofPattern("yyyyMMdd")));
        named_title_formatters.put("iso", formatterOf(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        named_title_formatters.put("long", formatterOfStyle("L-"));
        named_title_formatters.put("medium", formatterOfStyle("M-"));
        named_title_formatters.put("short", formatterOfStyle("S-"));
    }

    private final static ThreadLocal<String> override_title_pattern = new ThreadLocal<String>() {
        @Override
        protected String initialValue() {
            return null;
        }
    };


    private final static List<TimeParser> parse_formatters = _Lists.newArrayList();
    static {
        parse_formatters.add(parserOfStyle("L-"));
        parse_formatters.add(parserOfStyle("M-"));
        parse_formatters.add(parserOfStyle("S-"));
        parse_formatters.add(parserOf(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        parse_formatters.add(parserOf(DateTimeFormatter.ofPattern("yyyyMMdd")));
    }

    public static Class<? extends Facet> type() {
        return JodaLocalDateValueFacet.class;
    }


    // no default
    private static final LocalDate DEFAULT_VALUE = null;


    private final DateTimeFormatter encodingFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    private TimeFormatter titleStringFormatter;
    private String titleStringFormatNameOrPattern;


    // //////////////////////////////////////
    // constructor
    // //////////////////////////////////////

    /**
     * Required because implementation of {@link TimeParser} and
     * {@link EncoderDecoder}.
     */
    public Jdk8LocalDateValueSemanticsProvider() {
        this(null);
    }

    /**
     * Uses {@link #type()} as the facet type.
     */
    public Jdk8LocalDateValueSemanticsProvider(final FacetHolder holder) {
        super(type(), holder, LocalDate.class, TYPICAL_LENGTH, MAX_LENGTH, Immutability.IMMUTABLE, EqualByContent.HONOURED, DEFAULT_VALUE);

        String configuredNameOrPattern = getConfiguration()
                .getValue().getFormatOrElse(FormatIdentifier.DATE, "medium");
        updateTitleStringFormatter(configuredNameOrPattern);
    }


    private void updateTitleStringFormatter(String titleStringFormatNameOrPattern) {
        titleStringFormatter = named_title_formatters.get(titleStringFormatNameOrPattern);
        if (titleStringFormatter == null) {
            titleStringFormatter = formatterOf(DateTimeFormatter.ofPattern(titleStringFormatNameOrPattern));
        }
        this.titleStringFormatNameOrPattern = titleStringFormatNameOrPattern;
    }


    // //////////////////////////////////////////////////////////////////
    // Parsing
    // //////////////////////////////////////////////////////////////////

    @Override
    protected LocalDate doParse(
            final String entry,
            final Object context) {

        updateTitleStringFormatterIfOverridden();

        LocalDate contextDate = (LocalDate) context;

        final String dateString = entry.trim().toUpperCase();
        if (dateString.startsWith("+") && contextDate != null) {
            return Jdk8LocalDateUtil.relativeDate(contextDate, dateString, true);
        } else if (dateString.startsWith("-")  && contextDate != null) {
            return Jdk8LocalDateUtil.relativeDate(contextDate, dateString, false);
        } else {
            return parseDate(dateString, contextDate);
        }
    }

    private void updateTitleStringFormatterIfOverridden() {
        final String overridePattern = override_title_pattern.get();
        if (overridePattern == null ||
                titleStringFormatNameOrPattern.equals(overridePattern)) {
            return;
        }

        // (re)create format
        updateTitleStringFormatter(overridePattern);
    }

    private LocalDate parseDate(final String dateStr, final Object original) {
        return Jdk8LocalDateUtil.parseDate(dateStr, parse_formatters);
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
        //DateTimeFormatter f = titleStringFormatter.withLocale(Locale.getDefault());
        return Jdk8LocalDateUtil.titleString(titleStringFormatter, date);
    }

    @Override
    public String titleStringWithMask(final Object value, final String usingMask) {
        final LocalDate date = (LocalDate) value;
        return Jdk8LocalDateUtil.titleString(formatterOf(DateTimeFormatter.ofPattern(usingMask)), date);
    }


    // //////////////////////////////////////////////////////////////////
    // EncoderDecoder
    // //////////////////////////////////////////////////////////////////

    @Override
    protected String doEncode(final Object object) {
        final LocalDate date = (LocalDate) object;
        return encode(date);
    }

    private synchronized String encode(final LocalDate date) {
        return encodingFormatter.format(date);
    }

    @Override
    protected LocalDate doRestore(final String data) {
        try {
            return parse(data);
        } catch (final IllegalArgumentException e) {
            throw new EncodingException(e);
        }
    }

    private synchronized LocalDate parse(final String data) {
        return LocalDate.parse(data, encodingFormatter);
    }

    // //////////////////////////////////////////////////////////////////
    // Jdk8LocalDateValueFacet
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
        return "Jdk8LocalDateValueSemanticsProvider: " + titleStringFormatter;
    }

}
