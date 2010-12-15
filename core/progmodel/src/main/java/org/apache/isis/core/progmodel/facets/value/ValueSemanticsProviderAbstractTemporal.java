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


package org.apache.isis.core.progmodel.facets.value;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.apache.isis.applib.adapters.EncodingException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.TextEntryParseException;
import org.apache.isis.core.metamodel.config.ConfigurationConstants;
import org.apache.isis.core.metamodel.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facets.Facet;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import com.google.inject.internal.Maps;


public abstract class ValueSemanticsProviderAbstractTemporal extends ValueSemanticsProviderAbstract implements DateValueFacet {

    /**
     * Introduced to allow BDD tests to provide a different format string
     * "mid-flight".
     */
    public static void setFormat(String propertyType, String formatStr) {
        FORMATS.get().put(propertyType, formatStr);
    }

    private final static ThreadLocal<Map<String,String>> FORMATS = 
        new ThreadLocal<Map<String,String>>() {
            @Override
            protected java.util.Map<String,String> initialValue() { 
                return Maps.newHashMap();
            }
        };
    
    protected static final String ISO_ENCODING_FORMAT = "iso_encoding";
    private static final TimeZone UTC_TIME_ZONE;

    public final static String FORMAT_KEY_PREFIX = ConfigurationConstants.ROOT + "value.format.";

    static {
        TimeZone timeZone = TimeZone.getTimeZone("Etc/UTC");
        if (timeZone == null) {
            timeZone = TimeZone.getTimeZone("UTC");
        }
        UTC_TIME_ZONE = timeZone;
    }

    /**
     * The facet type, used if not specified explicitly in the constructor.
     */
    public static Class<? extends Facet> type() {
        return DateValueFacet.class;
    }

    protected static DateFormat createDateFormat(final String mask) {
        return new SimpleDateFormat(mask);
    }

    private final DateFormat encodingFormat;
    protected DateFormat format;
    private String configuredFormat;
    private String formatKey;


    /**
     * Uses {@link #type()} as the facet type.
     */
    public ValueSemanticsProviderAbstractTemporal(
            final String propertyName,
            final FacetHolder holder,
            final Class<?> adaptedClass,
            final int typicalLength,
            final boolean immutable,
            final boolean equalByContent,
            final Object defaultValue,
            final IsisConfiguration configuration,
            final SpecificationLoader specificationLoader,
            final RuntimeContext runtimeContext) {
        this(propertyName, type(), holder, adaptedClass, typicalLength, immutable, equalByContent, defaultValue, configuration,
                specificationLoader, runtimeContext);
    }

    /**
     * Allows the specific facet subclass to be specified (rather than use {@link #type()}.
     */
    public ValueSemanticsProviderAbstractTemporal(
            final String propertyName,
            final Class<? extends Facet> facetType,
            final FacetHolder holder,
            final Class<?> adaptedClass,
            final int typicalLength,
            final boolean immutable,
            final boolean equalByContent,
            final Object defaultValue,
            final IsisConfiguration configuration,
            final SpecificationLoader specificationLoader,
            final RuntimeContext runtimeContext) {
        super(facetType, holder, adaptedClass, typicalLength, immutable, equalByContent, defaultValue, configuration,
                specificationLoader, runtimeContext);
        configureFormats();
        
        this.formatKey = FORMAT_KEY_PREFIX + propertyName;
        configuredFormat = getConfiguration().getString(formatKey, defaultFormat());
        buildFormat(configuredFormat);

        encodingFormat = formats().get(ISO_ENCODING_FORMAT);
    }

    protected void configureFormats() {
        final Map<String, DateFormat> formats = formats();
        for (Map.Entry<String, DateFormat> mapEntry : formats.entrySet()) {
            final DateFormat format = mapEntry.getValue();
            format.setLenient(false);
            if (ignoreTimeZone()) {
                format.setTimeZone(UTC_TIME_ZONE);
            }
        }
    }

    protected void buildDefaultFormatIfRequired() {
        String currentlyConfiguredFormat = FORMATS.get().get(formatKey);
        if ( currentlyConfiguredFormat==null || configuredFormat.equals(currentlyConfiguredFormat)) {
            return;
        }

        // (re)create format
        configuredFormat = currentlyConfiguredFormat;
        buildFormat(configuredFormat);
    }

    protected void buildFormat(String configuredFormat) {
        final Map<String, DateFormat> formats = formats();
        final String required = configuredFormat.toLowerCase().trim();
        format = formats.get(required);
        if (format == null) {
            setMask(configuredFormat);
        }
    }

