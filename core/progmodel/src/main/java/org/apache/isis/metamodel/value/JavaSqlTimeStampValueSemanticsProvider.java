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

import java.sql.Timestamp;
import java.util.Date;
import java.util.Hashtable;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.core.metamodel.adapter.InvalidEntryException;
import org.apache.isis.core.metamodel.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.facets.properties.defaults.PropertyDefaultFacet;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;


public class JavaSqlTimeStampValueSemanticsProvider extends TimeStampValueSemanticsProviderAbstract {

    public static final boolean isAPropertyDefaultFacet() {
        return PropertyDefaultFacet.class.isAssignableFrom(JavaSqlTimeStampValueSemanticsProvider.class);
    }

    private static Hashtable formats = new Hashtable();

    static {
        initFormats(formats);
    }

    /**
     * Required because implementation of {@link Parser} and {@link EncoderDecoder}.
     */
    @SuppressWarnings("NP_NULL_PARAM_DEREF_NONVIRTUAL")
    public JavaSqlTimeStampValueSemanticsProvider() {
        this(null, null, null, null);
    }

    public JavaSqlTimeStampValueSemanticsProvider(
            final FacetHolder holder,
            final IsisConfiguration configuration,
            final SpecificationLoader specificationLoader,
            final RuntimeContext runtimeContext) {
        super(holder, configuration, specificationLoader, runtimeContext);
    }

    // //////////////////////////////////////////////////////////////////
    // temporal-specific stuff
    // //////////////////////////////////////////////////////////////////

    @Override
    protected Date dateValue(final Object value) {
        return new Date(((Timestamp) value).getTime());
    }

    @Override
    protected Hashtable formats() {
        return formats;
    }

    @Override
    protected Object now() {
        throw new InvalidEntryException("Can't change a timestamp.");
    }

    @Override
    protected Object setDate(final Date date) {
        return new Timestamp(date.getTime());
    }

}

