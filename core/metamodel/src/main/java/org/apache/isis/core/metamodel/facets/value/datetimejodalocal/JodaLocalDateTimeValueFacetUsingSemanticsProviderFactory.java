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

package org.apache.isis.core.metamodel.facets.value.datetimejodalocal;

import javax.inject.Inject;

import org.joda.time.LocalDateTime;

import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueFacetUsingSemanticsProviderFactory;

public class JodaLocalDateTimeValueFacetUsingSemanticsProviderFactory
extends ValueFacetUsingSemanticsProviderFactory<LocalDateTime> {

    @Inject
    public JodaLocalDateTimeValueFacetUsingSemanticsProviderFactory(final MetaModelContext mmc) {
        // as per inherited DateTimeValueSemanticsProvider#facetType
        super(mmc);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        final Class<?> type = processClassContext.getCls();
        final FacetHolder holder = processClassContext.getFacetHolder();

        if (type != LocalDateTime.class) {
            return;
        }
        addFacets(new JodaLocalDateTimeValueSemanticsProvider(holder));
    }

}
