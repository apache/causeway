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
package org.apache.isis.core.metamodel.facets.value.temporal;

import java.time.temporal.Temporal;
import java.util.function.Function;

import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueSemanticsProviderAndFacetAbstract;

import lombok.NonNull;
import lombok.val;

/**
 * Common base for {@link java.time.temporal.Temporal} types.
 *
 * @since 2.0
 *
 * @param <T> implementing {@link java.time.temporal.Temporal} type
 */
public abstract class TemporalValueFacetUsingSemanticsProviderFactory<T extends Temporal>
extends ValueFacetUsingSemanticsProviderFactory<T> {

    @NonNull protected final Class<T> valueType;
    @NonNull protected final Function<FacetHolder, ValueSemanticsProviderAndFacetAbstract<T>> facetFactory;

    protected TemporalValueFacetUsingSemanticsProviderFactory(
            final MetaModelContext mmc,
            final Class<T> valueType,
            final Function<FacetHolder, ValueSemanticsProviderAndFacetAbstract<T>> facetFactory) {
        super(mmc);
        this.valueType = valueType;
        this.facetFactory = facetFactory;
    }

    @Override
    public final void process(final ProcessClassContext processClassContext) {
        val type = processClassContext.getCls();
        if (!valueType.isAssignableFrom(type)) {
            return;
        }
        val facetHolder = processClassContext.getFacetHolder();
        addValueFacet(facetFactory.apply(facetHolder));
    }




}
