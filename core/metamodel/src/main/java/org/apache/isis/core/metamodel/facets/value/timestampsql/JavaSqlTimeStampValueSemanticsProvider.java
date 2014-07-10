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

package org.apache.isis.core.metamodel.facets.value.timestampsql;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

import com.google.common.collect.Maps;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.parseable.InvalidEntryException;
import org.apache.isis.core.metamodel.facets.properties.defaults.PropertyDefaultFacet;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueSemanticsProviderContext;
import org.apache.isis.core.metamodel.facets.value.timestamp.TimeStampValueSemanticsProviderAbstract;

public class JavaSqlTimeStampValueSemanticsProvider extends TimeStampValueSemanticsProviderAbstract<java.sql.Timestamp> {

    public static final boolean isAPropertyDefaultFacet() {
        return PropertyDefaultFacet.class.isAssignableFrom(JavaSqlTimeStampValueSemanticsProvider.class);
    }

    private static Map<String, DateFormat> formats = Maps.newHashMap();

    static {
        initFormats(formats);
    }

    /**
     * Required because implementation of {@link Parser} and
     * {@link EncoderDecoder}.
     */
    public JavaSqlTimeStampValueSemanticsProvider() {
        this(null, null, null);
    }

    public JavaSqlTimeStampValueSemanticsProvider(final FacetHolder holder, final IsisConfiguration configuration, final ValueSemanticsProviderContext context) {
        super(holder, java.sql.Timestamp.class, configuration, context);
    }

    // //////////////////////////////////////////////////////////////////
    // temporal-specific stuff
    // //////////////////////////////////////////////////////////////////

    @Override
    protected Date dateValue(final Object value) {
        return new Date(((Timestamp) value).getTime());
    }

    @Override
    protected Map<String, DateFormat> formats() {
        return formats;
    }

    @Override
    protected Timestamp now() {
        throw new InvalidEntryException("Can't change a timestamp.");
    }

    @Override
    protected Timestamp setDate(final Date date) {
        return new Timestamp(date.getTime());
    }

}
