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
package org.apache.isis.core.metamodel.facets;

import java.util.Optional;

import org.springframework.lang.Nullable;

import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.commons.internal.reflection._ClassCache;
import org.apache.isis.core.metamodel.context.HasMetaModelContext;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

public abstract class FacetFactoryAbstract
implements FacetFactory, HasMetaModelContext {

    @Getter(onMethod_ = {@Override}) private final @NonNull MetaModelContext metaModelContext;

    @Getter(onMethod_ = {@Override}) private final ImmutableEnumSet<FeatureType> featureTypes;

    @Getter(AccessLevel.PROTECTED) private final _ClassCache classCache;

    public FacetFactoryAbstract(
            final MetaModelContext metaModelContext,
            final ImmutableEnumSet<FeatureType> featureTypes) {
        this.metaModelContext = metaModelContext;
        this.featureTypes = featureTypes;
        this.classCache = _ClassCache.getInstance();
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
    }

    @Override
    public void processParams(final ProcessParameterContext processParameterContext) {
    }

    // -- FACET UTILITIES

    /**
     * Shortcut to {@link FacetUtil#addFacet}.
     * @param facet - non-null
     * @return the argument as is
     */
    public <F extends Facet> F addFacet(final @NonNull F facet) {
        return FacetUtil.addFacet(facet);
    }

    /**
     * Shortcut to {@link FacetUtil#addFacetIfPresent}. Acts as a no-op if facet is <tt>null</tt>.
     * @param facetIfAny - null-able
     * @return the argument as is - or just in case if null converted to an Optional.empty()
     */
    public <F extends Facet> Optional<F> addFacetIfPresent(final @Nullable Optional<F> facetIfAny) {
        return FacetUtil.addFacetIfPresent(facetIfAny);
    }

    // -- METHOD UTILITITES

    protected static final Class<?> ANY_RETURN = null;
    protected static final Class<?>[] NO_ARG = new Class<?>[0];
    protected static final Class<?>[] STRING_ARG = new Class<?>[] {String.class};

}
