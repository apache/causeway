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


package org.apache.isis.metamodel.value;

import java.util.Date;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.value.DateTime;
import org.apache.isis.core.metamodel.config.IsisConfiguration;
import org.apache.isis.metamodel.facets.FacetHolder;
import org.apache.isis.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.metamodel.specloader.SpecificationLoader;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;


public class DateTimeValueSemanticsProvider extends JavaUtilDateValueSemanticsProviderAbstract {
    private static final boolean IMMUTABLE = false;
    private static final boolean EQUAL_BY_CONTENT = false;

    /**
     * Required because implementation of {@link Parser} and {@link EncoderDecoder}.
     */
    @SuppressWarnings("NP_NULL_PARAM_DEREF_NONVIRTUAL")
    public DateTimeValueSemanticsProvider() {
        this(null, null, null, null);
    }

    public DateTimeValueSemanticsProvider(
    		final FacetHolder holder,
            final IsisConfiguration configuration,
            final SpecificationLoader specificationLoader,
            final RuntimeContext runtimeContext) {
        super(holder, DateTime.class, IMMUTABLE, EQUAL_BY_CONTENT, configuration, specificationLoader, runtimeContext);
    }

    @Override
    protected Date dateValue(final Object value) {
        final DateTime date = (DateTime) value;
        return date == null ? null : date.dateValue();
    }

    @Override
    protected Object add(
            final Object original,
            final int years,
            final int months,
            final int days,
            final int hours,
            final int minutes) {
        DateTime date = (DateTime) original;
        date = date.add(years, months, days, hours, minutes);
        return date;
    }

    @Override
    protected Object now() {
        return new DateTime();
    }

    @Override
    protected Object setDate(final Date date) {
        return new DateTime(date);
    }

}

