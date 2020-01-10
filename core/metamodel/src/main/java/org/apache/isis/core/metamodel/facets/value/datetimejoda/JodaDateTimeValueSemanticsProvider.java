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

package org.apache.isis.core.metamodel.facets.value.datetimejoda;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.EncodingException;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;

public class JodaDateTimeValueSemanticsProvider extends JodaDateTimeValueSemanticsProviderAbstract<DateTime> {

    // no default
    private static final DateTime DEFAULT_VALUE = null;


    /**
     * Required because implementation of {@link Parser} and
     * {@link EncoderDecoder}.
     */
    public JodaDateTimeValueSemanticsProvider() {
        this(null);
    }

    public JodaDateTimeValueSemanticsProvider(final FacetHolder holder) {
        super(holder, DateTime.class, DEFAULT_VALUE);
    }

    @Override
    protected DateTime add(final DateTime original, final int years, final int months, final int days, final int hours, final int minutes) {
        if(hours != 0 || minutes != 0) {
            throw new IllegalArgumentException("cannot add non-zero hours or minutes to a DateTime");
        }
        return original.plusYears(years).plusMonths(months).plusDays(days);
    }

    @Override
    protected DateTime now() {
        return new DateTime();
    }

    @Override
    protected Date dateValue(final Object value) {
        return ((DateTime) value).toDateTime().toDate();
    }

    @Override
    protected DateTime setDate(final Date date) {
        return new DateTime(date.getTime());
    }

    // //////////////////////////////////////////////////////////////////
    // EncoderDecoder
    // //////////////////////////////////////////////////////////////////

    private final DateTimeFormatter encodingFormatter = ISODateTimeFormat.basicDateTime();

    @Override
    protected String doEncode(final Object object) {
        final DateTime date = (DateTime) object;
        return encode(date);
    }

    private synchronized String encode(final DateTime date) {
        return encodingFormatter.print(date);
    }

    @Override
    protected DateTime doRestore(final String data) {
        try {
            return parse(data);
        } catch (final IllegalArgumentException e) {
            throw new EncodingException(e);
        }
    }

    private synchronized DateTime parse(final String data) {
        return encodingFormatter.parseDateTime(data);
    }

}
