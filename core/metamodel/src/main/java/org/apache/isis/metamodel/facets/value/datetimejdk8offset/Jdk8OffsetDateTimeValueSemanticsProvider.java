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

package org.apache.isis.metamodel.facets.value.datetimejdk8offset;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.EncodingException;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.value.datetimejoda.JodaDateTimeValueSemanticsProviderAbstract;

public class Jdk8OffsetDateTimeValueSemanticsProvider extends JodaDateTimeValueSemanticsProviderAbstract<OffsetDateTime> {

    // no default
    private static final OffsetDateTime DEFAULT_VALUE = null;


    /**
     * Required because implementation of {@link TimeParser} and
     * {@link EncoderDecoder}.
     */
    public Jdk8OffsetDateTimeValueSemanticsProvider() {
        this(null);
    }

    public Jdk8OffsetDateTimeValueSemanticsProvider(final FacetHolder holder) {
        super(holder, OffsetDateTime.class, DEFAULT_VALUE);
    }

    @Override
    protected OffsetDateTime add(final OffsetDateTime original, final int years, final int months, final int days, final int hours, final int minutes) {
        if(hours != 0 || minutes != 0) {
            throw new IllegalArgumentException("cannot add non-zero hours or minutes to a DateTime");
        }
        return original.plusYears(years).plusMonths(months).plusDays(days);
    }

    @Override
    protected OffsetDateTime now() {
        return OffsetDateTime.now();
    }

    @Override
    protected Date dateValue(final Object value) {
        long epochMilli = ((OffsetDateTime) value).toInstant().toEpochMilli();
        return new Date(epochMilli);
    }

    @Override
    protected OffsetDateTime setDate(final Date date) {
        return OffsetDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC);
    }

    // //////////////////////////////////////////////////////////////////
    // EncoderDecoder
    // //////////////////////////////////////////////////////////////////

    private final DateTimeFormatter encodingFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss.SSSZ");

    @Override
    protected String doEncode(final Object object) {
        final OffsetDateTime date = (OffsetDateTime) object;
        return encode(date);
    }

    private synchronized String encode(final OffsetDateTime date) {
        return encodingFormatter.format(date);
    }

    @Override
    protected OffsetDateTime doRestore(final String data) {
        try {
            return parse(data);
        } catch (final IllegalArgumentException e) {
            throw new EncodingException(e);
        }
    }

    private synchronized OffsetDateTime parse(final String data) {
        return OffsetDateTime.parse(data, encodingFormatter);
    }

}
