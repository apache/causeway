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

package org.apache.isis.core.progmodel.facets.value.datetimejodalocal;

import java.util.Date;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.progmodel.facets.object.value.ValueSemanticsProviderContext;
import org.joda.time.LocalDateTime;

public class JodaLocalDateTimeValueSemanticsProvider extends JodaLocalDateTimeValueSemanticsProviderAbstract<LocalDateTime> {

    // no default
    private static final LocalDateTime DEFAULT_VALUE = null;


    /**
     * Required because implementation of {@link Parser} and
     * {@link EncoderDecoder}.
     */
    public JodaLocalDateTimeValueSemanticsProvider() {
        this(null, null, null);
    }

    public JodaLocalDateTimeValueSemanticsProvider(final FacetHolder holder, final IsisConfiguration configuration, final ValueSemanticsProviderContext context) {
        super(holder, LocalDateTime.class, DEFAULT_VALUE, configuration, context);
    }

    @Override
    protected LocalDateTime add(final LocalDateTime original, final int years, final int months, final int days, final int hours, final int minutes) {
        if(hours != 0 || minutes != 0) {
            throw new IllegalArgumentException("cannot add non-zero hours or minutes to a LocalDateTime");
        }
        return original.plusYears(years).plusMonths(months).plusDays(days);
    }

    @Override
    protected LocalDateTime now() {
        return new LocalDateTime();
    }

    @Override
    protected Date dateValue(final Object value) {
        return ((LocalDateTime) value).toDateTime().toDate();
    }

    @Override
    protected LocalDateTime setDate(final Date date) {
        return new LocalDateTime(date.getTime());
    }
}
