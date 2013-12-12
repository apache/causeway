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

package org.apache.isis.core.progmodel.facets.value.datejodalocal;

import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.EncodingException;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.parseable.TextEntryParseException;
import org.apache.isis.core.progmodel.facets.object.value.ValueSemanticsProviderAndFacetAbstract;
import org.apache.isis.core.progmodel.facets.object.value.ValueSemanticsProviderContext;

public class JodaLocalDateValueSemanticsProvider extends ValueSemanticsProviderAndFacetAbstract<LocalDate> implements JodaLocalDateValueFacet {

    
    /**
     * Introduced to allow BDD tests to provide a different format string "mid-flight".
     * 
     * <p>
     * REVIEW: This seems only to have any effect if 'propertyType' is set to 'date'.
     * 
     * @see #setPatternOverride(String)
     * @deprecated - because 'propertyType' parameter is never used
     */
    @Deprecated
    public static void setFormat(final String propertyType, final String pattern) {
        setPatternOverride(pattern);
    }
    /**
     * A replacement for {@link #setFormat(String, String)}.
     */
    public static void setPatternOverride(final String pattern) {
        OVERRIDE_PATTERN.set(pattern);
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
     * {@link #NAMED_FORMATTERS}).  Alternatively,  can also specify a mask, eg <tt>dd-MMM-yyyy</tt>.
     * 
     * @see #NAMED_FORMATTERS  
     */
    public final static String CFG_FORMAT_KEY = ConfigurationConstants.ROOT + "value.format.date";
    
    
    /**
     * Keys represent the values which can be configured, and which are used for the rendering of dates.
     * 
     */
    private static Map<String, DateTimeFormatter> NAMED_FORMATTERS = Maps.newHashMap();
    static {
        NAMED_FORMATTERS.put("iso_encoding", DateTimeFormat.forPattern("yyyyMMdd"));
        NAMED_FORMATTERS.put("iso", DateTimeFormat.forPattern("yyyy-MM-dd"));
        NAMED_FORMATTERS.put("medium", DateTimeFormat.forStyle("M-"));
    }
    
    private final static ThreadLocal<String> OVERRIDE_PATTERN = new ThreadLocal<String>() {
        @Override
        protected String initialValue() {
            return null;
        }
    };


    public static Class<? extends Facet> type() {
        return JodaLocalDateValueFacet.class;
    }


    // no default
    private static final LocalDate DEFAULT_VALUE = null;


    private final DateTimeFormatter encodingFormatter;
    protected DateTimeFormatter titleStringFormatter;
    private String titleStringFormatNameOrPattern;

    
    // //////////////////////////////////////
    // constructor
    // //////////////////////////////////////

    /**
     * Required because implementation of {@link Parser} and
     * {@link EncoderDecoder}.
     */
    public JodaLocalDateValueSemanticsProvider() {
        this(null, null, null);
    }

    /**
     * Uses {@link #type()} as the facet type.
     */
    public JodaLocalDateValueSemanticsProvider(
            final FacetHolder holder, final IsisConfiguration configuration, final ValueSemanticsProviderContext context) {
        super(type(), holder, LocalDate.class, 12, Immutability.IMMUTABLE, EqualByContent.HONOURED, DEFAULT_VALUE, configuration, context);

        encodingFormatter = DateTimeFormat.forPattern("yyyyMMdd");
        
        String configuredNameOrPattern = getConfiguration().getString(CFG_FORMAT_KEY, "medium").toLowerCase().trim();
        updateTitleStringFormatter(configuredNameOrPattern);
    }


    private void updateTitleStringFormatter(String titleStringFormatNameOrPattern) {
        titleStringFormatter = NAMED_FORMATTERS.get(titleStringFormatNameOrPattern);
        if (titleStringFormatter == null) {
            titleStringFormatter = DateTimeFormat.forPattern(titleStringFormatNameOrPattern);
        }
        this.titleStringFormatNameOrPattern = titleStringFormatNameOrPattern; 
    }
    

    // //////////////////////////////////////////////////////////////////
    // Parsing
    // //////////////////////////////////////////////////////////////////

    @Override
    protected LocalDate doParse(final Object context, final String entry, final Localization localization) {
        
        updateTitleStringFormatterIfOverridden();

        final String dateString = entry.trim();
        final String str = dateString.toLowerCase();
        if (str.equals("today") || str.equals("now")) {
            return now();
        } else if (dateString.startsWith("+")) {
            return relativeDate(context == null ? now() : context, dateString, true);
        } else if (dateString.startsWith("-")) {
            return relativeDate(context == null ? now() : context, dateString, false);
        } else {
            return parseDate(dateString, context == null ? now() : context, localization);
        }
    }

    private void updateTitleStringFormatterIfOverridden() {
        final String overridePattern = OVERRIDE_PATTERN.get();
        if (overridePattern == null || 
            titleStringFormatNameOrPattern.equals(overridePattern)) {
            return;
        } 
        
        // (re)create format
        updateTitleStringFormatter(overridePattern);
    }

    private LocalDate parseDate(final String dateString, final Object original, final Localization localization) {
        List<DateTimeFormatter> elements = formatsToTry(localization);
        LocalDate parsedDate = parseDate(dateString, elements.iterator());
        return setDate(parsedDate);
    }

    private LocalDate parseDate(final String dateString, final Iterator<DateTimeFormatter> iterator) {
        final DateTimeFormatter format = iterator.next();
        try {
            return format.parseLocalDate(dateString);
        } catch (final IllegalArgumentException e) {
            if (iterator.hasNext()) {
                return parseDate(dateString, iterator);
            } else {
                throw new TextEntryParseException("Not recognised as a date: " + dateString);
            }
        }
    }

    private LocalDate relativeDate(final Object object, final String str, final boolean add) {
        if (str.equals("")) {
            return now();
        }

        try {
            LocalDate date = (LocalDate) object;
            final StringTokenizer st = new StringTokenizer(str.substring(1), " ");
            while (st.hasMoreTokens()) {
                final String token = st.nextToken();
                date = relativeDate2(date, token, add);
            }
            return date;
        } catch (final Exception e) {
            return now();
        }
    }

    private LocalDate relativeDate2(final LocalDate original, String str, final boolean add) {
        int hours = 0;
        int minutes = 0;
        int days = 0;
        int months = 0;
        int years = 0;

        if (str.endsWith("H")) {
            str = str.substring(0, str.length() - 1);
            hours = Integer.valueOf(str).intValue();
        } else if (str.endsWith("M")) {
            str = str.substring(0, str.length() - 1);
            minutes = Integer.valueOf(str).intValue();
        } else if (str.endsWith("w")) {
            str = str.substring(0, str.length() - 1);
            days = 7 * Integer.valueOf(str).intValue();
        } else if (str.endsWith("y")) {
            str = str.substring(0, str.length() - 1);
            years = Integer.valueOf(str).intValue();
        } else if (str.endsWith("m")) {
            str = str.substring(0, str.length() - 1);
            months = Integer.valueOf(str).intValue();
        } else if (str.endsWith("d")) {
            str = str.substring(0, str.length() - 1);
            days = Integer.valueOf(str).intValue();
        } else {
            days = Integer.valueOf(str).intValue();
        }

        if (add) {
            return add(original, years, months, days, hours, minutes);
        } else {
            return add(original, -years, -months, -days, -hours, -minutes);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // TitleProvider
    // ///////////////////////////////////////////////////////////////////////////

    @Override
    public String titleString(final Object value, final Localization localization) {
        if (value == null) {
            return null;
        }
        final LocalDate date = dateValue(value);
        DateTimeFormatter f = titleStringFormatter;
        if (localization != null) {
            f = format(localization);
        }
        return titleString(f, date);
    }

    @Override
    public String titleStringWithMask(final Object value, final String usingMask) {
        final LocalDate date = dateValue(value);
        return titleString(new SimpleDateFormat(usingMask), date);
    }

    private String titleString(final DateTimeFormatter formatter, final LocalDate date) {
        return date == null ? "" : formatter.print(date);
    }

    // //////////////////////////////////////////////////////////////////
    // EncoderDecoder
    // //////////////////////////////////////////////////////////////////

    @Override
    protected String doEncode(final Object object) {
        final LocalDate date = dateValue(object);
        return encode(date);
    }

    private synchronized String encode(final LocalDate date) {
        return encodingFormatter.print(date);
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
        return encodingFormatter.parseLocalDate(data);
    }

    // //////////////////////////////////////////////////////////////////
    // JodaLocalDateValueFacet
    // //////////////////////////////////////////////////////////////////

    @Override
    public final LocalDate dateValue(final ObjectAdapter object) {
        return (LocalDate) (object == null ? null : object.getObject());
    }

    @Override
    public final ObjectAdapter createValue(final LocalDate date) {
        return getAdapterManager().adapterFor(date);
    }


    // //////////////////////////////////////////////////////////////////
    // temporal-specific stuff
    // //////////////////////////////////////////////////////////////////

    protected boolean isEmpty() {
        return false;
    }



    
    // //////////////////////////////////////////////////////////////////
    // temporal-specific stuff
    // //////////////////////////////////////////////////////////////////

    protected DateTimeFormatter format(final Localization localization) {
        return DateTimeFormat.forStyle("M-").withLocale(localization.getLocale());
    }

    protected List<DateTimeFormatter> formatsToTry(Localization localization) {
        List<DateTimeFormatter> formats = Lists.newArrayList();
        
        formats.add(withLocale(DateTimeFormat.forStyle("L-"), localization));
        formats.add(withLocale(DateTimeFormat.forStyle("M-"), localization));
        formats.add(withLocale(DateTimeFormat.forStyle("S-"), localization));
        formats.add(withLocale(DateTimeFormat.forPattern("yyyy-MM-dd"), localization));
        formats.add(withLocale(DateTimeFormat.forPattern("yyyyMMdd"), localization));

        return formats;
    }


    private static DateTimeFormatter withLocale(DateTimeFormatter formatter, Localization localization) {
        if(localization != null) {
            Locale locale2 = localization.getLocale();
            formatter.withLocale(locale2);
        }
        return formatter;
    }

    // //////////////////////////////////////

    protected LocalDate add(final LocalDate original, final int years, final int months, final int days, final int hours, final int minutes) {
        if(hours != 0 || minutes != 0) {
            throw new IllegalArgumentException("cannot add non-zero hours or minutes to a LocalDate");
        }
        return original.plusYears(years).plusMonths(months).plusDays(days);
    }

    protected LocalDate now() {
        return new LocalDate();
    }

    protected LocalDate dateValue(final Object value) {
        return (LocalDate) value;
    }

    protected LocalDate setDate(final LocalDate date) {
        return date;
    }

    // //////////////////////////////////////
    
    @Override
    public String toString() {
        return "DateValueSemanticsProvider: " + titleStringFormatter;
    }

}
