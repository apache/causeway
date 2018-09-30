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

package org.apache.isis.core.metamodel.facets.value.datejdk8local;

import static org.apache.isis.core.metamodel.facets.value.datejdk8local.Jdk8LocalDateUtil.formatterOf;
import static org.apache.isis.core.metamodel.facets.value.datejdk8local.Jdk8LocalDateUtil.formatterOfStyle;
import static org.apache.isis.core.metamodel.facets.value.datejdk8local.Jdk8LocalDateUtil.parserOf;
import static org.apache.isis.core.metamodel.facets.value.datejdk8local.Jdk8LocalDateUtil.parserOfStyle;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.EncodingException;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueSemanticsProviderAndFacetAbstract;
import org.apache.isis.core.metamodel.facets.value.datejodalocal.JodaLocalDateValueFacet;
import org.apache.isis.core.metamodel.services.ServicesInjector;

public class Jdk8LocalDateValueSemanticsProvider extends ValueSemanticsProviderAndFacetAbstract<LocalDate> implements Jdk8LocalDateValueFacet {

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
     * Key to indicate how LocalDate should be parsed/rendered.
     *
     * <p>
     * eg:
     * <pre>
     * isis.value.format.date=iso
     * </pre>
     *
     * <p>
     * A pre-determined list of values is available, specifically 'iso_encoding', 'iso' and 'medium' (see
     * {@link #NAMED_TITLE_FORMATTERS}).  Alternatively,  can also specify a mask, eg <tt>dd-MMM-yyyy</tt>.
     *
     * @see #NAMED_TITLE_FORMATTERS
     */
    public final static String CFG_FORMAT_KEY = ConfigurationConstants.ROOT + "value.format.date";


    /**
     * Keys represent the values which can be configured, and which are used for the rendering of dates.
     *
     */
    private static Map<String, TimeFormatter> NAMED_TITLE_FORMATTERS = _Maps.newHashMap();
    static {
        NAMED_TITLE_FORMATTERS.put("iso_encoding", formatterOf(DateTimeFormatter.ofPattern("yyyyMMdd")));
        NAMED_TITLE_FORMATTERS.put("iso", formatterOf(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        NAMED_TITLE_FORMATTERS.put("long", formatterOfStyle("L-"));
        NAMED_TITLE_FORMATTERS.put("medium", formatterOfStyle("M-"));
        NAMED_TITLE_FORMATTERS.put("short", formatterOfStyle("S-"));
    }

    private final static ThreadLocal<String> OVERRIDE_TITLE_PATTERN = new ThreadLocal<String>() {
        @Override
        protected String initialValue() {
            return null;
        }
    };


    private final static List<TimeParser> PARSE_FORMATTERS = _Lists.newArrayList();
    static {
        PARSE_FORMATTERS.add(parserOfStyle("L-"));
        PARSE_FORMATTERS.add(parserOfStyle("M-"));
        PARSE_FORMATTERS.add(parserOfStyle("S-"));
        PARSE_FORMATTERS.add(parserOf(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        PARSE_FORMATTERS.add(parserOf(DateTimeFormatter.ofPattern("yyyyMMdd")));
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
        this(null, null);
    }

    /**
     * Uses {@link #type()} as the facet type.
     */
    public Jdk8LocalDateValueSemanticsProvider(final FacetHolder holder, final ServicesInjector context) {
        super(type(), holder, LocalDate.class, TYPICAL_LENGTH, MAX_LENGTH, Immutability.IMMUTABLE, EqualByContent.HONOURED, DEFAULT_VALUE, context);

        String configuredNameOrPattern = getConfiguration().getString(CFG_FORMAT_KEY, "medium").trim();
        updateTitleStringFormatter(configuredNameOrPattern);
    }


    private void updateTitleStringFormatter(String titleStringFormatNameOrPattern) {
        titleStringFormatter = NAMED_TITLE_FORMATTERS.get(titleStringFormatNameOrPattern);
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
        final String overridePattern = OVERRIDE_TITLE_PATTERN.get();
        if (overridePattern == null ||
                titleStringFormatNameOrPattern.equals(overridePattern)) {
            return;
        }

        // (re)create format
        updateTitleStringFormatter(overridePattern);
    }

    private LocalDate parseDate(final String dateStr, final Object original) {
        return Jdk8LocalDateUtil.parseDate(dateStr, PARSE_FORMATTERS);
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
    public final LocalDate dateValue(final ObjectAdapter object) {
        return (LocalDate) (object == null ? null : object.getPojo());
    }

    @Override
    public final ObjectAdapter createValue(final LocalDate date) {
        return getObjectAdapterProvider().adapterFor(date);
    }


    // //////////////////////////////////////

    @Override
    public String toString() {
        return "Jdk8LocalDateValueSemanticsProvider: " + titleStringFormatter;
    }

}
