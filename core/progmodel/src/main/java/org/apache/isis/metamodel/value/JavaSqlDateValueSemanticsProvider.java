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

import java.sql.Date;
import java.util.Calendar;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.core.metamodel.config.IsisConfiguration;
import org.apache.isis.metamodel.facets.FacetHolder;
import org.apache.isis.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.metamodel.specloader.SpecificationLoader;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;


/**
 * An adapter that handles {@link java.sql.Date} with only date component.
 *
 * @see JavaUtilDateValueSemanticsProvider
 * @see JavaSqlTimeValueSemanticsProvider
 */
public class JavaSqlDateValueSemanticsProvider extends DateValueSemanticsProviderAbstract {

    private static final boolean IMMUTABLE = false;
    private static final boolean EQUAL_BY_CONTENT = false;
    private static final Object DEFAULT_VALUE = null; // no default

    /**
     * Required because implementation of {@link Parser} and {@link EncoderDecoder}.
     */
    @SuppressWarnings("NP_NULL_PARAM_DEREF_NONVIRTUAL")
    public JavaSqlDateValueSemanticsProvider() {
        this(null, null, null, null);
    }

    public JavaSqlDateValueSemanticsProvider(
    		final FacetHolder holder,
            final IsisConfiguration configuration,
            final SpecificationLoader specificationLoader,
            final RuntimeContext runtimeContext) {
        super(holder, Date.class, IMMUTABLE, EQUAL_BY_CONTENT, DEFAULT_VALUE, configuration, specificationLoader, runtimeContext);
    }

    @Override
    protected Object add(
            final Object original,
            final int years,
            final int months,
            final int days,
            final int hours,
            final int minutes) {
        final Date date = (Date) original;
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.AM_PM, 0);
        cal.set(Calendar.MILLISECOND, 0);

        cal.add(Calendar.YEAR, years);
        cal.add(Calendar.MONTH, months);
        cal.add(Calendar.DAY_OF_MONTH, days);

        return setDate(cal.getTime());
    }

    @Override
    protected java.util.Date dateValue(final Object value) {
        return (java.util.Date) value;
    }

    @Override
    protected Object setDate(final java.util.Date date) {
        return new Date(date.getTime());
    }

    @Override
    protected Object now() {
        return new Date(Clock.getTime());
    }

}