    // //////////////////////////////////////////////////////////////////
    // Parsing
    // //////////////////////////////////////////////////////////////////

    @Override
    protected Object doParse(final Object original, final String entry) {
        buildDefaultFormatIfRequired();
        final String dateString = entry.trim();
        final String str = dateString.toLowerCase();
        if (str.equals("today") || str.equals("now")) {
            return now();
        } else if (dateString.startsWith("+")) {
            return relativeDate(original == null ? now() : original, dateString, true);
        } else if (dateString.startsWith("-")) {
            return relativeDate(original == null ? now() : original, dateString, false);
        } else {
            return parseDate(dateString, original == null ? now() : original);
        }
    }

    private Object parseDate(final String dateString, final Object original) {
        try {
            return setDate(format.parse(dateString));
        } catch (final ParseException e) {
            final Map<String, DateFormat> formats = formats();
            final Iterator<DateFormat> elements = formats.values().iterator();
            return setDate(parseDate(dateString, elements));
        }
    }

    private Date parseDate(final String dateString, final Iterator<DateFormat> elements) {
        final DateFormat format = elements.next();
        try {
            return format.parse(dateString);
        } catch (final ParseException e) {
            if (elements.hasNext()) {
                return parseDate(dateString, elements);
            } else {
                throw new TextEntryParseException("Not recognised as a date: " + dateString);
            }
        }
    }

    private Object relativeDate(final Object original, final String str, final boolean add) {
        if (str.equals("")) {
            return now();
        }

        Object date = original;
        final StringTokenizer st = new StringTokenizer(str.substring(1), " ");
        while (st.hasMoreTokens()) {
            final String token = st.nextToken();
            date = relativeDate2(date, token, add);
        }
        return date;
    }

    private Object relativeDate2(final Object original, String str, final boolean add) {
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
    public String titleString(final Object value) {
        if (value == null) {
            return null;
        }
        final Date date = dateValue(value);
        return titleString(format, date);
    }

    @Override
    public String titleStringWithMask(final Object value, final String usingMask) {
        final Date date = dateValue(value);
        return titleString(new SimpleDateFormat(usingMask), date);
    }

    private String titleString(final DateFormat formatter, final Date date) {
        return date == null ? "" : formatter.format(date);
    }

    // //////////////////////////////////////////////////////////////////
    // EncoderDecoder
    // //////////////////////////////////////////////////////////////////

    @Override
    protected String doEncode(final Object object) {
        final Date date = dateValue(object);
        return encode(date);
    }

    private synchronized String encode(final Date date) {
        return encodingFormat.format(date);
    }

    @Override
    protected Object doRestore(final String data) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeZone(UTC_TIME_ZONE);
        
        try {
            cal.setTime(parse(data));
            clearFields(cal);
            return setDate(cal.getTime());
        } catch (final ParseException e) {
        	if (data.charAt(0) == 'T'){
                long millis = Long.parseLong(data.substring(1));
                cal.setTimeInMillis(millis);
                clearFields(cal);
                return setDate(cal.getTime());
        	} else {
        		throw new EncodingException(e);
        	}
        }
    }

    private synchronized Date parse(final String data) throws ParseException {
        return encodingFormat.parse(data);
    }

    // //////////////////////////////////////////////////////////////////
    // DateValueFacet
    // //////////////////////////////////////////////////////////////////

    @Override
    public final Date dateValue(final ObjectAdapter object) {
        return object == null ? null : dateValue(object.getObject());
    }

    @Override
    public final ObjectAdapter createValue(final Date date) {
        return getRuntimeContext().adapterFor(setDate(date));
    }

    /**
     * For subclasses to implement.
     */
    @Override
    public abstract int getLevel();

    // //////////////////////////////////////////////////////////////////
    // temporal-specific stuff
    // //////////////////////////////////////////////////////////////////

    protected abstract Object add(Object original, int years, int months, int days, int hours, int minutes);

    protected void clearFields(final Calendar cal) {}

    protected abstract Date dateValue(Object value);

    protected abstract String defaultFormat();

    protected abstract Map<String, DateFormat> formats();

    protected boolean ignoreTimeZone() {
        return false;
    }

    protected abstract Object now();

    protected abstract Object setDate(Date date);

    public void setMask(final String mask) {
        format = new SimpleDateFormat(mask);
        format.setLenient(false);
        format.setTimeZone(UTC_TIME_ZONE);
    }

    protected boolean isEmpty() {
        return false;
    }


}
