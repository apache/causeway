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

package org.apache.isis.core.metamodel.facets.value.date;

import java.util.Date;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueSemanticsProviderContext;

public class DateValueSemanticsProvider extends DateValueSemanticsProviderAbstract<org.apache.isis.applib.value.Date> {

    // no default
    private static final org.apache.isis.applib.value.Date DEFAULT_VALUE = null; 


    /**
     * Required because implementation of {@link Parser} and
     * {@link EncoderDecoder}.
     */
    public DateValueSemanticsProvider() {
        this(null, null, null);
    }

    public DateValueSemanticsProvider(final FacetHolder holder, final IsisConfiguration configuration, final ValueSemanticsProviderContext context) {
        super(holder, org.apache.isis.applib.value.Date.class, Immutability.NOT_IMMUTABLE, EqualByContent.NOT_HONOURED, DEFAULT_VALUE, configuration, context);
    }

    @Override
    protected org.apache.isis.applib.value.Date add(final org.apache.isis.applib.value.Date original, final int years, final int months, final int days, final int hours, final int minutes) {
        final org.apache.isis.applib.value.Date date = original;
        return date.add(years, months, days);
    }

    @Override
    protected org.apache.isis.applib.value.Date now() {
        return new org.apache.isis.applib.value.Date();
    }

    @Override
    protected Date dateValue(final Object value) {
        return ((org.apache.isis.applib.value.Date) value).dateValue();
    }

    @Override
    protected org.apache.isis.applib.value.Date setDate(final Date date) {
        return new org.apache.isis.applib.value.Date(date);
    }
}
